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
package org.github.nlloyd.hornofmongo;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.github.nlloyd.hornofmongo.action.MongoScriptAction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.WrappedException;

/**
 * @author nlloyd
 * 
 */
@RunWith(Parameterized.class)
public class JSTest {

    private static File cwd = null;

    /**
     * Tests containing currently unsupported/unimplemented official mongodb
     * shell features such as:
     * 
     * startMongoProgramNoConnect() startParallelShell()
     * 
     * evalf.js is excluded due to a locking issue that has yet to be resolved
     * however the complexity of the test scenario makes this a safe-to-exclude
     * for now.
     * 
     * memory.js is excluded because it is testing the mongod rather than the
     * client api behavior (and it takes a while to run on slower machines).
     * This test does actually pass, however.
     */
    public static final List<String> excludedTests = Arrays
            .asList(new String[] { "basicc.js", "bench_test1.js",
                    "bench_test2.js", "bench_test3.js",
                    "connections_opened.js", "count8.js", "coveredIndex3.js",
                    "currentop.js", "cursora.js", "distinct3.js", "drop2.js",
                    "evalc.js", "evalf.js", "evald.js", "explain3.js",
                    "group7.js", "index12.js", "killop.js",
                    "loadserverscripts.js", "memory.js", "mr_drop.js",
                    "mr_killop.js", "orm.js", "orn.js", "queryoptimizer3.js",
                    "queryoptimizer5.js", "remove9.js", "removeb.js",
                    "removec.js", "shellkillop.js", "shellstartparallel.js",
                    "shellspawn.js", "updatef.js" });

    /**
     * Tests that throw an expected exception (whether by design or observed but
     * not invalid behavior).
     */
    public static final List<String> expectedExceptionTests = Arrays
            .asList(new String[] { "basicb.js", "update_arraymatch3.js" });

    public static Map<String, Class<? extends Throwable>> expectedExceptionTypes = new Hashtable<String, Class<? extends Throwable>>();
    public static Map<String, String> expectedExceptionMessages = new Hashtable<String, String>();

    static {
        expectedExceptionTypes.put("basicb.js", IllegalArgumentException.class);
        expectedExceptionMessages.put("basicb.js",
                "fields stored in the db can't start with '$' (Bad Key: '$a')");
        // document field order is changed although the contents are still
        // identical
        expectedExceptionTypes.put("update_arraymatch3.js",
                JavaScriptException.class);
        expectedExceptionMessages
                .put("update_arraymatch3.js",
                        "[{\n\t\"_id\" : 1,\n\t\"title\" : \"ABC\",\n\t\"comments\" : [\n\t\t{\n\t\t\t\"by\" : \"joe\",\n\t\t\t\"votes\" : 4\n\t\t},\n\t\t{\n\t\t\t\"by\" : \"jane\",\n\t\t\t\"votes\" : 7\n\t\t}\n\t]\n}] != [{\n\t\"_id\" : 1,\n\t\"comments\" : [\n\t\t{\n\t\t\t\"by\" : \"joe\",\n\t\t\t\"votes\" : 4\n\t\t},\n\t\t{\n\t\t\t\"by\" : \"jane\",\n\t\t\t\"votes\" : 7\n\t\t}\n\t],\n\t\"title\" : \"ABC\"\n}] are not equal : A2 (mongodb/assert.js#6)");
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getJsTestScripts() {
        if (cwd == null)
            cwd = new File(System.getProperty("user.dir"), "jstests");

        System.out.println("searching for *.js test files in path: "
                + cwd.toString());
        File[] jsFiles = cwd.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith("_") && name.endsWith(".js")
                        && (name.startsWith("find"))
                        && !excludedTests.contains(name);
            }

        });

        List<Object[]> testScripts = new ArrayList<Object[]>(jsFiles.length);
        // fileName is the first argument for naming the tests, otherwise it is
        // ignored
        for (File jsFile : jsFiles)
            testScripts.add(new Object[] { jsFile.getName(), jsFile });

        return testScripts;
    }

    private File jsTestFile = null;

    private static MongoScope testScope;

    public JSTest(String jsTestFileName, File jsTestFile) {
        this.jsTestFile = jsTestFile;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testScope = MongoRuntime.createMongoScope();
        // set the exception handling behavior of the test runtime to mimic the
        // official mongo shell client
        testScope.setUseMongoShellWriteConcern(true);
        testScope.setMimicShellExceptionBehavior(true);
        // MongoRuntime.call(new MongoAction(null) {
        //
        // @Override
        // public Object run(Context cx) {
        // return null;
        // }
        //
        // });
        //
        // String debugConfig = "transport=socket,suspend=y,address=9000";
        // System.out.println("LISTENING: " + debugConfig);
        // RhinoDebugger debugger = new RhinoDebugger(debugConfig);
        // debugger.start();
        // ContextFactory.getGlobal().addListener(debugger);
    }

    @Test
    public void test() throws Exception {
//         System.setProperty("DEBUG.MONGO", Boolean.TRUE.toString());
//         System.setProperty("DB.TRACE", Boolean.TRUE.toString());

        System.out.println("*** Running " + jsTestFile.getName());
        try {
            MongoRuntime.call(new MongoScriptAction(testScope, "connect",
                    "db = connect('test',null,null);"));
            MongoRuntime.call(new MongoScriptAction(testScope, jsTestFile));
        } catch (WrappedException e) {
            // a few tests throw expected exceptions, unwrap them if they are
            // wrapped
            verifyException((Exception) e.getWrappedException());
        } catch (Exception e) {
            // a few tests throw expected exceptions
            verifyException(e);
        }

        // System.out.println("*** Cleaning up");
        testScope.cleanup();
        // if (Context.getCurrentContext() != null) {
        // System.out.println("Context associated with thread, cleaning up");
        // Context.exit();
        // }
        // testScope = null;
    }

    private void verifyException(Exception e) throws Exception {
        if (expectedExceptionTests.contains(jsTestFile.getName())) {
            assertEquals(expectedExceptionTypes.get(jsTestFile.getName()),
                    e.getClass());
            assertEquals(expectedExceptionMessages.get(jsTestFile.getName()),
                    e.getMessage());
        } else {
            throw e;
        }
    }

}
