package com.delivery.planner;

import com.delivery.model.RouteDecision;

/**
 * Result of one strategy in comparison mode.
 * Either the strategy found a route (decision is set) or it failed (errorMessage is set).
 */
public record ComparisonEntry(String strategyName, RouteDecision decision, String errorMessage) {

    public static ComparisonEntry success(String strategyName, RouteDecision decision) {
        return new ComparisonEntry(strategyName, decision, null);
    }

    public static ComparisonEntry failure(String strategyName, String errorMessage) {
        return new ComparisonEntry(strategyName, null, errorMessage);
    }

    public boolean succeeded() {
        return decision != null;
    }
}
