# Smart Delivery & Route Optimization Engine

Assignment 5. Strategy Pattern

Java 17, Maven, JUnit 5

Sanzhar Zhunis, SE-2519

## About the project

This is a delivery system which selects a route for an order. For every order there are many possible routes and there is no "best" route for all situations. For example, an urgent medical delivery needs the fastest route, a normal delivery needs the cheapest route, and an eco delivery needs the route with the lowest CO2.

I solved this problem with the Strategy pattern, so in the code there is no long if / else if chain. Every algorithm has its own class and the algorithm can be changed when the program is running.

More details (formulas, design decisions, limitations) are in the file docs/REPORT.md

## How to run

To run all the tests:

```
mvn test
```

To run the console demo:

```
mvn compile exec:java
```

It is also possible to open the folder in IntelliJ IDEA and run the Main class or the tests from there.

## Answers to the questions

### 1. What problem does Strategy solve in this project?

A route can be selected in different ways: 
fastest, cheapest, eco-friendly, balanced or shortest. Without Strategy we would need one big method with many conditions, and for every new algorithm we must change this method again. With Strategy every algorithm is a separate class with the same interface, so the code is easier to maintain and to test, and the algorithm can be replaced at runtime.

### 2. Where is the Context?

The Context is the class `RoutePlanner` (src/main/java/com/delivery/planner/RoutePlanner.java).

It keeps one RouteStrategy object, and its method `plan()` only calls `strategy.decide(routes, request)`. There is no algorithm inside RoutePlanner, no `instanceof` and no `switch`.

### 3. Where is the Strategy interface?

The interface is `RouteStrategy` (src/main/java/com/delivery/strategy/RouteStrategy.java).

I made one small change comparing to the template from the assignment. The method `decide()` returns a `RouteDecision` (route, score, explanation and rejected routes) and not only a `Route`, because every strategy must explain its result. The method `selectRoute()` from the template still exists as a default method, and it returns only the Route.

### 4. What are the Concrete Strategies?

All of them are in the package `com.delivery.strategy`:

- **FastestRouteStrategy** - the lowest final time wins. Final time = estimated time + traffic penalty + weather penalty.
- **CheapestRouteStrategy** - the lowest total cost wins. Total cost = base cost + distance cost (km x 20) + priority surcharge + additional fees.
- **EcoFriendlyRouteStrategy** - the lowest eco score wins. Eco score = CO2 x emissionWeight + distance x distanceWeight.
- **BalancedRouteStrategy** - looks at time, cost, distance and CO2 together. The values are normalized (min-max) and multiplied by weights. The weights are configurable and must be 1.0 in sum, otherwise the constructor throws an exception.
- **DistanceFirstRouteStrategy** - the shortest distance wins. This is the fifth strategy and I added it in a separate commit.

All of them extend `AbstractRouteStrategy`. This class does the common work (checking the input, hard constraints, choosing the lowest score, creating the explanation), so every concrete strategy writes only its own `score()` and `explain()`.

### 5. Where is runtime strategy switching demonstrated?

- In `Main.runtimeSwitching()`. There is one RoutePlanner object and I call `setStrategy()` four times on it.
- In `StrategyComparator.compare()`, where one planner is used with all strategies in a loop.
- In the tests `RoutePlannerTest.sameDeliveryObjectWorksWithDifferentStrategiesAtRuntime` and `switchingBackGivesTheSameResultAsBefore`.

### 6. Why is this Strategy and not simple if/else?

- Every algorithm is in its own class, so it can be tested separately.
- RoutePlanner does not change when we add a new algorithm (Open/Closed Principle).
- The algorithm can be changed at runtime, and in the tests we can use a fake strategy.
- The decision "which algorithm to use" is separated from the code "how to calculate".

### 7. How can a new Strategy be added?

1. Create a new class which extends `AbstractRouteStrategy` and write `score()`, `explain()` and `getStrategyName()`.
2. Register it in `StrategyConfig.allStrategies()`. If there is a business rule for it, add one `addRule(...)` in `StrategyConfig.createSelector()`.

I did exactly this for DistanceFirstRouteStrategy in the last commit. I needed one new class and I changed only one existing class (`StrategyConfig`, one line). RoutePlanner, StrategySelector, StrategyComparator and the other strategies were not changed.

### 8. What is the difference between Strategy and Bridge?

Both patterns use composition and an interface, so the class diagrams look similar. But the idea is different:

