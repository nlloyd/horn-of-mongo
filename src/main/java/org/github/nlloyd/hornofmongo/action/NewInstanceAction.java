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

import org.apache.commons.lang3.StringUtils;
import org.github.nlloyd.hornofmongo.MongoScope;
import org.mozilla.javascript.Context;

/**
 * Helper action to create a new JS object instance in the mongo scope.
 * 
 * @author nlloyd
 *
 */
public class NewInstanceAction extends MongoAction {
	
	// for array construction
	private int arrayLength = -1;
	
	// for calling a specific constructor
	private String constructor = null;
	private Object[] constructorArgs = null;
	
	/**
	 * Constructor for a NewInstanceAction that will produce a basic native JS object instance.
	 */
	public NewInstanceAction(MongoScope mongoScope) {
		super(mongoScope);
	}

	/**
	 * Constructor for a NewInstanceAction that will produce a JS Array instance.
	 */
	public NewInstanceAction(MongoScope mongoScope, int arrayLength) {
		super(mongoScope);
		this.arrayLength = arrayLength;
	}
	
	
	/**
	 * Constructor for a NewInstanceAction that will produce a JS instance using the
	 * named JS constructor.
	 */
	public NewInstanceAction(MongoScope mongoScope, String constructor) {
		super(mongoScope);
		this.constructor = constructor;
	}
	
	/**
	 * Constructor for a NewInstanceAction that will produce a JS instance using the
	 * named JS constructor and the provided arguments.
	 */
	public NewInstanceAction(MongoScope mongoScope, String constructor, Object[] args) {
		super(mongoScope);
		this.constructor = constructor;
		this.constructorArgs = args;
	}

	/**
	 * @see org.mozilla.javascript.ContextAction#run(org.mozilla.javascript.Context)
	 */
	@Override
	public Object run(Context cx) {
		Object newInstance = null;
		if(arrayLength > -1) {
			newInstance = cx.newArray(mongoScope, arrayLength);
		} else if(StringUtils.isNotBlank(constructor)) {
			if(constructorArgs == null) {
				newInstance = cx.newObject(mongoScope, constructor);
			} else {
				newInstance = cx.newObject(mongoScope, constructor, constructorArgs);
			}
		} else {
			newInstance = cx.newObject(mongoScope);
		}
		return newInstance;
	}

}
