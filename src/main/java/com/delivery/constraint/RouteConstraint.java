package com.delivery.constraint;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;

import java.util.Optional;

/**
 * One hard rule. If a route breaks it, the route can never win,
 * no matter how good its score is.
 */
@FunctionalInterface
public interface RouteConstraint {

    /** @return empty if the route is OK, otherwise the reason why it is rejected */
    Optional<String> check(Route route, DeliveryRequest request);
}
