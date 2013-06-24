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

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSConstructor;

import com.github.nlloyd.hornofmongo.MongoRuntime;
import com.github.nlloyd.hornofmongo.action.CallMethodAction;

public class DBQuery extends ScriptableMongoObject {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6198995485514617952L;

    public DBQuery() {
        super();
    }

    @JSConstructor
    public DBQuery(Mongo mongo, DB db, Object collection, String ns,
            Object query, Object fields, Integer limit, Integer skip,
            Integer batchSize, Integer options) {
        super();
        put("_mongo", this, mongo);
        put("_db", this, db);
        put("_collection", this, collection);
        put("_ns", this, ns);

        put("_query", this, (query == null ? new NativeObject() : query));
        put("_fields", this, fields);
        put("_limit", this, (limit == null ? 0 : limit));
        put("_skip", this, (skip == null ? 0 : skip));
        put("_batchSize", this, (batchSize == null ? 0 : batchSize));
        put("_options", this, (options == null ? 0 : options));

        put("_cursor", this, null);
        put("_numReturned", this, 0);
        put("_special", this, false);
        put("_prettyShell", this, false);
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#get(int,
     *      org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(int index, Scriptable start) {
        Object got = null;
        if (this.equals(start)) {
            got = MongoRuntime.call(new CallMethodAction(mongoScope, this,
                    "arrayAccess", new Object[] { index }));
        }
        return got;
    }

}
