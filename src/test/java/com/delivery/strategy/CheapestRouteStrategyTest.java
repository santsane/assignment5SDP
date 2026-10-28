package com.delivery.strategy;

import com.delivery.TestData;
import com.delivery.exception.NoSuitableRouteException;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheapestRouteStrategyTest {

    private final RouteStrategy cheapest = new CheapestRouteStrategy();

    @Test
    void picksRouteWithLowestTotalCost() {
        RouteDecision decision = cheapest.decide(TestData.routes(), TestData.standard());

        assertEquals("R5", decision.selectedRoute().id());
        assertEquals(2420.0, decision.score(), 0.001); // 1800 + 31 km x 20
    }

    @Test
    void distanceCostCanChangeTheWinner() {
        // X has the lower base cost but is very long: 1000 + 100 x 20 = 3000
        // Y has the higher base cost but is short:    2500 +  10 x 20 = 2700
        Route x = TestData.route("X", 100, 60, 1000, 5.0);
        Route y = TestData.route("Y", 10, 60, 2500, 5.0);

        assertEquals("Y", cheapest.selectRoute(List.of(x, y), TestData.standard()).id());
    }

    @Test
    void additionalFeesAreIncluded() {
        // P: 1000 + 200 = 1200 + toll 500 = 1700.  Q: 1400 + 200 = 1600
        Route p = TestData.full("P", 10, 30, 1000, 1.0,
                com.delivery.model.TrafficLevel.LOW, 0, 500, true);
        Route q = TestData.route("Q", 10, 30, 1400, 1.0);

        assertEquals("Q", cheapest.selectRoute(List.of(p, q), TestData.standard()).id());
    }

    @Test
    void urgentSurchargeIsPartOfTheTotal() {
        DeliveryRequest urgent = TestData.request(DeliveryPriority.URGENT, 10000, 120);

        RouteDecision decision = cheapest.decide(TestData.routes(), urgent);

        assertEquals("R5", decision.selectedRoute().id());
        assertEquals(3920.0, decision.score(), 0.001); // 2420 + 1500
    }

    @Test
    void routesOverBudgetAreRejected() {
        DeliveryRequest request = TestData.request(DeliveryPriority.ECONOMY, 3000, 120);

        RouteDecision decision = cheapest.decide(TestData.routes(), request);

        assertEquals("R5", decision.selectedRoute().id());
        assertTrue(decision.rejectedRoutes().get("R7").contains("Budget exceeded")); // 3200 > 3000
    }

    @Test
    void budgetBoundaryRouteIsAllowed() {
        // R5 total is exactly 2420
        DeliveryRequest exact = TestData.request(DeliveryPriority.ECONOMY, 2420, 120);
        DeliveryRequest oneLess = TestData.request(DeliveryPriority.ECONOMY, 2419, 120);

        assertEquals("R5", cheapest.selectRoute(TestData.routes(), exact).id());
        assertThrows(NoSuitableRouteException.class, () -> cheapest.decide(TestData.routes(), oneLess));
    }

    @Test
    void explanationShowsTheCostFormula() {
        String text = cheapest.decide(TestData.routes(), TestData.standard()).explain();

        assertTrue(text.contains("Base cost: 1800.00"));
        assertTrue(text.contains("Distance cost"));
        assertTrue(text.contains("Priority surcharge"));
        assertTrue(text.contains("Total cost: 2420.00"));
    }
}
