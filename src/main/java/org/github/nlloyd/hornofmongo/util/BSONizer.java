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
package org.github.nlloyd.hornofmongo.util;

import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.regexp.NativeRegExp;

import com.mongodb.BasicDBObject;

/**
 * @author nlloyd
 *
 */
public class BSONizer {

	public static Object convertJStoBSON( Scriptable jsObject )
	{
		Object bsonResult = null;
		if(jsObject instanceof NativeArray) {
		    NativeArray array = (NativeArray)jsObject;
		    for(Object entry : array) {
		        System.out.println(entry.toString());
		    }
        } else if(jsObject instanceof NativeRegExp) {
            System.out.println(jsObject.toString());
            Object out = Context.jsToJava(jsObject, String.class);
            System.err.println(out);
		} else if(jsObject instanceof ScriptableObject) {
		    System.out.println(jsObject.getClassName());
//			BasicDBObject bson = new BasicDBObject();
//			bsonResult = bson;

			Object[] ids = ((ScriptableObject)jsObject).getAllIds();
			for( Object id : ids )
			{
				String key = id.toString();
				Object value = ScriptableObject.getProperty(jsObject,key);
				System.out.printf("obj has: %s -> %s of type %s\n", key, value.toString(), value.getClass().getSimpleName());
				if(value instanceof Scriptable)
				    convertJStoBSON((Scriptable)value);
//				bson.put( key, value );
			}
		} else {
			// TODO throw an exception?
			bsonResult = null;
		}
		
		return bsonResult;
	}

	
}
