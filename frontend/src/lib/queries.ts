import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "./api";
import { useAuthStore } from "./auth-store";
import type { Cart, Category, Order, Page, Product } from "./types";

export const PRODUCTS_PAGE_SIZE = 12;

export type ProductFilters = {
  category?: string;
  minPrice?: string;
  maxPrice?: string;
  sort?: string;
  page: number;
};

// Query key includes the full filter object so any filter/pagination change refetches.
export function useProducts(filters: ProductFilters) {
  return useQuery({
    queryKey: ["products", filters],
    queryFn: async () => {
      const { data } = await api.get<Page<Product>>("/products", {
        params: {
          category: filters.category || undefined,
          minPrice: filters.minPrice || undefined,
          maxPrice: filters.maxPrice || undefined,
          sort: filters.sort || undefined,
          page: filters.page,
          size: PRODUCTS_PAGE_SIZE,
        },
      });
      return data;
    },
  });
}

export function useProduct(id: string) {
  return useQuery({
    queryKey: ["product", id],
    queryFn: async () => {
      const { data } = await api.get<Product>(`/products/${id}`);
      return data;
    },
    // Don't retry: a 404 means "no such product" and retrying just delays notFound().
    retry: false,
  });
}

export function useCategories() {
  return useQuery({
    queryKey: ["categories"],
    queryFn: async () => {
      const { data } = await api.get<Category[]>("/categories");
      return data;
    },
    staleTime: 5 * 60 * 1000,
  });
}

// Cart: server is the source of truth, React Query just caches its responses.
// Query key is namespaced by user id (not just ["cart"]) so switching accounts
// in the same browser session can never show the previous user's cached cart
// while the fresh fetch is in flight.
function cartKey(userId: string | number | undefined) {
  return ["cart", userId] as const;
}

export function useCart() {
  const status = useAuthStore((s) => s.status);
  const userId = useAuthStore((s) => s.user?.id);
  return useQuery({
    queryKey: cartKey(userId),
    queryFn: async () => {
      const { data } = await api.get<Cart>("/cart");
      return data;
    },
    enabled: status === "authed",
  });
}

// Each mutation gets back the full updated cart from the backend, so we write
// it straight into the cache instead of refetching.
function useSetCartCache() {
  const queryClient = useQueryClient();
  return (data: Cart) => {
    const userId = useAuthStore.getState().user?.id;
    queryClient.setQueryData(cartKey(userId), data);
  };
}

export function useAddCartItem() {
  const setCartCache = useSetCartCache();
  return useMutation({
    mutationFn: async (payload: { variantId: number; quantity: number }) => {
      const { data } = await api.post<Cart>("/cart/items", payload);
      return data;
    },
    onSuccess: setCartCache,
  });
}

export function useUpdateCartItem() {
  const setCartCache = useSetCartCache();
  return useMutation({
    mutationFn: async ({ itemId, quantity }: { itemId: number; quantity: number }) => {
      const { data } = await api.put<Cart>(`/cart/items/${itemId}`, { quantity });
      return data;
    },
    onSuccess: setCartCache,
  });
}

export function useRemoveCartItem() {
  const setCartCache = useSetCartCache();
  return useMutation({
    mutationFn: async (itemId: number) => {
      const { data } = await api.delete<Cart>(`/cart/items/${itemId}`);
      return data;
    },
    onSuccess: setCartCache,
  });
}

// Orders: user-scoped, so gated on auth same as cart to avoid firing (and
// 401-ing) before AuthProvider resolves or for guests hitting the page directly.
export function useOrders() {
  const status = useAuthStore((s) => s.status);
  return useQuery({
    queryKey: ["orders"],
    queryFn: async () => {
      const { data } = await api.get<Order[]>("/orders");
      return data;
    },
    enabled: status === "authed",
  });
}

export function useOrder(id: string) {
  const status = useAuthStore((s) => s.status);
  return useQuery({
    queryKey: ["order", id],
    queryFn: async () => {
      const { data } = await api.get<Order>(`/orders/${id}`);
      return data;
    },
    enabled: status === "authed",
    // Don't retry: a 404 means "not your order / doesn't exist" and retrying just delays notFound().
    retry: false,
  });
}

// Creating an order clears the user's cart server-side as a side effect the
// client didn't directly cause, so (unlike the cart item mutations above)
// cache and reality can drift — invalidate rather than manually reconstruct.
// Also invalidate the orders list so history is fresh right after checkout.
export function useCreateOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: { shippingAddress: string }) => {
      const { data } = await api.post<Order>("/orders", payload);
      return data;
    },
    onSuccess: () => {
      const userId = useAuthStore.getState().user?.id;
      queryClient.invalidateQueries({ queryKey: cartKey(userId) });
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
  });
}

export function usePayOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await api.post<Order>(`/orders/${id}/pay`);
      return data;
    },
    onSuccess: (data) => {
      // We have the exact updated order, so write it straight into the detail
      // cache (avoids a PENDING flash right after the checkout redirect); the
      // list is just invalidated since it's simpler and no flash risk there.
      queryClient.setQueryData(["order", String(data.id)], data);
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
  });
}
