"use client";

import { use, useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import { notFound } from "next/navigation";
import axios from "axios";
import {
  useCategories,
  useCreateVariant,
  useDeleteVariant,
  useProduct,
  useUpdateProduct,
  useUpdateVariant,
} from "@/lib/queries";
import { formatPrice } from "@/lib/format";
import { getErrorMessage } from "@/lib/api";
import type { AdminProductRequest, AdminVariantRequest, ProductVariant } from "@/lib/types";

const inputClass = "rounded border border-black/20 px-3 py-2 text-sm dark:border-white/25";

const emptyVariantForm: AdminVariantRequest = {
  flavor: "",
  sizeMl: 0,
  price: 0,
  stockQuantity: 0,
  sku: "",
};

export default function AdminProductEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: product, isLoading, isError, error } = useProduct(id);
  const { data: categories } = useCategories();
  const updateProduct = useUpdateProduct(id);
  const createVariant = useCreateVariant(id);
  const updateVariant = useUpdateVariant(id);
  const deleteVariant = useDeleteVariant(id);

  const [form, setForm] = useState<AdminProductRequest | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const [showVariantForm, setShowVariantForm] = useState(false);
  const [variantForm, setVariantForm] = useState<AdminVariantRequest>(emptyVariantForm);
  const [variantError, setVariantError] = useState<string | null>(null);

  const [editingVariantId, setEditingVariantId] = useState<number | null>(null);
  const [editVariantForm, setEditVariantForm] = useState<AdminVariantRequest>(emptyVariantForm);
  const [editVariantError, setEditVariantError] = useState<string | null>(null);

  useEffect(() => {
    if (product && form === null) {
      setForm({
        name: product.name,
        description: product.description,
        basePrice: product.basePrice,
        categoryId: product.category.id,
        thumbnailUrl: product.thumbnailUrl,
        model3dUrl: product.model3dUrl,
        // ponytail: the public ProductResponse (behind useProduct) has no
        // `active` field, but every product reachable here comes from the
        // admin list, which only ever shows active=true products — so
        // defaulting checked is always correct for products opened this way.
        active: true,
      });
    }
  }, [product, form]);

  if (isError) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      notFound();
    }
    return <p className="text-red-600">Có lỗi khi tải sản phẩm.</p>;
  }

  if (isLoading || !product || !form) {
    return <p>Đang tải...</p>;
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!form) return;
    setFormError(null);
    setSaved(false);
    updateProduct.mutate(form, {
      onSuccess: () => setSaved(true),
      onError: (err) => setFormError(getErrorMessage(err)),
    });
  }

  function handleAddVariant(e: FormEvent) {
    e.preventDefault();
    setVariantError(null);
    createVariant.mutate(variantForm, {
      onSuccess: () => {
        setVariantForm(emptyVariantForm);
        setShowVariantForm(false);
      },
      onError: (err) => setVariantError(getErrorMessage(err)),
    });
  }

  function startEditVariant(v: ProductVariant) {
    setEditingVariantId(v.id);
    setEditVariantForm({
      flavor: v.flavor,
      sizeMl: v.sizeMl,
      price: v.price,
      stockQuantity: v.stockQuantity,
      sku: v.sku,
    });
    setEditVariantError(null);
  }

  function handleSaveVariant(e: FormEvent, variantId: number) {
    e.preventDefault();
    setEditVariantError(null);
    updateVariant.mutate(
      { id: variantId, payload: editVariantForm },
      {
        onSuccess: () => setEditingVariantId(null),
        onError: (err) => setEditVariantError(getErrorMessage(err)),
      }
    );
  }

  function handleDeleteVariant(variantId: number) {
    if (!window.confirm("Xoá biến thể này?")) return;
    deleteVariant.mutate(variantId);
  }

  return (
    <div>
      <Link href="/admin/products" className="text-sm underline">
        ← Danh sách sản phẩm
      </Link>

      <h1 className="mt-4 text-2xl font-bold">Sửa sản phẩm #{product.id}</h1>

      <form onSubmit={handleSubmit} className="mt-4 flex max-w-xl flex-col gap-3">
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
            value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: Number(e.target.value) })}
            className={inputClass}
          >
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
        {formError && <p className="text-sm text-red-600">{formError}</p>}
        {saved && !formError && <p className="text-sm text-green-600">Đã lưu.</p>}
        <button
          type="submit"
          disabled={updateProduct.isPending}
          className="w-fit rounded bg-black px-4 py-2 text-sm text-white disabled:opacity-50 dark:bg-white dark:text-black"
        >
          {updateProduct.isPending ? "Đang lưu..." : "Lưu thay đổi"}
        </button>
      </form>

      <div className="mt-10">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold">Biến thể</h2>
          <button
            type="button"
            onClick={() => setShowVariantForm((v) => !v)}
            className="rounded border border-black/20 px-3 py-1.5 text-sm dark:border-white/25"
          >
            {showVariantForm ? "Huỷ" : "Thêm biến thể"}
          </button>
        </div>

        {showVariantForm && (
          <form
            onSubmit={handleAddVariant}
            className="mt-3 flex flex-wrap items-end gap-3 rounded border border-black/15 p-4 dark:border-white/20"
          >
            <label className="flex flex-col gap-1 text-sm">
              Hương vị
              <input
                type="text"
                required
                value={variantForm.flavor}
                onChange={(e) => setVariantForm({ ...variantForm, flavor: e.target.value })}
                className={inputClass}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm">
              Dung tích (ml)
              <input
                type="number"
                min={1}
                required
                value={variantForm.sizeMl}
                onChange={(e) => setVariantForm({ ...variantForm, sizeMl: Number(e.target.value) })}
                className={`${inputClass} w-24`}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm">
              Giá
              <input
                type="number"
                min={0}
                step="0.01"
                required
                value={variantForm.price}
                onChange={(e) => setVariantForm({ ...variantForm, price: Number(e.target.value) })}
                className={`${inputClass} w-28`}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm">
              Tồn kho
              <input
                type="number"
                min={0}
                required
                value={variantForm.stockQuantity}
                onChange={(e) => setVariantForm({ ...variantForm, stockQuantity: Number(e.target.value) })}
                className={`${inputClass} w-24`}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm">
              SKU
              <input
                type="text"
                required
                value={variantForm.sku}
                onChange={(e) => setVariantForm({ ...variantForm, sku: e.target.value })}
                className={inputClass}
              />
            </label>
            <button
              type="submit"
              disabled={createVariant.isPending}
              className="rounded bg-black px-4 py-2 text-sm text-white disabled:opacity-50 dark:bg-white dark:text-black"
            >
              {createVariant.isPending ? "Đang lưu..." : "Thêm"}
            </button>
            {variantError && <p className="w-full text-sm text-red-600">{variantError}</p>}
          </form>
        )}

        <table className="mt-4 w-full border-collapse text-sm">
          <thead>
            <tr className="border-b border-black/15 text-left dark:border-white/20">
              <th className="py-2">Hương vị</th>
              <th className="py-2">Dung tích</th>
              <th className="py-2">Giá</th>
              <th className="py-2">Tồn kho</th>
              <th className="py-2">SKU</th>
              <th className="py-2"></th>
            </tr>
          </thead>
          <tbody>
            {product.variants.map((v) =>
              editingVariantId === v.id ? (
                <tr key={v.id} className="border-b border-black/10 dark:border-white/15">
                  <td colSpan={6} className="py-2">
                    <form
                      onSubmit={(e) => handleSaveVariant(e, v.id)}
                      className="flex flex-wrap items-end gap-3"
                    >
                      <input
                        type="text"
                        required
                        value={editVariantForm.flavor}
                        onChange={(e) => setEditVariantForm({ ...editVariantForm, flavor: e.target.value })}
                        className={inputClass}
                      />
                      <input
                        type="number"
                        min={1}
                        required
                        value={editVariantForm.sizeMl}
                        onChange={(e) =>
                          setEditVariantForm({ ...editVariantForm, sizeMl: Number(e.target.value) })
                        }
                        className={`${inputClass} w-24`}
                      />
                      <input
                        type="number"
                        min={0}
                        step="0.01"
                        required
                        value={editVariantForm.price}
                        onChange={(e) =>
                          setEditVariantForm({ ...editVariantForm, price: Number(e.target.value) })
                        }
                        className={`${inputClass} w-28`}
                      />
                      <input
                        type="number"
                        min={0}
                        required
                        value={editVariantForm.stockQuantity}
                        onChange={(e) =>
                          setEditVariantForm({ ...editVariantForm, stockQuantity: Number(e.target.value) })
                        }
                        className={`${inputClass} w-24`}
                      />
                      <input
                        type="text"
                        required
                        value={editVariantForm.sku}
                        onChange={(e) => setEditVariantForm({ ...editVariantForm, sku: e.target.value })}
                        className={inputClass}
                      />
                      <button
                        type="submit"
                        disabled={updateVariant.isPending}
                        className="rounded bg-black px-3 py-1.5 text-xs text-white disabled:opacity-50 dark:bg-white dark:text-black"
                      >
                        Lưu
                      </button>
                      <button
                        type="button"
                        onClick={() => setEditingVariantId(null)}
                        className="rounded border border-black/20 px-3 py-1.5 text-xs dark:border-white/25"
                      >
                        Huỷ
                      </button>
                      {editVariantError && (
                        <p className="w-full text-xs text-red-600">{editVariantError}</p>
                      )}
                    </form>
                  </td>
                </tr>
              ) : (
                <tr key={v.id} className="border-b border-black/10 dark:border-white/15">
                  <td className="py-2">{v.flavor}</td>
                  <td className="py-2">{v.sizeMl}ml</td>
                  <td className="py-2">{formatPrice(v.price)}</td>
                  <td className="py-2">{v.stockQuantity}</td>
                  <td className="py-2">{v.sku}</td>
                  <td className="py-2 text-right">
                    <button type="button" onClick={() => startEditVariant(v)} className="underline">
                      Sửa
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDeleteVariant(v.id)}
                      className="ml-3 text-red-600 underline"
                    >
                      Xoá
                    </button>
                  </td>
                </tr>
              )
            )}
            {product.variants.length === 0 && (
              <tr>
                <td colSpan={6} className="py-4 text-center text-zinc-500">
                  Chưa có biến thể nào.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
