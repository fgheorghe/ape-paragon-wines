package src.com.paragon;

import com.paragon.orders.Order;
import com.paragon.orders.OrderLedger;
import com.paragon.stock.Offer;
import com.paragon.stock.Quote;
import com.paragon.stock.Warehouse;
import com.paragon.OrderService;
import com.paragon.FulfillmentService;


import java.math.BigDecimal;
import java.util.*;

public class OrderSystem implements OrderService {

    private static final long MAX_QUOTE_AGE_MILLIS = 20 * 60 * 1000;

    public static final BigDecimal STANDARD_PROCESSING_CHARGE = new BigDecimal(5);
    public static final BigDecimal CASE_SIZE = new BigDecimal(12);

    private Map<UUID, Quote> quotes = new HashMap<UUID, Quote>();

    private Util util;
    private FulfillmentService fulfillmentService;

    public OrderSystem(Util utilObject, FulfillmentService fulfillmentServiceObject ) {
       util = utilObject;
       fulfillmentService = fulfillmentServiceObject;
    }

    @Override
    public List<Offer> searchForProduct(String query) {

        List<Offer> searchResults = Warehouse.getInstance().searchFor(query);
        for (Offer offer : searchResults) {
            quotes.put(offer.id, new Quote(offer, System.currentTimeMillis()));
        }
        return searchResults;
    }

    // HERE
    public boolean isQuoteInvalid( long timestamp ) {
        long timeNow = util.currentTimeMillis();
        return timeNow - timestamp > MAX_QUOTE_AGE_MILLIS;
    }

    // HERE
    public BigDecimal getQuoteChargeByTime( BigDecimal offerPrice, long timeLapsed ) {
        BigDecimal charge = STANDARD_PROCESSING_CHARGE;

        if ( timeLapsed <= 2 ) {
           charge = new BigDecimal(0);
        } else if ( timeLapsed <= 10 ) {
            // Either 5% or 10 GBP, whichever is lower
           charge = new BigDecimal(
                   Math.min( offerPrice.doubleValue() * 5 / 100, 10 )
           );
        } else if ( timeLapsed <= 20 ) {
           charge = new BigDecimal(20);
        }

        return charge;
    }

    // HERE
    public BigDecimal getQuotePrice( BigDecimal offerPrice, long timestamp ) {
        long timeNow = util.currentTimeMillis();

        long timeLapsed = ( timeNow - timestamp ) / (1000*60) % 60;

        return offerPrice.multiply(CASE_SIZE)
                .add( getQuoteChargeByTime(
                        offerPrice.multiply(CASE_SIZE)
                        , timeLapsed )
                );
    }

    @Override
    public void confirmOrder(UUID id, String userAuthToken) {

        if (!quotes.containsKey(id)) {
            throw new NoSuchElementException("Offer ID is invalid");
        }

        Quote quote = quotes.get(id);

        if ( isQuoteInvalid( quote.timestamp ) ) {
            throw new IllegalStateException("Quote expired, please get a new price");
        }

        Order completeOrder = new Order(
                getQuotePrice(quote.offer.price, quote.timestamp)
                , quote
                , util.currentTimeMillis()
                , userAuthToken
        );

        fulfillmentService.placeOrder(completeOrder);
    }

}