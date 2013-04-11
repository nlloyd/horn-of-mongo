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
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

/**
 * @author nlloyd
 *
 */
public class ObjectId extends ScriptableMongoObject {
	
	private org.bson.types.ObjectId realObjectId;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5594412197023274036L;
	
	@JSConstructor
	public ObjectId() {
		super();
		realObjectId = new org.bson.types.ObjectId();
	}
	
	@JSConstructor
	public ObjectId(Object obj) {
		super();
		if(obj instanceof Undefined)
			realObjectId = new org.bson.types.ObjectId();
		else {
			String str = Context.toString(obj);
			realObjectId = new org.bson.types.ObjectId(str);
		}
	}
	
	public ObjectId(org.bson.types.ObjectId oid) {
		realObjectId = oid;
	}

	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
	
	@JSSetter
	public void setStr(Object obj) {
		this.realObjectId = new org.bson.types.ObjectId(Context.toString(obj));
	}
	
	@JSGetter
	public Object getStr() {
		return Context.toString(this.realObjectId.toString());
	}
	
	public org.bson.types.ObjectId getRealObjectId() {
		return realObjectId;
	}
	
	public void setRealObjectId(org.bson.types.ObjectId oid) {
	    realObjectId = oid;
	}

}
