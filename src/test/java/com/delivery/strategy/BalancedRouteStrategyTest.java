package com.delivery.strategy;

import com.delivery.TestData;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.RouteDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BalancedRouteStrategyTest {

    private final DeliveryRequest standard = TestData.standard();

    @Test
    void defaultWeightsPickTheBestCompromise() {
        RouteDecision decision = new BalancedRouteStrategy().decide(TestData.routes(), standard);

        // R7 is never the best at one thing but is good at all of them
        assertEquals("R7", decision.selectedRoute().id());
        assertEquals(0.2953, decision.score(), 0.001);
    }

    @Test
    void balancedWinnerIsNotTheWinnerOfAnySingleCriterion() {
        RouteStrategy balanced = new BalancedRouteStrategy();

        String balancedId = balanced.selectRoute(TestData.routes(), standard).id();

        assertNotEquals(new FastestRouteStrategy().selectRoute(TestData.routes(), standard).id(), balancedId);
        assertNotEquals(new CheapestRouteStrategy().selectRoute(TestData.routes(), standard).id(), balancedId);
        assertNotEquals(new EcoFriendlyRouteStrategy().selectRoute(TestData.routes(), standard).id(), balancedId);
    }

    @Test
    void allWeightOnOneCriterionBehavesLikeThatCriterion() {
        assertEquals("R2", new BalancedRouteStrategy(1, 0, 0, 0).selectRoute(TestData.routes(), standard).id()); // time
        assertEquals("R5", new BalancedRouteStrategy(0, 1, 0, 0).selectRoute(TestData.routes(), standard).id()); // cost
        assertEquals("R3", new BalancedRouteStrategy(0, 0, 1, 0).selectRoute(TestData.routes(), standard).id()); // distance
        assertEquals("R4", new BalancedRouteStrategy(0, 0, 0, 1).selectRoute(TestData.routes(), standard).id()); // emission
    }

    @Test
    void scoreIsBetweenZeroAndOne() {
        RouteDecision decision = new BalancedRouteStrategy().decide(TestData.routes(), standard);

        assertTrue(decision.score() >= 0.0 && decision.score() <= 1.0);
    }

    @Test
    void weightsThatDoNotAddUpToOneAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.5, 0.3, 0.1, 0.2)); // 1.1
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.2, 0.2, 0.2, 0.2)); // 0.8
    }

    @Test
    void negativeOrTooBigWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(-0.2, 0.6, 0.3, 0.3));
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(1.5, -0.5, 0, 0));
    }

    @Test
    void validWeightsThatNeedToleranceAreAccepted() {
        // 0.4 + 0.3 + 0.1 + 0.2 is 0.9999999999999999 in double arithmetic
        BalancedRouteStrategy strategy = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);

        assertEquals(0.4, strategy.getTimeWeight(), 0.0);
    }

    @Test
    void routeWithBestScoreIsRejectedWhenItBreaksTheBudget() {
        // R7 (total 3200) is the best compromise, but the budget is only 3100
        DeliveryRequest tight = TestData.request(DeliveryPriority.STANDARD, 3100, 90);

        RouteDecision decision = new BalancedRouteStrategy().decide(TestData.routes(), tight);

        assertEquals("R5", decision.selectedRoute().id());
        assertTrue(decision.rejectedRoutes().containsKey("R7"));
    }

    @Test
    void prohibitedBestRouteGivesTheNextBestOne() {
        DeliveryRequest request = standard.withProhibitedRoutes("R7");

        assertEquals("R4", new BalancedRouteStrategy().selectRoute(TestData.routes(), request).id());
    }

    @Test
    void singleCandidateGetsScoreZero() {
        // boundary: only R5 fits the budget, so min == max for every criterion
        DeliveryRequest request = TestData.request(DeliveryPriority.STANDARD, 2500, 90);

        RouteDecision decision = new BalancedRouteStrategy().decide(TestData.routes(), request);

        assertEquals("R5", decision.selectedRoute().id());
        assertEquals(0.0, decision.score(), 0.0000001);
    }

    @Test
    void explanationListsAllFourCriteria() {
        String text = new BalancedRouteStrategy().decide(TestData.routes(), standard).explain();

        assertTrue(text.contains("Time:"));
        assertTrue(text.contains("Cost:"));
        assertTrue(text.contains("Distance:"));
        assertTrue(text.contains("CO2:"));
        assertTrue(text.contains("Balanced score"));
    }
}
