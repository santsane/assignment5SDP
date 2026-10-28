package com.delivery.constraint;

import com.delivery.util.Fmt;

import java.util.List;
import java.util.Optional;

/**
 * The hard constraints used by every strategy.
 * They live here (and not inside the strategies) so we don't copy the same checks four times.
 */
public final class RouteConstraints {

    private RouteConstraints() {
    }

    public static final RouteConstraint AVAILABLE = (route, request) ->
            route.available()
                    ? Optional.empty()
                    : Optional.of("Route is unavailable.");

    public static final RouteConstraint NOT_PROHIBITED = (route, request) ->
            request.prohibitedRouteIds().contains(route.id())
                    ? Optional.of("Route is prohibited for this delivery.")
                    : Optional.empty();

    public static final RouteConstraint WITHIN_BUDGET = (route, request) -> {
        double total = route.totalCost(request.priority());
        if (total > request.maxBudget()) {
            return Optional.of("Budget exceeded (" + Fmt.money(total)
                    + " > " + Fmt.money(request.maxBudget()) + ").");
        }
        return Optional.empty();
    };

    public static final RouteConstraint BEFORE_DEADLINE = (route, request) -> {
        int minutes = route.effectiveMinutes();
        if (minutes > request.maxDeliveryMinutes()) {
            return Optional.of("Delivery deadline exceeded (" + minutes
                    + " min > " + request.maxDeliveryMinutes() + " min).");
        }
        return Optional.empty();
    };

    public static final RouteConstraint WITHIN_EMISSION_LIMIT = (route, request) -> {
        if (route.carbonEmission() > request.maxCarbonKg()) {
            return Optional.of("Emission limit exceeded (" + Fmt.dec2(route.carbonEmission())
                    + " kg > " + Fmt.dec2(request.maxCarbonKg()) + " kg).");
        }
        return Optional.empty();
    };

    /** The standard set that every strategy applies before scoring. */
    public static List<RouteConstraint> defaults() {
        return List.of(AVAILABLE, NOT_PROHIBITED, WITHIN_BUDGET, BEFORE_DEADLINE, WITHIN_EMISSION_LIMIT);
    }
}
