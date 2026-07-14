import { create } from "zustand";

export type User = {
  id: number | string;
  email: string;
  fullName: string;
  role: "CUSTOMER" | "ADMIN";
};

type AuthState = {
  user: User | null;
  status: "loading" | "authed" | "guest";
  setUser: (user: User) => void;
  clear: () => void;
};

// In-memory UI state only — tokens live in localStorage (see tokens.ts), not here.
export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  status: "loading",
  setUser: (user) => set({ user, status: "authed" }),
  clear: () => set({ user: null, status: "guest" }),
}));
