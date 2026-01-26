import argparse
import csv
import os
import re
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path
from typing import Optional, Tuple, List

from codecarbon import EmissionsTracker


@dataclass
class JavaEntryPoint:
    package: Optional[str]   # e.g. "com.example"
    class_name: str          # e.g. "Main"

    @property
    def fqcn(self) -> str:
        return f"{self.package}.{self.class_name}" if self.package else self.class_name


PACKAGE_RE = re.compile(r"^\s*package\s+([a-zA-Z_]\w*(?:\.[a-zA-Z_]\w*)*)\s*;\s*$", re.MULTILINE)
PUBLIC_CLASS_RE = re.compile(r"^\s*public\s+class\s+([A-Za-z_]\w*)\s*", re.MULTILINE)
MAIN_METHOD_RE = re.compile(r"public\s+static\s+void\s+main\s*\(\s*String\s*\[\]\s*\w*\s*\)", re.MULTILINE)


def which_or_fail(cmd: str) -> None:
    if shutil.which(cmd) is None:
        raise RuntimeError(f"No encuentro '{cmd}' en tu PATH. Instala/configura el JDK (javac/java).")


def parse_entry_point(java_file: Path) -> JavaEntryPoint:
    """
    Heurística:
    - Lee package si existe
    - Verifica que existe main()
    - Usa el 'public class X' como nombre de clase principal
      (en Java, el archivo suele llamarse X.java si es public class X)
    """
    text = java_file.read_text(encoding="utf-8", errors="ignore")

    if not MAIN_METHOD_RE.search(text):
        raise ValueError(f"{java_file} no contiene un método main(String[] args).")

    pkg = None
    m = PACKAGE_RE.search(text)
    if m:
        pkg = m.group(1)

    m2 = PUBLIC_CLASS_RE.search(text)
    if not m2:
        # Si no hay public class, intenta inferir por nombre del fichero
        class_name = java_file.stem
    else:
        class_name = m2.group(1)

    return JavaEntryPoint(package=pkg, class_name=class_name)


def run_cmd(cmd: List[str], cwd: Path, timeout: Optional[int] = None) -> Tuple[int, str, str]:
    p = subprocess.run(
        cmd,
        cwd=str(cwd),
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        timeout=timeout
    )
    return p.returncode, p.stdout, p.stderr


def compile_java(java_file: Path, build_dir: Path) -> None:
    # Copiamos el .java respetando paquetes: lo más simple es copiar el árbol entero desde su raíz.
    # Aquí asumimos que el .java ya está en su estructura correcta (src/.../com/x/Main.java)
    # y compilamos desde su carpeta raíz detectada.
    #
    # Para hacerlo robusto en "un solo fichero suelto", lo compilamos tal cual y dejamos que javac
    # cree la estructura en build_dir con -d.

    cmd = ["javac", "-encoding", "UTF-8", "-d", str(build_dir), str(java_file)]
    code, out, err = run_cmd(cmd, cwd=java_file.parent)
    if code != 0:
        raise RuntimeError(f"Fallo compilación de {java_file}:\n{err.strip()}")


def execute_with_codecarbon(
    fqcn: str,
    classpath: Path,
    java_args: List[str],
    timeout: Optional[int],
    output_dir: Path,
    project_name: str,
    run_id: str,
) -> Tuple[float, str, str]:
    """
    Ejecuta: java -cp <classpath> <fqcn> [args...]
    Midiendo emisiones con CodeCarbon.
    Devuelve (emissions_kg, stdout, stderr)
    """
    tracker = EmissionsTracker(
        project_name=project_name,
        output_dir=str(output_dir),
        output_file="emissions.csv",   # CodeCarbon irá añadiendo registros
        save_to_file=True,
        log_level="error",
        run_id=run_id,
    )

    cmd = ["java", "-cp", str(classpath), fqcn] + java_args

    tracker.start()
    try:
        code, out, err = run_cmd(cmd, cwd=classpath, timeout=timeout)
    finally:
        emissions_kg = tracker.stop() or 0.0

    if code != 0:
        # No lo consideramos fatal: registramos emisiones igualmente y devolvemos stderr
        err = (err or "").strip()
    return emissions_kg, out, err


def iter_java_files(paths: List[Path]) -> List[Path]:
    files: List[Path] = []
    for p in paths:
        if p.is_dir():
            files.extend(sorted(p.rglob("*.java")))
        elif p.is_file() and p.suffix.lower() == ".java":
            files.append(p)
    return files


