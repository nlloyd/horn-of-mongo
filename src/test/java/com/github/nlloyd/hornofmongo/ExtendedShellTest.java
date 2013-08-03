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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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
                "ls('jstests');"));
        Object resultConverted2 = BSONizer.convertJStoBSON(result2, true);
        assertTrue(resultConverted2 instanceof List<?>);
        @SuppressWarnings("unchecked")
        List<Object> lsResult2 = (List<Object>) resultConverted2;
        File[] filesList2 = new File(testScope.getCwd(), "jstests").listFiles();
        for (File file : filesList2) {
            String nameToCheck = "./jstests/" + file.getName();
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
    public void test_cd() throws IOException {
        MongoRuntime.call(new MongoScriptAction(testScope, "cd('jstests')"));
        assertEquals(new File("jstests").getCanonicalPath(), testScope.getCwd()
                .getCanonicalPath());
        MongoRuntime.call(new MongoScriptAction(testScope, "cd('../')"));
        assertEquals(
                new File(System.getProperty("user.dir")).getCanonicalPath(),
                testScope.getCwd().getCanonicalPath());
        // now with an absolute path
        File absPath = new File(System.getProperty("user.dir"), "jstests");
        MongoRuntime.call(new MongoScriptAction(testScope, "cd('"
                + absPath.getCanonicalPath() + "')"));
        assertEquals(new File("jstests").getCanonicalPath(), testScope.getCwd()
                .getCanonicalPath());
        MongoRuntime.call(new MongoScriptAction(testScope, "cd('"
                + absPath.getParentFile().getCanonicalPath() + "')"));
        assertEquals(
                new File(System.getProperty("user.dir")).getCanonicalPath(),
                testScope.getCwd().getCanonicalPath());
    }

    @Test
    public void test_mkdir() throws IOException {
        final String pathToMake = "make_dir_test/make/this/dir";

        FileUtils.deleteDirectory(new File("make_dir_test"));

        final File expectedDir = new File(pathToMake);
        Object wasMade = MongoRuntime.call(new MongoScriptAction(testScope,
                "mkdir('" + pathToMake + "')"));
        assertTrue(wasMade instanceof Boolean);
        assertTrue((Boolean) wasMade);
        assertTrue(expectedDir.exists());
        assertTrue(expectedDir.isDirectory());

        // cleanup
        FileUtils.deleteDirectory(new File("make_dir_test"));
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
                "listFiles('jstests');"));
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
        File[] filesList2 = new File(testScope.getCwd(), "jstests").listFiles();
        for (File file : filesList2) {
            String nameToCheck = "./jstests/" + file.getName();
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

    @Test
    public void test_md5sumFile() throws IOException {
        File dummyFile = new File("md5sumTest.txt");
        if (dummyFile.exists())
            dummyFile.delete();
        BufferedWriter writer = new BufferedWriter(new FileWriter(dummyFile));
        writer.write("imatest!");
        writer.close();

        // generated against the file made above using the official shell
        final String md5sumExpected = "e51f6d6e9dda063436a59df30b17856e";

        String md5sumActual = (String) MongoScope.md5sumFile(null, testScope,
                new Object[] { dummyFile.getAbsolutePath() }, null);
        assertEquals(md5sumExpected, md5sumActual);

        String md5sumActualFromScript = (String) MongoRuntime
                .call(new MongoScriptAction(testScope, "md5sumFile('"
                        + dummyFile.getAbsolutePath() + "');"));
        assertEquals(md5sumExpected, md5sumActualFromScript);
    }

    @Test
    public void test_fuzzFile() throws IOException {
        final int byteToFuzz = 1;
        final String fileContent = "imatest!";
        byte[] fileContentBytes = fileContent.getBytes();
        fileContentBytes[byteToFuzz] = (byte) ~((int) fileContentBytes[byteToFuzz]);
        final String fuzzedFileContent = new String(fileContentBytes);

        File dummyFile = new File("fuzzFileTest.txt");
        if (dummyFile.exists())
            dummyFile.delete();
        BufferedWriter writer = new BufferedWriter(new FileWriter(dummyFile));
        writer.write(fileContent);
        writer.close();

        MongoScope.fuzzFile(null, testScope,
                new Object[] { dummyFile.getAbsolutePath(), byteToFuzz }, null);

        BufferedReader reader = new BufferedReader(new FileReader(dummyFile));
        String fuzzedContent = reader.readLine();
        reader.close();

        assertEquals(fuzzedFileContent, fuzzedContent);

        MongoRuntime.call(new MongoScriptAction(testScope, "fuzzFile('"
                + dummyFile.getAbsolutePath() + "', " + byteToFuzz + ");"));

        reader = new BufferedReader(new FileReader(dummyFile));
        String unfuzzedContent = reader.readLine();
        reader.close();

        assertEquals(fileContent, unfuzzedContent);
    }

    @Test
    public void test_run() {
        String result = (String) MongoRuntime.call(new MongoScriptAction(
                testScope, "run();"));
        assertTrue(result.contains("not supported"));
    }

    @Test
    public void test_runProgram() {
        String result = (String) MongoRuntime.call(new MongoScriptAction(
                testScope, "runProgram();"));
        assertTrue(result.contains("not supported"));
    }

    @Test
    public void test_getMemInfo() {
        String result = (String) MongoRuntime.call(new MongoScriptAction(
                testScope, "getMemInfo();"));
        assertTrue(result.contains("not supported"));
    }

}
