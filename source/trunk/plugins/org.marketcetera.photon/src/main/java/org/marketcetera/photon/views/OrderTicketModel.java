package org.marketcetera.photon.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.marketcetera.core.MarketceteraException;
import org.marketcetera.core.publisher.PublisherEngine;
import org.marketcetera.quickfix.FIXDataDictionaryManager;
import org.marketcetera.quickfix.FIXMessageFactory;
import org.marketcetera.quickfix.FIXMessageUtil;

import quickfix.DataDictionary;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix42.MarketDataSnapshotFullRefresh;

/**
 * The abstract superclass for model objects that represent order tickets.
 * This class has some support for event handling, implementing the 
 * JavaBean contract for bean listeners. 
 * 
 * Additionally this class holds a QuickFIX message that is intended to be
 * the message edited by the order ticket UI.
 * @author gmiller
 *
 */
public abstract class OrderTicketModel {
	public static class OrderTicketPublication
    	{
    	    public enum Type {
    	        BID,OFFER
    	    };	    
    	    private final Type type;
    	    private final MarketDataSnapshotFullRefresh.NoMDEntries message;
    	    OrderTicketPublication(Type inType,
    	                           MarketDataSnapshotFullRefresh.NoMDEntries inMessage)
    	    {
    	        type = inType;
    	        message = inMessage;
    	    }
    	    public Type getType()
    	    {
    	        return type;
    	    }
    	    public MarketDataSnapshotFullRefresh.NoMDEntries getMessage()
    	    {
    	        return message;
    	    }
    	    public String toString()
    	    {
    	        return String.format("OrderTicketModel %s publication: %s", 
    	                             type,
    	                             message);
    	    }
    	}

    private final PropertyChangeSupport propertyChangeSupport;
	protected Message orderMessage;
	private final WritableList customFieldsList = new WritableList();
	private final FIXMessageFactory messageFactory;
	private final DataDictionary dictionary;
    private final PublisherEngine mPublisher = new PublisherEngine();

	/**
	 * Create a new OrderTicketModel using the specified FIXMessageFactory
	 * for message creation and augmentation.
	 * 
	 * @param messageFactory the message factory
	 */
	public OrderTicketModel(FIXMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
		propertyChangeSupport = new PropertyChangeSupport(this);
		dictionary = FIXDataDictionaryManager
			.getCurrentFIXDataDictionary().getDictionary();
	}

	/**
	 * Get the order message (or cancel/replace message) that is the
	 * message being edited by the associated order ticket.
	 * @return
	 */
	public Message getOrderMessage() {
		return orderMessage;
	}
	
	/**
	 * Set the new value of the order message, and fire a property changed
	 * event.
	 * 
	 * @param newValue
	 * @see #getOrderMessage()
	 */
	public void setOrderMessage(Message newValue) {
		Object oldValue = this.orderMessage;
		this.orderMessage = newValue;
		propertyChangeSupport.firePropertyChange("orderMessage", oldValue, newValue);
	}
	
	
	/**
	 * Clear the order message by creating a new one and calling {@link #setOrderMessage(Message)}
	 */
	public void clearOrderMessage() {
		Message aMessage = createNewOrder();
		setOrderMessage(aMessage);
	}

	/**
	 * Subclassers should implement this method to create a message appropriate
	 * to the subclass's concept of an order.  Probably this means creating a
	 * message specific to a certain asset type.
	 * @return the new order message object
	 */
	protected abstract Message createNewOrder();

	/**
	 * The list that should store a collection of {@link CustomField} objects.
	 * These custom fields are presented to the user, and each can be activated
	 * for inclusion into all messages generated by this order ticket.
	 * 
	 * Modify this list directly to add and remove items.
	 * 
	 * @return the list of custom fields
	 */
	public WritableList getCustomFieldsList() {
		return customFieldsList;
	}
	
