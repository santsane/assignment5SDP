package com.delivery.selector;

import com.delivery.TestData;
import com.delivery.app.StrategyConfig;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.planner.RoutePlanner;
import com.delivery.strategy.BalancedRouteStrategy;
import com.delivery.strategy.CheapestRouteStrategy;
import com.delivery.strategy.EcoFriendlyRouteStrategy;
import com.delivery.strategy.FastestRouteStrategy;
import com.delivery.strategy.RouteStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategySelectorTest {

    private final StrategySelector selector = StrategyConfig.createSelector();

    @Test
    void urgentRequestGetsFastestStrategy() {
        assertInstanceOf(FastestRouteStrategy.class,
                selector.select(TestData.request(DeliveryPriority.URGENT, 5000, 60)));
    }

    @Test
    void economyRequestGetsCheapestStrategy() {
        assertInstanceOf(CheapestRouteStrategy.class,
                selector.select(TestData.request(DeliveryPriority.ECONOMY, 5000, 60)));
    }

    @Test
    void ecoRequestGetsEcoStrategy() {
        assertInstanceOf(EcoFriendlyRouteStrategy.class,
                selector.select(TestData.request(DeliveryPriority.ECO, 5000, 60)));
    }

    @Test
    void standardRequestGetsBalancedStrategy() {
        assertInstanceOf(BalancedRouteStrategy.class,
                selector.select(TestData.request(DeliveryPriority.STANDARD, 5000, 60)));
    }

    @Test
    void standardRequestWithEcoPreferenceGetsEcoStrategy() {
        DeliveryRequest request = new DeliveryRequest("1", DeliveryPriority.STANDARD, 5000, 60, true);

        assertInstanceOf(EcoFriendlyRouteStrategy.class, selector.select(request));
    }

    @Test
    void urgentBeatsEcoPreferenceBecauseRuleOrderMatters() {
        DeliveryRequest request = new DeliveryRequest("1", DeliveryPriority.URGENT, 5000, 60, true);

        assertInstanceOf(FastestRouteStrategy.class, selector.select(request));
    }

    @Test
    void noMatchingRuleFallsBackToDefaultStrategy() {
        RouteStrategy fallback = new BalancedRouteStrategy();
        StrategySelector empty = new StrategySelector(fallback);

        assertSame(fallback, empty.select(TestData.standard()));
    }

    @Test
    void newRuleCanBeAddedWithoutChangingTheSelectorClass() {
        RouteStrategy special = new CheapestRouteStrategy();
        StrategySelector custom = new StrategySelector(new BalancedRouteStrategy())
                .addRule("tiny budget", r -> r.maxBudget() < 1000, special);

        assertSame(special, custom.select(TestData.request(DeliveryPriority.STANDARD, 500, 60)));
        assertInstanceOf(BalancedRouteStrategy.class, custom.select(TestData.standard()));
    }

    @Test
    void explainChoiceNamesTheRuleAndTheStrategy() {
        String text = selector.explainChoice(TestData.request(DeliveryPriority.URGENT, 5000, 60));

        assertTrue(text.contains("URGENT"));
        assertTrue(text.contains("Fastest"));
    }

    @Test
    void selectingForMissingRequestIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> selector.select(null));
    }

    @Test
    void selectorAndPlannerTogetherGiveDifferentRoutesForDifferentRequests() {
        RoutePlanner planner = new RoutePlanner(new BalancedRouteStrategy());

        DeliveryRequest a = new DeliveryRequest("A", DeliveryPriority.URGENT, 10000, 60, false);
        DeliveryRequest b = new DeliveryRequest("B", DeliveryPriority.ECONOMY, 3000, 120, false);
        DeliveryRequest c = new DeliveryRequest("C", DeliveryPriority.ECO, 6000, 90, true);

        planner.setStrategy(selector.select(a));
        assertEquals("R2", planner.plan(TestData.routes(), a).selectedRoute().id());

        planner.setStrategy(selector.select(b));
        assertEquals("R5", planner.plan(TestData.routes(), b).selectedRoute().id());

        planner.setStrategy(selector.select(c));
        assertEquals("R4", planner.plan(TestData.routes(), c).selectedRoute().id());
    }
}
