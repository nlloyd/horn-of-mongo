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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * Runtime class for MongoDB that initializes and grants access to
 * the global MongoDB JavaScript scope.
 * 
 * @author nlloyd
 *
 */
public class MongoRuntime {

	private static MongoRuntime globalRuntime = new MongoRuntime();
	
	private Scriptable scope;
	private ContextFactory contextFactory = new ContextFactory();
	
	private MongoRuntime() {
		scope = (Scriptable)contextFactory.call(new ContextAction() {

			public Object run(Context cx) {
				Object scope = null;
				try {
					scope = new MongoScope(cx);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return scope;
			}
			
		});
	}
	
	public static Scriptable getMongoScope() {
		return globalRuntime.scope;
	}
	
	public static Object call(MongoAction action) {
		return globalRuntime.contextFactory.call(action);
	}
	
}
