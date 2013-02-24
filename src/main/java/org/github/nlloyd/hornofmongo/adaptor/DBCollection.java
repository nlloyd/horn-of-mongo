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
package org.github.nlloyd.hornofmongo.adaptor;

import org.apache.commons.lang3.StringUtils;
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;

/**
 * @author nlloyd
 * 
 */
public class DBCollection extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -82836958685458934L;

	protected Mongo mongo;
	protected DB db;
	protected String shortName;
	protected String fullName;

	public DBCollection() {
	}

	@JSConstructor
	public DBCollection(Mongo mongo, DB db, String shortName, String fullName) {
		this.mongo = null;
		this.db = db;
		this.shortName = shortName;
		this.fullName = fullName;
		put("_mongo", this, this.mongo);
		put("_db", this, this.db);
		put("_shortName", this, this.shortName);
		put("_fullName", this, this.fullName);
		ScriptableObject.callMethod(this, "verify", new Object[] {});
	}

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Returns either the JavaScript property if it exists or a new
	 * {@link DBCollection} instance with the provided name.
	 * 
	 * @see org.mozilla.javascript.ScriptableObject#get(java.lang.String,
	 *      org.mozilla.javascript.Scriptable)
	 */
	@Override
	public Object get(String name, Scriptable start) {
		Object property = super.get(name, start);
		if ((property == ScriptableObject.NOT_FOUND)
				&& this.equals(start)
				&& !isSpecialName(name)
				&& this.equals(ScriptableObject.getClassPrototype(
						MongoRuntime.getMongoScope(), this.getClassName()))
				&& !ScriptableObject.hasProperty(this, name)) {
			property = MongoRuntime.call(new NewInstanceAction("DBCollection",
					new Object[] { mongo, this, Context.toString(name),
							Context.toString(this.shortName + "." + name) }));
			this.put(name, this, property);
		}
		return property;
	}

	public static boolean isSpecialName(final String name) {
		boolean isSpecial = false;
		if (StringUtils.isNotEmpty(name)
				&& (name.startsWith("_") || "tojson".equalsIgnoreCase(name) || "toString"
						.equals(name))) {
			isSpecial = true;
		}

		return isSpecial;
	}

}
