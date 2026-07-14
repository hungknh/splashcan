"use client";

import { use, useState } from "react";
import Link from "next/link";
import { notFound } from "next/navigation";
import axios from "axios";
import { useProduct } from "@/lib/queries";
import { useAuthStore } from "@/lib/auth-store";
import { formatPrice } from "@/lib/format";

export default function ProductDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: product, isLoading, isError, error, refetch } = useProduct(id);
  const status = useAuthStore((s) => s.status);

  const [selectedVariantId, setSelectedVariantId] = useState<number | null>(null);
  const [quantity, setQuantity] = useState(1);

  if (isError) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      notFound();
    }
    return (
      <div className="mx-auto max-w-3xl px-6 py-12">
        <p className="text-red-600">Có lỗi khi tải sản phẩm.</p>
        <button
          type="button"
          onClick={() => refetch()}
          className="mt-4 rounded border border-black/20 px-4 py-2 text-sm dark:border-white/25"
        >
          Thử lại
        </button>
      </div>
    );
  }

  if (isLoading || !product) {
    return <div className="mx-auto max-w-3xl px-6 py-12">Đang tải...</div>;
  }

  const selectedVariant =
    product.variants.find((v) => v.id === selectedVariantId) ?? product.variants[0] ?? null;
  const canAddToCart = status === "authed" && !!selectedVariant && selectedVariant.stockQuantity > 0;

  function handleAddToCart() {
    // ponytail: cart API wiring lands in Task 4 (this button just proves the selector UI works for now).
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <Link href="/shop" className="text-sm underline">
        ← Quay lại cửa hàng
      </Link>

      <div className="mt-6 grid gap-8 sm:grid-cols-2">
        <div className="flex aspect-square items-center justify-center rounded bg-gradient-to-br from-sky-200 to-sky-400 p-6 text-center text-xl font-semibold text-sky-950 dark:from-sky-900 dark:to-sky-700 dark:text-sky-50">
          {product.name}
        </div>

        <div>
          <p className="text-sm text-zinc-500">{product.category.name}</p>
          <h1 className="text-2xl font-bold">{product.name}</h1>
          <p className="mt-2 text-zinc-600 dark:text-zinc-400">{product.description}</p>
          <p className="mt-4 text-xl font-bold">{formatPrice(product.basePrice)}</p>

          <div className="mt-6">
            <h2 className="font-semibold">Chọn loại</h2>
            <div className="mt-2 flex flex-col gap-2">
              {product.variants.map((v) => (
                <label
                  key={v.id}
                  className={`flex items-center justify-between gap-3 rounded border border-black/15 px-3 py-2 text-sm dark:border-white/20 ${
                    v.stockQuantity === 0 ? "opacity-50" : ""
                  }`}
                >
                  <span className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="variant"
                      checked={selectedVariant?.id === v.id}
                      disabled={v.stockQuantity === 0}
                      onChange={() => setSelectedVariantId(v.id)}
                    />
                    {v.flavor} · {v.sizeMl}ml
                  </span>
                  <span className="flex items-center gap-3">
                    {formatPrice(v.price)}
                    <span className={v.stockQuantity === 0 ? "text-red-600" : "text-zinc-500"}>
                      {v.stockQuantity === 0 ? "Hết hàng" : `Còn hàng (${v.stockQuantity})`}
                    </span>
                  </span>
                </label>
              ))}
            </div>
          </div>

          <label className="mt-4 flex items-center gap-2 text-sm">
            Số lượng
            <input
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, Number(e.target.value) || 1))}
              className="w-16 rounded border border-black/20 px-2 py-1 dark:border-white/25"
            />
          </label>

          <button
            type="button"
            disabled={!canAddToCart}
            title={status !== "authed" ? "Đăng nhập để mua" : undefined}
            onClick={handleAddToCart}
            className="mt-4 rounded bg-black px-4 py-2 text-white disabled:opacity-50 dark:bg-white dark:text-black"
          >
            Thêm vào giỏ
          </button>
          {status !== "authed" && <p className="mt-1 text-xs text-zinc-500">Đăng nhập để mua</p>}
        </div>
      </div>
    </div>
  );
}
