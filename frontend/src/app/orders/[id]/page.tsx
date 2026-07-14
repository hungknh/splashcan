"use client";

import { use, useEffect } from "react";
import Link from "next/link";
import { notFound, useRouter } from "next/navigation";
import axios from "axios";
import { useAuthStore } from "@/lib/auth-store";
import { useOrder, usePayOrder } from "@/lib/queries";
import { formatPrice, orderStatusColor, orderStatusLabel } from "@/lib/format";
import { getErrorMessage } from "@/lib/api";

export default function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const status = useAuthStore((s) => s.status);
  const { data: order, isLoading, isError, error } = useOrder(id);
  const payOrder = usePayOrder();

  useEffect(() => {
    if (status === "guest") router.replace("/login");
  }, [status, router]);

  if (status !== "authed") return null;

  if (isError) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      notFound();
    }
    return (
      <div className="mx-auto max-w-3xl px-6 py-12">
        <p className="text-red-600">Có lỗi khi tải đơn hàng.</p>
      </div>
    );
  }

  if (isLoading || !order) {
    return <div className="mx-auto max-w-3xl px-6 py-12">Đang tải...</div>;
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <Link href="/orders" className="text-sm underline">
        ← Xem tất cả đơn hàng
      </Link>

      <div className="mt-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Đơn hàng #{order.id}</h1>
        <span className={`rounded px-2 py-1 text-sm ${orderStatusColor(order.status)}`}>
          {orderStatusLabel(order.status)}
        </span>
      </div>

      <p className="mt-2 text-sm text-zinc-500">
        {new Date(order.createdAt).toLocaleString("vi-VN")}
      </p>
      <p className="mt-1 text-sm">Giao đến: {order.shippingAddress}</p>

      {order.status === "PENDING" && (
        <div className="mt-4">
          <button
            type="button"
            disabled={payOrder.isPending}
            onClick={() => payOrder.mutate(order.id)}
            className="rounded bg-black px-4 py-2 text-white disabled:opacity-50 dark:bg-white dark:text-black"
          >
            {payOrder.isPending ? "Đang xử lý..." : "Thanh toán ngay"}
          </button>
          {payOrder.isError && (
            <p className="mt-1 text-xs text-red-600">{getErrorMessage(payOrder.error)}</p>
          )}
        </div>
      )}

      <ul className="mt-6 flex flex-col gap-3">
        {order.items.map((item) => (
          <li
            key={item.variantId}
            className="flex items-center justify-between border-b border-black/10 pb-2 text-sm dark:border-white/15"
          >
            <span>
              {item.flavor} · {item.sizeMl}ml × {item.quantity} ({formatPrice(item.unitPrice)}/sp)
            </span>
            <span>{formatPrice(item.subtotal)}</span>
          </li>
        ))}
      </ul>
      <div className="mt-3 flex items-center justify-between font-semibold">
        <span>Tổng cộng</span>
        <span>{formatPrice(order.totalAmount)}</span>
      </div>
    </div>
  );
}
