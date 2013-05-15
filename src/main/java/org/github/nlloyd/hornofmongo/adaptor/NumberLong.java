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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.lang3.math.NumberUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
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

    public static final long JS_MAX_LONG = Long.valueOf(9007199254740992l);
    public static final long JS_MIN_LONG = Long.valueOf(-9007199254740992l);
	
	private long realLong = 0;
	
	public NumberLong() {
        super();
	}
	
	@JSConstructor
	public NumberLong(final Object obj) {
        super();
        if(obj instanceof Number) {
            realLong = ((Number)obj).longValue();
        } else if(!(obj instanceof Undefined)) {
            String str = Context.toString(obj);
            Number num = NumberUtils.createNumber(str);
            if((num instanceof BigInteger) || (num instanceof BigDecimal))
                throw new NumberFormatException(String.format("Error: could not convert %s to NumberLong, too big.", str));
            realLong = num.longValue();
        }
        put("floatApprox", this, realLong);
	}

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
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
	@Override
	public String toString() {
	    String toString;
	    // reproduction of what v8_db.cpp has
	    if((realLong <= Integer.MIN_VALUE) || (Integer.MAX_VALUE <= realLong))
	        toString = "NumberLong(\"" + realLong + "\")";
	    else
	        toString = "NumberLong(" + realLong + ")";
		return toString;
	}
	
    public void setRealLong(Long realLong) {
        this.realLong = realLong;
        put("floatApprox", this, realLong);
    }
    
    public long getRealLong() {
        return this.realLong;
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#equivalentValues(java.lang.Object)
     */
    @Override
    protected Object equivalentValues(Object value) {
        return ScriptRuntime.eq(this.realLong, value) ? Boolean.TRUE : Scriptable.NOT_FOUND;
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getDefaultValue(java.lang.Class)
     */
    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if(String.class.equals(typeHint))
            return this.toString();
        else
            return this.realLong;
    }

}
