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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * Extension and hacky mangling of the {@link Context} type which adds a
 * reference field to the {@link MongoScope} being executed in the same thread.
 * The {@link MongoScope} instance is set by the {@link MongoContextFactory}.
 * When the
 * {@link MongoContextFactory#call(org.mozilla.javascript.ContextAction)} method
 * is executed.
 * 
 * @author nlloyd
 * 
 */
public class MongoContext extends Context {

    private MongoScope executingMongoScope;

    protected MongoContext(ContextFactory factory) {
        super(factory);
    }

    /**
     * @return the executingMongoScope
     */
    public MongoScope getExecutingMongoScope() {
        return executingMongoScope;
    }

    /**
     * Updates the referenced executing {@link MongoScope} if and only if
     * the new {@link MongoScope} is not null.
     * 
     * @param executingMongoScope
     *            the executingMongoScope to set
     */
    public void updateExecutingMongoScope(MongoScope executingMongoScope) {
        if(executingMongoScope != null)
            this.executingMongoScope = executingMongoScope;
    }

}
