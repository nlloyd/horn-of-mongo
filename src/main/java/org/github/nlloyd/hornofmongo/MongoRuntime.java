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

import org.apache.commons.lang3.StringUtils;
import org.github.nlloyd.hornofmongo.scope.MongoScope;
import org.mozilla.javascript.Context;

/**
 * Runtime class for MongoDB that wraps a {@link Context}
 * and {@link MongoScope} instance for script execution.
 * 
 * Currently single threaded due to the MongoScope implementation.
 * 
 * @author nlloyd
 *
 */
public class MongoRuntime {

	private MongoScope scope;
	private Context context;
	
	public MongoRuntime() throws IllegalAccessException, InstantiationException, InvocationTargetException {
		context = Context.enter();
		scope = new MongoScope(context);
	}
	
	public Object exec(final String script) {
		return exec(script, null);
	}
	
	public Object exec(final String script, String scriptName) {
		if(StringUtils.isEmpty(scriptName)) {
			scriptName = "(anon)";
		}
		Object result = context.evaluateString(scope, script, scriptName, 1, null);
		return result;
	}
}
