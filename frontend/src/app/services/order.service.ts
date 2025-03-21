import { ProductService } from './product.service';
import { Injectable } from '@angular/core';
import { 
  HttpClient, 
  HttpParams, 
  HttpHeaders 
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { OrderDTO } from '../dtos/order/order.dto';
import { OrderResponse } from '../responses/order/order.response';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private apiUrl = `${environment.apiBaseUrl}/orders`;
  private apiGetAllOrders = `${environment.apiBaseUrl}/orders`;

  constructor(private http: HttpClient) {}

  placeOrder(orderData: OrderDTO): Observable<any> {    
    // Gửi yêu cầu đặt hàng
    return this.http.post(this.apiUrl, orderData);
  }
  getOrderById(orderId: number): Observable<any> {
    const url = `${environment.apiBaseUrl}/orders/${orderId}`;
    return this.http.get(url);
  }
  getAllOrders(keyword:string,
    page: number, limit: number
  ): Observable<OrderResponse[]> {
      const params = new HttpParams()
      .set('keyword', keyword)      
      .set('page', (page).toString())
      .set('limit', limit.toString());            
      return this.http.get<any>(this.apiGetAllOrders, { params });
  }
  getOrdersByUser(keyword: string, status: string): Observable<OrderResponse[]> {
    const params = new HttpParams()
    .set('keyword', keyword)
    .set('status', status);   
    return this.http.get<OrderResponse[]>(`${environment.apiBaseUrl}/orders/user`, {params});
  }
  
  updateOrder(orderId: number, orderData: OrderDTO): Observable<any> {
    const url = `${environment.apiBaseUrl}/orders/${orderId}`;
    return this.http.put(url, orderData);
  }
  deleteOrder(orderId: number): Observable<any> {
    const url = `${environment.apiBaseUrl}/orders/${orderId}`;
    return this.http.delete(url, { responseType: 'text' });
  }
  updateOrderStatus(orderId: number, status: string): Observable<any> {
    const url = `${environment.apiBaseUrl}/orders/${orderId}/status`;
    const params = new HttpParams().set('status', status); // Thêm tham số status vào query params
    return this.http.put(url, null, { params }); // Gửi yêu cầu PUT với tham số status
  }
}
