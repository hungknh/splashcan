"use client";

import { useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/auth-store";

const NAV_LINKS = [
  { href: "/admin/products", label: "Sản phẩm" },
  { href: "/admin/orders", label: "Đơn hàng" },
  { href: "/admin", label: "Thống kê" },
];

// Client-side guard (UX only — the backend independently enforces
// hasRole('ADMIN') on every /api/admin/** call, which is the real security
// boundary). loading -> render nothing (avoid a flash); guest -> /login;
// authed non-admin -> bounce home, no "forbidden" page.
export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const status = useAuthStore((s) => s.status);
  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    if (status === "guest") {
      router.replace("/login");
    } else if (status === "authed" && user?.role !== "ADMIN") {
      router.replace("/");
    }
  }, [status, user, router]);

  if (status !== "authed" || user?.role !== "ADMIN") return null;

  return (
    <div className="mx-auto max-w-5xl px-6 py-12">
      <nav className="mb-8 flex gap-4 border-b border-black/10 pb-4 text-sm dark:border-white/15">
        {NAV_LINKS.map((link) => (
          <Link
            key={link.href}
            href={link.href}
            className={pathname === link.href ? "font-semibold underline" : ""}
          >
            {link.label}
          </Link>
        ))}
      </nav>
      {children}
    </div>
  );
}
