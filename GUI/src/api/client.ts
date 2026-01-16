import axios from "axios";
import { ENV } from "../config/env.ts";

const apiClient = axios.create({
  baseURL: ENV.API_BASE_URL,
    headers: {
    "Content-Type": "application/json",
    "Accept": "application/json",
  },
  withCredentials: false,
  timeout: 5000,
});

//  REQUEST DEBUG
apiClient.interceptors.request.use(
  
  (config) => {
    console.log(" API REQUEST:", {
      method: config.method,
      url: (config.baseURL || "") + config.url,
      data: config.data,
    });
    return config;
  },
  (error) => {
    console.error(" REQUEST ERROR:", error);
    return Promise.reject(error);
  }
);

//RESPONSE DEBUG
apiClient.interceptors.response.use(
  (response) => {
    console.log("API RESPONSE:", response);
    return response;
  },
  (error) => {
    console.error(" API RESPONSE ERROR:", error);

    if (error.code === "ERR_NETWORK") {
      console.error(" NETWORK ERROR – backend unreachable");
    }

    return Promise.reject(error);
  }
);

export default apiClient;

