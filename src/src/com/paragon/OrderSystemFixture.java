package src.com.paragon;

import com.paragon.orders.OrderLedger;
import com.paragon.stock.Offer;
import src.com.paragon.OrderSystem;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import java.lang.String;

import java.math.BigDecimal;
import java.util.List;

/* Run this class as a JUnit test. */

@RunWith(ConcordionRunner.class)
public class OrderSystemFixture {
    OrderSystem orderSystem = new OrderSystem( new Util(), OrderLedger.getInstance());
    public String resultString( String searchString ) {
        List<Offer> searchResults = orderSystem.searchForProduct( searchString );

        if ( !searchResults.isEmpty() ) {
            return searchResults.get( 0 ).description;
        } else {
            return "No search results found.";
        }
    }

    // TODO: Implement tests
    public BigDecimal chargeIs(long lowerTimeLimit, long upperTimeLimit, String searchString) {
        List<Offer> searchResults = orderSystem.searchForProduct( searchString );

        long timeLapsed = 0;

        if ( upperTimeLimit <= 2 && lowerTimeLimit >= 0 ) {
            timeLapsed = 1;
        } else if ( upperTimeLimit >= 10 && upperTimeLimit <= 20 ) {
            timeLapsed = 14;
        }

        return orderSystem.getQuoteChargeByTime( searchResults.get( 0 ).price, timeLapsed );
    }

    public boolean chargeIsLowerOrEqualToTen(long lowerTimeLimit, long upperTimeLimit, String searchString) {
        List<Offer> searchResults = orderSystem.searchForProduct( searchString );
        return orderSystem.getQuoteChargeByTime( searchResults.get( 0 ).price, 10 ).doubleValue() <= 10;
    }

    public boolean quoteExpired(long upperTimeLimit, String searchString) {
        return orderSystem.isQuoteInvalid( System.currentTimeMillis() - 1 - upperTimeLimit * 60 * 1000 );
    }
}
