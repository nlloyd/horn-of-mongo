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
package org.github.nlloyd.hornofmongo.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang3.StringUtils;
import org.github.nlloyd.hornofmongo.MongoScope;
import org.mozilla.javascript.Context;

import com.mongodb.MongoException;

/**
 * A concrete implementation of {@link MongoAction} that wraps a JavaScript script 
 * for execution in either String, Reader, or File form.
 * 
 * @author nlloyd
 *
 */
public class MongoScriptAction extends MongoAction {
	
	protected String scriptName = "anon";
	protected String scriptString = null;
	protected Reader scriptReader = null;
	
	public MongoScriptAction(MongoScope mongoScope, String script) {
		super(mongoScope);
		this.scriptString = script;
	}
	
	public MongoScriptAction(MongoScope mongoScope, File script) {
		super(mongoScope);
		this.scriptName = script.getName();
		try {
			this.scriptReader = new BufferedReader(new FileReader(script));
		} catch (FileNotFoundException e) {
			throw new MongoException("Attempted to execute non-existent file " + script.getAbsolutePath(), e);
		}
	}
	
	public MongoScriptAction(MongoScope mongoScope, Reader script) {
		super(mongoScope);
		this.scriptReader = script;
	}
	
	public MongoScriptAction(MongoScope mongoScope, String name, String script) {
		super(mongoScope);
		this.scriptName = name;
		this.scriptString = script;
	}
	
	public MongoScriptAction(MongoScope mongoScope, String name, File script) {
		super(mongoScope);
		this.scriptName = name;
		try {
			this.scriptReader = new BufferedReader(new FileReader(script));
		} catch (FileNotFoundException e) {
			throw new MongoException("Attempted to execute non-existent file " + script.getAbsolutePath(), e);
		}
	}
	
	public MongoScriptAction(MongoScope mongoScope, String name, Reader script) {
		super(mongoScope);
		this.scriptName = name;
		this.scriptReader = script;
	}
	
	/**
	 * Uses the provided context to execute either a String script, 
	 * @see org.mozilla.javascript.ContextAction#run(org.mozilla.javascript.Context)
	 */
	@Override
	public Object run(Context cx) {
//		cx.setOptimizationLevel(-1);
		Object result = null;
		if(StringUtils.isNotBlank(scriptString)) {
			result =  cx.evaluateString(
					mongoScope, 
					scriptString,
					scriptName, 
					0, 
					null);
		} else if(scriptReader != null) {
			try {
				result =  cx.evaluateReader(
						mongoScope, 
						scriptReader,
						scriptName, 
						0, 
						null);
			} catch (IOException e) {
				throw new MongoException("IOException when executing a script in Reader form.", e);
			}
		}
		// TODO throw exception if nothing to execute?
		
		return result;
	}

}
