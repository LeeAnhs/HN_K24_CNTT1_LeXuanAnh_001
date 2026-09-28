package org.example.orderservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.models.constants.OrderStatus;
import org.example.orderservice.models.dto.requests.CreateOrderDetailRequest;
import org.example.orderservice.models.dto.requests.CreateOrderRequest;
import org.example.orderservice.models.dto.responses.OrderDetailResponse;
import org.example.orderservice.models.dto.responses.OrderResponse;
import org.example.orderservice.models.dto.responses.ProductResponse;
import org.example.orderservice.models.entities.Order;
import org.example.orderservice.models.entities.OrderDetail;
import org.example.orderservice.models.repositories.OrderDetailRepository;
import org.example.orderservice.models.repositories.OrderRepository;
import org.example.orderservice.models.services.OrderService;
import org.example.orderservice.producer.OrderProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

        private final OrderRepository orderRepository;
        private final OrderDetailRepository orderDetailRepository;
        private final ProductGatewayService productGatewayService;
        private final OrderProducer orderProducer;

        @Override
        @Transactional
        public OrderResponse createOrder(CreateOrderRequest request) {
                List<OrderDetailResponse> detailResponses = new ArrayList<>();
                double totalAmount = 0;
                for (CreateOrderDetailRequest item : request.items()) {
                        ProductResponse product = productGatewayService.getProductById(item.productId());
                        double lineTotal = product.price() * item.quantity();
                        totalAmount += lineTotal;

                        OrderDetailResponse detailResponse = new OrderDetailResponse(
                                null,
                                product.id(),
                                product.name(),
                                item.quantity(),
                                product.price(),
                                lineTotal
                        );
                        detailResponses.add(detailResponse);
                }

                Order order = Order.builder()
                        .customerName(request.customerName())
                        .total(totalAmount)
                        .status(OrderStatus.PENDING)
                        .build();
                order = orderRepository.save(order);
                for (int i = 0; i < request.items().size(); i++) {
                        CreateOrderDetailRequest item = request.items().get(i);
                        OrderDetailResponse detailResponse = detailResponses.get(i);
                        OrderDetail orderDetail = OrderDetail.builder()
                                .order(order)
                                .productId(detailResponse.productId())
                                .quantity(detailResponse.quantity())
                                .unitPrice(detailResponse.unitPrice())
                                .build();
                        orderDetail = orderDetailRepository.save(orderDetail);
                        detailResponses.set(i, new OrderDetailResponse(
                                orderDetail.getId(),
                                detailResponse.productId(),
                                detailResponse.productName(),
                                detailResponse.quantity(),
                                detailResponse.unitPrice(),
                                detailResponse.subtotal()
                        ));
                }

                orderProducer.sendOrderCreatedEvent(request.customerEmail());
                return new OrderResponse(
                        order.getId(),
                        order.getCustomerName(),
                        order.getTotal(),
                        order.getStatus(),
                        detailResponses
                );
        }
}
