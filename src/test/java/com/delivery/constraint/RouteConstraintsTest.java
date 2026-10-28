package com.delivery.constraint;

import com.delivery.TestData;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.TrafficLevel;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteConstraintsTest {

    @Test
    void unavailableRouteIsRejected() {
        Route closed = TestData.full("C", 10, 20, 1000, 1.0, TrafficLevel.LOW, 0, 0, false);

        Optional<String> problem = RouteConstraints.AVAILABLE.check(closed, TestData.standard());

        assertTrue(problem.isPresent());
        assertTrue(problem.get().contains("unavailable"));
    }

    @Test
    void budgetBoundaryIsAllowedButOneUnitMoreIsNot() {
        // total = 1000 + 10 km x 20 = 1200
        Route route = TestData.route("A", 10, 20, 1000, 1.0);
        DeliveryRequest exact = TestData.request(DeliveryPriority.STANDARD, 1200, 90);
        DeliveryRequest tooLow = TestData.request(DeliveryPriority.STANDARD, 1199, 90);

        assertTrue(RouteConstraints.WITHIN_BUDGET.check(route, exact).isEmpty());
        assertTrue(RouteConstraints.WITHIN_BUDGET.check(route, tooLow).isPresent());
    }

    @Test
    void urgentSurchargeCountsForTheBudget() {
        // standard total 1200, urgent total 2700
        Route route = TestData.route("A", 10, 20, 1000, 1.0);

        assertTrue(RouteConstraints.WITHIN_BUDGET
                .check(route, TestData.request(DeliveryPriority.STANDARD, 2000, 90)).isEmpty());
        assertTrue(RouteConstraints.WITHIN_BUDGET
                .check(route, TestData.request(DeliveryPriority.URGENT, 2000, 90)).isPresent());
    }

    @Test
    void deadlineBoundaryIsAllowedButOneMinuteMoreIsNot() {
        Route route = TestData.route("A", 10, 30, 1000, 1.0);

        assertTrue(RouteConstraints.BEFORE_DEADLINE
                .check(route, TestData.request(DeliveryPriority.STANDARD, 5000, 30)).isEmpty());
        assertTrue(RouteConstraints.BEFORE_DEADLINE
                .check(route, TestData.request(DeliveryPriority.STANDARD, 5000, 29)).isPresent());
    }

    @Test
    void deadlineUsesTrafficAdjustedTime() {
        // 20 min estimated but heavy traffic makes it 27 min
        Route jammed = TestData.full("J", 10, 20, 1000, 1.0, TrafficLevel.HIGH, 0, 0, true);

        assertTrue(RouteConstraints.BEFORE_DEADLINE
                .check(jammed, TestData.request(DeliveryPriority.STANDARD, 5000, 25)).isPresent());
    }

    @Test
    void prohibitedRouteIsRejected() {
        Route route = TestData.route("R2", 10, 20, 1000, 1.0);
        DeliveryRequest request = TestData.standard().withProhibitedRoutes("R2");

        assertTrue(RouteConstraints.NOT_PROHIBITED.check(route, request).isPresent());
    }

    @Test
    void emissionLimitIsChecked() {
        Route route = TestData.route("A", 10, 20, 1000, 2.5);

        assertTrue(RouteConstraints.WITHIN_EMISSION_LIMIT
                .check(route, TestData.standard().withMaxCarbonKg(2.5)).isEmpty());
        assertTrue(RouteConstraints.WITHIN_EMISSION_LIMIT
                .check(route, TestData.standard().withMaxCarbonKg(2.4)).isPresent());
    }

    @Test
    void defaultSetContainsAllFiveConstraints() {
        assertEquals(5, RouteConstraints.defaults().size());
    }
}
