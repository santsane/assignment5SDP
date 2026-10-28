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

class EcoFriendlyRouteStrategyTest {

    private final DeliveryRequest ecoRequest = TestData.request(DeliveryPriority.ECO, 6000, 90);

    @Test
    void picksRouteWithLowestEcoScore() {
        RouteDecision decision = new EcoFriendlyRouteStrategy().decide(TestData.routes(), ecoRequest);

        assertEquals("R4", decision.selectedRoute().id());
        assertEquals(2.5, decision.score(), 0.0001); // 1.4 x 1.0 + 22 km x 0.05
    }

    @Test
    void doesNotCareAboutPriceOnlyAboutImpact() {
        // R4 costs 3840 but R5 only 2420, eco still prefers R4
        RouteDecision decision = new EcoFriendlyRouteStrategy().decide(TestData.routes(), ecoRequest);

        assertTrue(decision.selectedRoute().totalCost(DeliveryPriority.ECO)
                > TestData.routes().get(4).totalCost(DeliveryPriority.ECO));
    }

    @Test
    void customWeightsChangeTheWinner() {
        // distance is now as important as emissions -> the short R3 (3.2 + 15 = 18.2) wins
        RouteStrategy distanceHeavy = new EcoFriendlyRouteStrategy(1.0, 1.0);

        assertEquals("R3", distanceHeavy.selectRoute(TestData.routes(), ecoRequest).id());
    }

    @Test
    void emissionLimitIsAHardConstraint() {
        // only R4 (1.4 kg) is under 2.0 kg
        DeliveryRequest limited = ecoRequest.withMaxCarbonKg(2.0);

        RouteDecision decision = new EcoFriendlyRouteStrategy().decide(TestData.routes(), limited);

        assertEquals("R4", decision.selectedRoute().id());
        assertTrue(decision.rejectedRoutes().get("R7").contains("Emission limit exceeded")); // 2.2 kg
    }

    @Test
    void emissionLimitTooLowMeansNoRoute() {
        DeliveryRequest limited = ecoRequest.withMaxCarbonKg(1.0);

        assertThrows(NoSuitableRouteException.class,
                () -> new EcoFriendlyRouteStrategy().decide(TestData.routes(), limited));
    }

    @Test
    void routeWithPerfectScoreStillLosesIfItBreaksAConstraint() {
        Route greenButClosed = TestData.full("GREEN", 5, 10, 500, 0.1,
                com.delivery.model.TrafficLevel.LOW, 0, 0, false);
        Route normal = TestData.route("NORMAL", 20, 30, 1000, 3.0);

        RouteStrategy eco = new EcoFriendlyRouteStrategy();

        assertEquals("NORMAL", eco.selectRoute(List.of(greenButClosed, normal), ecoRequest).id());
    }

    @Test
    void negativeWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(-1.0, 0.05));
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(1.0, -0.05));
    }
}
