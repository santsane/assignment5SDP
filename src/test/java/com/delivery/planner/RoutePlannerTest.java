package com.delivery.planner;

import com.delivery.TestData;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;
import com.delivery.strategy.BalancedRouteStrategy;
import com.delivery.strategy.CheapestRouteStrategy;
import com.delivery.strategy.EcoFriendlyRouteStrategy;
import com.delivery.strategy.FastestRouteStrategy;
import com.delivery.strategy.RouteStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoutePlannerTest {

    private final List<Route> routes = TestData.routes();
    private final DeliveryRequest request = TestData.standard();

    @Test
    void sameDeliveryObjectWorksWithDifferentStrategiesAtRuntime() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("R2", planner.plan(routes, request).selectedRoute().id());

        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("R5", planner.plan(routes, request).selectedRoute().id());

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals("R4", planner.plan(routes, request).selectedRoute().id());

        planner.setStrategy(new BalancedRouteStrategy());
        assertEquals("R7", planner.plan(routes, request).selectedRoute().id());
    }

    @Test
    void switchingBackGivesTheSameResultAsBefore() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        RouteDecision first = planner.plan(routes, request);

        planner.setStrategy(new CheapestRouteStrategy());
        planner.plan(routes, request);
        planner.setStrategy(new FastestRouteStrategy());

        assertEquals(first, planner.plan(routes, request)); // the planner keeps no hidden state
    }

    @Test
    void getStrategyReturnsTheCurrentOne() {
        RouteStrategy cheapest = new CheapestRouteStrategy();
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        planner.setStrategy(cheapest);

        assertSame(cheapest, planner.getStrategy());
    }

    @Test
    void plannerWorksWithAnyRouteStrategyImplementation() {
        // A stub that is NOT one of our strategies. The planner must still work (DIP).
        RouteStrategy alwaysFirst = new RouteStrategy() {
            @Override
            public RouteDecision decide(List<Route> routes, DeliveryRequest request) {
                return new RouteDecision("AlwaysFirst", routes.get(0), 0.0, List.of("stub"), java.util.Map.of());
            }

            @Override
            public String getStrategyName() {
                return "AlwaysFirst";
            }
        };

        RoutePlanner planner = new RoutePlanner(alwaysFirst);

        assertEquals("R1", planner.plan(routes, request).selectedRoute().id());
    }

    @Test
    void nullStrategyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RoutePlanner(null));

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertThrows(IllegalArgumentException.class, () -> planner.setStrategy(null));
    }

    @Test
    void differentRequestsGiveDifferentRoutesWithTheSameStrategy() {
        RoutePlanner planner = new RoutePlanner(new CheapestRouteStrategy());

        Route normal = planner.plan(routes, request).selectedRoute();
        Route withoutR5 = planner.plan(routes, request.withProhibitedRoutes("R5")).selectedRoute();

        assertEquals("R5", normal.id());
        assertEquals("R7", withoutR5.id()); // next cheapest: 2800 + 20 km x 20 = 3200
    }

    @Test
    void strategyThrowsAreNotHiddenByThePlanner() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest impossible = TestData.request(DeliveryPriority.STANDARD, 10, 90);

        assertThrows(com.delivery.exception.NoSuitableRouteException.class,
                () -> planner.plan(routes, impossible));
    }
}