- **Strategy** is used to change an algorithm (the "how"). There is only one part which changes, and the client can change it at runtime, for example `planner.setStrategy(...)`.
- **Bridge** is used to separate an abstraction from its implementation, so both can change independently. There are two hierarchies, and the connection between them is usually set when the object is created.

In this project only the route selection algorithm changes and it can be switched at runtime, so it is Strategy.

TODO: add here one or two sentences about my previous Bridge assignment (which was the abstraction and which was the implementor).

### 9. Which SOLID principles are demonstrated?

- **SRP** - every class has one job. RoutePlanner only delegates, every strategy has one algorithm, StrategySelector only selects a strategy, RouteConstraints keeps the hard rules, StrategyComparator only compares, and StrategyConfig only connects the objects.
- **OCP** - a new strategy can be added without changing RoutePlanner, StrategySelector or StrategyComparator. A new selection rule is only one `addRule()` call.
- **DIP** - RoutePlanner depends on the abstraction `RouteStrategy`, not on concrete strategies. The test `plannerWorksWithAnyRouteStrategyImplementation` uses a fake strategy to prove it.
- LSP and ISP are also used: every subclass of AbstractRouteStrategy can be used as RouteStrategy, and the interfaces are small (RouteStrategy has 3 methods, RouteConstraint has 1).

### 10. What limitation does the current architecture have?

- The system only chooses from routes which are already given. It does not create routes and it does not use a real map or traffic service.
- The prices (20 per km, urgent surcharge 1500) are constants in the code and not in a configuration file.
- The rules in StrategySelector are checked in order and the first one wins. There is no check for conflicts between rules.
- BalancedRouteStrategy uses min-max normalization, so the score of a route depends on the other routes which are still allowed.
- RoutePlanner has a mutable state (the current strategy), so it is not thread-safe.
- Money is stored as double. In a real system BigDecimal is better.

## Architecture description

The structure is like this:

```
                RouteStrategy  (interface)
                      ^
              AbstractRouteStrategy
   ______________________|______________________
   |          |           |          |          |
Fastest   Cheapest   EcoFriendly   Balanced   DistanceFirst

RoutePlanner (Context)  ---- uses only ---->  RouteStrategy

StrategySelector  ---- chooses which strategy ---->  RoutePlanner.setStrategy(...)
```

**Strategy chooses how to solve the problem. StrategySelector decides which strategy should be used.** These are two different responsibilities.

Packages (in src/main/java/com/delivery):

- `model` - Location, Route, DeliveryRequest, DeliveryPriority, TrafficLevel, RouteDecision
- `strategy` - RouteStrategy, AbstractRouteStrategy and the five concrete strategies
- `constraint` - RouteConstraint and RouteConstraints (hard rules for all strategies)
- `planner` - RoutePlanner (Context), StrategyComparator, ComparisonReport, ComparisonEntry
- `selector` - StrategySelector
- `exception` - NoSuitableRouteException
- `app` - Main (demo), DemoData, StrategyConfig
- `util` - Fmt (number formatting)

What is implemented from the assignment:

- **Hard constraints** - route is unavailable, route is prohibited, budget is exceeded, deadline is exceeded, emission limit is exceeded. A route which breaks a hard constraint cannot win, even if its score is very good.
- **No valid route** - the system throws `NoSuitableRouteException` with the reason for every rejected route. It never returns null.
- **Automatic selection** - URGENT -> Fastest, ECONOMY -> Cheapest, ECO -> Eco-friendly, STANDARD -> Balanced, and STANDARD with ecoPreferred -> Eco-friendly. The rules are objects in a list (`addRule`), so there is no big switch.
- **Comparison mode** - StrategyComparator runs all strategies for the same request.
- **Explanation** - every RouteDecision has the reasons and the rejected routes, and `explain()` prints them.
- **Failure scenarios** - empty route list, no route satisfies constraints, invalid Balanced weights, unavailable route, negative cost or distance, missing request data.

The demo data has 8 routes. R8 is closed on purpose (it would be the fastest, but it cannot be selected). The best routes are: R2 fastest, R5 cheapest, R4 greenest, R3 shortest and R7 the best compromise.

## UML diagrams

The PlantUML source files are in the folder docs/uml.

Class diagram (source: docs/uml/class-diagram.puml):

![Class diagram](docs/uml/class-diagram.png)

