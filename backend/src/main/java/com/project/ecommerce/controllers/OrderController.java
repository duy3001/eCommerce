package com.project.ecommerce.controllers;

import com.project.ecommerce.components.SecurityUtils;
import com.project.ecommerce.dtos.*;
import com.project.ecommerce.models.Order;
import com.project.ecommerce.models.User;
import com.project.ecommerce.responses.*;
import com.project.ecommerce.services.IOrderService;
import com.project.ecommerce.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final SecurityUtils securityUtils;
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result
    ) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            Order orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/user") // Thêm biến đường dẫn "user_id"
    //GET http://localhost:8088/api/v1/orders/user?status=&keyword=
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        try {
            User loggedUser = securityUtils.getLoggedInUser();
            Long userId = loggedUser.getId();
            List<OrderResponse> orders = orderService.findOrders(userId, status, keyword);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //GET http://localhost:8088/api/v1/orders/2
    @GetMapping("/{id}")
    @PostAuthorize("hasRole('ROLE_ADMIN') or returnObject.body.userId == authentication.principal.id")
    public ResponseEntity<?> getOrder(@Valid @PathVariable("id") Long orderId) {
        try {
//            User loggedUser = securityUtils.getLoggedInUser();
//            Long userId = loggedUser.getId();
            Order existingOrder = orderService.getOrderById(orderId);
            OrderResponse orderResponse = OrderResponse.fromOrder(existingOrder);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    //PUT http://localhost:8088/api/v1/orders/2
    //công việc của admin
    public ResponseEntity<?> updateOrder(
            @Valid @PathVariable long id,
            @Valid @RequestBody OrderDTO orderDTO) {

        try {
            Order order = orderService.updateOrder(id, orderDTO);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@Valid @PathVariable Long id) {
        //xóa mềm => cập nhật trường active = false
        orderService.deleteOrder(id);
        String result = MessageKeys.DELETE_ORDER_SUCCESSFULLY;
        return ResponseEntity.ok().body(result);
    }
    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OrderListResponse> getOrdersByKeyword(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                //Sort.by("createdAt").descending()
                Sort.by("id").ascending()
        );

        Page<OrderResponse> orderPage = orderService
                                        .getOrdersByKeyword(keyword, pageRequest);

        // Lấy tổng số trang
        int totalPages = orderPage.getTotalPages();
        List<OrderResponse> orderResponses = orderPage.getContent();
        return ResponseEntity.ok(OrderListResponse.builder()
                .totalPages(totalPages)
                .orders(orderResponses)
                .build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateOrderStatus(
            @Valid @PathVariable Long id,
            @RequestParam String status) throws Exception {
        // Gọi service để cập nhật trạng thái
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        // Trả về phản hồi thành công
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Order status updated successfully")
                .status(HttpStatus.OK)
                .data(OrderResponse.fromOrder(updatedOrder))
                .build());
    }
}
