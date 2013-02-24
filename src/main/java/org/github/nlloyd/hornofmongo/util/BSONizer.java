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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.BSONObject;
import org.bson.types.Symbol;
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.github.nlloyd.hornofmongo.adaptor.ObjectId;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.regexp.NativeRegExp;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBObject;

/**
 * @author nlloyd
 *
 */
public class BSONizer {
	
	public static Object convertJStoBSON(Object jsObject) {
		Object bsonObject = null;
		if(jsObject instanceof NativeArray) {
		    NativeArray jsArray = (NativeArray)jsObject;
			List<Object> bsonArray = new ArrayList<Object>(Long.valueOf(jsArray.getLength()).intValue());
		    for(Object jsEntry : jsArray) {
		    	bsonArray.add(convertJStoBSON(jsEntry));
		    }
		    bsonObject = bsonArray;
        } else if(jsObject instanceof NativeRegExp) {
        	DBObject bsonRegex = new BasicDBObject();
            Object source = ScriptableObject.getProperty((Scriptable)jsObject, "source");
            String fullRegex = (String)Context.jsToJava(jsObject, String.class);
            String options = fullRegex.substring(fullRegex.lastIndexOf("/") + 1);
            
            bsonRegex.put("$regex", source);
            bsonRegex.put("$options", options);
            
            bsonObject = bsonRegex;
        } else if(jsObject instanceof ObjectId) {
        	bsonObject = ((ObjectId)jsObject).getWrappedObjectId();
		} else if(jsObject instanceof ScriptableObject) {
			BasicDBObject bson = new BasicDBObject();
			bsonObject = bson;

			Object[] ids = ((ScriptableObject)jsObject).getAllIds();
			for( Object id : ids )
			{
				String key = id.toString();
				Object value = ScriptableObject.getProperty((Scriptable)jsObject,key);
				System.out.printf("obj has: %s -> %s of type %s\n", key, value.toString(), value.getClass().getSimpleName());
			    value = convertJStoBSON(value);
				bson.put( key, value );
			}
		} else if(jsObject instanceof Undefined) {
			bsonObject = null;
		} else {
			// TODO throw an exception?
			bsonObject = jsObject;
		}
		
		return bsonObject;
	}

	public static Object convertBSONtoJS(Object bsonObject) {
		Object jsObject = null;
		if(bsonObject instanceof List<?>) {
			List<?> bsonList = (List<?>)bsonObject;
			Scriptable jsArray = (Scriptable)MongoRuntime.call(new NewInstanceAction(bsonList.size()));

			int index = 0;
			for(Object bsonEntry : bsonList)
				ScriptableObject.putProperty(jsArray, index, convertBSONtoJS(bsonEntry));
			
			jsObject = jsArray;
		} else if(bsonObject instanceof BSONObject) {
			Scriptable jsObj = (Scriptable)MongoRuntime.call(new NewInstanceAction());
			BSONObject  bsonObj = (BSONObject)bsonObject;
			
			for(String key : bsonObj.keySet()) {
				Object value = convertBSONtoJS(bsonObj.get(key));
				ScriptableObject.putProperty(jsObj, key, value);
			}
			jsObject = jsObj;
		} else if(bsonObject instanceof Symbol) {
			jsObject = ((Symbol)bsonObject).getSymbol();
		} else if(bsonObject instanceof Date) {
			jsObject = MongoRuntime.call(new NewInstanceAction("Date", new Object[]{((Date)bsonObject).getTime()}));
		} else if(bsonObject instanceof Pattern) {
			Pattern regex = (Pattern)bsonObject;
			String source = regex.pattern();
			String options = Bytes.regexFlags(regex.flags());
			jsObject = MongoRuntime.call(new NewInstanceAction("RegExp", new Object[]{source, options}));
		} else if(bsonObject instanceof org.bson.types.ObjectId) {
			jsObject = new ObjectId((org.bson.types.ObjectId)bsonObject);
		} else {
			jsObject = bsonObject;
		}
		
		return jsObject;
	}
}
