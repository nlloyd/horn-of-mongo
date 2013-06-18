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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;

import com.github.nlloyd.hornofmongo.action.MongoScriptAction;

/**
 * @author nlloyd
 * 
 */
public class MongoToStringTest {

    private MongoScope testScope;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testScope = MongoRuntime.createMongoScope();
    }

    @Test
    public void testNoHostToString() {
        Object result = MongoRuntime.call(new MongoScriptAction(testScope,
                "new Mongo().toString();"));
        String resultStr = Context.toString(result);
        assertEquals("connection to 127.0.0.1", resultStr);
    }

    @Test
    public void testSingleHostToString() {
        Object result = MongoRuntime.call(new MongoScriptAction(testScope,
                "new Mongo('127.0.0.1:27017').toString();"));
        String resultStr = Context.toString(result);
        assertEquals("connection to 127.0.0.1:27017", resultStr);
    }

    @Test
    public void testMultiHostToString() {
        Object result = MongoRuntime.call(new MongoScriptAction(testScope,
                "new Mongo('127.0.0.1:27017,127.0.0.1:27018').toString();"));
        String resultStr = Context.toString(result);
        assertEquals("connection to 127.0.0.1:27017,127.0.0.1:27018", resultStr);
    }

}
