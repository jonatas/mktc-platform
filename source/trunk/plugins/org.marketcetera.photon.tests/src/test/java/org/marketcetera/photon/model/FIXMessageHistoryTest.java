package org.marketcetera.photon.model;

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.Test;

import org.marketcetera.core.AccessViolator;
import org.marketcetera.core.FIXVersionTestSuite;
import org.marketcetera.core.FIXVersionedTestCase;
import org.marketcetera.core.MSymbol;
import org.marketcetera.photon.core.FIXMessageHistory;
import org.marketcetera.photon.core.IncomingMessageHolder;
import org.marketcetera.photon.core.MessageHolder;
import org.marketcetera.photon.core.OutgoingMessageHolder;
import org.marketcetera.quickfix.FIXMessageFactory;
import org.marketcetera.quickfix.FIXVersion;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LastShares;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.SendingTime;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

public class FIXMessageHistoryTest extends FIXVersionedTestCase {

    public FIXMessageHistoryTest(String inName, FIXVersion version) {
		super(inName, version);
	}

    public static Test suite() {
    	return new FIXVersionTestSuite(FIXMessageHistoryTest.class, FIXVersion.values());
    }
    
	private FIXMessageFactory msgFactory = FIXVersion.FIX42.getMessageFactory();

	protected FIXMessageHistory getMessageHistory(){
		return new FIXMessageHistory(msgFactory);
	}

