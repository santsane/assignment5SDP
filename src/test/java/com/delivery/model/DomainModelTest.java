package com.delivery.model;

import com.delivery.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainModelTest {

    @Test
    void effectiveTimeAddsTrafficAndWeatherPenalty() {
        // 48 min x 1.15 (medium traffic) = 55 -> +7 min, plus 5 min rain = 60
        Route route = TestData.full("X", 36, 48, 2400, 4.1, TrafficLevel.MEDIUM, 5, 150, true);

        assertEquals(7, route.trafficPenaltyMinutes());
        assertEquals(60, route.effectiveMinutes());
    }

    @Test
    void lightTrafficAddsNoPenalty() {
        Route route = TestData.route("X", 10, 30, 1000, 1.0);

        assertEquals(0, route.trafficPenaltyMinutes());
        assertEquals(30, route.effectiveMinutes());
    }

    @Test
    void totalCostFollowsTheFormula() {
        // R1: base 4200 + 28 km x 20 + urgent surcharge 1500 + fees 600 = 6860
        Route r1 = TestData.routes().get(0);

        assertEquals(6860.0, r1.totalCost(DeliveryPriority.URGENT), 0.001);
        assertEquals(5360.0, r1.totalCost(DeliveryPriority.STANDARD), 0.001);
    }

    @Test
    void negativeDistanceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TestData.route("BAD", -5, 20, 1000, 1.0));
    }

    @Test
    void negativeCostIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TestData.route("BAD", 5, 20, -1000, 1.0));
    }

    @Test
    void routeWithoutIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TestData.route(" ", 5, 20, 1000, 1.0));
    }

    @Test
    void requestWithoutPriorityIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryRequest("1", null, 1000, 60, false));
    }

    @Test
    void requestNeedsPositiveBudgetAndDeadline() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryRequest("1", DeliveryPriority.STANDARD, 0, 60, false));
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryRequest("1", DeliveryPriority.STANDARD, 1000, 0, false));
    }

    @Test
    void withersReturnModifiedCopies() {
        DeliveryRequest original = TestData.standard();
        DeliveryRequest changed = original.withProhibitedRoutes("R2").withMaxCarbonKg(3.0);

        assertTrue(original.prohibitedRouteIds().isEmpty());
        assertTrue(changed.prohibitedRouteIds().contains("R2"));
        assertEquals(3.0, changed.maxCarbonKg(), 0.0001);
    }

    @Test
    void locationWithImpossibleLatitudeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Location("Nowhere", 120, 10));
    }
}
