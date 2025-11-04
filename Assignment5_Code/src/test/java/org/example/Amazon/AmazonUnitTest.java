package org.example.Amazon;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Amazon Unit Tests")
class AmazonUnitTest {

    @Mock
    private ShoppingCart cart;

    @InjectMocks
    private Amazon amazon;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        amazon = new Amazon(cart, List.of(
                new RegularCost(),
                new ExtraCostForElectronics(),
                new DeliveryPrice()
        ));
    }

    @Nested
    @DisplayName("specification-based")
    class SpecificationBased {

        @Test
        @DisplayName("If shopping cart has no items all pricing rules return 0")
        void noItemsAllRulesZero() {
            when(cart.getItems()).thenReturn(List.of());
            assertEquals(0.0, amazon.calculate(), 0.001);
        }

        @Test
        @DisplayName("Electronic item triggers ExtraCostForElectronics")
        void electronicTriggersExtraCost() {
            Item electronic = new Item(ItemType.ELECTRONIC, "Laptop", 1, 1000.0);
            when(cart.getItems()).thenReturn(List.of(electronic));
            assertEquals(1012.5, amazon.calculate(), 0.001);
        }
    }

    @Nested
    @DisplayName("structural-based")
    class StructuralBased {

        @Test
        @DisplayName("Amazon.calculate() method goes through loops over all PriceRule objects")
        void amazonLoopsOverAllRules() {
            PriceRule mockRule = mock(PriceRule.class);
            when(mockRule.priceToAggregate(any())).thenReturn(10.0);

            Amazon amazonWithMock = new Amazon(cart, List.of(mockRule, mockRule));
            when(cart.getItems()).thenReturn(List.of());

            assertEquals(20.0, amazonWithMock.calculate(), 0.001);
        }

        @Test
        @DisplayName("DeliveryPrice tests the case when there are more than 10 items in the cart.")
        void deliveryPriceHighTierBranch() {
            List<Item> manyItems = new ArrayList<>();
            manyItems.add(new Item(ItemType.OTHER, "A", 1, 1.0));
            manyItems.add(new Item(ItemType.OTHER, "B", 1, 1.0));
            for (int i = 0; i < 9; i++) {
                manyItems.add(new Item(ItemType.OTHER, "X" + i, 1, 1.0));
            }

            when(cart.getItems()).thenReturn(manyItems);
            assertEquals(31.0, amazon.calculate(), 0.001);
        }
    }
}