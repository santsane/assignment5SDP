package com.delivery.planner;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;
import com.delivery.strategy.RouteStrategy;

import java.util.List;

/**
 * The Context of the Strategy pattern.
 *
 * It only knows the RouteStrategy interface. It has no idea which algorithm is inside
 * (no instanceof, no switch), it just delegates. The strategy can be swapped
 * at runtime with setStrategy().
 */
public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        this.strategy = requireStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = requireStrategy(strategy);
    }

    public RouteStrategy getStrategy() {
        return strategy;
    }

    /** Delegates the work to the current strategy. */
    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        return strategy.decide(routes, request);
    }

    private static RouteStrategy requireStrategy(RouteStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy must not be null");
        }
        return strategy;
    }
}
