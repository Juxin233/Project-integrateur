export function clearSession() {
  localStorage.removeItem("session");
}

export function getSession() {
  const raw = localStorage.getItem("session");
  return raw ? JSON.parse(raw) : null;
}
