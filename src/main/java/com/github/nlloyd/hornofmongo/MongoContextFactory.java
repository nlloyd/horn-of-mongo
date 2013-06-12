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
package com.github.nlloyd.hornofmongo;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * @author nlloyd
 * 
 */
public class MongoContextFactory extends ContextFactory {
    private int languageVersion = Context.VERSION_1_8;

    @Override
    protected boolean hasFeature(Context cx, int featureIndex)
    {
        switch (featureIndex) {
          case Context.FEATURE_STRICT_VARS:
          case Context.FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER:
          case Context.FEATURE_LOCATION_INFORMATION_IN_ERROR:
          case Context.FEATURE_DYNAMIC_SCOPE:
            return true;
        }
        return super.hasFeature(cx, featureIndex);
    }

    @Override
    protected void onContextCreated(Context cx)
    {
        cx.setLanguageVersion(languageVersion);
        super.onContextCreated(cx);
    }

    /**
     * Override {@link #hasFeature(Context, int)}
     * 
     * Enables {@link Context#FEATURE_LOCATION_INFORMATION_IN_ERROR}
     * and {@link Context#FEATURE_DYNAMIC_SCOPE}.
     * 
     * Only {@link Context#FEATURE_DYNAMIC_SCOPE} is important, for DBRef types.
     */
//    @Override
//    public boolean hasFeature(Context cx, int featureIndex) {
//        switch (featureIndex) {
//        case FEATURE_LOCATION_INFORMATION_IN_ERROR:
//        case FEATURE_DYNAMIC_SCOPE:
//        case VERSION_1_8:
//            return true;
//        }
//        return super.hasFeature(cx, featureIndex);
//    }

}
