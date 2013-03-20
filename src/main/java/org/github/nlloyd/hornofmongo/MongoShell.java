/**
 *  Copyright (c) 2012 Nick Lloyd
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

import java.net.UnknownHostException;

import org.github.nlloyd.hornofmongo.action.MongoAction;
import org.github.nlloyd.hornofmongo.action.MongoScriptAction;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.github.nlloyd.hornofmongo.util.BSONizer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author nlloyd
 * 
 */
public class MongoShell {

    /**
     * @param args
     * @throws UnknownHostException
     */
    public static void main(String[] args) throws UnknownHostException {
        // Mongo mongo = new Mongo();
        // DB db = mongo.getDB("test");
        // DBCollection c = db.getCollection("test");
        // DBObject one = c.findOne();

        // System.err.println("------------------");

        Object result = MongoRuntime.call(new MongoScriptAction(
                "var junk = {'a' : {'0' : '1'}};"));
        MongoRuntime
                .call(new MongoScriptAction(
                        "print(junk.a[0]); for(var key in junk.a) { print(key + ' ' + junk.a[key]);}"));

        // "var db = connect('shell_test',null,null); print('connected to: ' + db._name); "
        // +
        // "db.test.findOne({" +
        // "'a': /abc.*def/im" +
        // // "db.test.insert({" +
        // // "'a': 1, " +
        // // "'today': new Date(), " +
        // // "'isotoday': new ISODate(), " +
        // // "'array': [1,2,'3']" +
        // "});",
        result = ScriptableObject.getProperty(MongoRuntime.getMongoScope(),
                "junk");

        Object jsObj = MongoRuntime.call(new MongoAction() {

            @Override
            public Object run(Context cx) {
                // {"a" : {"0" : "1"}}
                NativeObject jsObj = (NativeObject) cx.newObject(mongoScope);
                NativeObject nested = (NativeObject) cx.newObject(mongoScope);
                nested.defineProperty("0", "1", ScriptableObject.EMPTY);
                // nested.defineProperty(propertyName, value, attributes)
                jsObj.defineProperty("a", nested, ScriptableObject.EMPTY);
                ScriptRuntime.setObjectElem(nested, "0", "1", cx);
                ScriptRuntime.setObjectElem(jsObj, "a", nested, cx);
                return jsObj;
            }
        });

        // NativeObject jsObj = (NativeObject)MongoRuntime.call(new
        // NewInstanceAction());
        // NativeObject nested = (NativeObject)MongoRuntime.call(new
        // NewInstanceAction());
        // nested.def("0", nested, "1");
        // jsObj.put("a", jsObj, nested);
        ScriptableObject.putProperty(MongoRuntime.getMongoScope(), "junk2",
                jsObj);

        MongoRuntime
                .call(new MongoScriptAction(
                        "print(junk2.a[0]); for(var key in junk2.a) { print(key + ' ' + junk2.a[key]);}"));

        System.out.println(BSONizer.convertJStoBSON(result));
    }

}
