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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bson.BSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.WrappedException;

import com.github.nlloyd.hornofmongo.action.MongoScriptAction;
import com.github.nlloyd.hornofmongo.util.BSONizer;

/**
 * @author nlloyd
 * 
 */
public class ExtendedShellTest {

    private MongoScope testScope;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testScope = MongoRuntime.createMongoScope();
    }

    @Test
    public void test_ls() {
        Object result = MongoRuntime.call(new MongoScriptAction(testScope,
                "ls();"));
        Object resultConverted = BSONizer.convertJStoBSON(result, true);
        assertTrue(resultConverted instanceof List<?>);
        @SuppressWarnings("unchecked")
        List<Object> lsResult = (List<Object>) resultConverted;
        File[] filesList = testScope.getCwd().listFiles();
        for (File file : filesList) {
            String nameToCheck = "./" + file.getName();
            if (file.isDirectory())
                nameToCheck += "/";
            assertTrue("name not found: " + nameToCheck,
                    lsResult.contains(nameToCheck));
        }

        // with argument
        Object result2 = MongoRuntime.call(new MongoScriptAction(testScope,
                "ls('target');"));
        Object resultConverted2 = BSONizer.convertJStoBSON(result2, true);
        assertTrue(resultConverted2 instanceof List<?>);
        @SuppressWarnings("unchecked")
        List<Object> lsResult2 = (List<Object>) resultConverted2;
        File[] filesList2 = new File(testScope.getCwd(), "target").listFiles();
        for (File file : filesList2) {
            String nameToCheck = "target/" + file.getName();
            if (file.isDirectory())
                nameToCheck += "/";
            assertTrue("name not found: " + nameToCheck,
                    lsResult2.contains(nameToCheck));
        }

        // bad directory
        try {
            MongoRuntime.call(new MongoScriptAction(testScope,
                    "ls('does-not-exist');"));
        } catch (Exception e) {
            assertTrue(e instanceof WrappedException);
            assertTrue(((WrappedException) e).getMessage().contains(
                    "no such directory"));
        }
    }

    @Test
    public void test_cd() {
        MongoRuntime.call(new MongoScriptAction(testScope, "cd('target')"));
        assertEquals(new File("target"), testScope.getCwd());
        MongoRuntime.call(new MongoScriptAction(testScope, "cd('../')"));
        assertEquals(new File("../"), testScope.getCwd());
    }

    @Test
    public void test_mkdir() throws IOException {
        final String pathToMake = "target/make/this/dir";

        FileUtils.deleteDirectory(new File("target", "make"));

        final File expectedDir = new File(pathToMake);
        Object wasMade = MongoRuntime.call(new MongoScriptAction(testScope,
                pathToMake));
        assertTrue(wasMade instanceof Boolean);
        assertTrue((Boolean) wasMade);
        assertTrue(expectedDir.exists());
        assertTrue(expectedDir.isDirectory());
    }

    @Test
    public void test_pwd() {
        Object pwdResult = MongoRuntime.call(new MongoScriptAction(testScope,
                "pwd();"));
        final String pwd = Context.toString(pwdResult);
        final File pwdFile = new File(pwd);
        assertEquals(new File(".").getAbsolutePath(), pwdFile.getAbsolutePath());
    }

    @Test
    public void test_listFiles() {
        Object result = MongoRuntime.call(new MongoScriptAction(testScope,
                "listFiles();"));
        Object resultConverted = BSONizer.convertJStoBSON(result, true);
        assertTrue(resultConverted instanceof List<?>);
        @SuppressWarnings("unchecked")
        List<Object> lsResult = (List<Object>) resultConverted;
        Map<String, BSONObject> namesToData = new Hashtable<String, BSONObject>(
                lsResult.size());
        for (Object lsEntry : lsResult) {
            BSONObject fileData = (BSONObject) BSONizer.convertJStoBSON(
                    lsEntry, true);
            assertTrue(fileData.containsField("name"));
            assertTrue(fileData.containsField("isDirectory"));
            if (!(Boolean) fileData.get("isDirectory"))
                assertTrue(fileData.containsField("size"));
            namesToData.put(fileData.get("name").toString(), fileData);
        }
        File[] filesList = testScope.getCwd().listFiles();
        for (File file : filesList) {
            String nameToCheck = "./" + file.getName();
            assertTrue("name not found: " + nameToCheck,
                    namesToData.containsKey(nameToCheck));
            if (!(Boolean) namesToData.get(nameToCheck).get("isDirectory"))
                assertEquals(Long.valueOf(file.length()), (Long) namesToData
                        .get(nameToCheck).get("size"));
        }

        // with arument
        Object result2 = MongoRuntime.call(new MongoScriptAction(testScope,
                "listFiles('target');"));
        Object resultConverted2 = BSONizer.convertJStoBSON(result2, true);
        assertTrue(resultConverted2 instanceof List<?>);
        @SuppressWarnings("unchecked")
        List<Object> lsResult2 = (List<Object>) resultConverted2;
        Map<String, BSONObject> namesToData2 = new Hashtable<String, BSONObject>(
                lsResult2.size());
        for (Object lsEntry : lsResult2) {
            BSONObject fileData = (BSONObject) BSONizer.convertJStoBSON(
                    lsEntry, true);
            assertTrue(fileData.containsField("name"));
            assertTrue(fileData.containsField("isDirectory"));
            if (!(Boolean) fileData.get("isDirectory"))
                assertTrue(fileData.containsField("size"));
            namesToData2.put(fileData.get("name").toString(), fileData);
        }
        File[] filesList2 = new File(testScope.getCwd(), "target").listFiles();
        for (File file : filesList2) {
            String nameToCheck = "target/" + file.getName();
            assertTrue("name not found: " + nameToCheck,
                    namesToData2.containsKey(nameToCheck));
            if (!(Boolean) namesToData2.get(nameToCheck).get("isDirectory"))
                assertEquals(Long.valueOf(file.length()), (Long) namesToData2
                        .get(nameToCheck).get("size"));
        }

        // bad directory
        try {
            MongoRuntime.call(new MongoScriptAction(testScope,
                    "listFiles('does-not-exist');"));
        } catch (Exception e) {
            assertTrue(e instanceof WrappedException);
            assertTrue(((WrappedException) e).getMessage().contains(
                    "no such directory"));
        }
    }

    @Test
    public void test_hostname() throws UnknownHostException {
        final String expectedHostName = InetAddress.getLocalHost()
                .getHostName();
        Object hostName = MongoRuntime.call(new MongoScriptAction(testScope,
                "hostname();"));
        final String hostNameStr = Context.toString(hostName);
        assertEquals(expectedHostName, hostNameStr);
    }

    @Test
    public void test_cat() throws IOException {
        final String testData = "junk-data";
        File testFile = File.createTempFile("horn-of-mongo.cat", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
        writer.write(testData);
        writer.close();

        Object catResult = MongoRuntime.call(new MongoScriptAction(testScope,
                "cat('" + testFile.getAbsolutePath() + "');"));
        String catData = Context.toString(catResult);

        assertEquals(testData, catData);
    }

    @Test
    public void test_removeFile() throws IOException {
        File dummyFile = File.createTempFile("horn-of-mongo.removeFile",
                ".test");
        assertTrue(dummyFile.exists());
        MongoRuntime.call(new MongoScriptAction(testScope, "removeFile('"
                + dummyFile.getAbsolutePath() + "');"));
        assertFalse(dummyFile.exists());
    }

//    @Test
//    public void test_md5sumFile() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void test_fuzzFile() {
//        fail("Not yet implemented");
//    }

    @Test(expected = WrappedException.class)
    public void test_run() {
        MongoRuntime.call(new MongoScriptAction(testScope, "run();"));
    }

    @Test(expected = WrappedException.class)
    public void test_runProgram() {
        MongoRuntime.call(new MongoScriptAction(testScope, "runProgram();"));
    }

    @Test(expected = WrappedException.class)
    public void test_getMemInfo() {
        MongoRuntime.call(new MongoScriptAction(testScope, "getMemInfo();"));
    }

}
