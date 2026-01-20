import axios from "axios";

export const api = axios.create({
  // This tells axios to start every request with "/api"
  // Example: api.get("/route/nearest") becomes "/api/route/nearest"
  baseURL: "/api",
});