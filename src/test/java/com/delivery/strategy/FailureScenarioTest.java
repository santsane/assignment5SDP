package com.delivery.strategy;

import com.delivery.TestData;
import com.delivery.exception.NoSuitableRouteException;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.Route;
import com.delivery.model.TrafficLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Edge and failure cases. They use FastestRouteStrategy, but the checks live in
 * AbstractRouteStrategy so every strategy behaves the same way.
 */
class FailureScenarioTest {

    private final RouteStrategy strategy = new FastestRouteStrategy();

    @Test
    void emptyRouteListThrowsNoSuitableRoute() {
        assertThrows(NoSuitableRouteException.class,
                () -> strategy.decide(List.of(), TestData.standard()));
    }

    @Test
    void nullRouteListThrowsNoSuitableRoute() {
        assertThrows(NoSuitableRouteException.class,
                () -> strategy.decide(null, TestData.standard()));
    }

    @Test
    void missingRequestIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.decide(TestData.routes(), null));
    }

    @Test
    void impossibleBudgetThrowsAndExplainsEveryRejectedRoute() {
        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> strategy.decide(TestData.routes(), TestData.request(DeliveryPriority.STANDARD, 100, 90)));

        assertEquals(8, e.getRejectedRoutes().size());
        assertTrue(e.getRejectedRoutes().get("R1").contains("Budget exceeded"));
    }

    @Test
    void impossibleDeadlineThrowsNoSuitableRoute() {
        // the fastest open route needs 27 min
        assertThrows(NoSuitableRouteException.class,
                () -> strategy.decide(TestData.routes(), TestData.request(DeliveryPriority.STANDARD, 10000, 26)));
    }

    @Test
    void onlyUnavailableRoutesThrowsNoSuitableRoute() {
        Route closed = TestData.full("C", 10, 20, 1000, 1.0, TrafficLevel.LOW, 0, 0, false);

        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> strategy.decide(List.of(closed), TestData.standard()));

        assertTrue(e.getRejectedRoutes().get("C").contains("unavailable"));
    }

    @Test
    void routeBreakingSeveralConstraintsReportsAllOfThem() {
        Route bad = TestData.full("BAD", 10, 200, 99999, 1.0, TrafficLevel.LOW, 0, 0, false);

        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> strategy.decide(List.of(bad), TestData.standard()));

        String reason = e.getRejectedRoutes().get("BAD");
        assertTrue(reason.contains("unavailable"));
        assertTrue(reason.contains("Budget exceeded"));
        assertTrue(reason.contains("deadline"));
    }
}
