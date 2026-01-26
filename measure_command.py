import argparse
import subprocess
from codecarbon import EmissionsTracker

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--project", default="springboot-service")
    ap.add_argument("--output-dir", default="codecarbon_logs")
    ap.add_argument("--timeout", type=int, default=60)
    ap.add_argument("cmd", nargs="+", help="Command to execute (e.g. java -jar app.jar)")
    args = ap.parse_args()

    tracker = EmissionsTracker(
        project_name=args.project,
        output_dir=args.output_dir,
        save_to_file=True
    )

    tracker.start()
    try:
        try:
            subprocess.run(args.cmd, check=False, timeout=args.timeout)
        except subprocess.TimeoutExpired:
            # Normal for long-running services like Spring Boot
            pass
    finally:
        emissions = tracker.stop() or 0.0

    print(f"Emisiones: {emissions:.8f} kgCO2eq")

if __name__ == "__main__":
    main()
