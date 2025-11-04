package org.example.Amazon;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Amazon Integration Tests")
class AmazonIntegrationTest {

    private Database db;
    private ShoppingCart cart;
    private Amazon amazon;

    @BeforeEach
    void setUp() {
        db = new Database();
        db.resetDatabase();
        cart = new ShoppingCartAdaptor(db);
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
        @DisplayName("Empty cart returns 0 total")
        void emptyCartReturnsZero() {
            assertEquals(0.0, amazon.calculate(), 0.001);
        }

        @Test
        @DisplayName("Regular item cost its price plus delivery fee of 5")
        void singleRegularItem() {
            amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 10.0));
            assertEquals(15.0, amazon.calculate(), 0.001);
        }

        @Test
        @DisplayName("Electronic item cost an extra charge of 7.50 plus delivery fee.")
        void electronicItemExtraCost() {
            amazon.addToCart(new Item(ItemType.ELECTRONIC, "Phone", 1, 800.0));
            assertEquals(812.5, amazon.calculate(), 0.001);
        }

        @Test
        @DisplayName("5 items: delivery fee is 12.50")
        void fiveItemsDeliveryTier() {
            for (int i = 0; i < 5; i++) {
                amazon.addToCart(new Item(ItemType.OTHER, "Item" + i, 1, 10.0));
            }
            assertEquals(62.5, amazon.calculate(), 0.001);
        }
    }

    @Nested
    @DisplayName("structural-based")
    class StructuralBased {

        @Test
        @DisplayName("DeliveryPrice: covers 1-3 items branch")
        void deliveryPriceLowTierBranch() {
            amazon.addToCart(new Item(ItemType.OTHER, "A", 1, 1.0));
            amazon.addToCart(new Item(ItemType.OTHER, "B", 1, 1.0));
            assertEquals(7.0, amazon.calculate(), 0.001);
        }

        @Test
        @DisplayName("DeliveryPrice: covers 4-10 items branch")
        void deliveryPriceMidTierBranch() {
            for (int i = 0; i < 4; i++) {
                amazon.addToCart(new Item(ItemType.OTHER, "X" + i, 1, 1.0));
            }
            assertEquals(16.5, amazon.calculate(), 0.001);
        }

        @Test
        @DisplayName("ExtraCostForElectronics: covers no-electronics branch")
        void noElectronicsExtraCostZero() {
            amazon.addToCart(new Item(ItemType.OTHER, "Shirt", 2, 15.0));
            double total = amazon.calculate();
            assertEquals(35.0, total, 0.001);
        }

        @Test
        @DisplayName("RegularCost: loop over multiple quantities")
        void regularCostLoopQuantity() {
            amazon.addToCart(new Item(ItemType.OTHER, "Bulk", 3, 10.0));
            assertEquals(35.0, amazon.calculate(), 0.001);
        }
    }
}