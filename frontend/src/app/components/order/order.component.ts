import { Component, OnInit } from '@angular/core';
import { Product } from '../../models/product';
import { CartService } from '../../services/cart.service';
import { ProductService } from '../../services/product.service';
import { OrderService } from '../../services/order.service';
import { TokenService } from '../../services/token.service';
import { environment } from '../../../environments/environment';
import { OrderDTO } from '../../dtos/order/order.dto';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Order } from '../../models/order';
import { ToastService } from 'src/app/services/toast.service';
import { PaymentService } from 'src/app/services/payment.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiResponse } from 'src/app/responses/api.response';

@Component({
  selector: 'app-order',
  templateUrl: './order.component.html',
  styleUrls: ['./order.component.scss']
})
export class OrderComponent implements OnInit{
  orderForm: FormGroup; // Đối tượng FormGroup để quản lý dữ liệu của form
  cartItems: { product: Product, quantity: number }[] = [];
  couponCode: string = ''; // Mã giảm giá
  totalAmount: number = 0; // Tổng tiền
  cart: Map<number, number> = new Map();
  
  orderData: OrderDTO = {
    user_id: 0, // Thay bằng user_id thích hợp
    fullname: '', // Khởi tạo rỗng, sẽ được điền từ form
    email: '', // Khởi tạo rỗng, sẽ được điền từ form    
    phone_number: '', // Khởi tạo rỗng, sẽ được điền từ form
    address: '', // Khởi tạo rỗng, sẽ được điền từ form
    status: 'pending',
    note: '', // Có thể thêm trường ghi chú nếu cần
    total_money: 0, // Sẽ được tính toán dựa trên giỏ hàng và mã giảm giá
    payment_method: 'cod', // Mặc định là thanh toán khi nhận hàng (COD)
    shipping_method: 'express', // Mặc định là vận chuyển nhanh (Express)
    coupon_code: '', // Sẽ được điền từ form khi áp dụng mã giảm giá
    cart_items: []
  };

  constructor(
    private cartService: CartService,
    private productService: ProductService,
    private orderService: OrderService,
    private tokenService: TokenService,
    private formBuilder: FormBuilder,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private paymentService: PaymentService
  ) {
    // Tạo FormGroup và các FormControl tương ứng
    this.orderForm = this.formBuilder.group({
      fullname: ['', Validators.required], // fullname là FormControl bắt buộc      
      email: ['', [Validators.email]], // Sử dụng Validators.email cho kiểm tra định dạng email
      phone_number: ['', [Validators.required, Validators.minLength(6)]], // phone_number bắt buộc và ít nhất 6 ký tự
      address: ['', [Validators.required, Validators.minLength(5)]], // address bắt buộc và ít nhất 5 ký tự
      note: [''],
      shipping_method: ['express'],
      payment_method: ['cod']
    });
  }
  
