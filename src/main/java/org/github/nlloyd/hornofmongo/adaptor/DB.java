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

import org.apache.commons.lang3.StringUtils;
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;

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
	
	protected Mongo mongo;
	protected String name;
	
	public DB() {}
	
	@JSConstructor
	public DB(Mongo mongo) {
		super();
		this.mongo = mongo;
		this.name = "test";
		put("_mongo", this, this.mongo);
		put("_name", this, this.name);
	}
	
	@JSConstructor
	public DB(Mongo mongo, String name) {
		super();
		this.mongo = mongo;
		this.name = name;
		if(StringUtils.isBlank(name))
			this.name = "test";
		put("_mongo", this, this.mongo);
		put("_name", this, this.name);
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
		if((property == ScriptableObject.NOT_FOUND)
				&& this.equals(start)
				&& !isSpecialName(name)
				&& !ScriptableObject.hasProperty(this, name)) {
			property = MongoRuntime.call(new NewInstanceAction("DBCollection", new Object[]{
	    			mongo, 
	    			this, 
	    			Context.toString(name), 
	    			Context.toString(this.name + "." + name)
			}));
			this.put(name, this, property);
		}
		return property;
	}
	
	
    private boolean isSpecialName(final String name) {
    	boolean isSpecial = false;
    	if(StringUtils.isNotEmpty(name)
    			&& (name.startsWith("_")
					|| "tojson".equalsIgnoreCase(name)
					|| "toString".equals(name))) {
    		isSpecial = true;
    	}
    	
    	return isSpecial;
    }

}