Sequence diagram for an URGENT request: StrategySelector -> RoutePlanner -> FastestRouteStrategy -> RouteDecision (source: docs/uml/sequence-diagram.puml):

![Sequence diagram](docs/uml/sequence-diagram.png)

To generate the images again:

```
java -jar plantuml.jar -tpng docs/uml/class-diagram.puml docs/uml/sequence-diagram.puml
```

## Example console output

The full output is saved in docs/example-output.txt. Here are the main parts.

Automatic strategy selection:

```
Request A: URGENT priority -> Fastest
Selected: R2 (Fastest strategy)
Reason:
  Estimated time: 27 min
  Traffic penalty (LOW traffic): +0 min
  Weather penalty: +0 min
  Final time: 27 min
Route R8 rejected:
  Route is unavailable.

Request B: ECONOMY priority -> Cheapest
Selected: R5 (Cheapest strategy)
Reason:
  Base cost: 1800.00
  Distance cost: 31.0 km x 20.0 = 620.00
  Priority surcharge (ECONOMY): 0.00
  Additional fees: 0.00
  Total cost: 2420.00
Route R1 rejected:
  Budget exceeded (5360.00 > 3000.00).
  (R2, R3, R4, R6, R7 are rejected in the same way)

Request C: ECO priority -> Eco-Friendly
Selected: R4 (Eco-Friendly strategy)
Reason:
  CO2: 1.40 kg x 1.00 = 1.40
  Distance: 22.0 km x 0.05 = 1.10
  Eco score: 2.50 (lowest wins)
```

Comparison mode (request #105, all five strategies, short version):

```
DELIVERY REQUEST #105 (STANDARD, budget 8000.00, deadline 90 min)

Fastest:         Route R2  Time: 27 min  Cost: 5680.00  CO2: 4.60 kg  Distance: 24.0 km  Score: 27.00
Cheapest:        Route R5  Time: 55 min  Cost: 2420.00  CO2: 3.90 kg  Distance: 31.0 km  Score: 2420.00
Eco-Friendly:    Route R4  Time: 42 min  Cost: 3840.00  CO2: 1.40 kg  Distance: 22.0 km  Score: 2.50
Balanced:        Route R7  Time: 40 min  Cost: 3200.00  CO2: 2.20 kg  Distance: 20.0 km  Score: 0.30
Distance-First:  Route R3  Time: 47 min  Cost: 3300.00  CO2: 3.20 kg  Distance: 15.0 km  Score: 15.00
```

(In the real output every strategy is printed on several lines, see docs/example-output.txt.)

Failure scenarios:

```
2. budget too small for every route
  NoSuitableRouteException: No route satisfies the constraints (8 of 8 routes rejected)
    R1: Budget exceeded (5360.00 > 500.00).
    ...
4. invalid Balanced weights (sum is 1.5)
  IllegalArgumentException: Weights must add up to 1.0 but the sum is 1.5
5. negative route cost
  IllegalArgumentException: Route BAD: cost cannot be negative (-100.0)
6. missing request data
  IllegalArgumentException: Delivery request is missing
```

## Test results

There are 89 JUnit 5 tests (the minimum was 15):

- DomainModelTest - 10 tests
- RouteConstraintsTest - 8 tests (also the boundary cases for budget, deadline and emission)
- FastestRouteStrategyTest - 9 tests
- CheapestRouteStrategyTest - 7 tests
- EcoFriendlyRouteStrategyTest - 7 tests
- BalancedRouteStrategyTest - 11 tests (also invalid weights)
- RoutePlannerTest - 7 tests (runtime switching)
- StrategySelectorTest - 11 tests (automatic selection)
- StrategyComparatorTest - 6 tests (comparison of strategies)
- FailureScenarioTest - 7 tests (empty list, no valid route, missing request)
- DistanceFirstRouteStrategyTest - 6 tests (the new fifth strategy)

Result of `mvn test`:

```
(paste the output of mvn test here)
```

## Git history

1. Initialize delivery domain model
2. Add RouteStrategy abstraction and RouteDecision
3. Add hard constraints and shared strategy skeleton
4. Implement fastest strategy
5. Implement cheapest strategy
6. Add eco-friendly strategy
7. Add balanced weighted strategy
8. Add RoutePlanner context with runtime strategy switching
9. Add StrategySelector with rule-based strategy choice
10. Add strategy comparison mode
11. Add console demo application
12. Add DistanceFirstRouteStrategy as fifth strategy
13. Add UML and documentation