def main():
    which_or_fail("javac")
    which_or_fail("java")

    ap = argparse.ArgumentParser(description="Medir CO2eq (CodeCarbon) ejecutando ficheros Java uno a uno.")
    ap.add_argument("inputs", nargs="+", help="Ficheros .java y/o directorios (se buscarán .java recursivamente).")
    ap.add_argument("--output", default="java_emissions_report.csv", help="CSV final con resumen por fichero.")
    ap.add_argument("--codecarbon-dir", default="codecarbon_logs", help="Directorio donde CodeCarbon guarda emissions.csv.")
    ap.add_argument("--project", default="java-codecarbon", help="Nombre del proyecto en CodeCarbon.")
    ap.add_argument("--timeout", type=int, default=120, help="Timeout (segundos) por ejecución Java.")
    ap.add_argument("--args", nargs=argparse.REMAINDER, default=[], help="Argumentos para pasar al programa Java (después de --args).")
    args = ap.parse_args()

    inputs = [Path(x).resolve() for x in args.inputs]
    java_files = iter_java_files(inputs)

    if not java_files:
        print("No encontré ficheros .java en las rutas dadas.", file=sys.stderr)
        sys.exit(1)

    out_dir = Path(args.codecarbon_dir).resolve()
    out_dir.mkdir(parents=True, exist_ok=True)

    rows = []
    for java_file in java_files:
        print(f"\n=== Procesando: {java_file}")
        try:
            entry = parse_entry_point(java_file)
        except Exception as e:
            print(f"  [SKIP] No puedo determinar main(): {e}")
            rows.append({
                "java_file": str(java_file),
                "status": "skipped_no_main",
                "fqcn": "",
                "emissions_kgco2eq": "",
                "notes": str(e),
            })
            continue

        with tempfile.TemporaryDirectory(prefix="javacc_") as tmp:
            build_dir = Path(tmp) / "classes"
            build_dir.mkdir(parents=True, exist_ok=True)

            try:
                compile_java(java_file, build_dir)
            except Exception as e:
                print(f"  [ERROR] Compilación falló: {e}")
                rows.append({
                    "java_file": str(java_file),
                    "status": "compile_error",
                    "fqcn": entry.fqcn,
                    "emissions_kgco2eq": "",
                    "notes": str(e),
                })
                continue

            run_id = java_file.stem
            try:
                emissions_kg, stdout, stderr = execute_with_codecarbon(
                    fqcn=entry.fqcn,
                    classpath=build_dir,
                    java_args=args.args,
                    timeout=args.timeout,
                    output_dir=out_dir,
                    project_name=args.project,
                    run_id=run_id,
                )
                status = "ok" if not stderr else "ok_with_stderr"
                print(f"  Emisiones: {emissions_kg:.8f} kgCO2eq")
                if stderr:
                    print(f"  STDERR (resumen): {stderr[:400]}{'...' if len(stderr) > 400 else ''}")
                rows.append({
                    "java_file": str(java_file),
                    "status": status,
                    "fqcn": entry.fqcn,
                    "emissions_kgco2eq": f"{emissions_kg:.8f}",
                    "notes": (stderr[:1000] if stderr else ""),
                })
            except subprocess.TimeoutExpired:
                print(f"  [TIMEOUT] Se pasó de {args.timeout}s.")
                rows.append({
                    "java_file": str(java_file),
                    "status": "timeout",
                    "fqcn": entry.fqcn,
                    "emissions_kgco2eq": "",
                    "notes": f"Timeout after {args.timeout}s",
                })
            except Exception as e:
                print(f"  [ERROR] Ejecución falló: {e}")
                rows.append({
                    "java_file": str(java_file),
                    "status": "run_error",
                    "fqcn": entry.fqcn,
                    "emissions_kgco2eq": "",
                    "notes": str(e),
                })

    # CSV resumen final
    out_csv = Path(args.output).resolve()
    with out_csv.open("w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=["java_file", "status", "fqcn", "emissions_kgco2eq", "notes"])
        w.writeheader()
        for r in rows:
            w.writerow(r)

    print(f" Reporte generado: {out_csv}")
    print(f" Logs CodeCarbon:  {out_dir / 'emissions.csv'}")


if __name__ == "__main__":
    main()
