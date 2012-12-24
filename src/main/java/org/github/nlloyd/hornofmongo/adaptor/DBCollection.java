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

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

/**
 * @author nlloyd
 *
 */
public class DBCollection extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5725152911815441729L;
	
	private Mongo mongo;
	private DB db;
	private String shortName;
	private String name;
	
	public DBCollection(Mongo mongo, DB db, String shortName, String name) {
		this.mongo = mongo;
		this.db = db;
		this.shortName = shortName;
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
	 * @return the mongo
	 */
	@JSGetter
	public Mongo _mongo() {
		return mongo;
	}

	/**
	 * @param mongo the mongo to set
	 */
	@JSSetter
	public void _mongo(Mongo mongo) {
		this.mongo = mongo;
	}

	/**
	 * @return the db
	 */
	@JSGetter
	public DB _db() {
		return db;
	}

	/**
	 * @param db the db to set
	 */
	@JSSetter
	public void _db(DB db) {
		this.db = db;
	}

	/**
	 * @return the shortName
	 */
	@JSGetter
	public String _shortName() {
		return shortName;
	}

	/**
	 * @param shortName the shortName to set
	 */
	 @JSSetter
	public void _shortName(String shortName) {
		this.shortName = shortName;
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

}
