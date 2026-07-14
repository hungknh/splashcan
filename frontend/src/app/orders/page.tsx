"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/lib/auth-store";
import { useOrders } from "@/lib/queries";
import { formatPrice, orderStatusColor, orderStatusLabel } from "@/lib/format";

export default function OrdersPage() {
  const router = useRouter();
  const status = useAuthStore((s) => s.status);
  const { data: orders, isLoading } = useOrders();

  useEffect(() => {
    if (status === "guest") router.replace("/login");
  }, [status, router]);

  if (status !== "authed") return null;

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <h1 className="text-2xl font-bold">Đơn hàng của tôi</h1>

      {isLoading ? (
        <p className="mt-6">Đang tải...</p>
      ) : !orders || orders.length === 0 ? (
        <div className="mt-6 flex flex-col gap-2">
          <p>Bạn chưa có đơn hàng nào</p>
          <Link href="/shop" className="underline">
            Xem cửa hàng
          </Link>
        </div>
      ) : (
        <ul className="mt-6 flex flex-col gap-3">
          {orders.map((order) => (
            <li key={order.id}>
              <Link
                href={`/orders/${order.id}`}
                className="flex items-center justify-between gap-4 rounded border border-black/15 px-4 py-3 text-sm dark:border-white/20"
              >
                <div className="flex flex-col gap-1">
                  <span className="font-medium">Đơn #{order.id}</span>
                  <span className="text-zinc-500">
                    {new Date(order.createdAt).toLocaleString("vi-VN")}
                  </span>
                  <span className="text-zinc-500">{order.items.length} sản phẩm</span>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <span className={`rounded px-2 py-0.5 text-xs ${orderStatusColor(order.status)}`}>
                    {orderStatusLabel(order.status)}
                  </span>
                  <span className="font-semibold">{formatPrice(order.totalAmount)}</span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
