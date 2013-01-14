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
package org.github.nlloyd.hornofmongo.adaptor;

import org.github.nlloyd.hornofmongo.MongoAction;
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.exception.MongoScopeException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.mongodb.DBCollection;

/**
 * DB JavaScriptable implementation to support dynamic collection creation on
 * property access.
 * 
 * This class is associated with the MongoDB JavaScript API as opposed to the 
 * MongoDB Java Driver.
 * 
 * @author nlloyd
 *
 */
public class DB extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3314929237218125656L;
	
	private Mongo mongo;
	private String name;
	
	@JSConstructor
	public DB(Mongo mongo, String name) {
		super();
		this.mongo = mongo;
		this.name = name;
	}

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Returns either the JavaScript property if it exists or a new {@link DBCollection} instance
	 * with the provided name.
	 * 
	 * @see org.mozilla.javascript.ScriptableObject#get(java.lang.String, org.mozilla.javascript.Scriptable)
	 */
	@Override
	public Object get(String name, Scriptable start) {
		Object property = super.get(name, start);
		if(property == ScriptableObject.NOT_FOUND) {
			// TODO how to handle isSpecialName(name) call from sm_db.cpp... or is it even necessary?
			// create JS API DBCollection instance as opposed to Java driver equivalent to support JS API implementation
			property = MongoRuntime.call(new DBCollectionConstructor(mongo, this, name));
			this.put(name, this, property);
		}
		return property;
	}

	/**
	 * @return the _mongo
	 */
	@JSGetter
	public Mongo _mongo() {
		return mongo;
	}

	/**
	 * @param _mongo the _mongo to set
	 */
	@JSSetter
	public void _mongo(Mongo mongo) {
		this.mongo = mongo;
	}

	/**
	 * @return the name
	 */
	@JSGetter
	public String _name() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	@JSSetter
	public void _name(String name) {
		this.name = name;
		
	}
	
	private class DBCollectionConstructor extends MongoAction {

		Mongo mongo;
		DB db;
		String name;
		String fullName;
		
		public DBCollectionConstructor(Mongo mongo, DB db, String name) {
			this.mongo = mongo;
			this.db = db;
			this.fullName = db._name() + "." + name;
		}

		public Object run(Context cx) {
			Object dbc = mongoScope.get("DBCollection", mongoScope);
			if((dbc != null) && (dbc instanceof Function)) {
				Function dbcc = (Function)dbc;
		        Scriptable newCollection = dbcc.construct(cx, mongoScope, 
		        		new Object[]{mongo, db, name, fullName});
		        return newCollection;
			} else {
				throw new MongoScopeException("MongoDB JS API function named DB not found!  " +
						"MongoScope is either not being used or is not initialized.");
			}
		}
		
	}

}
