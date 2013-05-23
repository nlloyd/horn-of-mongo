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
package org.github.nlloyd.hornofmongo;

import static org.junit.Assert.assertEquals;

import org.github.nlloyd.hornofmongo.action.MongoScriptAction;
import org.github.nlloyd.hornofmongo.util.PrintHandler;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author nlloyd
 * 
 */
public class MongoScopeWithPrintHandlerTest {

    @Test
    public void test() {
        MongoScope testScope = MongoRuntime.createMongoScope();
        CapturingPrintHandler capturingPrintHandler = new CapturingPrintHandler();
        testScope.setPrintHandler(capturingPrintHandler);

        final String expected1 = "ohai!";
        final String expected2 = "kbye!";

        MongoRuntime.call(new MongoScriptAction(testScope, String.format(
                "print('%s')", expected1)));
        assertEquals(expected1, capturingPrintHandler.lastMessage);

        MongoRuntime.call(new MongoScriptAction(testScope, String.format(
                "print('%s')", expected2)));
        assertEquals(expected2, capturingPrintHandler.lastMessage);

        MongoRuntime.call(new MongoScriptAction(testScope, String.format(
                "print('%s','%s')", expected1, expected2)));
        assertEquals(expected1 + " " + expected2,
                capturingPrintHandler.lastMessage);
    }

    private static final class CapturingPrintHandler implements PrintHandler {
        public String lastMessage;

        @Override
        public void doPrint(Context cx, Scriptable s, Object[] args) {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                if (sb.length() > 0)
                    sb.append(' ');
                sb.append(Context.toString(arg));
            }
            lastMessage = sb.toString();
        }

    }

}
