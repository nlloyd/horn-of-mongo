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
package org.github.nlloyd.hornofmongo;

import static org.mozilla.javascript.Context.FEATURE_WARNING_AS_ERROR;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * @author nlloyd
 * 
 */
public class MongoContextFactory extends ContextFactory {

    /**
     * Override {@link #hasFeature(Context, int)}
     * 
     * Enables {@link Context#FEATURE_WARNING_AS_ERROR}.
     */
    @Override
    public boolean hasFeature(Context cx, int featureIndex) {
        switch (featureIndex) {
        case FEATURE_WARNING_AS_ERROR:
            return true;
        }
        return super.hasFeature(cx, featureIndex);
    }

}