"use client";

import { useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useCategories, useProducts } from "@/lib/queries";
import { formatPrice } from "@/lib/format";

const SORT_OPTIONS = [
  { value: "", label: "Mới nhất" },
  { value: "price,asc", label: "Giá tăng dần" },
  { value: "price,desc", label: "Giá giảm dần" },
];

const inputClass = "rounded border border-black/20 px-3 py-2 text-sm dark:border-white/25";

export default function ShopContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const category = searchParams.get("category") ?? "";
  const sort = searchParams.get("sort") ?? "";
  const page = Number(searchParams.get("page") ?? "0");
  const minPrice = searchParams.get("minPrice") ?? "";
  const maxPrice = searchParams.get("maxPrice") ?? "";

  // Local state for price inputs so typing doesn't refetch on every keystroke —
  // only applied to the URL (and thus the query) on blur/submit.
  const [minPriceInput, setMinPriceInput] = useState(minPrice);
  const [maxPriceInput, setMaxPriceInput] = useState(maxPrice);

  useEffect(() => {
    setMinPriceInput(minPrice);
    setMaxPriceInput(maxPrice);
  }, [minPrice, maxPrice]);

  const { data: categories } = useCategories();
  const { data, isLoading, isError, refetch } = useProducts({
    category: category || undefined,
    sort: sort || undefined,
    minPrice: minPrice || undefined,
    maxPrice: maxPrice || undefined,
    page,
  });

  function updateParams(updates: Record<string, string | number | undefined>, resetPage = true) {
    const params = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(updates)) {
      if (value === undefined || value === "") {
        params.delete(key);
      } else {
        params.set(key, String(value));
      }
    }
    if (resetPage) params.delete("page");
    router.replace(`/shop?${params.toString()}`);
  }

  function applyPriceFilter(e?: FormEvent) {
    e?.preventDefault();
    updateParams({ minPrice: minPriceInput, maxPrice: maxPriceInput });
  }

  return (
    <div>
      <form
        onSubmit={applyPriceFilter}
        className="mt-6 flex flex-wrap items-end gap-4 border-b border-black/10 pb-6 dark:border-white/15"
      >
        <label className="flex flex-col gap-1 text-sm">
          Danh mục
          <select
            value={category}
            onChange={(e) => updateParams({ category: e.target.value })}
            className={inputClass}
          >
            <option value="">Tất cả</option>
            {categories?.map((c) => (
              <option key={c.id} value={c.slug}>
                {c.name}
              </option>
            ))}
          </select>
        </label>

        <label className="flex flex-col gap-1 text-sm">
          Sắp xếp
          <select
            value={sort}
            onChange={(e) => updateParams({ sort: e.target.value })}
            className={inputClass}
          >
            {SORT_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </label>

        <label className="flex flex-col gap-1 text-sm">
          Giá từ
          <input
            type="number"
            min={0}
            value={minPriceInput}
            onChange={(e) => setMinPriceInput(e.target.value)}
            onBlur={() => applyPriceFilter()}
            className={`${inputClass} w-28`}
          />
        </label>

        <label className="flex flex-col gap-1 text-sm">
          Giá đến
          <input
            type="number"
            min={0}
            value={maxPriceInput}
            onChange={(e) => setMaxPriceInput(e.target.value)}
            onBlur={() => applyPriceFilter()}
            className={`${inputClass} w-28`}
          />
        </label>

        <button type="submit" className="rounded border border-black/20 px-3 py-2 text-sm dark:border-white/25">
          Áp dụng
        </button>
      </form>

      {isLoading && <p className="mt-6">Đang tải...</p>}

      {isError && (
        <div className="mt-6 flex items-center gap-4">
          <p className="text-red-600">Có lỗi khi tải sản phẩm.</p>
          <button
            type="button"
            onClick={() => refetch()}
            className="rounded border border-black/20 px-3 py-1.5 text-sm dark:border-white/25"
          >
            Thử lại
          </button>
        </div>
      )}

      {data && (
        <>
          {data.content.length === 0 ? (
            <p className="mt-6 text-zinc-500">Không tìm thấy sản phẩm phù hợp.</p>
          ) : (
            <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {data.content.map((product) => (
                <Link
                  key={product.id}
                  href={`/shop/${product.id}`}
                  className="rounded border border-black/10 p-3 transition hover:border-black/30 dark:border-white/15 dark:hover:border-white/40"
                >
                  <div className="flex aspect-square items-center justify-center rounded bg-gradient-to-br from-sky-200 to-sky-400 p-4 text-center text-sm font-semibold text-sky-950 dark:from-sky-900 dark:to-sky-700 dark:text-sky-50">
                    {product.name}
                  </div>
                  <h3 className="mt-3 font-semibold">{product.name}</h3>
                  <p className="text-sm text-zinc-500">{product.category.name}</p>
                  <p className="mt-1 font-bold">{formatPrice(product.basePrice)}</p>
                  <p className="text-xs text-zinc-500">{product.variants.length} lựa chọn</p>
                </Link>
              ))}
            </div>
          )}

          <div className="mt-8 flex items-center justify-center gap-4 text-sm">
            <button
              type="button"
              disabled={page <= 0}
              onClick={() => updateParams({ page: page - 1 }, false)}
              className="rounded border border-black/20 px-3 py-1.5 disabled:opacity-40 dark:border-white/25"
            >
              Trước
            </button>
            <span>
              Trang {data.number + 1} / {Math.max(data.totalPages, 1)}
            </span>
            <button
              type="button"
              disabled={page >= data.totalPages - 1}
              onClick={() => updateParams({ page: page + 1 }, false)}
              className="rounded border border-black/20 px-3 py-1.5 disabled:opacity-40 dark:border-white/25"
            >
              Sau
            </button>
          </div>
        </>
      )}
    </div>
  );
}
