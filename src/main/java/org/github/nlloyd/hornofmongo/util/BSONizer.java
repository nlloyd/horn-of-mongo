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
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bson.BSONObject;
import org.bson.types.Code;
import org.bson.types.Symbol;
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.MongoScope;
import org.github.nlloyd.hornofmongo.action.MongoAction;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.github.nlloyd.hornofmongo.adaptor.MaxKey;
import org.github.nlloyd.hornofmongo.adaptor.MinKey;
import org.github.nlloyd.hornofmongo.adaptor.NumberInt;
import org.github.nlloyd.hornofmongo.adaptor.NumberLong;
import org.github.nlloyd.hornofmongo.adaptor.ObjectId;
import org.github.nlloyd.hornofmongo.adaptor.ScriptableMongoObject;
import org.github.nlloyd.hornofmongo.adaptor.Timestamp;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.regexp.NativeRegExp;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;

/**
 * @author nlloyd
 * 
 */
public class BSONizer {

    public static Object convertJStoBSON(Object jsObject) {
        Object bsonObject = null;
        if (jsObject instanceof NativeArray) {
            NativeArray jsArray = (NativeArray) jsObject;
            List<Object> bsonArray = new ArrayList<Object>(Long.valueOf(
                    jsArray.getLength()).intValue());
            for (Object jsEntry : jsArray) {
                bsonArray.add(convertJStoBSON(jsEntry));
            }
            bsonObject = bsonArray;
        } else if (jsObject instanceof NativeRegExp) {
            Object source = ScriptableObject.getProperty((Scriptable) jsObject,
                    "source");
            String fullRegex = (String) Context
                    .jsToJava(jsObject, String.class);
            String options = fullRegex
                    .substring(fullRegex.lastIndexOf("/") + 1);

            bsonObject = Pattern.compile(source.toString(),
                    Bytes.regexFlags(options));
            ;
        } else if (jsObject instanceof NativeObject) {
            BasicDBObject bson = new BasicDBObject();
            bsonObject = bson;

            NativeObject rawJsObject = (NativeObject) jsObject;
            for (Entry<Object, Object> jsEntry : rawJsObject.entrySet()) {
                // System.out.printf("obj has: %s -> %s\n", jsEntry.getKey(),
                // jsEntry.getValue());
                bson.put(jsEntry.getKey().toString(),
                        convertJStoBSON(jsEntry.getValue()));
            }
        } else if (jsObject instanceof ScriptableMongoObject) {
            if (jsObject instanceof ObjectId) {
                bsonObject = ((ObjectId) jsObject).getRealObjectId();
            } else if (jsObject instanceof MinKey) {
                bsonObject = new org.bson.types.MinKey();
            } else if (jsObject instanceof MaxKey) {
                bsonObject = new org.bson.types.MaxKey();
            } else if (jsObject instanceof NumberInt) {
                bsonObject = ((NumberInt) jsObject).valueOf();
            } else if (jsObject instanceof NumberLong) {
                bsonObject = ((NumberLong) jsObject).valueOf();
            } else if (jsObject instanceof Timestamp) {
                // TODO ???
            }
        } else if (jsObject instanceof BaseFunction) {
            // comes from eval calls
            String decompiledCode = (String) MongoRuntime
                    .call(new JSDecompileAction((BaseFunction) jsObject));
            bsonObject = new Code(decompiledCode);
        } else if (jsObject instanceof ScriptableObject) {
            // we found a ScriptableObject that isn't any of the concrete
            // ScriptableObjects above...
            String jsClassName = ((ScriptableObject) jsObject).getClassName();
            if ("Date".equals(jsClassName)) {
                bsonObject = Context.jsToJava(jsObject, Date.class);
            } else {
                // TODO something better than this...
                System.err.println("bsonizer couldnt convert js class: "
                        + jsClassName);
                bsonObject = jsObject;
            }
        } else if (jsObject instanceof ConsString) {
            bsonObject = jsObject.toString();
        } else if (jsObject instanceof Undefined) {
            bsonObject = null;
        } else {
            bsonObject = jsObject;
        }

        return bsonObject;
    }

    public static Object convertBSONtoJS(MongoScope mongoScope,
            Object bsonObject) {
        Object jsObject = null;
        if (bsonObject instanceof List<?>) {
            List<?> bsonList = (List<?>) bsonObject;
            Scriptable jsArray = (Scriptable) MongoRuntime
                    .call(new NewInstanceAction(mongoScope, bsonList.size()));

            int index = 0;
            for (Object bsonEntry : bsonList) {
                ScriptableObject.putProperty(jsArray, index,
                        convertBSONtoJS(mongoScope, bsonEntry));
                index++;
            }

            jsObject = jsArray;
        } else if (bsonObject instanceof BSONObject) {
            Scriptable jsObj = (Scriptable) MongoRuntime
                    .call(new NewInstanceAction(mongoScope));
            BSONObject bsonObj = (BSONObject) bsonObject;

            for (String key : bsonObj.keySet()) {
                Object value = convertBSONtoJS(mongoScope, bsonObj.get(key));
                MongoRuntime.call(new JSPopulatePropertyAction(jsObj, key,
                        value));
            }
            jsObject = jsObj;
        } else if (bsonObject instanceof Symbol) {
            jsObject = ((Symbol) bsonObject).getSymbol();
        } else if (bsonObject instanceof Date) {
            jsObject = MongoRuntime.call(new NewInstanceAction(mongoScope,
                    "Date", new Object[] { ((Date) bsonObject).getTime() }));
        } else if (bsonObject instanceof Pattern) {
            Pattern regex = (Pattern) bsonObject;
            String source = regex.pattern();
            String options = Bytes.regexFlags(regex.flags());
            jsObject = MongoRuntime.call(new NewInstanceAction(mongoScope,
                    "RegExp", new Object[] { source, options }));
        } else if (bsonObject instanceof org.bson.types.ObjectId) {
            jsObject = MongoRuntime.call(new NewInstanceAction(mongoScope,
                    "ObjectId"));
            ((ObjectId) jsObject)
                    .setRealObjectId((org.bson.types.ObjectId) bsonObject);
        } else if (bsonObject instanceof org.bson.types.MinKey) {
            jsObject = MongoRuntime.call(new NewInstanceAction(mongoScope,
                    "MinKey"));
        } else if (bsonObject instanceof org.bson.types.MaxKey) {
            jsObject = MongoRuntime.call(new NewInstanceAction(mongoScope,
                    "MaxKey"));
        } else if (bsonObject instanceof Long) {
            jsObject = MongoRuntime.call(new NewInstanceAction(mongoScope,
                    "NumberLong"));
            ((NumberLong) jsObject).setRealLong((Long) bsonObject);
        } else if (bsonObject instanceof Code) {
            jsObject = ((Code)bsonObject).getCode();
        } else {
            jsObject = bsonObject;
        }

        return jsObject;
    }

    private static class JSPopulatePropertyAction extends MongoAction {

        private Scriptable obj;
        private Object key;
        private Object value;

        public JSPopulatePropertyAction(Scriptable obj, Object key, Object value) {
            super(null);
            this.obj = obj;
            this.key = key;
            this.value = value;
        }

        @Override
        public Object run(Context cx) {
            return ScriptRuntime.setObjectElem(obj, key, value, cx);
        }

    }

    private static class JSDecompileAction extends MongoAction {

        private BaseFunction toDecompile;

        public JSDecompileAction(BaseFunction toDecompile) {
            super(null);
            this.toDecompile = toDecompile;
        }

        @Override
        public Object run(Context cx) {
            return cx.decompileFunction(toDecompile, 2);
        }

    }
}
