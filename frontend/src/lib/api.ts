import axios from "axios";

// ponytail: no auth interceptor yet, that's Task 2's job. This is the clean seam.
const api = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

export default api;
