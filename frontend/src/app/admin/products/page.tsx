"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useAdminProducts, useCategories, useCreateProduct, useDeleteProduct } from "@/lib/queries";
import { formatPrice } from "@/lib/format";
import { getErrorMessage } from "@/lib/api";
import type { AdminProductRequest } from "@/lib/types";

const inputClass = "rounded border border-black/20 px-3 py-2 text-sm dark:border-white/25";

const emptyForm: AdminProductRequest = {
  name: "",
  description: "",
  basePrice: 0,
  categoryId: 0,
  thumbnailUrl: "",
  model3dUrl: "",
  active: true,
};

export default function AdminProductsPage() {
  const { data, isLoading } = useAdminProducts();
  const { data: categories } = useCategories();
  const createProduct = useCreateProduct();
  const deleteProduct = useDeleteProduct();

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<AdminProductRequest>(emptyForm);
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    createProduct.mutate(form, {
      onSuccess: () => {
        setForm(emptyForm);
        setShowForm(false);
      },
      onError: (err) => setError(getErrorMessage(err)),
    });
  }

  function handleDelete(id: number) {
    if (!window.confirm("Xoá sản phẩm này?")) return;
    deleteProduct.mutate(id);
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Sản phẩm</h1>
        <button
          type="button"
          onClick={() => setShowForm((v) => !v)}
          className="rounded border border-black/20 px-3 py-1.5 text-sm dark:border-white/25"
        >
          {showForm ? "Huỷ" : "Thêm sản phẩm"}
        </button>
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="mt-4 flex max-w-xl flex-col gap-3 rounded border border-black/15 p-4 dark:border-white/20"
        >
          <label className="flex flex-col gap-1 text-sm">
            Tên sản phẩm
            <input
              type="text"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className={inputClass}
            />
          </label>
          <label className="flex flex-col gap-1 text-sm">
            Mô tả
            <textarea
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              className={inputClass}
            />
          </label>
          <label className="flex flex-col gap-1 text-sm">
            Giá cơ bản
            <input
              type="number"
              min={0}
              step="0.01"
              required
              value={form.basePrice}
              onChange={(e) => setForm({ ...form, basePrice: Number(e.target.value) })}
              className={inputClass}
            />
          </label>
          <label className="flex flex-col gap-1 text-sm">
            Danh mục
            <select
              required
              value={form.categoryId || ""}
              onChange={(e) => setForm({ ...form, categoryId: Number(e.target.value) })}
              className={inputClass}
            >
              <option value="" disabled>
                -- Chọn danh mục --
              </option>
              {categories?.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label className="flex flex-col gap-1 text-sm">
            URL ảnh
            <input
              type="text"
              value={form.thumbnailUrl}
              onChange={(e) => setForm({ ...form, thumbnailUrl: e.target.value })}
              className={inputClass}
            />
          </label>
          <label className="flex flex-col gap-1 text-sm">
            URL mô hình 3D
            <input
              type="text"
              value={form.model3dUrl}
              onChange={(e) => setForm({ ...form, model3dUrl: e.target.value })}
              className={inputClass}
            />
          </label>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
            Đang bán
          </label>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <button
            type="submit"
            disabled={createProduct.isPending}
            className="w-fit rounded bg-black px-4 py-2 text-sm text-white disabled:opacity-50 dark:bg-white dark:text-black"
          >
            {createProduct.isPending ? "Đang lưu..." : "Lưu"}
          </button>
        </form>
      )}

      {isLoading ? (
        <p className="mt-6">Đang tải...</p>
      ) : (
        <table className="mt-6 w-full border-collapse text-sm">
          <thead>
            <tr className="border-b border-black/15 text-left dark:border-white/20">
              <th className="py-2">ID</th>
              <th className="py-2">Tên</th>
              <th className="py-2">Danh mục</th>
              <th className="py-2">Giá</th>
              <th className="py-2">Biến thể</th>
              <th className="py-2"></th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((product) => (
              <tr key={product.id} className="border-b border-black/10 dark:border-white/15">
                <td className="py-2">{product.id}</td>
                <td className="py-2">{product.name}</td>
                <td className="py-2">{product.category.name}</td>
                <td className="py-2">{formatPrice(product.basePrice)}</td>
                <td className="py-2">{product.variants.length}</td>
                <td className="py-2 text-right">
                  <Link href={`/admin/products/${product.id}`} className="underline">
                    Sửa
                  </Link>
                  <button
                    type="button"
                    onClick={() => handleDelete(product.id)}
                    className="ml-3 text-red-600 underline"
                  >
                    Xoá
                  </button>
                </td>
              </tr>
            ))}
            {data && data.content.length === 0 && (
              <tr>
                <td colSpan={6} className="py-4 text-center text-zinc-500">
                  Chưa có sản phẩm nào.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}
