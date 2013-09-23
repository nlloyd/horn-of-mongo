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
package com.github.nlloyd.hornofmongo;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.WrappedException;

import com.github.joelittlejohn.embedmongo.log.Loggers;
import com.github.nlloyd.hornofmongo.action.MongoScriptAction;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Net;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Storage;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Timeout;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;

/**
 * @author nlloyd
 *
 */
@RunWith(Parameterized.class)
public class JSTest {

    private static final String MONGOD_VERSION = "2.4.6";

    private static File cwd = null;

    /**
     * Tests containing currently unsupported/unimplemented official mongodb
     * shell features such as:
     *
     * startMongoProgramNoConnect() startParallelShell()
     *
     * run_program1.js is excluded due to the currently unsupported runProgram()
     * function.
     *
     * mr_noscripting.js and logpath.js are excluded due to the currently
     * unsupported MongoRunner js class.
     *
     * evalf.js is excluded due to a locking issue that has yet to be resolved
     * however the complexity of the test scenario makes this a safe-to-exclude
     * for now.
     *
     * indexOtherNamespace.js excluded because I can't for the life of me get
     * the failure scenario to actually fail using the mongo java driver :-(
     *
     * memory.js is excluded because it is testing the mongod rather than the
     * client api behavior (and it takes a while to run on slower machines).
     * This test does actually pass, however.
     *
     * remove_justone.js excluded until the mongo java driver supports that
     * feature: https://jira.mongodb.org/browse/JAVA-759
     *
     * dbref3.js excluded until mongo java driver follows the DBRef spec,
     * currently only a naive check for $ref and $id fields is made which is
     * incorrect
     * jira issue for tracking:
     *
     */
    public static final List<String> excludedTests = Arrays
            .asList("basicc.js", "bench_test1.js",
                    "bench_test2.js", "bench_test3.js",
                    "connections_opened.js", "count8.js", "coveredIndex3.js",
                    "currentop.js", "cursora.js", "distinct3.js", "drop2.js",
                    "evalc.js", "evalf.js", "evald.js", "explain3.js",
                    "group7.js", "index12.js", "killop.js",
                    "loadserverscripts.js", "logpath.js", "memory.js",
                    "mr_drop.js", "mr_killop.js", "mr_noscripting.js",
                    "orm.js", "orn.js", "queryoptimizer3.js",
                    "queryoptimizer5.js", "remove9.js", "removeb.js",
                    "removec.js", "shellkillop.js", "shellstartparallel.js",
                    "shellspawn.js", "updatef.js", "run_program1.js",
                    "indexOtherNamespace.js", "remove_justone.js", "dbref3.js");

    /**
     * Tests that throw an expected exception (whether by design or observed but
     * not invalid behavior).
     *
     * numberint.js is a special case since there IS a fix for
     */
    public static final List<String> expectedExceptionTests = Arrays
            .asList("basicb.js", "update_arraymatch3.js",
                    "numberint.js");

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
        expectedExceptionTypes.put("numberint.js", JavaScriptException.class);
        expectedExceptionMessages.put("numberint.js",
                "[2] != [1] are not equal : roundtrip 1 (mongodb/assert.js#6)");
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getJsTestScripts() {
        if (cwd == null)
            cwd = new File(System.getProperty("user.dir"), "jstests");

        final FilenameFilter jsFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith("_") && name.endsWith(".js")
                        && !excludedTests.contains(name);
            }

        };

        System.out
                .println("searching for official mongodb *.js test files in path: "
                        + cwd.toString());
        File[] jsFiles = cwd.listFiles(jsFilter);

        List<Object[]> testScripts = new ArrayList<Object[]>(jsFiles.length);
        // fileName is the first argument for naming the tests, otherwise it is
        // ignored
        for (File jsFile : jsFiles)
            testScripts.add(new Object[] { jsFile.getName(), jsFile });

        File rootJsTestDir = cwd.getParentFile();
        System.out
                .println("searching for horn-of-mongo *.js test files in path: "
                        + rootJsTestDir.toString());
        File[] moreJsFiles = rootJsTestDir.listFiles(jsFilter);

        for (File jsFile : moreJsFiles)
            testScripts.add(new Object[] { jsFile.getName(), jsFile });

        return testScripts;
    }

    private File jsTestFile = null;

    private static MongoScope testScope;

    private static MongodExecutable mongodExec;
    private static MongodProcess mongod;

    public JSTest(final String jsTestFileName, final File jsTestFile) {
        this.jsTestFile = jsTestFile;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        File testClassesDir = new File(System.getProperty("user.dir"));
        File databaseDir = new File(testClassesDir.getParent(), "mongodb");
        databaseDir.mkdirs();
        File mongodLogFile = new File(databaseDir.getParent(), "mongodb.log");
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .commandLinePostProcessor(new ICommandLinePostProcessor() {

                    @Override
                    public List<String> process(Distribution arg0,
                            List<String> arg1) {
                        arg1.remove("-v");
                        arg1.remove("--noprealloc");
                        arg1.add("--setParameter");
                        arg1.add("enableTestCommands=1");
                        return arg1;
                    }

                })
                .processOutput(
                        Loggers.file(mongodLogFile.getAbsolutePath(), "UTF8"))
                .build();

        MongodConfig mongodConfig = new MongodConfig(new GenericVersion(
                MONGOD_VERSION), new Net("127.0.0.1", 27017, false),
                new Storage(databaseDir.getAbsolutePath(), null, 0),
                new Timeout());

        mongodExec = MongodStarter.getInstance(runtimeConfig).prepare(
                mongodConfig);
        mongod = mongodExec.start();

        // System.setProperty("DEBUG.MONGO", Boolean.TRUE.toString());
        // System.setProperty("DB.TRACE", Boolean.TRUE.toString());

        testScope = MongoRuntime.createMongoScope();
        // set the exception handling behavior of the test runtime to mimic the
        // official mongo shell client
        testScope.setUseMongoShellWriteConcern(true);
        testScope.setStdoutMongoErrorMessages(true);
    }

    @AfterClass
    public static void tearDownClass() throws InterruptedException {
        mongod.stop();
        mongodExec.stop();
    }

    @Test
    public void test() throws Exception {
        System.out.println("*** Running " + jsTestFile.getName());
        try {
            // testScope = MongoRuntime.createMongoScope(new MongoClientURI(
            // "mongodb://localhost/test"), true, true);
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

        testScope.cleanup();
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
