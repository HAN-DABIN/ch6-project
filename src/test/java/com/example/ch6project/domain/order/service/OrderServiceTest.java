package com.example.ch6project.domain.order.service;

import com.example.ch6project.common.exception.CustomException;
import com.example.ch6project.domain.menu.entity.Menu;
import com.example.ch6project.domain.menu.entity.MenuStatus;
import com.example.ch6project.domain.menu.repository.MenuRepository;
import com.example.ch6project.domain.order.dto.OrderRequest;
import com.example.ch6project.domain.order.repository.OrderRepository;
import com.example.ch6project.domain.payment.event.PaymentCompletedProducer;
import com.example.ch6project.domain.payment.repository.PaymentRepository;
import com.example.ch6project.domain.point.entity.Point;
import com.example.ch6project.domain.point.repository.PointRepository;
import com.example.ch6project.domain.user.entity.User;
import com.example.ch6project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentCompletedProducer paymentCompletedProducer;

    @Test
    void 동시에_같은_사용자가_주문하면_포인트는_중복_차감되지_않는다() throws InterruptedException {
        // given
        User user = userRepository.saveAndFlush(new User("test123", "password", "테스트"));
        Menu menu = menuRepository.saveAndFlush(new Menu ("아메리카노", 5000L, MenuStatus.ACTIVE));

        Point point = Point.create(user);
        point.charge(5000L);
        pointRepository.saveAndFlush(point);

        Long userId = user.getId();
        Long menuId = menu.getId();

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        List<Throwable> unexpectedExceptions = new CopyOnWriteArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.order(userId, new OrderRequest(menuId));
                    successCount.incrementAndGet();
                } catch (CustomException e) {
                    failCount.incrementAndGet();
                } catch (Throwable e) {
                    unexpectedExceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Point savedPoint = pointRepository.findByUserId(userId)
                .orElseThrow();

        String failureContext = buildFailureContext(
                successCount.get(),
                failCount.get(),
                unexpectedExceptions,
                savedPoint
        );

        assertThat(unexpectedExceptions)
                .as(failureContext)
                .isEmpty();
        assertThat(successCount.get())
                .as(failureContext)
                .isEqualTo(1);
        assertThat(failCount.get())
                .as(failureContext)
                .isEqualTo(1);
        assertThat(savedPoint.getBalance())
                .as(failureContext)
                .isEqualTo(0L);
        assertThat(orderRepository.count())
                .as(failureContext)
                .isEqualTo(1);
        assertThat(paymentRepository.count())
                .as(failureContext)
                .isEqualTo(1);
    }

    private String buildFailureContext(
            int successCount,
            int failCount,
            List<Throwable> unexpectedExceptions,
            Point savedPoint
    ) {
        return """
                successCount=%d, failCount=%d, unexpectedExceptionCount=%d, balance=%d, orderCount=%d, paymentCount=%d, unexpectedExceptions=%s
                """.formatted(
                successCount,
                failCount,
                unexpectedExceptions.size(),
                savedPoint.getBalance(),
                orderRepository.count(),
                paymentRepository.count(),
                formatUnexpectedExceptions(unexpectedExceptions)
        );
    }

    private String formatUnexpectedExceptions(List<Throwable> unexpectedExceptions) {
        if (unexpectedExceptions.isEmpty()) {
            return "[]";
        }

        return unexpectedExceptions.stream()
                .map(e -> e.getClass().getName() + ": " + e.getMessage())
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
