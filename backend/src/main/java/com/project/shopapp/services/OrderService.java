package com.project.shopapp.services;

import com.project.shopapp.dtos.CartItemDTO;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.dtos.OrderWithDetailsDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        //tìm xem user'id có tồn tại ko
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id: "+orderDTO.getUserId()));
        //convert orderDTO => Order
        //dùng thư viện Model Mapper
        // Tạo một luồng bảng ánh xạ riêng để kiểm soát việc ánh xạ
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        // Cập nhật các trường của đơn hàng từ orderDTO
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUser(user);
        order.setOrderDate(LocalDate.now());//lấy thời điểm hiện tại
        order.setStatus(OrderStatus.PENDING);
        //Kiểm tra shipping date phải >= ngày hôm nay
        LocalDate shippingDate = orderDTO.getShippingDate() == null
                ? LocalDate.now().plusDays(3) : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today !");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);//đoạn này nên set sẵn trong sql
        order.setTotalMoney(orderDTO.getTotalMoney());
        orderRepository.save(order);
        // Tạo danh sách các đối tượng OrderDetail từ cartItems
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            // Tạo một đối tượng OrderDetail từ CartItemDTO
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            // Lấy thông tin sản phẩm từ cartItemDTO
            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();

            // Tìm thông tin sản phẩm từ cơ sở dữ liệu (hoặc sử dụng cache nếu cần)
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + productId));

            // Đặt thông tin cho OrderDetail
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProducts(quantity);
            // Các trường khác của OrderDetail nếu cần
            orderDetail.setPrice(product.getPrice());

            // Thêm OrderDetail vào danh sách
            orderDetails.add(orderDetail);
        }


        // Lưu danh sách OrderDetail vào cơ sở dữ liệu
        orderDetailRepository.saveAll(orderDetails);
        return order;
    }
    @Transactional
    public Order updateOrderWithDetails(OrderWithDetailsDTO orderWithDetailsDTO) {
        modelMapper.typeMap(OrderWithDetailsDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderWithDetailsDTO, order);
        Order savedOrder = orderRepository.save(order);

        // Set the order for each order detail
        for (OrderDetailDTO orderDetailDTO : orderWithDetailsDTO.getOrderDetailDTOS()) {
            //orderDetail.setOrder(OrderDetail);
        }

        // Save or update the order details
        List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(order.getOrderDetails());

        // Set the updated order details for the order
        savedOrder.setOrderDetails(savedOrderDetails);

        return savedOrder;
    }
    @Override
    public Order getOrderById(Long orderId) {
        // Tìm theo ID
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            // Nếu không tìm thấy theo ID, tìm theo vnpTxnRef
            order = orderRepository.findByVnpTxnRef(orderId.toString()).orElse(null);
        }
        return order;
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO)
            throws DataNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(() ->
                new DataNotFoundException("Cannot find order with id: " + id));
        User existingUser = userRepository.findById(
                orderDTO.getUserId()).orElseThrow(() ->
                new DataNotFoundException("Cannot find user with id: " + id));
        // Tạo một luồng bảng ánh xạ riêng để kiểm soát việc ánh xạ
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        // Cập nhật các trường của đơn hàng từ orderDTO
        modelMapper.map(orderDTO, order);
        order.setUser(existingUser);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        //no hard-delete, => please soft-delete
        if(order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }
    @Override
    public List<OrderResponse> findOrders(Long userId, String status, String keyword) {
        List<Order> orders = orderRepository.findOrders(userId, status, keyword);
        return orders.stream().map(OrderResponse::fromOrder).toList();
    }

    @Override
    public Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable) {
        Page<Order> orderPages;
        orderPages = orderRepository.findAll(keyword, pageable);
        return orderPages.map(OrderResponse::fromOrder);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, String status) throws DataNotFoundException, IllegalArgumentException {
        // Tìm đơn hàng theo ID
        Order order = getOrderById(id); // Sẽ tìm theo ID trước, sau đó tìm theo vnpTxnRef

        // Kiểm tra trạng thái hợp lệ
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        // Kiểm tra xem trạng thái có nằm trong danh sách hợp lệ không
        if (!OrderStatus.VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        // Kiểm tra logic chuyển đổi trạng thái
        String currentStatus = order.getStatus();
        if (currentStatus.equals(OrderStatus.DELIVERED) && !status.equals(OrderStatus.CANCELLED)) {
            throw new IllegalArgumentException("Cannot change status from DELIVERED to " + status);
        }

        if (currentStatus.equals(OrderStatus.CANCELLED)) {
            throw new IllegalArgumentException("Cannot change status of a CANCELLED order");
        }

        if (status.equals(OrderStatus.CANCELLED) && !currentStatus.equals(OrderStatus.PENDING)) {
                throw new IllegalArgumentException("Order can only be cancelled from PENDING status");
            }

        // Cập nhật trạng thái đơn hàng
        order.setStatus(status);

        // Lưu đơn hàng đã cập nhật
        return orderRepository.save(order);
    }


}
