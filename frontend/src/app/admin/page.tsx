"use client";

import { useAdminStats } from "@/lib/queries";
import { formatPrice } from "@/lib/format";

export default function AdminDashboardPage() {
  const { data: stats, isLoading, isError } = useAdminStats();

  return (
    <div>
      <h1 className="text-2xl font-bold">Thống kê</h1>

      {isLoading && <p className="mt-6">Đang tải...</p>}
      {isError && <p className="mt-6 text-red-600">Có lỗi khi tải thống kê.</p>}

      {stats && (
        <>
          <div className="mt-6 flex flex-wrap gap-6">
            <div className="rounded border border-black/15 px-4 py-3 dark:border-white/20">
              <p className="text-sm text-zinc-500">Tổng doanh thu</p>
              <p className="text-xl font-bold">{formatPrice(stats.totalRevenue)}</p>
            </div>
            <div className="rounded border border-black/15 px-4 py-3 dark:border-white/20">
              <p className="text-sm text-zinc-500">Tổng đơn hàng</p>
              <p className="text-xl font-bold">{stats.totalOrders}</p>
            </div>
          </div>

          <h2 className="mt-8 font-semibold">Đơn hàng theo ngày</h2>
          {stats.ordersByDay.length === 0 ? (
            <p className="mt-3 text-zinc-500">Chưa có dữ liệu.</p>
          ) : (
            <table className="mt-3 w-full max-w-lg border-collapse text-sm">
              <thead>
                <tr className="border-b border-black/15 text-left dark:border-white/20">
                  <th className="py-2">Ngày</th>
                  <th className="py-2">Số đơn</th>
                  <th className="py-2">Doanh thu</th>
                </tr>
              </thead>
              <tbody>
                {stats.ordersByDay.map((row) => (
                  <tr key={row.date} className="border-b border-black/10 dark:border-white/15">
                    <td className="py-2">{row.date}</td>
                    <td className="py-2">{row.orderCount}</td>
                    <td className="py-2">{formatPrice(row.revenue)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  );
}
