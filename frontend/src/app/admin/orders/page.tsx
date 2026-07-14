"use client";

import { useState } from "react";
import { useAdminOrders, useUpdateOrderStatus } from "@/lib/queries";
import { formatPrice, nextOrderStatuses, orderStatusColor, orderStatusLabel } from "@/lib/format";
import { getErrorMessage } from "@/lib/api";
import type { OrderStatus } from "@/lib/types";

export default function AdminOrdersPage() {
  const { data: orders, isLoading } = useAdminOrders();
  const updateStatus = useUpdateOrderStatus();

  const [selected, setSelected] = useState<Record<number, OrderStatus>>({});
  const [errors, setErrors] = useState<Record<number, string>>({});

  function handleUpdate(orderId: number) {
    const status = selected[orderId];
    if (!status) return;
    setErrors((e) => ({ ...e, [orderId]: "" }));
    updateStatus.mutate(
      { id: orderId, status },
      { onError: (err) => setErrors((e) => ({ ...e, [orderId]: getErrorMessage(err) })) }
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold">Đơn hàng</h1>

      {isLoading ? (
        <p className="mt-6">Đang tải...</p>
      ) : !orders || orders.length === 0 ? (
        <p className="mt-6 text-zinc-500">Chưa có đơn hàng nào.</p>
      ) : (
        <table className="mt-6 w-full border-collapse text-sm">
          <thead>
            <tr className="border-b border-black/15 text-left dark:border-white/20">
              <th className="py-2">ID</th>
              <th className="py-2">Khách hàng</th>
              <th className="py-2">Ngày tạo</th>
              <th className="py-2">Trạng thái</th>
              <th className="py-2">SP</th>
              <th className="py-2">Tổng</th>
              <th className="py-2">Cập nhật</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => {
              const options = nextOrderStatuses(order.status);
              return (
                <tr key={order.id} className="border-b border-black/10 align-top dark:border-white/15">
                  <td className="py-2">{order.id}</td>
                  <td className="py-2">{order.userEmail}</td>
                  <td className="py-2">{new Date(order.createdAt).toLocaleString("vi-VN")}</td>
                  <td className="py-2">
                    <span className={`rounded px-2 py-0.5 text-xs ${orderStatusColor(order.status)}`}>
                      {orderStatusLabel(order.status)}
                    </span>
                  </td>
                  <td className="py-2">{order.items.length}</td>
                  <td className="py-2">{formatPrice(order.totalAmount)}</td>
                  <td className="py-2">
                    {options.length > 0 ? (
                      <div className="flex items-center gap-2">
                        <select
                          value={selected[order.id] ?? ""}
                          onChange={(e) =>
                            setSelected((s) => ({ ...s, [order.id]: e.target.value as OrderStatus }))
                          }
                          className="rounded border border-black/20 px-2 py-1 text-xs dark:border-white/25"
                        >
                          <option value="" disabled>
                            -- Chọn --
                          </option>
                          {options.map((opt) => (
                            <option key={opt} value={opt}>
                              {orderStatusLabel(opt)}
                            </option>
                          ))}
                        </select>
                        <button
                          type="button"
                          disabled={!selected[order.id] || updateStatus.isPending}
                          onClick={() => handleUpdate(order.id)}
                          className="rounded border border-black/20 px-2 py-1 text-xs disabled:opacity-50 dark:border-white/25"
                        >
                          Cập nhật
                        </button>
                      </div>
                    ) : (
                      <span className="text-xs text-zinc-500">—</span>
                    )}
                    {errors[order.id] && <p className="mt-1 text-xs text-red-600">{errors[order.id]}</p>}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
