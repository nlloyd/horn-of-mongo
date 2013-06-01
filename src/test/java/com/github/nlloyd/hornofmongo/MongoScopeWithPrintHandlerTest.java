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
package com.github.nlloyd.hornofmongo;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.Capture;
import org.junit.Test;
import org.mozilla.javascript.Context;

import com.github.nlloyd.hornofmongo.action.MongoScriptAction;
import com.github.nlloyd.hornofmongo.util.PrintHandler;

/**
 * @author nlloyd
 * 
 */
public class MongoScopeWithPrintHandlerTest {

    @Test
    public void test() {
        MongoScope testScope = MongoRuntime.createMongoScope();
        PrintHandler mockPrintHandler = createStrictMock(PrintHandler.class);
        testScope.setPrintHandler(mockPrintHandler);

        final String expected1 = "ohai!";
        final String expected2 = "kbye!";

        Capture<Object[]> args1Capture = new Capture<Object[]>();
        Capture<Object[]> args2Capture = new Capture<Object[]>();

        mockPrintHandler.doPrint(isA(Context.class), eq(testScope),
                capture(args1Capture));
        expectLastCall().once();
        mockPrintHandler.doPrint(isA(Context.class), eq(testScope),
                capture(args2Capture));
        expectLastCall().once();
        replay(mockPrintHandler);

        MongoRuntime.call(new MongoScriptAction(testScope, String.format(
                "print('%s')", expected1)));
        MongoRuntime.call(new MongoScriptAction(testScope, String.format(
                "print('%s','%s')", expected1, expected2)));

        verify(mockPrintHandler);
        
        Object[] args1 = args1Capture.getValue();
        Object[] args2 = args2Capture.getValue();
        
        assertEquals(1, args1.length);
        assertEquals(2, args2.length);
        assertEquals(expected1, args1[0]);
        assertEquals(expected1, args2[0]);
        assertEquals(expected2, args2[1]);
    }

}
