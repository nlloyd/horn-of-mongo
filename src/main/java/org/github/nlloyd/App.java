package org.github.nlloyd;

import org.mozilla.javascript.Context;
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
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
//        ContextFactory cxf = new ContextFactory();
        Context cx = Context.enter();
        Global global = new Global(cx);
//        global.init(cxf);
//        Context jsCx = cxf.enterContext();
        cx.evaluateString(global, "print('Hello World!')", 
        		"helloWorld.js", 1, null);
        Context.exit();
    }
}
