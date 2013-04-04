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

/**
 * @author nlloyd
 * 
 */
@RunWith(Parameterized.class)
public class JSTest {

    private static File cwd = null;

    /**
     * Tests containing not-supported official mongodb shell features
     * <ul>
     * <li>basicc.js : because function startMongoProgramNoConnect() is not
     * supported</li>
     * <li>updatef.js : because function startParallelShell() is not supported</li>
     * </ul>
     */
    public static final List<String> excludedTests = Arrays
            .asList(new String[] { "basicc.js", "updatef.js" });

    /**
     * Tests that throw an expected exception (whether by design or observed but
     * not invalid behavior).
     */
    public static final List<String> expectedExceptionTests = Arrays
            .asList(new String[] { "basicb.js", "update_arraymatch3.js" });

    public static Map<String, Class<? extends Throwable>> expectedExceptionTypes = new Hashtable<String, Class<? extends Throwable>>();
    public static Map<String, String> expectedExceptionMessages = new Hashtable<String, String>();

    static {
        // document field order is changed although the contents are still
        // identical
        expectedExceptionTypes.put("update_arraymatch3.js",
                JavaScriptException.class);
        expectedExceptionMessages
                .put("update_arraymatch3.js",
                        "[{\n\t\"_id\" : 1,\n\t\"title\" : \"ABC\",\n\t\"comments\" : [\n\t\t{\n\t\t\t\"by\" : \"joe\",\n\t\t\t\"votes\" : 4\n\t\t},\n\t\t{\n\t\t\t\"by\" : \"jane\",\n\t\t\t\"votes\" : 7\n\t\t}\n\t]\n}] != [{\n\t\"_id\" : 1,\n\t\"comments\" : [\n\t\t{\n\t\t\t\"by\" : \"joe\",\n\t\t\t\"votes\" : 4\n\t\t},\n\t\t{\n\t\t\t\"by\" : \"jane\",\n\t\t\t\"votes\" : 7\n\t\t}\n\t],\n\t\"title\" : \"ABC\"\n}] are not equal : A2 (mongodb/assert.js#6)");
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
//        System.setProperty("DEBUG.MONGO", Boolean.TRUE.toString());
//        System.setProperty("DB.TRACE", Boolean.TRUE.toString());

        MongoRuntime.rebuild();
        
        // set the exception handling behavior of the test runtime to mimic the
        // official mongo shell client
        MongoRuntime.getMongoScope().setMimicShellExceptionBehavior(true);
        MongoRuntime.call(new MongoScriptAction("connect",
                "var db = connect('test',null,null);"));
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getJsTestScripts() {
        if (cwd == null)
            cwd = new File(System.getProperty("user.dir"),
                    "target/test-classes");

        File[] jsFiles = cwd.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith("_") && name.endsWith(".js")
                        && !excludedTests.contains(name);
//                 return name.startsWith("auth") && name.endsWith(".js")
//                 && !excludedTests.contains(name);
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

    public JSTest(String jsTestFileName, File jsTestFile) {
        this.jsTestFile = jsTestFile;
    }

    @Test
    public void test() throws Exception {
        try {
            MongoRuntime.call(new MongoScriptAction(jsTestFile));
        } catch (Exception e) {
            // a few tests throw expected exceptions
            if (expectedExceptionTests.contains(jsTestFile.getName())) {
                assertEquals(expectedExceptionTypes.get(jsTestFile.getName()),
                        e.getClass());
                assertEquals(
                        expectedExceptionMessages.get(jsTestFile.getName()),
                        e.getMessage());
            } else {
                throw e;
            }
        }
    }

}
