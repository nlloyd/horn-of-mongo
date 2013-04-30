package org.github.nlloyd.hornofmongo.adaptor;

import static org.github.nlloyd.hornofmongo.bson.HornOfMongoBSONEncoder.FACTORY;

import java.net.UnknownHostException;
import java.util.List;

import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.MongoScope;
import org.github.nlloyd.hornofmongo.action.NewInstanceAction;
import org.github.nlloyd.hornofmongo.util.BSONizer;
import org.mozilla.javascript.Scriptable;
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
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

/**
 * JavaScript host Mongo object that acts as an adaptor between the JavaScript
 * Mongo API and the {@link com.mongodb.Mongo} Java driver class.
 * 
 * @author nlloyd
 * 
 */
public class Mongo extends ScriptableMongoObject {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6810309240609504412L;

    protected com.mongodb.Mongo innerMongo;

    protected String host;

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
        MongoClientOptions clientOptions = MongoClientOptions.builder()
                .dbEncoderFactory(FACTORY).build();
        this.innerMongo = new com.mongodb.MongoClient(this.host, clientOptions);
    }

    /**
     * Extracts the useMongoShellWriteConcern flag from the owning
     * {@link MongoScope} when the parent heirarchy is set.
     * 
     * @see org.mozilla.javascript.ScriptableObject#setParentScope(org.mozilla.javascript.Scriptable)
     */
    @Override
    public void setParentScope(Scriptable m) {
        super.setParentScope(m);
        if (mongoScope.useMongoShellWriteConcern())
            innerMongo.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
        mongoScope.addOpenedConnection(innerMongo);
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public com.mongodb.Mongo getJavaClient() {
        return innerMongo;
    }

    // --- Mongo JavaScript function implementation ---

    @JSFunction
    public Object find(final String ns, final Object query,
            final Object fields, Integer limit, Integer skip,
            Integer batchSize, Integer options) {
        Object result = null;

        Object rawQuery = BSONizer.convertJStoBSON(query);
        Object rawFields = BSONizer.convertJStoBSON(fields);
        DBObject bsonQuery = null;
        DBObject bsonFields = null;
        if (rawQuery instanceof DBObject)
            bsonQuery = (DBObject) rawQuery;
        if (rawFields instanceof DBObject)
            bsonFields = (DBObject) rawFields;

        // System.out.printf("find(%s, %s, %s, %d, %d, %d, %d)\n", ns,
        // bsonQuery, bsonFields, limit, skip, batchSize,
        // options);

        com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
        String collectionName = ns.substring(ns.indexOf('.') + 1);
        if ("$cmd".equals(collectionName)) {
            try {
                CommandResult cmdResult = db.command(bsonQuery, options, FACTORY.create());
                handlePostCommandActions(db, bsonQuery);
                Object jsCmdResult = BSONizer.convertBSONtoJS(mongoScope,
                        cmdResult);
                result = MongoRuntime.call(new NewInstanceAction(mongoScope,
                        "InternalCursor", new Object[] { jsCmdResult }));
            } catch (MongoException me) {
                handleMongoException(me);
            }
        } else {
            // System.out.println("regularFind");
            DBCollection collection = db.getCollection(collectionName);
            collection.setDBEncoderFactory(FACTORY);
            DBCursor cursor = collection.find(bsonQuery, bsonFields).skip(skip)
                    .batchSize(batchSize).limit(limit).addOption(options);
            InternalCursor jsCursor = (InternalCursor) MongoRuntime
                    .call(new NewInstanceAction(mongoScope, "InternalCursor",
                            new Object[] { cursor }));
            result = jsCursor;
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @JSFunction
    public void insert(final String ns, Object obj, int options) {
        Object rawObj = BSONizer.convertJStoBSON(obj);
        DBObject bsonObj = null;
        if (rawObj instanceof DBObject)
            bsonObj = (DBObject) rawObj;

        // System.out.printf("insert(%s, %s)\n", ns, bsonObj);

        try {
            // unfortunately the Java driver does not expose the _allow_dot
            // argument
            // in insert calls so we need to translate system.indexes inserts
            // into
            // index creation calls through the java driver
            if (ns.endsWith("system.indexes")) {
//                System.out.printf("ensureIndex(%s, %s)\n", ns, bsonObj);
                com.mongodb.DB db = innerMongo.getDB(ns.substring(0,
                        ns.indexOf('.')));
                String indexNS = bsonObj.get("ns").toString();
                DBCollection collection = db.getCollection(indexNS.substring(ns
                        .indexOf('.') + 1));
                collection.setDBEncoderFactory(FACTORY);
                DBObject keys = (DBObject) bsonObj.get("key");
                bsonObj.removeField("_id");
                bsonObj.removeField("ns");
                bsonObj.removeField("key");
                DBObject indexOpts = new BasicDBObject();
                for (String bsonKey : bsonObj.keySet()) {
                    indexOpts.put(bsonKey, bsonObj.get(bsonKey));
                }
                collection.ensureIndex(keys, indexOpts);
            } else {
                com.mongodb.DB db = innerMongo.getDB(ns.substring(0,
                        ns.indexOf('.')));
                DBCollection collection = db.getCollection(ns.substring(ns
                        .indexOf('.') + 1));
                collection.setDBEncoderFactory(FACTORY);
                int oldOptions = collection.getOptions();
                collection.setOptions(options);

                if (rawObj instanceof List)
                    collection.insert((List) rawObj);
                else
                    collection.insert(bsonObj);
                collection.setOptions(oldOptions);
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

        // System.out.printf("remove(%s, %s)\n", ns, bsonPattern);

        com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
        DBCollection collection = db
                .getCollection(ns.substring(ns.indexOf('.') + 1));
        collection.setDBEncoderFactory(FACTORY);

        try {
            collection.remove(bsonPattern);
        } catch (MongoException me) {
            handleMongoException(me);
        }
    }

    @JSFunction
    public void update(final String ns, Object query, Object obj,
            final Boolean upsert, final Boolean multi) {
        Object rawQuery = BSONizer.convertJStoBSON(query);
        Object rawObj = BSONizer.convertJStoBSON(obj);
        DBObject bsonQuery = null;
        DBObject bsonObj = null;
        if (rawQuery instanceof DBObject)
            bsonQuery = (DBObject) rawQuery;
        if (rawObj instanceof DBObject)
            bsonObj = (DBObject) rawObj;

        boolean upsertOp = (upsert != null) ? upsert : false;
        boolean multiOp = (multi != null) ? multi : false;

        // System.out.printf("update(%s, %s, %s, %b)\n", ns, bsonQuery, bsonObj,
        // upsert);

        com.mongodb.DB db = innerMongo.getDB(ns.substring(0, ns.indexOf('.')));
        DBCollection collection = db
                .getCollection(ns.substring(ns.indexOf('.') + 1));
        collection.setDBEncoderFactory(FACTORY);

        try {
            collection.update(bsonQuery, bsonObj, upsertOp, multiOp);
        } catch (MongoException me) {
            handleMongoException(me);
        }
    }

    /**
     * Root js api authenticate function. Returns nothing, only throws an
     * exception on authentication failure.
     * 
     * @param authObj
     */
    @JSFunction
    public void auth(final Object authObj) {
        DBObject bsonAuth = (DBObject) BSONizer.convertJStoBSON(authObj);
        DB db = innerMongo.getDB(bsonAuth.get("userSource").toString());
        // hackety hack hack hack... we need a fresh, unauthenticated Mongo
        // instance
        // since the java driver does not support multiple calls to
        // db.authenticateCommand()
        if (db.isAuthenticated()) {
            mongoScope.closeConnection(innerMongo);
            try {
                initMongo(host);
                mongoScope.addOpenedConnection(innerMongo);
                if (mongoScope.useMongoShellWriteConcern())
                    innerMongo.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
            } catch (UnknownHostException e) {
                // we should never get here
                e.printStackTrace();
            }
            db = innerMongo.getDB(bsonAuth.get("userSource").toString());
        }

        db.authenticateCommand(bsonAuth.get("user").toString(),
                bsonAuth.get("pwd").toString().toCharArray());
    }

    /**
     * Run the { logout: 1 } command against the db with the given name.
     * 
     * @param dbName
     * @return
     */
    @JSFunction
    public Object logout(final String dbName) {
        DB db = innerMongo.getDB(dbName);
        CommandResult result = db.command(new BasicDBObject("logout", 1));
        return BSONizer.convertBSONtoJS(mongoScope, result);
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
        if (mongoScope == null)
            mongoScope = (MongoScope) ScriptableObject.getTopLevelScope(this);
        mongoScope.handleMongoException(me);
    }

}
