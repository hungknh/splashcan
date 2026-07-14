import { Suspense } from "react";
import ShopContent from "./shop-content";

export default function ShopPage() {
  return (
    <div className="mx-auto max-w-5xl px-6 py-12">
      <h1 className="text-2xl font-bold">Cửa hàng</h1>
      <Suspense fallback={<p className="mt-6">Đang tải...</p>}>
        <ShopContent />
      </Suspense>
    </div>
  );
}
