import { useQuery } from "@tanstack/react-query";
import api from "./api";
import type { Category, Page, Product } from "./types";

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
