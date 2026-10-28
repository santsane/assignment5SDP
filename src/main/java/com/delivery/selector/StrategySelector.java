package com.delivery.selector;

import com.delivery.model.DeliveryRequest;
import com.delivery.strategy.RouteStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Decides WHICH strategy should be used for a request.
 * (The strategy itself decides HOW the route is chosen - different responsibility.)
 *
 * Instead of one giant switch, the selector keeps a list of rules:
 * "if this condition is true -> use this strategy". The first matching rule wins,
 * and if nothing matches the default strategy is used.
 * A new business condition = one more addRule(...) call, the selector class stays the same.
 */
public class StrategySelector {

    private record Rule(String description, Predicate<DeliveryRequest> condition, RouteStrategy strategy) {
    }

    private final List<Rule> rules = new ArrayList<>();
    private final RouteStrategy defaultStrategy;

    public StrategySelector(RouteStrategy defaultStrategy) {
        if (defaultStrategy == null) {
            throw new IllegalArgumentException("Default strategy must not be null");
        }
        this.defaultStrategy = defaultStrategy;
    }

    /** Adds a rule at the end of the list (earlier rules have higher priority). */
    public StrategySelector addRule(String description, Predicate<DeliveryRequest> condition,
                                    RouteStrategy strategy) {
        if (condition == null || strategy == null) {
            throw new IllegalArgumentException("A rule needs a condition and a strategy");
        }
        rules.add(new Rule(description, condition, strategy));
        return this;
    }

    public RouteStrategy select(DeliveryRequest request) {
        Rule rule = findRule(request);
        return rule == null ? defaultStrategy : rule.strategy();
    }

    /** Text for the demo: which rule fired and which strategy was chosen. */
    public String explainChoice(DeliveryRequest request) {
        Rule rule = findRule(request);
        if (rule == null) {
            return "No rule matched -> default " + defaultStrategy.getStrategyName();
        }
        return rule.description() + " -> " + rule.strategy().getStrategyName();
    }

    private Rule findRule(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request is missing");
        }
        for (Rule rule : rules) {
            if (rule.condition().test(request)) {
                return rule;
            }
        }
        return null;
    }
}
