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
package com.github.nlloyd.hornofmongo.bson;

import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DefaultDBDecoder;

/**
 * @author nlloyd
 *
 */
public class HornOfMongoBSONDecoder extends DefaultDBDecoder {

    static class HornOfMongoBSONDecoderFactory implements DBDecoderFactory {
        @Override
        public DBDecoder create( ){
            return new HornOfMongoBSONDecoder( );
        }

        @Override
        public String toString() {
            return "HornOfMongoBSONDecoder.HornOfMongoBSONDecoderFactory";
        }
    }

    public static DBDecoderFactory FACTORY = new HornOfMongoBSONDecoderFactory();

    public HornOfMongoBSONDecoder(){}
    
    @Override
    public DBCallback getDBCallback(DBCollection collection) {
        // brand new callback every time
        return new HornOfMongoDBCallback(collection);
    }

    @Override
    public String toString() {
        return "HornOfMongoBSONDecoder";
    }

}
