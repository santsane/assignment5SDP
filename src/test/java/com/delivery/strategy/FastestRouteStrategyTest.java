package com.delivery.strategy;

import com.delivery.TestData;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;
import com.delivery.model.TrafficLevel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FastestRouteStrategyTest {

    private final RouteStrategy fastest = new FastestRouteStrategy();

    @Test
    void picksRouteWithLowestFinalTime() {
        DeliveryRequest urgent = TestData.request(DeliveryPriority.URGENT, 10000, 60);

        RouteDecision decision = fastest.decide(TestData.routes(), urgent);

        assertEquals("R2", decision.selectedRoute().id());
        assertEquals(27.0, decision.score(), 0.0001);
    }

    @Test
    void closedRouteIsNeverChosenEvenIfItWouldBeFastest() {
        // R8 has 25 min which is faster than R2, but it is closed
        RouteDecision decision = fastest.decide(TestData.routes(),
                TestData.request(DeliveryPriority.URGENT, 10000, 60));

        assertEquals(Set.of("R8"), decision.rejectedRoutes().keySet());
        assertTrue(decision.rejectedRoutes().get("R8").contains("unavailable"));
    }

    @Test
    void trafficPenaltyCanChangeTheWinner() {
        // A looks faster on paper (20 min) but heavy traffic makes it 27 min. B is 25 min.
        Route a = TestData.full("A", 10, 20, 1000, 1.0, TrafficLevel.HIGH, 0, 0, true);
        Route b = TestData.route("B", 10, 25, 1000, 1.0);

        Route winner = fastest.selectRoute(List.of(a, b), TestData.standard());

        assertEquals("B", winner.id());
    }

    @Test
    void weatherPenaltyCanChangeTheWinner() {
        Route sunny = TestData.route("SUNNY", 10, 30, 1000, 1.0);
        Route rainy = TestData.full("RAINY", 10, 25, 1000, 1.0, TrafficLevel.LOW, 10, 0, true); // 35 min

        assertEquals("SUNNY", fastest.selectRoute(List.of(rainy, sunny), TestData.standard()).id());
    }

    @Test
    void budgetRestrictionRejectsFasterRoutes() {
        // Urgent delivery with budget 5000: R2 (7180) and R1 (6860) are too expensive.
        // R4 (5340) is also too expensive, so the fastest allowed route is R7 (40 min).
        DeliveryRequest request = TestData.request(DeliveryPriority.URGENT, 5000, 120);

        RouteDecision decision = fastest.decide(TestData.routes(), request);

        assertEquals("R7", decision.selectedRoute().id());
        assertTrue(decision.rejectedRoutes().get("R2").contains("Budget exceeded"));
        assertTrue(decision.rejectedRoutes().containsKey("R1"));
    }

    @Test
    void deadlineRestrictionRejectsSlowRoutes() {
        // deadline 45 min -> R3 (47), R5 (55) and R6 (60) are too slow
        DeliveryRequest request = TestData.request(DeliveryPriority.STANDARD, 10000, 45)
                .withProhibitedRoutes("R2");

        RouteDecision decision = fastest.decide(TestData.routes(), request);

        assertEquals("R7", decision.selectedRoute().id()); // 40 min
        assertTrue(decision.rejectedRoutes().get("R5").contains("deadline"));
        assertTrue(decision.rejectedRoutes().get("R2").contains("prohibited"));
    }

    @Test
    void deadlineBoundaryRouteIsStillAllowed() {
        // R2 takes exactly 27 min and the deadline is exactly 27 min
        DeliveryRequest request = TestData.request(DeliveryPriority.STANDARD, 10000, 27);

        assertEquals("R2", fastest.selectRoute(TestData.routes(), request).id());
    }

    @Test
    void equalTimesAreResolvedByRouteIdSoResultIsStable() {
        Route second = TestData.route("B", 10, 30, 1000, 1.0);
        Route first = TestData.route("A", 12, 30, 1500, 2.0);

        assertEquals("A", fastest.selectRoute(List.of(second, first), TestData.standard()).id());
    }

    @Test
    void explanationShowsTrafficPenaltyAndFinalTime() {
        Route jammed = TestData.full("J", 10, 20, 1000, 1.0, TrafficLevel.HIGH, 3, 0, true);

        RouteDecision decision = fastest.decide(List.of(jammed), TestData.standard());
        String text = decision.explain();

        assertTrue(text.contains("Estimated time: 20 min"));
        assertTrue(text.contains("Traffic penalty"));
        assertTrue(text.contains("Final time: 30 min")); // 20 + 7 traffic + 3 weather
    }
}
