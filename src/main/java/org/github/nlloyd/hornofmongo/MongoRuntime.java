/**
 *  Copyright (c) 2012 Nick Lloyd
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

import java.lang.reflect.InvocationTargetException;

import org.github.nlloyd.hornofmongo.action.MongoAction;
import org.github.nlloyd.hornofmongo.exception.MongoScopeException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * Runtime class for the Horn of Mongo client library that initializes the
 * global {@link ContextFactory} and provides convenience methods for executing
 * JS scripts with mongodb JSAPI code.
 * 
 * @author nlloyd
 * 
 */
public class MongoRuntime {

    /**
     * Creates a newly initialized {@link MongoScope} instance. This will use
     * {@link MongoRuntime#call(MongoAction)} to initialize the
     * {@link MongoScope} instance, possibly resulting in the global
     * {@link MongoContextFactory} being set.
     * 
     * @return
     */
    public static final MongoScope createMongoScope() {
        MongoScope mongoScope = (MongoScope) call(new MongoAction(null) {

            @Override
            public Object run(Context cx) {
                try {
                    return new MongoScope(cx);
                } catch (IllegalAccessException e) {
                    throw new MongoScopeException(
                            "caught when attempting to create a new MongoScope",
                            e);
                } catch (InstantiationException e) {
                    throw new MongoScopeException(
                            "caught when attempting to create a new MongoScope",
                            e);
                } catch (InvocationTargetException e) {
                    throw new MongoScopeException(
                            "caught when attempting to create a new MongoScope",
                            e);
                }
            }

        });
        
        return mongoScope;
    }

    /**
     * Convenience method to call the {@link MongoAction} using the global
     * {@link ContextFactory}. The global {@link ContextFactory} has not
     * explicitly been set yet then this method will set an instance of
     * {@link MongoContextFactory} as the global {@link ContextFactory}.
     * 
     * @param mongoAction
     * @return
     */
    public static final Object call(MongoAction mongoAction) {
        if (!ContextFactory.hasExplicitGlobal())
            ContextFactory.initGlobal(new MongoContextFactory());
        return ContextFactory.getGlobal().call(mongoAction);
    }

}
