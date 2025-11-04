# SE333-Assignment-5

## Project Overview
Bookstore system with Barnes & Noble and Amazon packages. 
Implements cart pricing, Checking items availability, and purchase processing. 
Uses JUnit 5, Mockito for testing, Checkstyle for code style, and JaCoCo for test coverage. 
CI is set up with GitHub Actions.

![SE333 CI](https://github.com/sptl01/SE333-Assignment-5/actions/workflows/SE333_CI.yml/badge.svg)

## Part 1 - BarnesAndNoble Tests
- **Specification-Based**: Tests include: empty cart, null orders, full/partial availability
- **Structural-Based**: Covers code branches, loops, and null books.
- Used `@Nested`, `@DisplayName`, and Mockito for isolation.
- All tests pass locally.

## Part 2 - CI Workflow
- Runs Checkstyle
- Runs JUnit 5 + Mockito tests
- Generates JaCoCo coverage report
- Output: `checkstyle-result.xml`, `jacoco.xml`

## Part 3 - Amazon Tests
- **AmazonIntegrationTest.java**:
    - Uses real `Database` and `ShoppingCartAdaptor`
    - Resets database before each test
    - Tests full flow: add items → calculate price → apply rules
    - **Specification-based**: Covers empty cart, electronics, and delivery pricing tiers
    - **Structural-based**: branches in DeliveryPrice, ExtraCostForElectronics, loops in RegularCost


- **AmazonUnitTest.java**:
    - Mocks `ShoppingCart` to test`Amazon.calculate()`
    - **Specification-based**: empty cart, electronics
    - **Structural-based**: rule loop, delivery high-tier branch