	/*
	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.addIncomingMessage(Message)'
	 */
	public void testAddIncomingMessage() throws FieldNotFound {
		FIXMessageHistory history = getMessageHistory();
		String orderID1 = "1";
		String clOrderID1 = "2";
		String execID = "3";
		char execTransType = ExecTransType.STATUS;
		char execType = ExecType.PARTIAL_FILL;
		char ordStatus = OrdStatus.PARTIALLY_FILLED;
		char side = Side.SELL_SHORT;
		BigDecimal orderQty = new BigDecimal(1000);
		BigDecimal orderPrice = new BigDecimal(789);
		BigDecimal lastQty = new BigDecimal(100);
		BigDecimal lastPrice = new BigDecimal("12.3");
		BigDecimal leavesQty = new BigDecimal(900);
		BigDecimal cumQty = new BigDecimal(100);
		BigDecimal avgPrice = new BigDecimal("12.3");
		MSymbol symbol = new MSymbol("ASDF");
		

		Message message = msgFactory.newExecutionReport(orderID1, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);

		{
			history.addIncomingMessage(message);
			EventList<MessageHolder> historyList = history.getAllMessagesList();
			assertEquals(1, historyList.size());
			assertEquals(IncomingMessageHolder.class, historyList.get(0).getClass());
			IncomingMessageHolder holder = (IncomingMessageHolder) historyList.get(0);
			Message historyMessage = holder.getMessage();
			assertEquals(orderID1.toString(), historyMessage.getString(OrderID.FIELD));
			assertEquals(clOrderID1.toString(), historyMessage.getString(ClOrdID.FIELD));
			assertEquals(execID, historyMessage.getString(ExecID.FIELD));
			assertEquals(""+execTransType, historyMessage.getString(ExecTransType.FIELD));
			assertEquals(""+execType, historyMessage.getString(ExecType.FIELD));
			assertEquals(""+ordStatus, historyMessage.getString(OrdStatus.FIELD));
			assertEquals(""+side, historyMessage.getString(Side.FIELD));
			assertEquals(orderQty, new BigDecimal(historyMessage.getString(OrderQty.FIELD)));
			assertEquals(lastQty, new BigDecimal(historyMessage.getString(LastShares.FIELD)));
			assertEquals(lastPrice, new BigDecimal(historyMessage.getString(LastPx.FIELD)));
			assertEquals(cumQty, new BigDecimal(historyMessage.getString(CumQty.FIELD)));
			assertEquals(avgPrice, new BigDecimal(historyMessage.getString(AvgPx.FIELD)));
			assertEquals(symbol.getFullSymbol(), historyMessage.getString(Symbol.FIELD));
		}		

		{
			String orderID2 = "1001";
			String clOrderID2 = "1002";
			Message message2 = msgFactory.newExecutionReport(orderID2, clOrderID2, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
			history.addIncomingMessage(message2);
			EventList<MessageHolder> historyList = history.getAllMessagesList();
			assertEquals(2, historyList.size());
			assertEquals(IncomingMessageHolder.class, historyList.get(1).getClass());
			IncomingMessageHolder holder = (IncomingMessageHolder) historyList.get(1);
			Message historyMessage = holder.getMessage();
			assertEquals(orderID2.toString(), historyMessage.getString(OrderID.FIELD));
			assertEquals(clOrderID2.toString(), historyMessage.getString(ClOrdID.FIELD));
			assertEquals(execID, historyMessage.getString(ExecID.FIELD));
			assertEquals(""+execTransType, historyMessage.getString(ExecTransType.FIELD));
			assertEquals(""+execType, historyMessage.getString(ExecType.FIELD));
			assertEquals(""+ordStatus, historyMessage.getString(OrdStatus.FIELD));
			assertEquals(""+side, historyMessage.getString(Side.FIELD));
			assertEquals(orderQty, new BigDecimal(historyMessage.getString(OrderQty.FIELD)));
			assertEquals(lastQty, new BigDecimal(historyMessage.getString(LastShares.FIELD)));
			assertEquals(lastPrice, new BigDecimal(historyMessage.getString(LastPx.FIELD)));
			assertEquals(cumQty, new BigDecimal(historyMessage.getString(CumQty.FIELD)));
			assertEquals(avgPrice, new BigDecimal(historyMessage.getString(AvgPx.FIELD)));
			assertEquals(symbol.getFullSymbol(), historyMessage.getString(Symbol.FIELD));
		}
	}

	/*
	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.addOutgoingMessage(Message)'
	 */
	public void testAddOutgoingMessage() throws FieldNotFound {
		FIXMessageHistory history = getMessageHistory();
		String orderID = "1";
		char side = Side.SELL_SHORT_EXEMPT;
		BigDecimal quantity = new BigDecimal("2000");
		MSymbol symbol = new MSymbol("QWER");
		char timeInForce = TimeInForce.DAY;
		String account = "ACCT";
		Message message = msgFactory.newMarketOrder(orderID, side, quantity, symbol, timeInForce, account);
		history.addOutgoingMessage(message);

		EventList<MessageHolder> historyList = history.getAllMessagesList();
		assertEquals(1, historyList.size());
		assertEquals(OutgoingMessageHolder.class, historyList.get(0).getClass());
		OutgoingMessageHolder holder = (OutgoingMessageHolder) historyList.get(0);
		Message historyMessage = holder.getMessage();
		assertEquals(orderID.toString(), historyMessage.getString(ClOrdID.FIELD));
		assertEquals(""+side, historyMessage.getString(Side.FIELD));
		assertEquals(quantity, new BigDecimal(historyMessage.getString(OrderQty.FIELD)));
		assertEquals(symbol.getFullSymbol(), historyMessage.getString(Symbol.FIELD));
		assertEquals(""+timeInForce, historyMessage.getString(TimeInForce.FIELD));
		assertEquals(account.toString(), historyMessage.getString(Account.FIELD));
	}


	/*
	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.getLatestExecutionReports()'
	 */
	public void testGetLatestExecutionReports() throws FieldNotFound {
		long currentTime = System.currentTimeMillis();
		FIXMessageHistory history = getMessageHistory();
		Message order1 = msgFactory.newMarketOrder("1", Side.BUY, new BigDecimal(1000), new MSymbol("ASDF"), TimeInForce.FILL_OR_KILL, "1");
		Message executionReportForOrder1 = msgFactory.newExecutionReport("1001", "1", "2001", OrdStatus.NEW, Side.BUY, new BigDecimal(1000), new BigDecimal(789), null, null, BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("ASDF"), null);
		executionReportForOrder1.getHeader().setField(new SendingTime(new Date(currentTime - 10000)));
		Message order2 = msgFactory.newLimitOrder("3", Side.SELL, new BigDecimal(2000), new MSymbol("QWER"), new BigDecimal("12.3"), TimeInForce.DAY, "1");
		Message executionReportForOrder2 = msgFactory.newExecutionReport("1003", "3", "2003", OrdStatus.NEW, Side.SELL, new BigDecimal(2000), new BigDecimal(789), null, null, BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("QWER"), null);
		executionReportForOrder2.getHeader().setField(new SendingTime(new Date(currentTime - 8000)));
		Message secondExecutionReportForOrder1 = msgFactory.newExecutionReport("1001", "1", "2004", OrdStatus.PARTIALLY_FILLED, Side.BUY, new BigDecimal(1000), new BigDecimal(789), new BigDecimal(100), new BigDecimal("11.5"), new BigDecimal(100), new BigDecimal("11.5"), new MSymbol("ASDF"), null);
		secondExecutionReportForOrder1.getHeader().setField(new SendingTime(new Date(currentTime - 7000)));

		EventList<MessageHolder> historyList = history.getLatestExecutionReportsList();
		history.addOutgoingMessage(order1);
		assertEquals(0, historyList.size());
		history.addIncomingMessage(executionReportForOrder1);
		assertEquals(1, historyList.size());
		history.addOutgoingMessage(order2);
		history.addIncomingMessage(executionReportForOrder2);
		assertEquals(2, historyList.size());
		history.addIncomingMessage(secondExecutionReportForOrder1);
		assertEquals(2, historyList.size());
		
		Message historyExecutionReportForOrder1 = historyList.get(0).getMessage();
		Message historyExecutionReportForOrder2 = historyList.get(1).getMessage();

		assertEquals("1001", historyExecutionReportForOrder1.getString(OrderID.FIELD));
		assertEquals("2004", historyExecutionReportForOrder1.getString(ExecID.FIELD));
		assertEquals(order1.getString(ClOrdID.FIELD), historyExecutionReportForOrder1.getString(ClOrdID.FIELD));
		assertEquals(order1.getString(Side.FIELD), historyExecutionReportForOrder1.getString(Side.FIELD));
		assertEquals(order1.getString(OrderQty.FIELD), historyExecutionReportForOrder1.getString(OrderQty.FIELD));
		assertEquals(order1.getString(Symbol.FIELD), historyExecutionReportForOrder1.getString(Symbol.FIELD));

		assertEquals("1003", historyExecutionReportForOrder2.getString(OrderID.FIELD));
		assertEquals("2003", historyExecutionReportForOrder2.getString(ExecID.FIELD));
		assertEquals(order2.getString(ClOrdID.FIELD), historyExecutionReportForOrder2.getString(ClOrdID.FIELD));
		assertEquals(order2.getString(Side.FIELD), historyExecutionReportForOrder2.getString(Side.FIELD));
		assertEquals(order2.getString(OrderQty.FIELD), historyExecutionReportForOrder2.getString(OrderQty.FIELD));
		assertEquals(order2.getString(Symbol.FIELD), historyExecutionReportForOrder2.getString(Symbol.FIELD));
}

	/*
	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.addFIXMessageListener(IFIXMessageListener)'
	 */
	public void testAddFIXMessageListener() throws NoSuchFieldException, IllegalAccessException, FieldNotFound {
		FIXMessageHistory history = getMessageHistory();
		
		Message order1 = msgFactory.newMarketOrder("1", Side.BUY, new BigDecimal(1000), new MSymbol("ASDF"), TimeInForce.FILL_OR_KILL, "1");
		Message executionReportForOrder1 = msgFactory.newExecutionReport("1001", "1", "2001", OrdStatus.NEW, Side.BUY, new BigDecimal(1000), new BigDecimal(789), null, null, BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("ASDF"), null);

		ListEventListener<MessageHolder> fixMessageListener = new ListEventListener<MessageHolder>() {
			public int numIncomingMessages = 0;
			public int numOutgoingMessages = 0;

			@SuppressWarnings("unchecked")
			public void listChanged(ListEvent<MessageHolder> event) {
				if (event.hasNext())
				{
					event.next();
					if (event.getType() == ListEvent.INSERT){
						EventList<MessageHolder> source = (EventList<MessageHolder>) event.getSource();
						int index = event.getIndex();
						MessageHolder holder = source.get(index);
						if (holder instanceof IncomingMessageHolder) {
							IncomingMessageHolder incoming = (IncomingMessageHolder) holder;
							try {
								assertEquals("1001", incoming.getMessage().getString(OrderID.FIELD));
								numIncomingMessages++;
							} catch (FieldNotFound e) {
								fail(e.getMessage());
							}
						} else if (holder instanceof OutgoingMessageHolder) {
							OutgoingMessageHolder outgoing = (OutgoingMessageHolder) holder;
							try {
								assertEquals("1", outgoing.getMessage().getString(ClOrdID.FIELD));
								numOutgoingMessages++;
							} catch (FieldNotFound e) {
								fail(e.getMessage());
							}
						}
					}	
				}
			}
			
		};
		history.getAllMessagesList().addListEventListener(fixMessageListener);
		
		history.addOutgoingMessage(order1);
		history.addIncomingMessage(executionReportForOrder1);
		//just use the AccessViolator to get the fields out of the anon inner class
		AccessViolator violator = new AccessViolator(fixMessageListener.getClass());
		assertEquals(1,violator.getField("numIncomingMessages", fixMessageListener));
		assertEquals(1,violator.getField("numOutgoingMessages", fixMessageListener));
	}

	/*
	 * Test method for 'org.marketcetera.photon.model.FIXMessageHistory.removePortfolioListener(IFIXMessageListener)'
	 */
	public void testRemovePortfolioListener() throws NoSuchFieldException, IllegalAccessException, FieldNotFound {
		FIXMessageHistory history = getMessageHistory();
		
		Message order1 = msgFactory.newMarketOrder("1", Side.BUY, new BigDecimal(1000), new MSymbol("ASDF"), TimeInForce.FILL_OR_KILL, "1");
		Message executionReportForOrder1 = msgFactory.newExecutionReport("1", "1", "2001", OrdStatus.NEW, Side.BUY, new BigDecimal(1000), new BigDecimal(789), null, null, BigDecimal.ZERO, BigDecimal.ZERO, new MSymbol("ASDF"), null);

		ListEventListener<MessageHolder> fixMessageListener = new ListEventListener<MessageHolder>() {
			public int numIncomingMessages = 0;
			public int numOutgoingMessages = 0;

			public void listChanged(ListEvent<MessageHolder> event) {
				if (event.getType() == ListEvent.INSERT){
					Object source = event.getSource();
					if (source instanceof IncomingMessageHolder) {
						IncomingMessageHolder incoming = (IncomingMessageHolder) source;
						try {
							assertEquals("1001", incoming.getMessage().getString(OrderID.FIELD));
							numIncomingMessages++;
						} catch (FieldNotFound e) {
							fail(e.getMessage());
						}
					} else if (source instanceof OutgoingMessageHolder) {
						OutgoingMessageHolder outgoing = (OutgoingMessageHolder) source;
						try {
							assertEquals("1", outgoing.getMessage().getString(ClOrdID.FIELD));
							numOutgoingMessages++;
						} catch (FieldNotFound e) {
							fail(e.getMessage());
						}
					}

				}
			}
			
		};
		
		history.getAllMessagesList().addListEventListener(fixMessageListener);
		history.getAllMessagesList().removeListEventListener(fixMessageListener);
		
		history.addOutgoingMessage(order1);
		history.addIncomingMessage(executionReportForOrder1);
		//just use the AccessViolator to get the fields out of the anon inner class
		AccessViolator violator = new AccessViolator(fixMessageListener.getClass());
		assertEquals(0,violator.getField("numIncomingMessages", fixMessageListener));
		assertEquals(0,violator.getField("numOutgoingMessages", fixMessageListener));
	}
	
	
	public void testAveragePriceList() throws Exception {
		FIXMessageHistory messageHistory = getMessageHistory();
		String orderID1 = "1";
		String clOrderID1 = "1";
		String execID = "300";
		char execTransType = ExecTransType.STATUS;
		char execType = ExecType.PARTIAL_FILL;
		char ordStatus = OrdStatus.PARTIALLY_FILLED;
		char side = Side.SELL_SHORT;
		BigDecimal orderQty = new BigDecimal(1000);
		BigDecimal orderPrice = new BigDecimal(789);
		BigDecimal lastQty = new BigDecimal(100);
		BigDecimal lastPrice = new BigDecimal("12.3");
		BigDecimal leavesQty = new BigDecimal(900);
		BigDecimal cumQty = new BigDecimal("100");
		BigDecimal avgPrice = new BigDecimal("12.3");
		MSymbol symbol = new MSymbol("ASDF");

		Message message = msgFactory.newExecutionReport(orderID1, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
		messageHistory.addIncomingMessage(message);
		
		orderID1 = "1";
		clOrderID1 = "1";
		execID = "301";
		lastQty = new BigDecimal(900);
		lastPrice = new BigDecimal("12.4");
		cumQty = new BigDecimal(900);
		avgPrice = new BigDecimal("12.4");

		message = msgFactory.newExecutionReport(orderID1, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
		messageHistory.addIncomingMessage(message);
		
		EventList<MessageHolder> averagePriceList = messageHistory.getAveragePricesList();

		assertEquals(1, averagePriceList.size());
		
		IncomingMessageHolder holder = (IncomingMessageHolder) averagePriceList.get(0);
		Message returnedMessage = holder.getMessage();
		assertEquals(MsgType.EXECUTION_REPORT, returnedMessage.getHeader().getString(MsgType.FIELD));

		BigDecimal returnedAvgPrice = new BigDecimal(returnedMessage.getString(AvgPx.FIELD));
		assertTrue( new BigDecimal("1000").compareTo(new BigDecimal(returnedMessage.getString(CumQty.FIELD))) == 0);
		assertEquals( ((12.3*100)+(12.4*900))/1000, returnedAvgPrice.doubleValue(), .0001);
		
		
		orderID1 = "1";
		clOrderID1 = "1";
		execID = "302";
		lastQty = new BigDecimal(900);
		lastPrice = new BigDecimal("12.4");
		cumQty = new BigDecimal(900);
		avgPrice = new BigDecimal("12.4");
		side = Side.BUY;
		
		message = msgFactory.newExecutionReport(orderID1, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
		messageHistory.addIncomingMessage(message);

		assertEquals(2, messageHistory.getAveragePricesList().size());
		holder = (IncomingMessageHolder) averagePriceList.get(0);
		returnedMessage = holder.getMessage();
		assertEquals(MsgType.EXECUTION_REPORT, returnedMessage.getHeader().getString(MsgType.FIELD));

		returnedAvgPrice = new BigDecimal(returnedMessage.getString(AvgPx.FIELD));
		assertEquals(Side.BUY, returnedMessage.getChar(Side.FIELD));
		assertEquals( 12.4, returnedAvgPrice.doubleValue(), .0001);
		assertTrue( new BigDecimal("900").compareTo(new BigDecimal(returnedMessage.getString(CumQty.FIELD))) == 0);


		
		orderID1 = "1";
		clOrderID1 = "1";
		execID = "305";
		lastQty = new BigDecimal(900);
		lastPrice = new BigDecimal("12.4");
		cumQty = new BigDecimal(900);
		avgPrice = new BigDecimal("12.4");
		side = Side.SELL_SHORT;
		
		message = msgFactory.newExecutionReport(orderID1, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
		messageHistory.addIncomingMessage(message);

		assertEquals(2, messageHistory.getAveragePricesList().size());
		holder = (IncomingMessageHolder) averagePriceList.get(1);
		returnedMessage = holder.getMessage();
		assertEquals(MsgType.EXECUTION_REPORT, returnedMessage.getHeader().getString(MsgType.FIELD));

		returnedAvgPrice = new BigDecimal(returnedMessage.getString(AvgPx.FIELD));
		assertEquals(Side.SELL_SHORT, returnedMessage.getChar(Side.FIELD));
		assertEquals( ((12.3*100)+(12.4*900)+(12.4*(900)))/1900, returnedAvgPrice.doubleValue(), .0001);
		assertTrue( new BigDecimal("1900").compareTo(new BigDecimal(returnedMessage.getString(CumQty.FIELD))) == 0);

	}

	public void testExecutionReportOrder() throws FieldNotFound
	{
		String orderID1 = "1";
		String clOrderID1 = "1";
		String execID = "3";
		char execTransType = ExecTransType.STATUS;
		char execType = ExecType.PARTIAL_FILL;
		char ordStatus = OrdStatus.PARTIALLY_FILLED;
		char side = Side.SELL_SHORT;
		BigDecimal orderQty = new BigDecimal(1000);
		BigDecimal orderPrice = new BigDecimal(789);
		BigDecimal lastQty = new BigDecimal(100);
		BigDecimal lastPrice = new BigDecimal("12.3");
		BigDecimal leavesQty = new BigDecimal(900);
		BigDecimal cumQty = new BigDecimal(100);
		BigDecimal avgPrice = new BigDecimal("12.3");
		MSymbol symbol = new MSymbol("ASDF");

		SendingTime stField = new SendingTime(new Date(10000000));
		SendingTime stFieldLater = new SendingTime(new Date(10010000));
		
		Message message1 = msgFactory.newExecutionReport(null, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
		message1.getHeader().setField(stField);
		
		lastQty = new BigDecimal(200);
		Message message2 = msgFactory.newExecutionReport(orderID1, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
		message2.getHeader().setField(stField);

		lastQty = new BigDecimal(300);
		Message message3 = msgFactory.newExecutionReport(orderID1, clOrderID1, execID, ordStatus, side, orderQty, orderPrice, lastQty, lastPrice, cumQty, avgPrice, symbol, null);
		message3.getHeader().setField(stFieldLater);
		
		FIXMessageHistory history = getMessageHistory();
		history.addIncomingMessage(message1);
		history.addIncomingMessage(message2);
		assertEquals(new BigDecimal(200), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));
		
		// execution reports come in out of order, use the one that has the OrderID in it.
		history = getMessageHistory();
		history.addIncomingMessage(message2);
		history.addIncomingMessage(message1);
		assertEquals(new BigDecimal(200), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
		assertTrue(history.getLatestExecutionReport(clOrderID1.toString()).isSetField(OrderID.FIELD));

		history = getMessageHistory();
		history.addIncomingMessage(message1);
		history.addIncomingMessage(message2);
		history.addIncomingMessage(message3);
		assertEquals(new BigDecimal(300), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));
		
		history = getMessageHistory();
		history.addIncomingMessage(message3);
		history.addIncomingMessage(message2);
		history.addIncomingMessage(message1);
		assertEquals(new BigDecimal(300), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));

		history = getMessageHistory();
		history.addIncomingMessage(message3);
		history.addIncomingMessage(message1);
		history.addIncomingMessage(message2);
		assertEquals(new BigDecimal(300), new BigDecimal(history.getLatestExecutionReport(clOrderID1.toString()).getString(LastQty.FIELD)));
		assertEquals(orderID1.toString(), history.getLatestExecutionReport(clOrderID1.toString()).getString(OrderID.FIELD));
	}

	String [] messageStrings = {
		"8=FIX.4.29=14135=86=011=1171508063701-server02/127.0.0.114=017=ZZ-INTERNAL20=\u000031=032=038=1039=044=1054=155=R60=20070215-02:54:27150=0151=1010=237",
		"8=FIX.4.29=16235=D34=449=sender-2026-OMS52=20070215-02:54:27.29156=MRKTC-EXCH11=1171508063701-server02/127.0.0.121=338=1040=244=1054=155=R59=060=20070215-02:54:2910=058",
		"8=FIX.4.29=20635=834=449=MRKTC-EXCH52=20070215-02:54:29.43056=sender-2026-OMS6=011=1171508063701-server02/127.0.0.114=017=1203720=331=032=037=732438=1039=044=1054=155=R60=20070215-02:54:29150=0151=1010=196",
		"8=FIX.4.29=6835=034=549=MRKTC-EXCH52=20070215-02:54:59.62656=sender-2026-OMS10=078",
		"8=FIX.4.29=6835=034=549=sender-2026-OMS52=20070215-02:54:57.61456=MRKTC-EXCH10=073",
		"8=FIX.4.29=14335=86=011=1171508063702-server02/127.0.0.114=017=ZZ-INTERNAL20=\u000031=032=038=1239=044=10.154=155=R60=20070215-02:55:06150=0151=1210=081",
		"8=FIX.4.29=17235=D34=649=sender-2026-OMS50=asdf52=20070215-02:55:06.97456=MRKTC-EXCH11=1171508063702-server02/127.0.0.121=338=1240=244=10.154=155=R59=060=20070215-02:55:0910=229",
		"8=FIX.4.29=20835=834=649=MRKTC-EXCH52=20070215-02:55:09.08456=sender-2026-OMS6=011=1171508063702-server02/127.0.0.114=017=1203820=331=032=037=732538=1239=044=10.154=155=R60=20070215-02:55:09150=0151=1210=049",
		"8=FIX.4.29=13535=86=011=1171508063703-server02/127.0.0.114=017=ZZ-INTERNAL20=\u000031=032=038=2239=054=555=R60=20070215-02:55:27150=0151=2210=246",
		"8=FIX.4.29=15635=D34=749=sender-2026-OMS52=20070215-02:55:27.24656=MRKTC-EXCH11=1171508063703-server02/127.0.0.121=338=2240=154=555=R59=060=20070215-02:55:2910=067",
		"8=FIX.4.29=21535=834=749=MRKTC-EXCH52=20070215-02:55:29.37856=sender-2026-OMS6=10.111=1171508063702-server02/127.0.0.114=1217=1203920=331=10.132=1237=732538=1239=244=10.154=155=R60=20070215-02:55:29150=2151=010=146",
		"8=FIX.4.29=20835=834=849=MRKTC-EXCH52=20070215-02:55:29.37956=sender-2026-OMS6=10.111=1171508063703-server02/127.0.0.114=1217=1204020=331=10.132=1237=732638=2239=154=555=R60=20070215-02:55:29150=1151=1010=094",
		"8=FIX.4.29=20935=834=949=MRKTC-EXCH52=20070215-02:55:29.38056=sender-2026-OMS6=1011=1171508063701-server02/127.0.0.114=1017=1204120=331=1032=1037=732438=1039=244=1054=155=R60=20070215-02:55:29150=2151=010=100",
		"8=FIX.4.29=23735=834=1049=MRKTC-EXCH52=20070215-02:55:29.38156=sender-2026-OMS6=10.0545454545454545454545454545454511=1171508063703-server02/127.0.0.114=2217=1204220=331=1032=1037=732638=2239=254=555=R60=20070215-02:55:29150=2151=010=080",
		"8=FIX.4.29=17935=F34=849=sender-2026-OMS52=20070215-02:55:44.63056=MRKTC-EXCH11=1171508063704-server02/127.0.0.137=732441=1171508063701-server02/127.0.0.154=155=R60=20070215-02:54:2910=112",
		"8=FIX.4.29=20935=934=1149=MRKTC-EXCH52=20070215-02:55:46.85856=sender-2026-OMS11=1171508063704-server02/127.0.0.137=732439=841=1171508063701-server02/127.0.0.158=Unable to cancel non-existing orderID [7324].434=110=208",
		"8=FIX.4.29=6835=034=949=sender-2026-OMS52=20070215-02:56:15.18856=MRKTC-EXCH10=079",
		"8=FIX.4.29=6935=034=1249=MRKTC-EXCH52=20070215-02:56:17.29856=sender-2026-OMS10=126",
		"8=FIX.4.29=6935=034=1049=sender-2026-OMS52=20070215-02:56:45.19856=MRKTC-EXCH10=124",
		"8=FIX.4.29=6935=034=1349=MRKTC-EXCH52=20070215-02:56:47.32456=sender-2026-OMS10=120",
		"8=FIX.4.29=6935=034=1149=sender-2026-OMS52=20070215-02:57:15.19856=MRKTC-EXCH10=123",
		"8=FIX.4.29=6935=034=1449=MRKTC-EXCH52=20070215-02:57:17.62456=sender-2026-OMS10=122",
		"8=FIX.4.29=6935=034=1249=sender-2026-OMS52=20070215-02:57:46.18756=MRKTC-EXCH10=126",
		"8=FIX.4.29=6935=034=1549=MRKTC-EXCH52=20070215-02:57:48.34756=sender-2026-OMS10=129",
		"8=FIX.4.29=14135=86=011=1171508063705-server02/127.0.0.114=017=ZZ-INTERNAL20=\u000031=032=038=1039=044=1054=155=T60=20070215-02:57:58150=0151=1010=250",
		"8=FIX.4.29=16335=D34=1349=sender-2026-OMS52=20070215-02:57:58.79456=MRKTC-EXCH11=1171508063705-server02/127.0.0.121=338=1040=244=1054=155=T59=060=20070215-02:58:0010=121",
		"8=FIX.4.29=20735=834=1649=MRKTC-EXCH52=20070215-02:58:00.93056=sender-2026-OMS6=011=1171508063705-server02/127.0.0.114=017=1204320=331=032=037=732738=1039=044=1054=155=T60=20070215-02:58:00150=0151=1010=245",
		"8=FIX.4.29=18035=F34=1449=sender-2026-OMS52=20070215-02:58:07.26556=MRKTC-EXCH11=1171508063706-server02/127.0.0.137=732741=1171508063705-server02/127.0.0.154=155=T60=20070215-02:58:0010=159",
		"8=FIX.4.29=20635=834=1749=MRKTC-EXCH52=20070215-02:58:09.39356=sender-2026-OMS6=011=1171508063705-server02/127.0.0.114=017=1204420=331=032=037=732738=1039=444=1054=155=T60=20070215-02:58:09150=4151=010=226"
	};
	public void testStrandedOpenOrder() throws Exception {
		FIXMessageHistory history = new FIXMessageHistory(fixVersion.getMessageFactory());
		for (String aMessageString : messageStrings) {
			Message aMessage = new Message(aMessageString);
			String msgType = aMessage.getHeader().getString(MsgType.FIELD);
			if ("8".equals(msgType)){
				history.addIncomingMessage(aMessage);
			} else if ("D".equals(msgType)){
				history.addOutgoingMessage(aMessage);
			} else if ("F".equals(msgType)){
				history.addOutgoingMessage(aMessage);
			} else if ("9".equals(msgType)){
				history.addIncomingMessage(aMessage);
			} else if ("0".equals(msgType)){
				history.addIncomingMessage(aMessage);
			} else {
				fail();
			}
		}
		
		FilterList<MessageHolder> openOrdersList = history.getOpenOrdersList();
		assertEquals(0, openOrdersList.size());
	}
}
