/**
 *  Copyright (c) 2013 Nick Lloyd
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.github.nlloyd.hornofmongo.util;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.github.nlloyd.hornofmongo.util.LoggingPrintHandler.Level;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nlloyd
 * 
 */
public class LoggingPrintHandlerTest {

    private static final Object[] testArgs = new Object[] { "ohai", "kbye" };
    private static final String expectedMessage = "ohai kbye";

    private Logger mockLogger;

    @Before
    public void setUp() {
        mockLogger = createStrictMock(Logger.class);
    }

    @Test
    public void testConstructors() {
        LoggingPrintHandler testHandler = new LoggingPrintHandler();
        Logger testLogger = testHandler.printLogger;

        assertEquals("org.github.nlloyd.hornofmongo", testLogger.getName());
        assertEquals(Level.INFO, testHandler.printLogLevel);

        // ***

        testHandler = new LoggingPrintHandler(Level.TRACE);
        testLogger = testHandler.printLogger;

        assertEquals("org.github.nlloyd.hornofmongo", testLogger.getName());
        assertEquals(Level.TRACE, testHandler.printLogLevel);

        // ***

        testHandler = new LoggingPrintHandler(LoggerFactory.getLogger(this
                .getClass()));
        testLogger = testHandler.printLogger;

        assertEquals(this.getClass().getName(), testLogger.getName());
        assertEquals(Level.INFO, testHandler.printLogLevel);

        // ***

        testHandler = new LoggingPrintHandler(LoggerFactory.getLogger(this
                .getClass()), Level.TRACE);
        testLogger = testHandler.printLogger;

        assertEquals(this.getClass().getName(), testLogger.getName());
        assertEquals(Level.TRACE, testHandler.printLogLevel);
    }

    @Test
    public void testTraceLoggingLevelHandling() {
        LoggingPrintHandler testHandler = new LoggingPrintHandler(mockLogger,
                Level.TRACE);

        mockLogger.trace(expectedMessage);
        expectLastCall().once();
        replay(mockLogger);

        testHandler.doPrint(null, null, testArgs);

        verify(mockLogger);
    }

    @Test
    public void testDebugLoggingLevelHandling() {
        LoggingPrintHandler testHandler = new LoggingPrintHandler(mockLogger,
                Level.DEBUG);

        mockLogger.debug(expectedMessage);
        expectLastCall().once();
        replay(mockLogger);

        testHandler.doPrint(null, null, testArgs);

        verify(mockLogger);
    }

    @Test
    public void testInfoLoggingLevelHandling() {
        LoggingPrintHandler testHandler = new LoggingPrintHandler(mockLogger,
                Level.INFO);

        mockLogger.info(expectedMessage);
        expectLastCall().once();
        replay(mockLogger);

        testHandler.doPrint(null, null, testArgs);

        verify(mockLogger);
    }

    @Test
    public void testWarnLoggingLevelHandling() {
        LoggingPrintHandler testHandler = new LoggingPrintHandler(mockLogger,
                Level.WARN);

        mockLogger.warn(expectedMessage);
        expectLastCall().once();
        replay(mockLogger);

        testHandler.doPrint(null, null, testArgs);

        verify(mockLogger);
    }

    @Test
    public void testErrorLoggingLevelHandling() {
        LoggingPrintHandler testHandler = new LoggingPrintHandler(mockLogger,
                Level.ERROR);

        mockLogger.error(expectedMessage);
        expectLastCall().once();
        replay(mockLogger);

        testHandler.doPrint(null, null, testArgs);

        verify(mockLogger);
    }

}
