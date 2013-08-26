package test;

import com.paragon.stock.Offer;
import org.junit.Test;
import src.com.paragon.OrderSystem;
import src.com.paragon.Util;
import org.junit.Test;
import com.paragon.orders.OrderLedger;
import com.paragon.orders.Order;
import com.paragon.FulfillmentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;


/**
 * Created with IntelliJ IDEA.
 * User: fgheorghe
 * Date: 24/08/13
 * Time: 18:04
 * To change this template use File | Settings | File Templates.
 */
public class OrderSystemTest {
    @Test
    public void testSearchForProduct() {
        OrderSystem orderSystem = new OrderSystem( new Util(), OrderLedger.getInstance() );
        List<Offer> searchResults = orderSystem.searchForProduct( "Cabernet Sauvignon" );
        assertFalse(searchResults.isEmpty());
    }

    // Create mocks
    Util util = mock(Util.class);
    FulfillmentService fulfillmentService = spy(OrderLedger.getInstance());

    // Search and place order
    OrderSystem orderSystem = new OrderSystem(
            util
            ,fulfillmentService
    );

    @Test
    public void testConfirmOrder() throws Exception {
        // "Simulate" System.currentTimeMillis, with a 10 second delay
        when(util.currentTimeMillis()).thenReturn(System.currentTimeMillis() + 10000);

        List<Offer> searchResults = orderSystem.searchForProduct( "Cabernet Sauvignon" );

        Offer offer = searchResults.get( 0 );

        orderSystem.confirmOrder( offer.id, "" );

        verify( fulfillmentService ).placeOrder((Order) anyObject());
    }

    @Test(expected = IllegalStateException.class)
    public void testExpireOffer() throws Exception {
        // "Simulate" System.currentTimeMillis, with a 30 minute delay
        when(util.currentTimeMillis()).thenReturn(System.currentTimeMillis() + 1800000);

        List<Offer> searchResults = orderSystem.searchForProduct( "Cabernet Sauvignon" );

        Offer offer = searchResults.get( 0 );

        orderSystem.confirmOrder( offer.id, "" );
    }

    @Test
    public void testQuoteIsInvalid() {
        // Revert util.currentTimeMillis method to initial behaviour
        when(util.currentTimeMillis()).thenReturn(System.currentTimeMillis());
        assertTrue( orderSystem.isQuoteInvalid( System.currentTimeMillis() - 21 * 60 * 1000 ) );
    }

    @Test
    public void testQuoteChargeByTime() {
        // Charge is 0, after 1 minute and price of 1000
        assertEquals(
                orderSystem.getQuoteChargeByTime( new BigDecimal(1000), 1 )
                , new BigDecimal(0)
        );

        // Charge is 5, after 5 minutes, and price of 100
        assertEquals(
                orderSystem.getQuoteChargeByTime( new BigDecimal(100), 5 )
                , new BigDecimal(5)
        );

        // Charge is 10, after 5 minutes, and price of 1000
        assertEquals(
                orderSystem.getQuoteChargeByTime( new BigDecimal(1000), 5 )
                , new BigDecimal(10)
        );

        // Charge is 20, after 15 minutes, and price of 1000
        assertEquals(
                orderSystem.getQuoteChargeByTime( new BigDecimal(1000), 15 )
                , new BigDecimal(20)
        );
    }

    @Test
    public void testQuotePrice() {
        // Revert util.currentTimeMillis method to initial behaviour
        when(util.currentTimeMillis()).thenReturn(System.currentTimeMillis());

        // Per bottle price is 1000, order placed 5 minutes ago
        // Expected value: 1000 * 12 + 10 = 12010
        assertEquals(
                orderSystem.getQuotePrice( new BigDecimal(1000)
                ,System.currentTimeMillis() - 300000 ), new BigDecimal(12010)
        );
    }
}
