package com.delivery.strategy;

import com.delivery.TestData;
import com.delivery.app.StrategyConfig;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;
import com.delivery.model.TrafficLevel;
import com.delivery.planner.ComparisonReport;
import com.delivery.planner.RoutePlanner;
import com.delivery.planner.StrategyComparator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceFirstRouteStrategyTest {

    private final RouteStrategy distanceFirst = new DistanceFirstRouteStrategy();

    @Test
    void picksTheShortestRoute() {
        RouteDecision decision = distanceFirst.decide(TestData.routes(), TestData.standard());

        assertEquals("R3", decision.selectedRoute().id());
        assertEquals(15.0, decision.score(), 0.0001);
    }

    @Test
    void shortestRouteIsSkippedWhenItIsClosed() {
        Route closedShort = TestData.full("SHORT", 5, 10, 500, 1.0, TrafficLevel.LOW, 0, 0, false);
        Route longer = TestData.route("LONGER", 12, 20, 900, 2.0);

        assertEquals("LONGER", distanceFirst.selectRoute(List.of(closedShort, longer), TestData.standard()).id());
    }

    @Test
    void shortestRouteIsSkippedWhenItBreaksTheDeadline() {
        // R3 is 15 km but takes 47 min with heavy traffic. Deadline is 45 min -> R7 (20 km) wins.
        DeliveryRequest request = TestData.request(DeliveryPriority.STANDARD, 8000, 45);

        assertEquals("R7", distanceFirst.selectRoute(TestData.routes(), request).id());
    }

    @Test
    void equalDistanceIsResolvedByRouteId() {
        Route b = TestData.route("B", 10, 20, 1000, 1.0);
        Route a = TestData.route("A", 10, 40, 2000, 3.0);

        assertEquals("A", distanceFirst.selectRoute(List.of(b, a), TestData.standard()).id());
    }

    @Test
    void worksInTheSamePlannerWithoutChangingIt() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("R2", planner.plan(TestData.routes(), TestData.standard()).selectedRoute().id());

        planner.setStrategy(distanceFirst);

        assertEquals("R3", planner.plan(TestData.routes(), TestData.standard()).selectedRoute().id());
    }

    @Test
    void fifthStrategyIsRegisteredForComparisonMode() {
        boolean registered = StrategyConfig.allStrategies().stream()
                .anyMatch(s -> s instanceof DistanceFirstRouteStrategy);
        assertTrue(registered);

        ComparisonReport report = new StrategyComparator(StrategyConfig.allStrategies())
                .compare(TestData.routes(), TestData.standard());

        assertEquals(5, report.entries().size());
        assertTrue(report.format().contains("Distance-First Strategy:"));
    }
}
