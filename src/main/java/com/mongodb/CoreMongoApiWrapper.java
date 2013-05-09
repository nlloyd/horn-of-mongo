package com.mongodb;

import static org.github.nlloyd.hornofmongo.bson.HornOfMongoBSONEncoder.FACTORY;
import java.util.List;

public class CoreMongoApiWrapper extends DBApiLayer {
    
    protected CoreMongoApiWrapper(Mongo mongo, String name,
            DBConnector connector) {
        super(mongo, name, connector);
    }

    public static WriteResult callInsert(DBCollection collection, List<DBObject> list, boolean shouldApply) {
        return ((MyCollection)collection).insert(list, false, collection.getWriteConcern(), FACTORY.create());
    }

}
