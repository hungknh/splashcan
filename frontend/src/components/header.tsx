"use client";

import Link from "next/link";

export default function Header() {
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
          <Link href="/login">Đăng nhập</Link>
          <button type="button" className="rounded border border-black/20 px-3 py-1.5 dark:border-white/25">
            Giỏ hàng
          </button>
        </div>
      </div>
    </header>
  );
}
