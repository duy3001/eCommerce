import { Product } from "./product";
import {Order} from './order'
export interface OrderDetail {
    id: number;
    order_id: number; // ✅ Chỉ lưu ID thay vì object
    product_id: number;
    product_name: string;
    thumbnail: string;
    price: number;
    number_of_products: number;
    total_money: number;
    color?: string;
}