  ngOnInit(): void {  
    debugger
    //this.cartService.clearCart();
    this.orderData.user_id = this.tokenService.getUserId();    
    // Lấy danh sách sản phẩm từ giỏ hàng
    debugger
    this.cart = this.cartService.getCart();
    const productIds = Array.from(this.cart.keys()); // Chuyển danh sách ID từ Map giỏ hàng    

    // Gọi service để lấy thông tin sản phẩm dựa trên danh sách ID
    debugger    
    if(productIds.length === 0) {
      return;
    }    
    this.productService.getProductsByIds(productIds).subscribe({
      next: (products) => {            
        debugger
        // Lấy thông tin sản phẩm và số lượng từ danh sách sản phẩm và giỏ hàng
        this.cartItems = productIds.map((productId) => {
          debugger
          const product = products.find((p) => p.id === productId);
          if (product) {
            product.thumbnail = `${environment.apiBaseUrl}/products/images/${product.thumbnail}`;
          }          
          return {
            product: product!,
            quantity: this.cart.get(productId)!
          };
        });
        console.log('haha');
      },
      complete: () => {
        debugger;
        this.calculateTotal()
      },
      error: (error: any) => {
        debugger;
        console.error('Error fetching detail:', error);
      }
    });        
  }
  placeOrder() {
    debugger
    if (this.orderForm.valid) {
      this.orderData = {
        ...this.orderData,
        ...this.orderForm.value
      };
      this.orderData.cart_items = this.cartItems.map(cartItem => ({
        product_id: cartItem.product.id,
        quantity: cartItem.quantity
      }));
      this.orderData.total_money =  this.totalAmount;
      // Dữ liệu hợp lệ, bạn có thể gửi đơn hàng đi
  
    // Kiểm tra: Nếu payment_method = 'vnpay' => Gọi createPaymentUrl, 
      // ngược lại => placeOrder
    if (this.orderData.payment_method === 'vnpay') {
      debugger
      const amount = this.orderData.total_money || 0;
    
      // Bước 1: Gọi API tạo link thanh toán
      this.paymentService.createPaymentUrl({ amount, language: 'vn' })
        .subscribe({
          next: (res: ApiResponse) => {
            // res.data là URL thanh toán, ví dụ:
            // https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=49800&...&vnp_TxnRef=18425732&...
            const paymentUrl = res.data;
            console.log('URL thanh toán:', paymentUrl);              
            // Bước 2: Tách vnp_TxnRef từ URL vừa trả về
            const vnp_TxnRef = new URL(paymentUrl).searchParams.get('vnp_TxnRef') || '';
    
            // Bước 3: Gọi placeOrder kèm theo vnp_TxnRef
            this.orderService.placeOrder({
              ...this.orderData,
              vnp_txn_ref: vnp_TxnRef
            }).subscribe({
              next: (placeOrderResponse: Order) => {
                // Bước 4: Nếu đặt hàng thành công, điều hướng sang trang thanh toán VNPAY
                debugger
                window.location.href = paymentUrl;
              },
              error: (err: HttpErrorResponse) => {
                debugger
                this.toastService.showToast({
                  error: err,
                  defaultMsg: 'Lỗi trong quá trình đặt hàng',
                  title: 'Lỗi Đặt Hàng'
                });
              }
            });
          },
          error: (err: HttpErrorResponse) => {
            this.toastService.showToast({
              error: err,
              defaultMsg: 'Lỗi kết nối đến cổng thanh toán',
              title: 'Lỗi Thanh Toán'
            });
          }
        });
    } else {
      debugger
      // Chọn COD => Gọi this.orderService.placeOrder
      this.orderService.placeOrder(this.orderData).subscribe({
        next: (response: Order) => {
          debugger
          console.log('Đặt hàng COD thành công!', response);
          // Xoá giỏ hàng, về trang chủ
          this.cartService.clearCart();
          this.router.navigate(['/']);
        },
        error: (err: HttpErrorResponse) => {
          debugger
          this.toastService.showToast({
            error: err,
            defaultMsg: 'Lỗi trong quá trình đặt hàng',
            title: 'Lỗi Đặt Hàng'
          });
        }
      });
    }

  } else {
    this.toastService.showToast({
      error: 'Vui lòng điền đầy đủ thông tin bắt buộc',
      defaultMsg: 'Vui lòng điền đầy đủ thông tin bắt buộc',
      title: 'Lỗi Dữ Liệu'
    });
    this.orderForm.markAllAsTouched();
  }     
  }
    
  decreaseQuantity(index: number): void {
    if (this.cartItems[index].quantity > 1) {
      this.cartItems[index].quantity--;
      // Cập nhật lại this.cart từ this.cartItems
      this.updateCartFromCartItems();
      this.calculateTotal();
    }
  }
  
  increaseQuantity(index: number): void {
    this.cartItems[index].quantity++;   
    // Cập nhật lại this.cart từ this.cartItems
    this.updateCartFromCartItems();
    this.calculateTotal();
  }    
  
  // Hàm tính tổng tiền
  calculateTotal(): void {
      this.totalAmount = this.cartItems.reduce(
          (total, item) => total + item.product.price * item.quantity,
          0
      );
  }
  confirmDelete(index: number): void {
    // Xóa sản phẩm khỏi danh sách cartItems
    this.cartItems.splice(index, 1);
    // Cập nhật lại this.cart từ this.cartItems
    this.updateCartFromCartItems();
    // Tính toán lại tổng tiền
    this.calculateTotal();
  }
  // Hàm xử lý việc áp dụng mã giảm giá
  applyCoupon(): void {
      // Viết mã xử lý áp dụng mã giảm giá ở đây
      // Cập nhật giá trị totalAmount dựa trên mã giảm giá nếu áp dụng
  }
  private updateCartFromCartItems(): void {
    this.cart.clear();
    this.cartItems.forEach((item) => {
      this.cart.set(item.product.id, item.quantity);
    });
    this.cartService.setCart(this.cart);
  }

}
