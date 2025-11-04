package org.example.Barnes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BarnesAndNoble - Specification & Structural Tests")
class BarnesAndNobleTest {

    @Mock
    private BookDatabase bookDatabase;

    @Mock
    private BuyBookProcess buyBookProcess;

    private BarnesAndNoble barnesAndNoble;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        barnesAndNoble = new BarnesAndNoble(bookDatabase, buyBookProcess);
    }


    // SPECIFICATION-BASED TESTS
    @Nested
    @DisplayName("specification-based")
    class SpecificationBasedTests {

        @Test
        @DisplayName("should return null when order is null")
        void getPriceForCart_nullOrder_returnsNull() {
            PurchaseSummary result = barnesAndNoble.getPriceForCart(null);
            assertNull(result, "Null order should return null");
        }

        @Test
        @DisplayName("should calculate total price for fully available books")
        void getPriceForCart_fullyAvailableBooks_calculatesTotal() {
            Book book1 = new Book("111", 30, 5);
            Book book2 = new Book("222", 40, 3);

            when(bookDatabase.findByISBN("111")).thenReturn(book1);
            when(bookDatabase.findByISBN("222")).thenReturn(book2);

            Map<String, Integer> order = Map.of("111", 2, "222", 1);

            PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

            assertNotNull(summary);
            assertEquals(2 * 30 + 1 * 40, summary.getTotalPrice());
            verify(buyBookProcess).buyBook(book1, 2);
            verify(buyBookProcess).buyBook(book2, 1);
        }

        @Test
        @DisplayName("should handle partial availability and record unavailable items")
        void getPriceForCart_partialAvailability_recordsUnavailable() {
            Book book = new Book("333", 50, 3);

            when(bookDatabase.findByISBN("333")).thenReturn(book);

            Map<String, Integer> order = Map.of("333", 7);

            PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

            assertEquals(3 * 50, summary.getTotalPrice());
            Map<Book, Integer> unavailable = summary.getUnavailable();
            assertEquals(1, unavailable.size());
            assertEquals(4, unavailable.get(book).intValue());
            verify(buyBookProcess).buyBook(book, 3);
        }

        @Test
        @DisplayName("should handle empty order map")
        void getPriceForCart_emptyOrder_returnsEmptySummary() {
            Map<String, Integer> order = new HashMap<>();
            PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

            assertNotNull(summary);
            assertEquals(0, summary.getTotalPrice());
            assertTrue(summary.getUnavailable().isEmpty());
        }
    }


    // STRUCTURAL-BASED TESTS
    @Nested
    @DisplayName("structural-based")
    class StructuralBasedTests {

        @Test
        @DisplayName("should enter if-branch when requested quantity > available (quantity reduced)")
        void retrieveBook_quantityExceedsStock_entersIfBranch() {
            Book book = new Book("999", 25, 2);
            when(bookDatabase.findByISBN("999")).thenReturn(book);

            Map<String, Integer> order = Map.of("999", 5);

            PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

            assertEquals(2 * 25, summary.getTotalPrice());
            assertEquals(3, summary.getUnavailable().get(book).intValue());
            verify(buyBookProcess).buyBook(book, 2);
        }

        @Test
        @DisplayName("should skip if-branch when requested quantity <= available")
        void retrieveBook_quantityWithinStock_skipsIfBranch() {
            Book book = new Book("888", 35, 10);
            when(bookDatabase.findByISBN("888")).thenReturn(book);

            Map<String, Integer> order = Map.of("888", 5);

            PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

            assertEquals(5 * 35, summary.getTotalPrice());
            assertTrue(summary.getUnavailable().isEmpty(), "No unavailable items");
            verify(buyBookProcess).buyBook(book, 5);
        }

        @Test
        @DisplayName("should loop through all entries in order map (for-each coverage)")
        void getPriceForCart_multipleItems_loopsOverAllEntries() {
            Book b1 = new Book("001", 10, 5);
            Book b2 = new Book("002", 20, 5);
            Book b3 = new Book("003", 15, 3);

            when(bookDatabase.findByISBN("001")).thenReturn(b1);
            when(bookDatabase.findByISBN("002")).thenReturn(b2);
            when(bookDatabase.findByISBN("003")).thenReturn(b3);

            Map<String, Integer> order = new LinkedHashMap<>();
            order.put("001", 1);
            order.put("002", 2);
            order.put("003", 1);

            PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

            assertEquals(1 * 10 + 2 * 20 + 1 * 15, summary.getTotalPrice());
            verify(buyBookProcess, times(3)).buyBook(any(), anyInt());
        }

        @Test
        @DisplayName("boundary case: requested quantity exactly equals available")
        void retrieveBook_quantityExactlyEqualsStock_boundaryCase() {
            Book book = new Book("777", 100, 3);
            when(bookDatabase.findByISBN("777")).thenReturn(book);

            Map<String, Integer> order = Map.of("777", 3);

            PurchaseSummary summary = barnesAndNoble.getPriceForCart(order);

            assertEquals(3 * 100, summary.getTotalPrice());
            assertTrue(summary.getUnavailable().isEmpty());
            verify(buyBookProcess).buyBook(book, 3);
        }
    }
}