// src/api/http.js
import axios from "axios";

const baseURL = import.meta.env.VITE_API_BASE || "/api"; 

const http = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default http;
