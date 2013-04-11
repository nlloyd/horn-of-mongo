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
    
    @JSConstructor
    public Timestamp() {
        super();
        put("t", this, Long.valueOf(0));
        put("i", this, Long.valueOf(0));
    }
    
    @JSConstructor
    public Timestamp(Object t, Object i) {
        super();
        long largestVal = ((2039-1970)*365*24*60*60);   // seconds between 1970=2038
        if(Long.parseLong(t.toString()) > largestVal)
            throw new IllegalArgumentException("The first argument must be in seconds;"
                  + t.toString() + " is too large (max " + largestVal + ")");
        put("t", this, t);
        put("i", this, i);
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        return this.getClass().getSimpleName();
    }

}
