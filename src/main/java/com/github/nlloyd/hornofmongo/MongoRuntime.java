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
package com.github.nlloyd.hornofmongo;

import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.ContextFactory;

import com.github.nlloyd.hornofmongo.action.MongoAction;
import com.github.nlloyd.hornofmongo.action.MongoScriptAction;
import com.mongodb.MongoClientURI;

/**
 * Runtime class for the Horn of Mongo client library that initializes the
 * global {@link ContextFactory} and provides convenience methods for executing
 * JS scripts with mongodb JSAPI code.
 * 
 * @author nlloyd
 * 
 */
public class MongoRuntime {

    /**
     * Creates a newly initialized {@link MongoScope} instance. This will use
     * {@link MongoRuntime#call(MongoAction)} to initialize the
     * {@link MongoScope} instance, possibly resulting in the global
     * {@link MongoContextFactory} being set.
     * 
     * @return
     */
    public static final MongoScope createMongoScope() {
        MongoScope mongoScope = (MongoScope) call(new MongoScope.InitMongoScopeAction());
        return mongoScope;
    }

    /**
     * Creates a newly initialized {@link MongoScope} instance with a connection
     * to the specified mongodb instance/cluster. This will use
     * {@link MongoRuntime#call(MongoAction)} to initialize the
     * {@link MongoScope} instance, possibly resulting in the global
     * {@link MongoContextFactory} being set.
     * 
     * After the scope is initialized a call via the mongo JS API to the
     * "connect()" method will be made to initialize the global db instance.
     * 
     * @return
     */
    public static final MongoScope createMongoScope(
            final MongoClientURI mongoClientURI,
            boolean useMongoShellWriteConcern,
            boolean mimicShellExceptionBehavior) throws UnknownHostException {
        if (StringUtils.isBlank(mongoClientURI.getDatabase()))
            throw new IllegalArgumentException(
                    "mongo client uri must have a database");
        MongoScope mongoScope = createMongoScope();
        mongoScope.setUseMongoShellWriteConcern(useMongoShellWriteConcern);
        mongoScope.setMimicShellExceptionBehavior(mimicShellExceptionBehavior);
        
        StringBuilder connectStrBuilder = new StringBuilder("db = connect('");

        if ((mongoClientURI.getHosts().size() == 1)
                && (mongoClientURI.getHosts().get(0).equals("localhost") || mongoClientURI
                        .getHosts().get(0).equals("localhost:27017")))
            connectStrBuilder.append(mongoClientURI.getDatabase());
        else
            connectStrBuilder.append(mongoClientURI.getURI());
        
        connectStrBuilder.append("', null, null);");
        
        call(new MongoScriptAction(mongoScope, "connect", connectStrBuilder.toString()));

        return mongoScope;
    }

    /**
     * Convenience method to call the {@link MongoAction} using the global
     * {@link ContextFactory}. If the global {@link ContextFactory} has not
     * explicitly been set yet then this method will set an instance of
     * {@link MongoContextFactory} as the global {@link ContextFactory}.
     * 
     * @param mongoAction
     * @return
     */
    public static final Object call(MongoAction mongoAction) {
        if (!ContextFactory.hasExplicitGlobal())
            ContextFactory.initGlobal(new MongoContextFactory());
        return ContextFactory.getGlobal().call(mongoAction);
    }

}
