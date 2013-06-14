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
package com.github.nlloyd.hornofmongo.action;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;

import com.github.nlloyd.hornofmongo.MongoContext;
import com.github.nlloyd.hornofmongo.MongoScope;

/**
 * @author nlloyd
 *
 */
public abstract class MongoAction implements ContextAction {
	
	protected MongoScope mongoScope;
	
	public MongoAction(MongoScope mongoScope) {
		this.mongoScope = mongoScope;
	}
	
	public MongoScope getScope() {
	    return mongoScope;
	}

    /**
     * Checks if the given {@link Context} is a {@link MongoContext}. If this is the
     * case then sets the member {@link MongoScope} as the reference execution scope
     * for the current action in the {@link MongoContext} (specifically for handling
     * situations where "new" keyword is not provided with a constructor resulting in
     * Rhino engine not properly setting parent scope and prototype for a Scriptable 
     * instance).
     * 
     * @see org.mozilla.javascript.ContextAction#run(org.mozilla.javascript.Context)
     */
    @Override
    public Object run(Context cx) {
        if(cx instanceof MongoContext)
            ((MongoContext)cx).updateExecutingMongoScope(mongoScope);
        return doRun(cx);
    }

    /**
     * The real {@link MongoAction} execution method.
     * 
     * @param cx
     * @return
     */
    protected abstract Object doRun(Context cx);

	
}
