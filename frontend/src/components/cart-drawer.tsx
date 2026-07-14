"use client";

import { useState } from "react";
import Link from "next/link";
import { useCart, useRemoveCartItem, useUpdateCartItem } from "@/lib/queries";
import { useCartUiStore } from "@/lib/cart-ui-store";
import { formatPrice } from "@/lib/format";
import { getErrorMessage } from "@/lib/api";

// Mounted once in Providers. useCart() is enabled:false for guests, so this
// never fires a request when logged out — it just renders closed and empty.
export default function CartDrawer() {
  const isOpen = useCartUiStore((s) => s.isOpen);
  const close = useCartUiStore((s) => s.close);
  const { data: cart } = useCart();
  const updateItem = useUpdateCartItem();
  const removeItem = useRemoveCartItem();
  const [itemError, setItemError] = useState<{ itemId: number; message: string } | null>(null);

  const items = cart?.items ?? [];

  function changeQuantity(itemId: number, quantity: number) {
    if (quantity < 1) return;
    setItemError(null);
    updateItem.mutate(
      { itemId, quantity },
      { onError: (err) => setItemError({ itemId, message: getErrorMessage(err) }) }
    );
  }

  function remove(itemId: number) {
    setItemError(null);
    removeItem.mutate(itemId);
  }

  return (
    <>
      <div
        className={`fixed inset-0 z-40 bg-black/40 transition-opacity ${
          isOpen ? "opacity-100" : "pointer-events-none opacity-0"
        }`}
        onClick={close}
      />
      <aside
        className={`fixed right-0 top-0 z-50 flex h-full w-full max-w-sm flex-col bg-white shadow-xl transition-transform duration-200 dark:bg-zinc-900 ${
          isOpen ? "translate-x-0" : "translate-x-full"
        }`}
      >
        <div className="flex items-center justify-between border-b border-black/10 px-4 py-3 dark:border-white/15">
          <h2 className="font-semibold">Giỏ hàng</h2>
          <button type="button" onClick={close} className="text-sm underline">
            Đóng
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-3">
          {items.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center gap-2 text-center text-sm text-zinc-500">
              <p>Giỏ hàng trống</p>
              <Link href="/shop" onClick={close} className="underline">
                Xem cửa hàng
              </Link>
            </div>
          ) : (
            <ul className="flex flex-col gap-4">
              {items.map((item) => (
                <li
                  key={item.id}
                  className="flex flex-col gap-1 border-b border-black/10 pb-3 dark:border-white/15"
                >
                  <div className="flex items-center justify-between text-sm">
                    <span>
                      {item.flavor} · {item.sizeMl}ml
                    </span>
                    <span>{formatPrice(item.unitPrice)}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        disabled={updateItem.isPending || item.quantity <= 1}
                        onClick={() => changeQuantity(item.id, item.quantity - 1)}
                        className="h-6 w-6 rounded border border-black/20 disabled:opacity-50 dark:border-white/25"
                      >
                        -
                      </button>
                      <span className="w-6 text-center text-sm">{item.quantity}</span>
                      <button
                        type="button"
                        disabled={updateItem.isPending}
                        onClick={() => changeQuantity(item.id, item.quantity + 1)}
                        className="h-6 w-6 rounded border border-black/20 disabled:opacity-50 dark:border-white/25"
                      >
                        +
                      </button>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-medium">{formatPrice(item.subtotal)}</span>
                      <button
                        type="button"
                        disabled={removeItem.isPending}
                        onClick={() => remove(item.id)}
                        className="text-xs text-red-600 underline disabled:opacity-50"
                      >
                        Xoá
                      </button>
                    </div>
                  </div>
                  {/* ponytail: server is the stock authority — just render its 400, no client-side stock math */}
                  {itemError?.itemId === item.id && (
                    <p className="text-xs text-red-600">{itemError.message}</p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>

        {items.length > 0 && cart && (
          <div className="border-t border-black/10 px-4 py-3 dark:border-white/15">
            <div className="mb-3 flex items-center justify-between font-semibold">
              <span>Tổng cộng</span>
              <span>{formatPrice(cart.totalAmount)}</span>
            </div>
            <Link
              href="/checkout"
              onClick={close}
              className="block rounded bg-black px-4 py-2 text-center text-white dark:bg-white dark:text-black"
            >
              Thanh toán
            </Link>
          </div>
        )}
      </aside>
    </>
  );
}
