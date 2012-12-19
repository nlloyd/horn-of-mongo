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
package org.github.nlloyd.hornofmongo.scope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
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

	public MongoScope(Context context) {
		super(context);
		initMongoJS(context);
	}

	protected void initMongoJS(Context context) {
		if(!isInitialized()) {
			super.init(context);
			for(String jsSetupFile : mongoApiFiles) {
				try {
					context.evaluateReader(this, loadFromClasspath(jsSetupFile), 
							"setup", 0, null);
				} catch (IOException e) {
					logger.error("Caught IOException attempting to load from classpath: " + jsSetupFile, e);
				}
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