	/**
	 * This method is responsible for "completing" the order message
	 * prior to sending it.  Currently this method adds the appropriate
	 * custom fields to the message, and then adds the TransactTime if 
	 * necessary.  This should only be done just prior to sending the message
	 * as the TransactTime should be as close to the sending time of the message
	 * as possible.
	 * 
	 * @throws MarketceteraException
	 */
	public void completeMessage() throws MarketceteraException {
		addCustomFields();
		messageFactory.addTransactionTimeIfNeeded(orderMessage);
	}

	/**
	 * This method returns the {@link IObservableValue} implementation that
	 * directly accesses the Symbol field of the embedded QuickFIX message.  Note
	 * that this observable will not generate events in the case of a change to 
	 * the Symbol field, but will only work with explicit gets and sets.
	 * 
	 * Subclassers should consider overriding this message for different methods of
	 * handling the concept of a "symbol".
	 * 
	 * @return the observable value for the symbol field of the model
	 */
	public IObservableValue getObservableSymbol(){
		return new AbstractObservableValue() {
			@Override
			protected Object doGetValue() {
				if (orderMessage != null){
					try {
						return orderMessage.getString(Symbol.FIELD);
					} catch (FieldNotFound fnf){
						// allow fall through to return null
					}
				}
				return null;
			}
			
			public Object getValueType() {
				return String.class;
			}
			@Override
			protected void doSetValue(Object value) {
				orderMessage.setField(new Symbol((String)value));
			}
		};
	}
	
	/**
	 * Determines if a particular order is valid, based on the current
	 * state of the model.
	 * 
	 * @return true if the order is valid, false otherwise
	 */
	public boolean isOrderMessageValid() {
		return orderMessage.isSetField(Side.FIELD)
		&& orderMessage.isSetField(OrderQty.FIELD)
		&& orderMessage.isSetField(Symbol.FIELD)
		&& orderMessage.isSetField(Price.FIELD)
		&& orderMessage.isSetField(TimeInForce.FIELD);
	}

	
	/**
	 * Loops through the list of custom fields and adds the enabled fields
	 * to the message.
	 * 
	 * This method should insert header, body and trailer fields in the appropriate
	 * place based on the data dictionary in this model.  By default, fields
	 * will be placed in the body of the FIX message.
	 * 
	 * @throws MarketceteraException
	 */
	private void addCustomFields() 
	    throws MarketceteraException 
	{
		for (Object customFieldObject : customFieldsList) {
			CustomField customField = (CustomField) customFieldObject;
			if (customField.isEnabled()) {
				String key = customField.getKeyString();
				String value = customField.getValueString();
				int fieldNumber = -1;
				try {
					fieldNumber = Integer.parseInt(key);
				} catch (Throwable e) {
					try {
						fieldNumber = dictionary.getFieldTag(key);
					} catch (Throwable ex) {
						// leave field number as is
					}
				}
				if (fieldNumber > 0) {
					if (dictionary.isHeaderField(fieldNumber)) {
						FIXMessageUtil.insertFieldIfMissing(fieldNumber, value,
								orderMessage.getHeader());
					} else if (dictionary.isTrailerField(fieldNumber)) {
						FIXMessageUtil.insertFieldIfMissing(fieldNumber, value,
								orderMessage.getTrailer());
					} else {
						FIXMessageUtil.insertFieldIfMissing(fieldNumber, value,
								orderMessage);
					}
				} else {
					throw new MarketceteraException("Could not find field "
							+ key);
				}
			}
		}
	}
	
	/**
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @see PropertyChangeSupport#hasListeners(String)
	 */
	public boolean hasListeners(String propertyName) {
		return propertyChangeSupport.hasListeners(propertyName);
	}

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	/**
	 * Get the {@link FIXMessageFactory} for this model
	 * @return the FIXMessageFactory
	 */
	public FIXMessageFactory getMessageFactory() {
		return messageFactory;
	}

	protected final PublisherEngine getPublisher()
	{
	    return mPublisher;
	}
}
