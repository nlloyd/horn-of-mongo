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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.github.nlloyd.hornofmongo.action.MongoScriptAction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author nlloyd
 * 
 */
@RunWith(Parameterized.class)
public class JSTest {

    private static File cwd = null;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
                return !name.startsWith("_") && name.endsWith(".js");
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
    public void test() {
        MongoRuntime.call(new MongoScriptAction(jsTestFile));
    }

}
