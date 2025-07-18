import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Location } from '@angular/common';
import { environment } from '../../../../environments/environment';
import { Product } from '../../../models/product';
import { ProductService } from '../../../services/product.service';
import { ApiResponse } from 'src/app/responses/api.response';

@Component({
  selector: 'app-product-admin',
  templateUrl: './product.admin.component.html',
  styleUrls: [
    './product.admin.component.scss',
  ]
})
export class ProductAdminComponent implements OnInit {
  products: Product[] = [];
  selectedCategoryId: number = 0; // Giá trị category được chọn
  currentPage: number = 0;
  itemsPerPage: number = 12;
  pages: number[] = [];
  totalPages: number = 0;
  visiblePages: number[] = [];
  keyword: string = "";
  constructor(
    private productService: ProductService,
    private router: Router,
    private location: Location
  ) {

  }
  ngOnInit() {
    this.currentPage = 0;
    //this.currentPage = Number(localStorage.getItem('currentProductAdminPage')) || 0; 
    this.keyword = localStorage.getItem('currentKeyword') || "";
    this.getProducts(this.keyword,
      this.selectedCategoryId,
      this.currentPage, this.itemsPerPage);
  }
  searchProducts() {
    this.currentPage = 0;
    this.itemsPerPage = 12;
    //Mediocre Iron Wallet
    localStorage.setItem('currentKeyword', this.keyword.trim());
    debugger
    this.getProducts(this.keyword.trim(), this.selectedCategoryId, this.currentPage, this.itemsPerPage);
  }
  getProducts(keyword: string, selectedCategoryId: number, page: number, limit: number) {
    debugger
    this.productService.getProducts(keyword, selectedCategoryId, page, limit).subscribe({
      next: (apiResponse: ApiResponse) => {
        debugger
        console.log(page);
        const response = apiResponse.data;
        response.products.forEach((product: Product) => {
          if (product) {
            product.thumbnail = `${environment.apiBaseUrl}/products/images/${product.thumbnail}`;
          }
        });
        this.products = response.products;
        this.totalPages = response.totalPages;
        this.visiblePages = this.generateVisiblePageArray(this.currentPage, this.totalPages);
      },
      complete: () => {
        debugger;
      },
      error: (error: any) => {
        debugger;
        console.error('Error fetching products:', error);
      }
    });
  }
  onPageChange(page: number) {
    debugger;
    this.currentPage = page < 0 ? 0 : page;
    //localStorage.setItem('currentProductAdminPage', String(this.currentPage));     
    this.getProducts(this.keyword, this.selectedCategoryId, this.currentPage, this.itemsPerPage);
  }

  generateVisiblePageArray(currentPage: number, totalPages: number): number[] {
    const maxVisiblePages = 5;
    const halfVisiblePages = Math.floor(maxVisiblePages / 2);

    let startPage = Math.max(currentPage - halfVisiblePages, 1);
    let endPage = Math.min(startPage + maxVisiblePages - 1, totalPages);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(endPage - maxVisiblePages + 1, 1);
    }

    return new Array(endPage - startPage + 1).fill(0)
      .map((_, index) => startPage + index);
  }

  // Hàm xử lý sự kiện khi thêm mới sản phẩm
  insertProduct() {
    debugger
    // Điều hướng đến trang detail-product với productId là tham số
    this.router.navigate(['/admin/products/insert']);
  }

  // Hàm xử lý sự kiện khi sản phẩm được bấm vào
  updateProduct(productId: number) {
    debugger
    // Điều hướng đến trang detail-product với productId là tham số
    this.router.navigate(['/admin/products/update', productId]);
  }
  deleteProduct(product: Product) {
    const confirmation = window
      .confirm('Are you sure you want to delete this product?');
    if (confirmation) {
      debugger
      this.productService.deleteProduct(product.id).subscribe({
        next: (response: any) => {
          debugger
          alert('Xóa thành công')
          location.reload();
        },
        complete: () => {
          debugger;
        },
        error: (error: any) => {
          debugger;
          alert(error.error)
          console.error('Error fetching products:', error);
        }
      });
    }
  }
}