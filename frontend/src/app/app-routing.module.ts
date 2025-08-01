import { NgModule, importProvidersFrom } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { 
  DetailProductComponent 
} from './components/detail-product/detail-product.component';
import { OrderComponent } from './components/order/order.component';
import { OrderUserComponent } from './components/order_user/order.user.component';
import { UserProfileComponent } from './components/user-profile/user.profile.component';
import { AdminComponent } from './components/admin/admin.component';
import { AuthGuardFn } from './guards/auth.guard';
import { AdminGuardFn } from './guards/admin.guard';
import { OrderDetailComponent } from './components/detail-order/order.detail.component';
import { PaymentCallbackComponent } from './payment-callback/payment-callback.component';
import { ChangePasswordComponent } from './components/change_password/change.password.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },  
  { path: 'register', component: RegisterComponent },
  { path: 'products/:id', component: DetailProductComponent },  
  { path: 'orders', component: OrderComponent,canActivate:[AuthGuardFn] },
  { path: 'user-profile', component: UserProfileComponent, canActivate:[AuthGuardFn] },
  { path: 'orders/user', component: OrderUserComponent },
  { path: 'orders/user/:orderId', component: OrderDetailComponent, canActivate: [AuthGuardFn] },
  { path: 'payments/payment-callback', component: PaymentCallbackComponent },
  { path: 'change-password', component: ChangePasswordComponent, canActivate:[AuthGuardFn] },
  //Admin   
  { 
    path: 'admin', 
    canMatch: [AdminGuardFn],
    children: [
      { path: '', component: AdminComponent },

      // Thêm các admin routes khác ở đây
    ] 
  },      
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes),
    CommonModule
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
