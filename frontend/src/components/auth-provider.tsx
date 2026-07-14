"use client";

import { useEffect } from "react";
import api from "@/lib/api";
import { getAccessToken } from "@/lib/tokens";
import { useAuthStore } from "@/lib/auth-store";

// Bootstraps auth state on mount: if a token exists, fetch the current user;
// an expired access token is handled transparently by the api.ts interceptor
// (silent refresh + retry). No token at all -> guest immediately.
export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const setUser = useAuthStore((s) => s.setUser);
  const clear = useAuthStore((s) => s.clear);

  useEffect(() => {
    if (!getAccessToken()) {
      clear();
      return;
    }
    api
      .get("/auth/me")
      .then((res) => setUser(res.data))
      .catch(() => clear());
  }, [setUser, clear]);

  return <>{children}</>;
}
