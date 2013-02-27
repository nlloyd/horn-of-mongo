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
package org.github.nlloyd.hornofmongo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.github.nlloyd.hornofmongo.adaptor.BinData;
import org.github.nlloyd.hornofmongo.adaptor.DB;
import org.github.nlloyd.hornofmongo.adaptor.DBCollection;
import org.github.nlloyd.hornofmongo.adaptor.DBQuery;
import org.github.nlloyd.hornofmongo.adaptor.InternalCursor;
import org.github.nlloyd.hornofmongo.adaptor.Mongo;
import org.github.nlloyd.hornofmongo.adaptor.NumberInt;
import org.github.nlloyd.hornofmongo.adaptor.NumberLong;
import org.github.nlloyd.hornofmongo.adaptor.ObjectId;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.tools.shell.Global;

/**
 * The MongoDB-specific {@link Scope} implementation. This extends
 * {@link Global} to add MongoDB shell JavaScript global functions objects, and
 * variables.
 * 
 * Meant to emulate engine.cpp (and the more specific engine_*.cpp
 * implementations) in the official mongodb source.
 * 
 * @author nlloyd
 * 
 */
public class MongoScope extends Global {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4650743395507077775L;
	
	private static final Logger logger = Logger.getLogger(MongoScope.class);
	
	private static String[] mongoApiFiles = {
		"mongodb/utils.js",
		"mongodb/utils_sh.js",
		"mongodb/db.js",
		"mongodb/mongo.js",
		"mongodb/mr.js",
		"mongodb/query.js",
		"mongodb/collection.js"
	};

	public MongoScope() {
		super();
	}

	public MongoScope(Context context) throws IllegalAccessException, InstantiationException, InvocationTargetException {
		super(context);
		initMongoJS(context);
		execCoreFiles(context);
	}
	
	protected void initMongoJS(Context context) throws IllegalAccessException, InstantiationException, InvocationTargetException {
		if(!isInitialized()) {
			super.init(context);
		}
		ScriptableObject.defineClass(this, Mongo.class, false, false);
		ScriptableObject.defineClass(this, ObjectId.class, false, false);
		ScriptableObject.defineClass(this, DB.class, false, false);
		ScriptableObject.defineClass(this, DBCollection.class, false, false);
		ScriptableObject.defineClass(this, InternalCursor.class, false, false);
		ScriptableObject.defineClass(this, DBQuery.class, false, false);
		ScriptableObject.defineClass(this, BinData.class, false, false);
		
		ScriptableObject.defineClass(this, NumberLong.class, false, false);
		ScriptableObject.defineClass(this, NumberInt.class, false, false);
		
//        assert( JS_InitClass( cx , global , 0 , &mongo_class , local ? mongo_local_constructor : mongo_external_constructor , 0 , 0 , mongo_functions , 0 , 0 ) );
//
//        assert( JS_InitClass( cx , global , 0 , &object_id_class , object_id_constructor , 0 , 0 , object_id_functions , 0 , 0 ) );
//        assert( JS_InitClass( cx , global , 0 , &db_class , db_constructor , 2 , 0 , 0 , 0 , 0 ) );
//        XXXXXXassert( JS_InitClass( cx , global , 0 , &db_collection_class , db_collection_constructor , 4 , 0 , 0 , 0 , 0 ) );
//        XXXXXXassert( JS_InitClass( cx , global , 0 , &internal_cursor_class , internal_cursor_constructor , 0 , 0 , internal_cursor_functions , 0 , 0 ) );
//        ???assert( JS_InitClass( cx , global , 0 , &dbquery_class , dbquery_constructor , 0 , 0 , 0 , 0 , 0 ) );
//        assert( JS_InitClass( cx , global , 0 , &dbpointer_class , dbpointer_constructor , 0 , 0 , dbpointer_functions , 0 , 0 ) );
//        assert( JS_InitClass( cx , global , 0 , &bindata_class , bindata_constructor , 0 , 0 , bindata_functions , 0 , 0 ) );
//
//        ???assert( JS_InitClass( cx , global , 0 , &timestamp_class , timestamp_constructor , 0 , 0 , 0 , 0 , 0 ) );
//        assert( JS_InitClass( cx , global , 0 , &numberlong_class , numberlong_constructor , 0 , 0 , numberlong_functions , 0 , 0 ) );
//        assert( JS_InitClass( cx , global , 0 , &numberint_class , numberint_constructor , 0 , 0 , numberint_functions , 0 , 0 ) );
//        assert( JS_InitClass( cx , global , 0 , &minkey_class , 0 , 0 , 0 , 0 , 0 , 0 ) );
//        assert( JS_InitClass( cx , global , 0 , &maxkey_class , 0 , 0 , 0 , 0 , 0 , 0 ) );
//
//        ???assert( JS_InitClass( cx , global , 0 , &map_class , map_constructor , 0 , 0 , map_functions , 0 , 0 ) );
//
//        XXXXXXassert( JS_InitClass( cx , global , 0 , &bson_ro_class , bson_cons , 0 , 0 , bson_functions , 0 , 0 ) );
//        XXXXXXassert( JS_InitClass( cx , global , 0 , &bson_class , bson_cons , 0 , 0 , bson_functions , 0 , 0 ) );
//
//        static const char *dbrefName = "DBRef";
//        dbref_class.name = dbrefName;
//        assert( JS_InitClass( cx , global , 0 , &dbref_class , dbref_constructor , 2 , 0 , bson_functions , 0 , 0 ) );

	}
	
	protected void execCoreFiles(Context context) {
		for(String jsSetupFile : mongoApiFiles) {
			try {
				context.evaluateReader(this, loadFromClasspath(jsSetupFile), 
						jsSetupFile, 0, null);
			} catch (IOException e) {
				logger.error("Caught IOException attempting to load from classpath: " + jsSetupFile, e);
			} catch (JavaScriptException e) {
				logger.error("Caught JavaScriptException attempting to load from classpath: " + jsSetupFile, e);
			}
		}
	}

	protected Reader loadFromClasspath(String filePath) {
		Reader reader = null;
		reader = new BufferedReader(new InputStreamReader(
				ClassLoader.getSystemResourceAsStream(filePath)));
		return reader;
	}
}
