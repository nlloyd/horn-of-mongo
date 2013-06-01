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

import org.apache.commons.lang3.math.NumberUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;

/**
 * @author nlloyd
 * 
 */
public class Timestamp extends ScriptableMongoObject {

    /**
     * 
     */
    private static final long serialVersionUID = 4063412321929267268L;
    
    /**
     * seconds between 1970 and 2038
     */
    public static final long largestVal = ((2039l - 1970l) * 365l * 24l * 60l * 60l);
    
    private long t;
    private long i;

    public Timestamp() {
        super();
        t = 0l;
        i = 0l;
        put("t", this, t);
        put("i", this, i);
    }

    @JSConstructor
    public Timestamp(Object t, Object i) {
        super();
        if (t instanceof Undefined) {
            this.t = 0l;
            this.i = 0l;
        } else {
            Number tNum = NumberUtils.createNumber(Context.toString(t));
            Number iNum = NumberUtils.createNumber(Context.toString(i));
            if (tNum.longValue() > largestVal)
                throw new IllegalArgumentException(
                        "The first argument must be in seconds;" + t.toString()
                                + " is too large (max " + largestVal + ")");
            this.t = tNum.longValue();
            this.i = iNum.longValue();
        }
        put("t", this, this.t);
        put("i", this, this.i);
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public long getT() {
        return t;
    }

    public long getI() {
        return i;
    }
    
}
