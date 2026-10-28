package com.delivery.app;

import com.delivery.model.DeliveryPriority;
import com.delivery.selector.StrategySelector;
import com.delivery.strategy.BalancedRouteStrategy;
import com.delivery.strategy.CheapestRouteStrategy;
import com.delivery.strategy.EcoFriendlyRouteStrategy;
import com.delivery.strategy.FastestRouteStrategy;
import com.delivery.strategy.RouteStrategy;
import com.delivery.strategy.DistanceFirstRouteStrategy

import java.util.List;

/**
 * The one place where concrete strategies are wired together.
 * Everything else (RoutePlanner, StrategySelector, StrategyComparator) only uses abstractions,
 * so adding a new strategy means touching this class and nothing else.
 */
public final class StrategyConfig {

    private StrategyConfig() {
    }

    /** Business rules: which request gets which strategy. Order matters, first match wins. */
    public static StrategySelector createSelector() {
        RouteStrategy fastest = new FastestRouteStrategy();
        RouteStrategy cheapest = new CheapestRouteStrategy();
        RouteStrategy eco = new EcoFriendlyRouteStrategy();
        RouteStrategy balanced = new BalancedRouteStrategy();

        return new StrategySelector(balanced)
                .addRule("URGENT priority", r -> r.priority() == DeliveryPriority.URGENT, fastest)
                .addRule("ECONOMY priority", r -> r.priority() == DeliveryPriority.ECONOMY, cheapest)
                .addRule("ECO priority", r -> r.priority() == DeliveryPriority.ECO, eco)
                .addRule("STANDARD priority + eco preferred", r -> r.ecoPreferred(), eco)
                .addRule("STANDARD priority", r -> r.priority() == DeliveryPriority.STANDARD, balanced);
    }

    /** Every strategy we have, used by comparison mode. */
    public static List<RouteStrategy> allStrategies() {
        return List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy()
                new DistanceFirstRouteStrategy()
        );
    }
}
