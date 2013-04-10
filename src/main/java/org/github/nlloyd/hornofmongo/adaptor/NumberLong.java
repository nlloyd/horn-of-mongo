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
package org.github.nlloyd.hornofmongo.adaptor;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

/**
 * @author nlloyd
 *
 */
public class NumberLong extends ScriptableMongoObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7412902144340924262L;
	private long realLong = 0;

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
	
	@JSConstructor
	public NumberLong() {
		put("floatApprox", this, realLong);
	}
	
	@JSConstructor
	public NumberLong(Object obj) {
		String str = Context.toString(obj);
		realLong = Long.valueOf(str);
		put("floatApprox", this, realLong);
	}
	
	@JSFunction
	public long valueOf() {
		return realLong;
	}
	
	@JSFunction
	public long toNumber() {
		return realLong;
	}
	
	@JSFunction
	public String toString() {
		return "NumberLong(" + realLong + ")";
	}

}
