package com.delivery.planner;

import com.delivery.exception.NoSuitableRouteException;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;
import com.delivery.strategy.RouteStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Comparison mode: runs the same request through ALL strategies.
 *
 * It uses one RoutePlanner and switches the strategy inside a loop, so it also shows
 * runtime switching. A strategy that finds no route does not stop the others.
 */
public class StrategyComparator {

    private final List<RouteStrategy> strategies;

    public StrategyComparator(List<RouteStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one strategy is needed for comparison");
        }
        this.strategies = List.copyOf(strategies);
    }

    public ComparisonReport compare(List<Route> routes, DeliveryRequest request) {
        RoutePlanner planner = new RoutePlanner(strategies.get(0));
        List<ComparisonEntry> entries = new ArrayList<>();

        for (RouteStrategy strategy : strategies) {
            planner.setStrategy(strategy);
            try {
                RouteDecision decision = planner.plan(routes, request);
                entries.add(ComparisonEntry.success(strategy.getStrategyName(), decision));
            } catch (NoSuitableRouteException e) {
                entries.add(ComparisonEntry.failure(strategy.getStrategyName(), e.getMessage()));
            }
        }
        return new ComparisonReport(request, entries);
    }
}
