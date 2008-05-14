package org.marketcetera.util.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.marketcetera.util.test.TestCaseBase;

import static org.junit.Assert.*;

public class CloseableRegistryTest
	extends TestCaseBase
{
    private static final String TEST_CATEGORY=
        CloseableRegistry.class.getName();
    private static final String TEST_MESSAGE=
        "Closing failed";

    private static final class OrderedCloseable
        implements Closeable
    {
        private static int sSequence;

        private int mSequence;

        public static void resetStaticSequence()
        {
            sSequence=0;
        }

        public int getSequence()
        {
            return mSequence;
        }

        public void close()
        {
            mSequence=++sSequence;
        }
    }

    private static final class ThrowingCloseable
        implements Closeable
    {
        public void close()
            throws IOException
        {
            throw new IOException();
        }
    }


    @Before
    public void setupCloseableRegistryTest()
    {
        OrderedCloseable.resetStaticSequence();
        Messages.PROVIDER.setLocale(Locale.US);
        setLevel(TEST_CATEGORY,Level.ERROR);
    }


    @Test
    public void orderedClosing()
    {
        CloseableRegistry r=new CloseableRegistry();
        OrderedCloseable t1=new OrderedCloseable();
        r.register(t1);
        OrderedCloseable t2=new OrderedCloseable();
        r.register(t2);
        r.close();
        assertEquals(1,t2.getSequence());
        assertEquals(2,t1.getSequence());
    }

    @Test
    public void exceptionsIgnored()
    {
        CloseableRegistry r=new CloseableRegistry();
        OrderedCloseable t1=new OrderedCloseable();
        r.register(t1);
        r.register(new ThrowingCloseable());
        OrderedCloseable t2=new OrderedCloseable();
        r.register(t2);
        r.register(new ThrowingCloseable());
        r.close();
        assertEquals(1,t2.getSequence());
        assertEquals(2,t1.getSequence());
        Iterator<LoggingEvent> events=getAppender().getEvents().iterator();
        assertEvent(events.next(),Level.ERROR,TEST_CATEGORY,TEST_MESSAGE);
        assertEvent(events.next(),Level.ERROR,TEST_CATEGORY,TEST_MESSAGE);
        assertFalse(events.hasNext());
    }
}
