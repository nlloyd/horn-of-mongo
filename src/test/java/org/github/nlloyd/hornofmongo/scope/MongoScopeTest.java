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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.Context;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.directories.IDirectory;

/**
 * @author nlloyd
 *
 */
public class MongoScopeTest {
    private static MongodExecutable mongodExe;
    private static MongodProcess mongod;
    private static int mongoPort = 0;
    
    protected static Context context = null;
    protected static MongoScope mongoScope = null;
    
    @BeforeClass
    public static void setUpMongod() throws Exception {
        IDirectory mvnBuildPath = new IDirectory() {
            @Override
            public File asFile() {
                String currentDir = System.getProperty("user.dir");
                return new File(currentDir, "target");
            }
        };
        ITempNaming mvnMongoNamer = new ITempNaming() {
            @Override
            public String nameFor(String prefix, String postfix) {
                return prefix + postfix;
            }
        };
        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setTempDirFactory(mvnBuildPath);
        runtimeConfig.setExecutableNaming(mvnMongoNamer);
        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        mongodExe = runtime.prepare(new MongodConfig(Version.V2_0_5));
        mongod = mongodExe.start();
        mongoPort = mongod.getConfig().getPort();
        
        context = Context.enter();
        mongoScope = new MongoScope(context);
    }

    @SuppressWarnings("deprecation")
    @AfterClass
    public static void cleanUpMongod() throws Exception {
        Context.exit();
    	
        mongod.stop();
        mongodExe.cleanup();
    }

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void basicTest() {
		context.evaluateString(mongoScope, "db.test.insert({'a':1});", "MongoScopeTest", 1, null);
	}

}
