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
package org.github.nlloyd.sandbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.bson.BSON;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.tools.shell.Global;

/**
 * Hello world!
 *
 */
public class App 
{
//	private static Global global = new Global();
//	
//	private class ScriptAction implements ContextAction {
//		
//		private String script;
//		
//		public ScriptAction(String script) {
//			this.script = script;
//		}
//
//		public Object run(Context cx) {
//			Script compiled = cx.compileString(this.script, "<script>", 1, null);
//			compiled.exec(cx, global);
//			return null;
//		}
//		
//	}
	
	private static Scriptable global;
	
    public static void main( String[] args ) throws Exception
    {
    	String flags = "cdgimstux";
    	String reFlags = BSON.regexFlags(BSON.regexFlags(flags));
    	System.out.println(flags);
    	System.out.println(reFlags);
    	
////        ContextFactory cxf = new ContextFactory();
//        Context cx = Context.enter();
//        Global global = new Global(cx);
////        global.init(cxf);
////        Context jsCx = cxf.enterContext();
//        cx.evaluateString(global, "var hullo = 'Hello World!'", 
//        		"helloWorld.js", 1, null);
//        cx.evaluateString(global, "print(hullo)", 
//        		"helloWorld.js", 2, null);
//        ScriptableObject.defineClass(global, SandboxJS.class);
//        cx.evaluateString(global, 
//        		"var test = new SandboxJS(); test.wtf(); test.wtf; test.notafunc();",
//        		"noSuchMethodTester.js", 1, null);
//        cx.evaluateString(global, 
//        		"test.prop; test.prop = 1; print(test.prop);",
//        		"noSuchMethodTester2.js", 1, null);
//
//        cx.evaluateReader(global, loadFromClasspath("mongodb/db.js"), 
//				"setup", 0, null);
//        
//        Object db = global.get("DB", global);
//        Function dbc = (Function)db;
//        Scriptable result = dbc.construct(cx, global, new Object[]{"dummyMongo", "dummy"});
//        System.out.println(result instanceof IdScriptableObject);
//        String report = "new DB(mongo,name) = " + Context.toString(result);
//        System.out.println(report);
//        global.put("newDB", global, result);
        
//        cx.evaluateString(global,
//        		"print(newDB.getName()); print('***************');",
//        		"java-test", 0, null);
        
//        Context.exit();

        ContextFactory.getGlobal().call(new ContextAction() {

			public Object run(Context cx) {
				global = new Global(cx);
				Object result = null;
				try {
					result = cx.evaluateReader(global, loadFromClasspath("mongodb/db.js"), 
									"setup", 0, null);
				} catch (IOException e) {
					e.printStackTrace();
				};
				return result;
			}
        	
        });

		ScriptableObject.defineProperty(global, "something2", new NativeObject(), 0);
    	
    	Object empty = ContextFactory.getGlobal().call(new ContextAction() {
    		public Object run(Context cx) {
    			Object result = cx.evaluateString(global, "var something = {}; print(something); print(something2);", "blah", 0, null);
    			return result;
    		}
    	});
		
		
    	System.out.println(Context.toString(empty));
    	
        ScriptableObject.defineClass(global, Counter.class);
//        Thread t1 = new Thread(new RunnableScript(global, "var t1 = new DB('db','t1Name'); print('created: '+t1.getName());"));
//        Thread t2 = new Thread(new RunnableScript(global, "var t2 = new DB('db','t2Name'); print('created: '+t2.getName());"));
//        Thread t3 = new Thread(new RunnableScript(global, "print('*****'); print(t1.getName()); print(t2.getName());"));
        
        Thread t = new Thread(new RunnableScript(global,
        		"c = new Counter(10); print(c.count); print(c.count); print(c.count); c.resetCount(); print(c.count);" +
        		"print(c.dynamic);print(c.dynamic);"));
        
        t.run();
        t.join();
        
//        t1.run();
//        t2.run();
//        t3.run();
//        t1.join();
//        t2.join();
//        t3.join();
        
    }

	protected static Reader loadFromClasspath(String filePath) {
		Reader reader = null;
		reader = new BufferedReader(new InputStreamReader(
				ClassLoader.getSystemResourceAsStream(filePath)));
		return reader;
	}
	
	protected static class RunnableScript implements Runnable, ContextAction {

		Scriptable scope;
		String script;
		
		public RunnableScript(Scriptable scope, String script) {
			this.scope = scope;
			this.script = script;
		}
		
		public void run() {
			ContextFactory.getGlobal().call(this);
		}

		public Object run(Context cx) {
			return cx.evaluateString(scope, script, "ThreadedScript", 0, null);
		}
		
	}
	


	 public static class Counter extends ScriptableObject {
	    private static final long serialVersionUID = 438270592527335642L;
	
	    // The zero-argument constructor used by Rhino runtime to create instances
	    public Counter() { }
	
	    // @JSConstructor annotation defines the JavaScript constructor
	    @JSConstructor
	    public Counter(int a) { count = a; }
	
	    // The class name is defined by the getClassName method
	    @Override
	    public String getClassName() { return "Counter"; }
	
	    // The method getCount defines the count property.
	    @JSGetter
	    public int getCount() { return count++; }
	
	    // Methods can be defined the @JSFunction annotation.
	    // Here we define resetCount for JavaScript.
	    @JSFunction
	    public void resetCount() { count = 0; }
	    
	    /* (non-Javadoc)
		 * @see org.mozilla.javascript.ScriptableObject#get(java.lang.String, org.mozilla.javascript.Scriptable)
		 */
		@Override
		public Object get(String name, Scriptable start) {
			Object got = super.get(name, start);
			if((got == ScriptableObject.NOT_FOUND)
					&& this.equals(start)
					&& !ScriptableObject.hasProperty(this, name)) {
				got = "newProperty-" + name;
				System.out.println("made new property: " + got.toString());
				put(name, this, got);
			}
			return got;
		}

		private int count;
	}
}
