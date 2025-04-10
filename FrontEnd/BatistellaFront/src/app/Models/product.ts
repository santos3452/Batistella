export interface Product {
    id: number;
    name: string;
    price: number;
    category: 'dog' | 'cat' | 'farm';
    description: string;
    image: string;
  }