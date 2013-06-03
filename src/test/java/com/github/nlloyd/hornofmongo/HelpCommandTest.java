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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Test;
import org.mozilla.javascript.Context;

import com.github.nlloyd.hornofmongo.action.MongoScriptAction;
import com.github.nlloyd.hornofmongo.util.PrintHandler;

/**
 * @author nlloyd
 * 
 */
public class HelpCommandTest {

    @Test
    public void testHelpNoArgs() {
        MongoScope testScope = MongoRuntime.createMongoScope();
        PrintHandler mockPrintHandler = createStrictMock(PrintHandler.class);
        testScope.setPrintHandler(mockPrintHandler);

        mockPrintHandler.doPrint(isA(Context.class), eq(testScope),
                isA(Object[].class));
        expectLastCall().times(1, 100);
        replay(mockPrintHandler);

        MongoRuntime.call(new MongoScriptAction(testScope, "help"));

        verify(mockPrintHandler);
    }

    @Test
    public void testHelpAdmin() {
        MongoScope testScope = MongoRuntime.createMongoScope();
        PrintHandler mockPrintHandler = createStrictMock(PrintHandler.class);
        testScope.setPrintHandler(mockPrintHandler);

        mockPrintHandler.doPrint(isA(Context.class), eq(testScope),
                isA(Object[].class));
        expectLastCall().times(1, 100);
        replay(mockPrintHandler);

        MongoRuntime.call(new MongoScriptAction(testScope, "help admin"));

        verify(mockPrintHandler);
    }

    @Test
    public void testHelpAdminBunchOfWhitespace() {
        MongoScope testScope = MongoRuntime.createMongoScope();
        PrintHandler mockPrintHandler = createStrictMock(PrintHandler.class);
        testScope.setPrintHandler(mockPrintHandler);

        mockPrintHandler.doPrint(isA(Context.class), eq(testScope),
                isA(Object[].class));
        expectLastCall().times(1, 100);
        replay(mockPrintHandler);

        MongoRuntime.call(new MongoScriptAction(testScope,
                "        help admin    "));

        verify(mockPrintHandler);
    }

}
