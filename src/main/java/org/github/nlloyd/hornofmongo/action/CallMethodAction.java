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
package org.github.nlloyd.hornofmongo.action;

import org.github.nlloyd.hornofmongo.MongoScope;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author nlloyd
 *
 */
public class CallMethodAction extends MongoAction {
	
	private Scriptable object;
	private String method;
	private Object[] methodArgs;
	
	/**
	 * Constructor for a CallMethodAction that will produce a basic native JS object instance.
	 */
	public CallMethodAction(Scriptable object, String method) {
		this(object, method, new Object[]{});
	}

	/**
	 * Constructor for a CallMethodAction that will produce a basic native JS object instance.
	 */
	public CallMethodAction(MongoScope mongoScope, Scriptable object, String method) {
		this(mongoScope, object, method, new Object[]{});
	}
	
	/**
	 * Constructor for a CallMethodAction that will produce a basic native JS object instance.
	 */
	public CallMethodAction(Scriptable object, String method, Object[] args) {
		super();
		this.object = object;
		this.method = method;
		this.methodArgs = args;
	}

	/**
	 * Constructor for a CallMethodAction that will produce a basic native JS object instance.
	 */
	public CallMethodAction(MongoScope mongoScope, Scriptable obj, String method, Object[] args) {
		super(mongoScope);
		this.object = obj;
		this.method = method;
		this.methodArgs = args;
	}
	
	/**
	 * @see org.mozilla.javascript.ContextAction#run(org.mozilla.javascript.Context)
	 */
	@Override
	public Object run(Context cx) {
		return ScriptableObject.callMethod(cx, object, method, methodArgs);
	}

}
