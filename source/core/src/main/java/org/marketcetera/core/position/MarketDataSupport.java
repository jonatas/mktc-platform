package org.marketcetera.core.position;

import java.math.BigDecimal;
import java.util.EventObject;

import org.apache.commons.lang.Validate;
import org.marketcetera.util.misc.ClassVersion;

/* $License$ */

/**
 * Interface providing market data for position-related calculations.
 * 
 * Listeners can subscribe to be notified when the trade price changes for a symbol.
 * 
 * @author <a href="mailto:will@marketcetera.com">Will Horn</a>
 * @version $Id$
 * @since $Release$
 */
@ClassVersion("$Id$")
public interface MarketDataSupport {

    /**
     * Returns the price of the last trade for the given symbol.
     * 
     * @param symbol
     *            the symbol in question, will not be null
     * @return the last trade price, or null if unknown
     */
    BigDecimal getLastTradePrice(String symbol);

    /**
     * Returns the closing price for the given symbol. This is the closing price that applies to the
     * incoming position provided by {@link IncomingPositionSupport}. The position engine does not
     * have any notion of a trading day so it is the responsibility of the implementor of this class
     * and IncomingPositionSupport to make sure that values match.
     * 
     * @param symbol
     *            the symbol in question, will not be null
     * @return the closing price, or null if unknown
     */
    BigDecimal getClosingPrice(String symbol);

    /**
     * Adds a listener to be notified when the trade price for a given symbol has changed. This
     * method has no effect if the listener has already been added.
     * 
     * @param symbol
     *            the symbol to listen for
     * @param listener
     *            the listener to add
     */
    void addSymbolChangeListener(String symbol, SymbolChangeListener listener);

    /**
     * Removes a listener. This has no effect if the listener does not exist.
     * 
     * @param symbol
     *            the symbol being listened to
     * @param listener
     *            the listener to remove
     */
    void removeSymbolChangeListener(String symbol, SymbolChangeListener listener);

    /**
     * Interface to notify listeners of changes.
     */
    @ClassVersion("$Id$")
    public interface SymbolChangeListener {

        /**
         * Callback for change notification.
         * 
         * @param event
         *            event describing the change
         */
        void symbolChanged(SymbolChangeEvent event);
    }

    /**
     * Event object for {@link SymbolChangeListener}.
     */
    @ClassVersion("$Id$")
    public static class SymbolChangeEvent extends EventObject {

        /**
         * Constructor.
         * 
         * @param source
         *            the object on which the Event initially occurred
         * @param newPrice
         *            the new value for the symbol price, must not be null
         */
        public SymbolChangeEvent(Object source, BigDecimal newPrice) {
            super(source);
            Validate.notNull(newPrice);
            mNewPrice = newPrice;
        }

        /**
         * The new price for the symbol.
         * 
         * @return the new price for the symbol, will not be null
         */
        public BigDecimal getNewPrice() {
            return mNewPrice;
        }

        private BigDecimal mNewPrice;

        private static final long serialVersionUID = 1L;
    }
}
