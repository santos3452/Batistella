import { Routes } from '@angular/router';
import { LoginComponent } from './Components/login/login.component';
import { HomeComponent } from './Components/home/home.component';
import { RegisterComponent } from './Components/register/register.component';
import { ProfileComponent } from './Components/profile/profile.component';
import { ResetPasswordComponent } from './Components/reset-password/reset-password.component';
import { AdminProductsComponent } from './Components/admin/admin-products/admin-products.component';
import { ProductFormComponent } from './Components/admin/product-form/product-form.component';
import { SearchResultsComponent } from './Components/search-results/search-results.component';
import { ProductDetailComponent } from './Components/product-detail/product-detail.component';
import { MisPedidosComponent } from './Components/mis-pedidos/mis-pedidos.component';
import { AuthGuard } from './Services/Auth/auth.guard';
import { AdminGuard } from './Services/Auth/admin.guard';
import { OrderSummaryComponent } from './Pages/checkout/order-summary/order-summary.component';
import { PaymentMethodComponent } from './Pages/checkout/payment-method/payment-method.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'search', component: SearchResultsComponent },
  { path: 'product/:id', component: ProductDetailComponent },
  { path: 'mis-pedidos', component: MisPedidosComponent, canActivate: [AuthGuard] },
  { 
    path: 'admin/products', 
    component: AdminProductsComponent, 
    canActivate: [AuthGuard, AdminGuard] 
  },
  { 
    path: 'admin/products/new', 
    component: ProductFormComponent, 
    canActivate: [AuthGuard, AdminGuard] 
  },
  { 
    path: 'admin/products/edit/:id', 
    component: ProductFormComponent, 
    canActivate: [AuthGuard, AdminGuard] 
  },
  { 
    path: 'checkout/summary', 
    component: OrderSummaryComponent, 
    canActivate: [AuthGuard] 
  },
  { 
    path: 'checkout/payment', 
    component: PaymentMethodComponent, 
    canActivate: [AuthGuard] 
  },
  { path: '**', redirectTo: '' }
];
