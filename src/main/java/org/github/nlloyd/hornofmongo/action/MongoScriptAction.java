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

import org.apache.commons.lang3.StringUtils;
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.MongoScope;
import org.mozilla.javascript.Context;

/**
 * A concrete implementation of {@link MongoAction} that wraps a JavaScript script 
 * for execution in either String, Reader, or File form.
 * 
 * @author nlloyd
 *
 */
public class MongoScriptAction extends MongoAction {
	
	protected String scriptString = null;
	
	public MongoScriptAction() {
		super();
	}
	
	public MongoScriptAction(MongoScope mongoScope) {
		super(mongoScope);
	}
	
	/**
	 * Uses the provided context to execute either a String script, 
	 * @see org.mozilla.javascript.ContextAction#run(org.mozilla.javascript.Context)
	 */
	@Override
	public Object run(Context cx) {
		Object result = null;
		if(StringUtils.isNotBlank(scriptString)) {
		result =  cx.evaluateString(
				mongoScope, 
				"var db = connect('shell_test',null,null); print('connected to: ' + db._name); " +
				"db.test.findOne({" +
				"'a': /abc.*def/im" +
//				"db.test.insert({" +
//				"'a': 1, " +
//				"'today': new Date(), " +
//				"'isotoday': new ISODate(), " +
//				"'array': [1,2,'3']" +
				"});",
				"shell", 
				0, 
				null);
		}
		
		return result;
	}

}
