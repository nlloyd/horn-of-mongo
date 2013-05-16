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
package org.github.nlloyd.hornofmongo.bson;

import org.mozilla.javascript.Undefined;

import com.mongodb.DBEncoder;
import com.mongodb.DBEncoderFactory;
import com.mongodb.DefaultDBEncoder;

/**
 * Extends DefaultDBEncoder adding support for encoding types that can't be
 * serialized by the BasicBSONEncoder as Undefined types.
 * 
 * @author nlloyd
 * 
 */
public class HornOfMongoBSONEncoder extends DefaultDBEncoder {

    /**
     * @see org.bson.BasicBSONEncoder#_putObjectField(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    protected void _putObjectField(String name, Object val) {
        try {
            super._putObjectField(name, val);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("can't serialize ")) {
                if (!(val instanceof Undefined))
                    System.err.println(e.getMessage()
                            + ", encoding as Undefined");
                putUndefined(name);
            }
        }
    }

    public static class HornOfMongoBSONEncoderFactory implements
            DBEncoderFactory {
        @Override
        public DBEncoder create() {
            return new HornOfMongoBSONEncoder();
        }

        @Override
        public String toString() {
            return "HornOfMongoBSONEncoder.HornOfMongoBSONEncoderFactory";
        }

    }

    public static HornOfMongoBSONEncoderFactory FACTORY = new HornOfMongoBSONEncoderFactory();

    public HornOfMongoBSONEncoder() {
    }

    @Override
    public String toString() {
        return "HornOfMongoBSONEncoder";
    }
}
