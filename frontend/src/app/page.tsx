import Link from "next/link";

export default function Home() {
  return (
    <div className="mx-auto flex max-w-3xl flex-col items-center gap-6 px-6 py-24 text-center">
      <h1 className="text-4xl font-bold tracking-tight">SplashCan</h1>
      <p className="text-lg text-zinc-600 dark:text-zinc-400">
        Nước giải khát đóng lon, giao nhanh tận nơi.
      </p>
      <Link
        href="/shop"
        className="rounded bg-black px-6 py-3 text-white dark:bg-white dark:text-black"
      >
        Xem cửa hàng
      </Link>
    </div>
  );
}
