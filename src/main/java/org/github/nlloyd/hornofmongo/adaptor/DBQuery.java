package org.github.nlloyd.hornofmongo.adaptor;

import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.action.CallMethodAction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;


public class DBQuery extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6198995485514617952L;
	
	public DBQuery() {}
	
	@JSConstructor
	public DBQuery(Mongo mongo, DB db, Object collection, String ns, Object query, Object fields, Integer limit, Integer skip, Integer batchSize, Integer options) {
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
	 * @see org.mozilla.javascript.ScriptableObject#get(int, org.mozilla.javascript.Scriptable)
	 */
	@Override
	public Object get(int index, Scriptable start) {
		Object got = null;
		if(this.equals(start)) {
			got = MongoRuntime.call(new CallMethodAction(this, "arrayAccess", new Object[]{index}));
		}
		return got;
	}

}
