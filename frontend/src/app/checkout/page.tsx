"use client";

import { useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/auth-store";
import { useCart, useCreateOrder, usePayOrder } from "@/lib/queries";
import { formatPrice } from "@/lib/format";
import { getErrorMessage } from "@/lib/api";

export default function CheckoutPage() {
  const router = useRouter();
  const status = useAuthStore((s) => s.status);
  const { data: cart, isLoading } = useCart();
  const createOrder = useCreateOrder();
  const payOrder = usePayOrder();
  const [address, setAddress] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (status === "guest") router.replace("/login");
  }, [status, router]);

  if (status !== "authed") return null;

  const items = cart?.items ?? [];
  const submitting = createOrder.isPending || payOrder.isPending;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const order = await createOrder.mutateAsync({ shippingAddress: address });
      try {
        await payOrder.mutateAsync(order.id);
      } catch {
        // ponytail: a pay hiccup after a successful create still lands the user
        // on the order (cart is already cleared either way) — the order detail
        // page's PENDING fallback button covers a retry, no retry loop here.
      }
      router.push(`/orders/${order.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <div className="mx-auto max-w-2xl px-6 py-12">
      <h1 className="text-2xl font-bold">Thanh toán</h1>

      {isLoading ? (
        <p className="mt-6">Đang tải...</p>
      ) : items.length === 0 ? (
        <div className="mt-6 flex flex-col gap-2">
          <p>Giỏ hàng trống</p>
          <Link href="/shop" className="underline">
            Xem cửa hàng
          </Link>
        </div>
      ) : (
        <>
          <ul className="mt-6 flex flex-col gap-3">
            {items.map((item) => (
              <li
                key={item.id}
                className="flex items-center justify-between border-b border-black/10 pb-2 text-sm dark:border-white/15"
              >
                <span>
                  {item.flavor} · {item.sizeMl}ml × {item.quantity}
                </span>
                <span>{formatPrice(item.subtotal)}</span>
              </li>
            ))}
          </ul>
          <div className="mt-3 flex items-center justify-between font-semibold">
            <span>Tổng cộng</span>
            <span>{formatPrice(cart!.totalAmount)}</span>
          </div>

          <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
            <label className="flex flex-col gap-1 text-sm">
              Địa chỉ giao hàng
              <input
                type="text"
                required
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                className="rounded border border-black/20 px-3 py-2 dark:border-white/25"
              />
            </label>
            {error && <p className="text-sm text-red-600">{error}</p>}
            <button
              type="submit"
              disabled={submitting}
              className="rounded bg-black px-4 py-2 text-white disabled:opacity-50 dark:bg-white dark:text-black"
            >
              {submitting ? "Đang xử lý..." : "Đặt hàng"}
            </button>
          </form>
        </>
      )}
    </div>
  );
}
