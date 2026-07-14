"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/auth-store";
import { useCartUiStore } from "@/lib/cart-ui-store";
import { useCart } from "@/lib/queries";
import { clearTokens } from "@/lib/tokens";

export default function Header() {
  const router = useRouter();
  const status = useAuthStore((s) => s.status);
  const user = useAuthStore((s) => s.user);
  const clear = useAuthStore((s) => s.clear);
  const toggleCart = useCartUiStore((s) => s.toggle);
  const { data: cart } = useCart();

  // Guard on status (not just cart presence) so a stale/guest render never shows a badge.
  const itemCount =
    status === "authed" ? (cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0) : 0;

  function handleLogout() {
    clearTokens();
    clear();
    router.push("/");
  }

  function handleCartClick() {
    if (status === "authed") {
      toggleCart();
    } else {
      router.push("/login");
    }
  }

  return (
    <header className="border-b border-black/10 dark:border-white/15">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <Link href="/" className="text-lg font-bold tracking-tight">
          SplashCan
        </Link>

        <nav className="flex items-center gap-6 text-sm">
          <Link href="/shop">Shop</Link>
        </nav>

        <div className="flex items-center gap-4 text-sm">
          {status === "authed" && user ? (
            <>
              <span>{user.fullName}</span>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded border border-black/20 px-3 py-1.5 dark:border-white/25"
              >
                Đăng xuất
              </button>
            </>
          ) : status === "guest" ? (
            <Link href="/login">Đăng nhập</Link>
          ) : null}
          <button
            type="button"
            onClick={handleCartClick}
            className="relative rounded border border-black/20 px-3 py-1.5 dark:border-white/25"
          >
            Giỏ hàng
            {itemCount > 0 && (
              <span className="absolute -right-2 -top-2 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-xs text-white">
                {itemCount}
              </span>
            )}
          </button>
        </div>
      </div>
    </header>
  );
}
