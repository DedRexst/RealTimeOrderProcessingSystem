export interface ProductRef {
  id: number;
  name: string;
  description: string;
}

export interface CreateProductRequest {
  name: string;
  description?: string;
  initialStock: number;
}

export interface CustomerRef {
  id: number;
  name: string;
  address: string;
}
