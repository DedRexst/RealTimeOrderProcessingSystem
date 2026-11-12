package com.project.orderservice.service.order.impl;

import com.project.orderservice.dto.ExternalStatusUpdateDto;
import com.project.orderservice.dto.UpdateOrderStatusDto;
import com.project.orderservice.enity.Customer;
import com.project.orderservice.enity.Order;
import com.project.orderservice.enity.Product;
import com.project.orderservice.enity.Status;
import com.project.orderservice.service.order.mapper.OrderServiceMapper;
import com.project.orderservice.reporsitory.CustomerRepository;
import com.project.orderservice.reporsitory.OrderRepository;
import com.project.orderservice.reporsitory.ProductRepository;
import com.project.orderservice.service.order.OrderService;
import com.project.orderservice.service.outbox.OutboxService;
import com.project.orderservice.service.OrderStatusChange;
import com.project.orderservice.service.OrderStatusUpdateResult;
import com.project.orderservice.service.StatusChangeDetails;
import com.project.orderservice.util.InvalidStatusTransitionException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  /**
   * The order lifecycle, as the only authority on what an operator may ask for. Anything absent is
   * refused with a 409 rather than quietly applied, which is what stops a FAILED order from being
   * revived or a never-reserved one from being confirmed.
   *
   * <p>SUCCESSFUL and FAILED are terminal on purpose: an order that has run its course is corrected
   * by a new order, not by editing history. RELEASED can still be closed off as FAILED, because
   * releasing stock is what usually precedes cancelling the order it was held for.
   */
  private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = Map.of(
      Status.PENDING, Set.of(Status.RESERVED, Status.FAILED),
      Status.RESERVED, Set.of(Status.SUCCESSFUL, Status.RELEASED, Status.FAILED),
      Status.RELEASED, Set.of(Status.FAILED),
      Status.SUCCESSFUL, Set.of(),
      Status.FAILED, Set.of());

  /** Attributed to changes the saga makes, to keep them apart from an operator's in the audit. */
  private static final String INVENTORY_SERVICE = "inventory-service";

  /** Fallback attribution when a caller of the status endpoint does not identify itself. */
  private static final String ADMIN_API = "admin-api";

  private final OutboxService outboxService;
  private final OrderServiceMapper orderServiceMapper;
  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;

  @Override
  @Transactional("transactionManager")
  public Order createOrder(Order order, Long customerId, List<Long> productIds) {
    Customer customer = customerRepository.findById(customerId)
        .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));
    List<Product> products = loadRequestedProducts(productIds);

    order = orderServiceMapper.setCustomerAndProducts(order, customer, products);
    order = orderRepository.save(order);

    outboxService.enqueueOrderCreated(orderServiceMapper.toOrderCreatedEvent(order));
    outboxService.enqueueOrderNotification(orderServiceMapper.entityToNotificationEvent(order));

    return order;
  }

  @Override
  @Transactional(value = "transactionManager", readOnly = true)
  public Order getOrder(Long orderId) {

    return orderRepository.findByIdWithProducts(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
  }

  
  
  
  @Override
  @Transactional(value = "transactionManager", readOnly = true)
  public Map<Long, Order> loadWithDetailsById(Page<Order> page) {
    return orderRepository
        .findAllByIdWithProducts(page.getContent().stream().map(Order::getId).toList())
        .stream()
        .collect(Collectors.toMap(Order::getId, Function.identity(), (first, duplicate) -> first));
  }

  @Override
  @Transactional(value = "transactionManager", readOnly = true)
  public Page<Order> findOrders(Status status, Long customerId, Pageable pageable) {
    return orderRepository.findAll(filterBy(status, customerId), pageable);
  }

  @Override
  @Transactional("transactionManager")
  public Optional<OrderStatusChange> markInventoryReserved(Long orderId) {
    Order order = getOrder(orderId);

    Status current = order.getStatus();
    if (current == Status.RESERVED) {
      log.info("Order {} is already RESERVED, skipping duplicate event", orderId);
      return Optional.empty();
    }
    if (current != null && current != Status.PENDING) {
      log.warn("Refusing to move order {} from {} to RESERVED", orderId, current);
      return Optional.empty();
    }

    return Optional.of(changeStatus(order, Status.RESERVED, StatusChangeDetails.internal(
        INVENTORY_SERVICE, "InventoryService reserved the requested units")));
  }

  @Override
  @Transactional("transactionManager")
  public OrderStatusUpdateResult updateOrderStatus(Long orderId, UpdateOrderStatusDto req) {
    return requestStatus(orderId, req.status(),
        StatusChangeDetails.internal(attributionFor(req.changedBy()), req.reason()));
  }

  @Override
  @Transactional("transactionManager")
  public OrderStatusUpdateResult applyExternalStatusUpdate(
      Long orderId, ExternalStatusUpdateDto req, String reportedBy) {

    log.info("System {} reports order {} as {} against its record {}",
        reportedBy, orderId, req.status(), req.externalReference());

    return requestStatus(orderId, req.status(), new StatusChangeDetails(
        attributionFor(reportedBy), req.reason(), req.externalReference(), req.occurredAt()));
  }

  /**
   * One requested transition, wherever the request came from. Who may ask for what is settled
   * before this point — by SecurityConfig, and by the facade that names the caller — so an
   * operator's request and a third-party system's report are held to exactly the same lifecycle
   * here, and neither can reach a status the other could not.
   *
   * <p>The order is loaded here, inside the caller's write transaction, on purpose. One loaded
   * earlier in a read-only transaction is read-only to Hibernate, so changing its status would
   * never be flushed — while the email and webhook announcing that status still went out.
   */
  private OrderStatusUpdateResult requestStatus(
      Long orderId, Status requested, StatusChangeDetails details) {
    Order order = orderRepository.findByIdWithProducts(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

    // A legacy null reads as PENDING, the status such a row would have been written with today.
    Status current = Objects.requireNonNullElse(order.getStatus(), Status.PENDING);
    if (current == requested) {
      log.info("Order {} is already {}, leaving it untouched", orderId, requested);
      return OrderStatusUpdateResult.unchanged(order);
    }
    if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(requested)) {
      throw new InvalidStatusTransitionException(orderId, current, requested);
    }

    return OrderStatusUpdateResult.changed(order, changeStatus(order, requested, details));
  }

  /**
   * The single place an order's status is mutated after creation. Every transition returns both
   * payloads describing it, so a status added later reaches the customer and analytics as soon as
   * it routes through here rather than needing its own wiring.
   */
  private OrderStatusChange changeStatus(
      Order order, Status newStatus, StatusChangeDetails details) {
    Status previous = order.getStatus();
    order.setStatus(newStatus);
    log.info("Order {} moved from {} to {} by {}",
        order.getId(), previous, newStatus, details.changedBy());

    return new OrderStatusChange(
        orderServiceMapper.entityToNotificationEvent(order, previous, details),
        orderServiceMapper.entityToStatusChangedEvent(order, previous, details));
  }

  /** A caller that leaves changedBy out is still attributed, so no transition is left anonymous. */
  private String attributionFor(String changedBy) {
    return changedBy == null || changedBy.isBlank() ? ADMIN_API : changedBy;
  }


  private List<Product> loadRequestedProducts(List<Long> productIds) {
    Map<Long, Product> productsById = productRepository.findAllById(Set.copyOf(productIds)).stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    return productIds.stream()
        .map(id -> Optional.ofNullable(productsById.get(id))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id)))
        .toList();
  }

  /** An empty {@code and()} is a true predicate, so absent filters widen to "every order". */
  private Specification<Order> filterBy(Status status, Long customerId) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>(2);
      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      if (customerId != null) {
        predicates.add(builder.equal(root.get("customer").get("id"), customerId));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
