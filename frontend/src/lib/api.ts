import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from "./tokens";
import { useAuthStore } from "./auth-store";

const api = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.set("Authorization", `Bearer ${token}`);
  }
  return config;
});

// Endpoints that must never trigger a refresh-and-retry on 401: refresh itself
// (would recurse) and login/register (a 401 there means bad credentials, not
// an expired token).
function isNonRetryableAuthCall(url: string | undefined): boolean {
  if (!url) return false;
  return (
    url.includes("/auth/refresh") ||
    url.includes("/auth/login") ||
    url.includes("/auth/register")
  );
}

// Single-flight refresh: concurrent 401s share one in-flight refresh call so a
// second refresh never fires with the (rotation-revoked) old refresh token.
let refreshPromise: Promise<string> | null = null;

function refreshAccessToken(): Promise<string> {
  if (!refreshPromise) {
    refreshPromise = axios
      .post("/api/auth/refresh", { refreshToken: getRefreshToken() })
      .then((res) => {
        setTokens(res.data);
        return res.data.accessToken as string;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const config = error.config as RetriableConfig | undefined;

    if (
      error.response?.status === 401 &&
      config &&
      !isNonRetryableAuthCall(config.url) &&
      !config._retry
    ) {
      config._retry = true;
      try {
        const accessToken = await refreshAccessToken();
        config.headers.set("Authorization", `Bearer ${accessToken}`);
        return api(config);
      } catch (refreshError) {
        clearTokens();
        useAuthStore.getState().clear();
        if (typeof window !== "undefined") {
          window.location.assign("/login");
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    if (typeof message === "string" && message.length > 0) return message;
  }
  return "Có lỗi xảy ra, vui lòng thử lại.";
}

export default api;
