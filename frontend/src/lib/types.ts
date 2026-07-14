// Shapes returned by the public product/category API (see backend
// com.splashcan.backend.product.dto.ProductResponse / ProductVariantResponse
// and com.splashcan.backend.category.dto.CategoryResponse).

export type Category = {
  id: number;
  name: string;
  slug: string;
};

export type ProductVariant = {
  id: number;
  flavor: string;
  sizeMl: number;
  price: number;
  stockQuantity: number;
  sku: string;
};

export type Product = {
  id: number;
  name: string;
  description: string;
  basePrice: number;
  category: Category;
  thumbnailUrl: string;
  model3dUrl: string;
  variants: ProductVariant[];
};

// Spring Data's Page<T> envelope — only the fields the UI actually reads.
export type Page<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
};

// Shapes returned by the cart API (see backend
// com.splashcan.backend.cart.dto.CartResponse / CartItemResponse — verified
// against the source, not from memory). Note there is NO productId/productName
// on the item, just the variant's own flavor/sizeMl/sku.
export type CartItem = {
  id: number;
  variantId: number;
  flavor: string;
  sizeMl: number;
  sku: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
};

export type Cart = {
  id: number;
  items: CartItem[];
  totalAmount: number;
};
