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
package org.github.nlloyd.hornofmongo.adaptor;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

/**
 * @author nlloyd
 *
 */
public class ObjectId extends ScriptableObject {
	
	private org.bson.types.ObjectId realObjectId;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5594412197023274036L;

	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
	
	@JSConstructor
	public ObjectId() {
		realObjectId = new org.bson.types.ObjectId();
		put("str", this, realObjectId.toString());
	}
	
	@JSConstructor
	public ObjectId(Object obj) {
		String str = Context.toString(obj);
		realObjectId = new org.bson.types.ObjectId(str);
		put("str", this, realObjectId.toString());
	}
	
	@JSFunction
	public String toString() {
		return realObjectId.toString();
	}
	
	public org.bson.types.ObjectId getWrappedObjectId() {
		return realObjectId;
	}

}
