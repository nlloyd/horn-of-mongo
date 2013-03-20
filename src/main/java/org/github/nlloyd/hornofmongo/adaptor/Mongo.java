package org.github.nlloyd.hornofmongo.adaptor;

import java.net.UnknownHostException;

import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.MongoScope;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.github.nlloyd.hornofmongo.util.BSONizer;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * JavaScript host Mongo object that acts as an adaptor between the JavaScript
 * Mongo API and the {@link com.mongodb.Mongo} Java driver class.
 * 
 * @author nlloyd
 * 
 */
public class Mongo extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6810309240609504412L;

	protected com.mongodb.Mongo innerMongo;

	protected String host;

	/**
	 * Reference to the owning {@link MongoScope} to check for certain
	 * scope-level behavior flags.
	 */
	protected MongoScope mongoScope;

	@JSConstructor
	public Mongo() throws UnknownHostException {
		super();
		initMongo("localhost");
	}

	@JSConstructor
	public Mongo(final Object host) throws UnknownHostException {
		super();
		if (host instanceof Undefined)
			initMongo("localhost");
		else
			initMongo(host.toString());
	}

	private void initMongo(String host) throws UnknownHostException {
		this.host = host;
		this.innerMongo = new com.mongodb.Mongo(this.host);
	}

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	// --- Mongo JavaScript function implementation ---

	@JSFunction
	public Object find(final String ns, final Object query,
			final Object fields, int limit, int skip, int batchSize, int options) {
		Object result = null;

		Object rawQuery = BSONizer.convertJStoBSON(query);
		Object rawFields = BSONizer.convertJStoBSON(fields);
		DBObject bsonQuery = null;
		DBObject bsonFields = null;
		if (rawQuery instanceof DBObject)
			bsonQuery = (DBObject) rawQuery;
		if (rawFields instanceof DBObject)
			bsonFields = (DBObject) rawFields;

//        System.out.printf("find(%s, %s, %s, %d, %d, %d, %d)\n", ns, bsonQuery,
//                bsonFields, limit, skip, batchSize, options);
		
		com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
		String collectionName = ns.substring(ns.indexOf('.') + 1);
		if ("$cmd".equals(collectionName)) {
			try {
				CommandResult cmdResult = db.command(bsonQuery, options);
				handlePostCommandActions(db, bsonQuery);
				Object jsCmdResult = BSONizer.convertBSONtoJS(cmdResult);
				result = MongoRuntime.call(new NewInstanceAction(
						"InternalCursor", new Object[] { jsCmdResult }));
			} catch (MongoException me) {
				handleMongoException(me);
			}
		} else {
			DBCollection collection = db.getCollection(collectionName);
			DBCursor cursor = collection.find(bsonQuery, bsonFields).skip(skip)
					.limit(limit).batchSize(batchSize).addOption(options);
			InternalCursor jsCursor = (InternalCursor) MongoRuntime
					.call(new NewInstanceAction("InternalCursor",
							new Object[] { cursor }));
			result = jsCursor;
		}

		return result;
	}

	@JSFunction
	public void insert(final String ns, Object obj) {
		Object rawObj = BSONizer.convertJStoBSON(obj);
		DBObject bsonObj = null;
        if (rawObj instanceof DBObject)
            bsonObj = (DBObject) rawObj;

//        System.out.printf("insert(%s, %s)\n", ns, bsonObj);

		try {
			// unfortunately the Java driver does not expose the _allow_dot
			// argument
			// in insert calls so we need to translate system.indexes inserts
			// into
			// index creation calls through the java driver
			if (ns.endsWith("system.indexes")) {
				com.mongodb.DB db = innerMongo.getDB(ns.substring(0,
						ns.indexOf('.')));
				String indexNS = bsonObj.get("ns").toString();
				DBCollection collection = db.getCollection(indexNS.substring(ns
						.indexOf('.') + 1));
				DBObject keys = (DBObject) bsonObj.get("key");
				DBObject indexOpts = new BasicDBObject();
				if (bsonObj.containsField("name"))
					indexOpts.put("name", bsonObj.get("name"));
				if (bsonObj.containsField("unique"))
					indexOpts.put("unique", bsonObj.get("unique"));
				if (bsonObj.containsField("dropDups"))
					indexOpts.put("dropDups", bsonObj.get("dropDups"));
				collection.ensureIndex(keys, indexOpts);
			} else {
				com.mongodb.DB db = innerMongo.getDB(ns.substring(0,
						ns.indexOf('.')));
				DBCollection collection = db.getCollection(ns.substring(ns
						.indexOf('.') + 1));
				collection.insert(bsonObj);
			}
		} catch (MongoException me) {
			handleMongoException(me);
		}
	}

	@JSFunction
	public void remove(final String ns, Object pattern) {
		Object rawPattern = BSONizer.convertJStoBSON(pattern);
		DBObject bsonPattern = null;
		if (rawPattern instanceof DBObject)
			bsonPattern = (DBObject) rawPattern;
        
//		System.out.printf("remove(%s, %s)\n", ns, bsonPattern);
        
		com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
		DBCollection collection = db
				.getCollection(ns.substring(ns.indexOf('.') + 1));

		try {
			collection.remove(bsonPattern);
		} catch (MongoException me) {
			handleMongoException(me);
		}
	}

	@JSFunction
	public void update(final String ns, Object query, Object obj, boolean upsert) {
		Object rawQuery = BSONizer.convertJStoBSON(query);
		Object rawObj = BSONizer.convertJStoBSON(obj);
		DBObject bsonQuery = null;
		DBObject bsonObj = null;
		if (rawQuery instanceof DBObject)
			bsonQuery = (DBObject) rawQuery;
		if (rawObj instanceof DBObject)
			bsonObj = (DBObject) rawObj;
        
//		System.out.printf("update(%s, %s, %s, %b)\n", ns, bsonQuery, bsonObj, upsert);
        
		com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
		DBCollection collection = db.getCollection(ns.substring(ns
				.lastIndexOf('.') + 1));

		try {
			collection.update(bsonQuery, bsonObj, upsert, false);
		} catch (MongoException me) {
			handleMongoException(me);
		}
	}

	private static enum ResetIndexCacheCommand {
		drop, deleteIndexes;
	}

	private void handlePostCommandActions(DB db, DBObject bsonQuery) {
		for (ResetIndexCacheCommand command : ResetIndexCacheCommand.values()) {
			String commandName = command.toString();
			if (bsonQuery.containsField(commandName))
				db.getCollection(bsonQuery.get(commandName).toString())
						.resetIndexCache();
		}
	}
    
    private void handleMongoException(MongoException me) {
        if(mongoScope == null)
            mongoScope = (MongoScope) ScriptableObject.getTopLevelScope(this);
        mongoScope.handleMongoException(me);
    }

}
