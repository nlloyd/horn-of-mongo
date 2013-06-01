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
package com.github.nlloyd.hornofmongo.adaptor;

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
public class NumberInt extends ScriptableMongoObject {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7412902144340924262L;
    private int realInt = 0;

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public NumberInt() {
        super();
    }

    @JSConstructor
    public NumberInt(final Object obj) {
        super();
        if(obj instanceof Number) {
            realInt = ((Number)obj).intValue();
        } else if(!(obj instanceof Undefined)) {
            String str = Context.toString(obj);
            realInt = Integer.valueOf(str);
        }
        put("floatApprox", this, realInt);
    }

    @JSFunction
    public long valueOf() {
        return realInt;
    }

    @JSFunction
    public long toNumber() {
        return realInt;
    }

    @JSFunction
    public String toString() {
        return "NumberInt(" + realInt + ")";
    }

    public void setRealInt(Integer realInt) {
        this.realInt = realInt;
        put("floatApprox", this, realInt);
    }
    
    public int getRealInt() {
        return this.realInt;
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#equivalentValues(java.lang.Object)
     */
    @Override
    protected Object equivalentValues(Object value) {
        return ScriptRuntime.eq(this.realInt, value) ? Boolean.TRUE : Scriptable.NOT_FOUND;
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getDefaultValue(java.lang.Class)
     */
    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if(String.class.equals(typeHint))
            return this.toString();
        else
            return this.realInt;
    }

}
