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
package com.github.nlloyd.hornofmongo;

import static java.util.Collections.synchronizedSet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.bson.BSON;
import org.bson.io.BasicOutputBuffer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.tools.shell.Global;

import com.github.nlloyd.hornofmongo.action.MongoAction;
import com.github.nlloyd.hornofmongo.action.NewInstanceAction;
import com.github.nlloyd.hornofmongo.adaptor.BinData;
import com.github.nlloyd.hornofmongo.adaptor.DB;
import com.github.nlloyd.hornofmongo.adaptor.DBCollection;
import com.github.nlloyd.hornofmongo.adaptor.DBPointer;
import com.github.nlloyd.hornofmongo.adaptor.DBQuery;
import com.github.nlloyd.hornofmongo.adaptor.DBRef;
import com.github.nlloyd.hornofmongo.adaptor.InternalCursor;
import com.github.nlloyd.hornofmongo.adaptor.MaxKey;
import com.github.nlloyd.hornofmongo.adaptor.MinKey;
import com.github.nlloyd.hornofmongo.adaptor.Mongo;
import com.github.nlloyd.hornofmongo.adaptor.NumberInt;
import com.github.nlloyd.hornofmongo.adaptor.NumberLong;
import com.github.nlloyd.hornofmongo.adaptor.ObjectId;
import com.github.nlloyd.hornofmongo.adaptor.Timestamp;
import com.github.nlloyd.hornofmongo.exception.MongoRuntimeException;
import com.github.nlloyd.hornofmongo.exception.MongoScopeException;
import com.github.nlloyd.hornofmongo.exception.MongoScriptException;
import com.github.nlloyd.hornofmongo.util.BSONizer;
import com.github.nlloyd.hornofmongo.util.ClearHandler;
import com.github.nlloyd.hornofmongo.util.CurrentDirectoryHandler;
import com.github.nlloyd.hornofmongo.util.DefaultCurrentDirectoryHandler;
import com.github.nlloyd.hornofmongo.util.PrintHandler;
import com.github.nlloyd.hornofmongo.util.QuitHandler;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBEncoder;
import com.mongodb.MongoException;
import com.mongodb.util.Util;

/**
 * The MongoDB-specific {@link Scope} implementation. This extends
 * {@link Global} to add MongoDB shell JavaScript global functions objects, and
 * variables.
 * 
 * Meant to emulate engine.cpp (and the more specific engine_*.cpp
 * implementations) in the official mongodb source.
 * 
 * @author nlloyd
 * 
 */
public class MongoScope extends Global {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4650743395507077775L;

    private static ThreadLocal<Random> threadLocalRandomGen = new ThreadLocal<Random>() {
        protected Random initialValue() {
            return new Random();
        }
    };

    private static String[] mongoApiFiles = { "mongodb/assert.js",
            "mongodb/types.js", "mongodb/utils.js", "mongodb/utils_sh.js",
            "mongodb/db.js", "mongodb/mongo.js", "mongodb/mr.js",
            "mongodb/query.js", "mongodb/collection.js",
            "mongodb/servers_misc.js", "mongodb/servers.js",
            "mongodb/shardingtest.js" };

    private CurrentDirectoryHandler currentDirHandler = new DefaultCurrentDirectoryHandler();

    private PrintHandler printHandler;

    private ClearHandler clearHandler;

    private QuitHandler quitHandler;

    /**
     * If true then some {@link MongoException} will be caught and the messages
     * will be printed to stdout depending on behavior of the official mongodb
     * client shell. If false then the exceptions will be rethrown.
     * 
     * Defaults to false.
     * 
     * Meant to support the same behavior as the official mongodb shell client.
     */
    private boolean stdoutMongoErrorMessages = false;

    /**
     * {@link http
     * ://docs.mongodb.org/manual/release-notes/drivers-write-concern/}
     * 
     * Default write concern has changed for all official mongo drivers, which
     * differs from the default mongo shell behavior. Set this flag to true
     * configure this MongoScope to behave like mongo shell as opposed to mongo
     * java driver (defaults to false).
     */
    private boolean useMongoShellWriteConcern = false;

    private Set<Mongo> mongoConnections = synchronizedSet(new HashSet<Mongo>());

    public MongoScope() {
        super();
    }

    public MongoScope(Context context) throws IllegalAccessException,
            InstantiationException, InvocationTargetException {
        super(context);
        initMongoJS(context);
        execCoreFiles(context);
    }

    /**
     * @return the stdoutMongoErrorMessages
     */
    public boolean isStdoutMongoErrorMessages() {
        return stdoutMongoErrorMessages;
    }

    /**
     * @param stdoutMongoErrorMessages
     *            the stdoutMongoErrorMessages to set
     */
    public void setStdoutMongoErrorMessages(boolean stdoutMongoErrorMessages) {
        this.stdoutMongoErrorMessages = stdoutMongoErrorMessages;
    }

    /**
     * @return the useMongoShellWriteConcern
     */
    public boolean useMongoShellWriteConcern() {
        return useMongoShellWriteConcern;
    }

    /**
     * @param useMongoShellWriteConcern
     *            the useMongoShellWriteConcern to set
     */
    public void setUseMongoShellWriteConcern(boolean useMongoShellWriteConcern) {
        this.useMongoShellWriteConcern = useMongoShellWriteConcern;
    }

    /**
     * @return the currentDirHandler
     */
    public CurrentDirectoryHandler getCurrentDirHandler() {
        return currentDirHandler;
    }

    /**
     * @param currentDirHandler
     *            the currentDirHandler to set
     */
    public void setCurrentDirHandler(CurrentDirectoryHandler currentDirHandler) {
        this.currentDirHandler = currentDirHandler;
    }

    /**
     * @return the printHandler
     */
    public PrintHandler getPrintHandler() {
        return printHandler;
    }

    /**
     * @param printHandler
     *            the printHandler to set
     */
    public void setPrintHandler(PrintHandler printHandler) {
        this.printHandler = printHandler;
    }

    /**
     * @return the clearHandler
     */
    public ClearHandler getClearHandler() {
        return clearHandler;
    }

    /**
     * @param clearHandler
     *            the clearHandler to set
     */
    public void setClearHandler(ClearHandler clearHandler) {
        this.clearHandler = clearHandler;
    }

    /**
     * @return the quitHandler
     */
    public QuitHandler getQuitHandler() {
        return quitHandler;
    }

    /**
     * @param quitHandler
     *            the quitHandler to set
     */
    public void setQuitHandler(QuitHandler quitHandler) {
        this.quitHandler = quitHandler;
    }

    /**
     * @return the cwd
     */
    public File getCwd() {
        return getCurrentDirHandler().getCurrentDirectory();
    }

    /**
     * @param cwd
     *            the cwd to set
     */
    public void setCwd(File cwd) {
        getCurrentDirHandler().setCurrentDirectory(cwd);
    }

    public void addMongoConnection(Mongo mongoConnection) {
        mongoConnections.add(mongoConnection);
    }

    public int countMongoConnections() {
        return mongoConnections.size();
    }

    public void removeMongoConnection(Mongo mongoConnection) {
        mongoConnections.remove(mongoConnection);
    }

    public void cleanup() {
        for (Mongo connection : mongoConnections) {
            connection.close();
        }
        mongoConnections.clear();
    }

    protected void initMongoJS(Context context) throws IllegalAccessException,
            InstantiationException, InvocationTargetException {
        if (!isInitialized()) {
            super.init(context);
        }

        String[] names = { "quit", "sleep", "hex_md5", "_isWindows", "_srand",
                "_rand", "UUID", "MD5", "HexData", "print", "ls", "cd",
                "mkdir", "pwd", "listFiles", "hostname", "cat", "removeFile",
                "md5sumFile", "fuzzFile", "run", "runProgram", "getMemInfo",
                "load" };
        defineFunctionProperties(names, this.getClass(),
                ScriptableObject.DONTENUM);
        ScriptableObject objectPrototype = (ScriptableObject) ScriptableObject
                .getClassPrototype(this, "Object");
        objectPrototype.defineFunctionProperties(new String[] { "bsonsize" },
                this.getClass(), ScriptableObject.DONTENUM);

        ScriptableObject.defineClass(this, Mongo.class, false, false);
        ScriptableObject.defineClass(this, ObjectId.class, false, false);
        ScriptableObject.defineClass(this, DB.class, false, false);
        ScriptableObject.defineClass(this, DBCollection.class, false, false);
        ScriptableObject.defineClass(this, InternalCursor.class, false, false);
        ScriptableObject.defineClass(this, DBQuery.class, false, false);
        ScriptableObject.defineClass(this, DBPointer.class, false, false);
        ScriptableObject.defineClass(this, BinData.class, false, false);

        ScriptableObject.defineClass(this, Timestamp.class, false, false);
        ScriptableObject.defineClass(this, NumberLong.class, false, false);
        ScriptableObject.defineClass(this, NumberInt.class, false, false);
        ScriptableObject.defineClass(this, MinKey.class, false, false);
        ScriptableObject.defineClass(this, MaxKey.class, false, false);

        ScriptableObject.defineClass(this, DBRef.class, false, false);
    }

    protected void execCoreFiles(Context context) {
        for (String jsSetupFile : mongoApiFiles) {
            try {
                context.evaluateReader(this, loadFromClasspath(jsSetupFile),
                        jsSetupFile, 0, null);
            } catch (IOException e) {
                throw new MongoScopeException(
                        "Caught IOException attempting to load from classpath: "
                                + jsSetupFile, e);
            } catch (JavaScriptException e) {
                throw new MongoScopeException(
                        "Caught JavaScriptException attempting to load from classpath: "
                                + jsSetupFile, e);
            }
        }
    }

    protected Reader loadFromClasspath(String filePath) {
        Reader reader = null;
        ClassLoader loader = this.getClass().getClassLoader();
        reader = new BufferedReader(new InputStreamReader(
                loader.getResourceAsStream(filePath)));
        return reader;
    }

    public void handleMongoException(MongoException me) {
        if (this.isStdoutMongoErrorMessages()) {
            // check error codes that do NOT result in an exception
            switch (me.getCode()) {
            case 10088: // cannot index parallel arrays [b] [d]
            case 10096: // invalid ns to index
            case 10098: // bad index key pattern
            case 10148: // Mod on _id not allowed
            case 10149: // Invalid mod field name, may not end in a period
            case 10159: // multi update only works with $ operators
            case 11000: // E11000 duplicate key error index:
            case 15896: // Modified field name may not start with $
            case 16650: // Cannot apply the positional operator without a
                        // corresponding query field containing an array.
            case 10141: // Cannot apply $push/$pushAll modifier to non-array
            case 16734: // Unknown index plugin '*' in index { *: * }
            case 10089: // can't remove from a capped collection
            case 13023: // 2d has to be first in index
            case 13028: // bits in geo index must be between 1 and 32
            case 13027: // point not in interval of [ -0.99995, 0.99995 ]
            case 16572: // Can't extract geo keys from object, malformed
                        // geometry?
            case 16687: // coarsestIndexedLevel must be >= 0
            case 16688: // finestIndexedLevel must be <= 30
            case 16241: // Currently only single field hashed index supported.
            case 16242: // Currently hashed indexes cannot guarantee uniqueness.
                        // Use a regular index.
            case 15855: // Ambiguous field name found in array (do not use
                        // numeric field names in embedded elements in
                        // an array)
            case 12505: // add index fails, too many indexes
                MongoScope.print(Context.getCurrentContext(), this,
                        new Object[] { me.getMessage() }, null);
                return;
            default:
                throw me;
            }
        } else
            throw me;
    }

    /* --- global and globalish utility functions --- */

    // public static Object eval(Context cx, Scriptable thisObj, Object[] args,
    // Function funObj) {
    // final String evalScript = Context.toString(args[0]);
    // Object result = MongoRuntime.call(new MongoScriptAction(
    // (MongoScope) thisObj, "(eval)", evalScript));
    // return result;
    // }

    // public static Object version(Context cx, Scriptable thisObj, Object[]
    // args,
    // Function funObj) {
    // return MongoScope.print(cx, thisObj, new Object[]{}, funObj);
    // }

    public static String hex_md5(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        // just like mongo native_hex_md5 call, only expects a single string
        final String str = Context.toString(args[0]);
        return Util.hexMD5(str.getBytes());
    }

    public static Boolean _isWindows(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static void _srand(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        Random randomGen = threadLocalRandomGen.get();
        if (args[0] instanceof Long)
            randomGen.setSeed((Long) args[0]);
        else
            randomGen.setSeed(Double.valueOf(args[0].toString()).longValue());
    }

    public static Double _rand(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        return threadLocalRandomGen.get().nextDouble();
    }

    private static final DBEncoder bsonEncoder = DefaultDBEncoder.FACTORY
            .create();

    public static Long bsonsize(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) throws IOException {
        DBObject bsonObj = (DBObject) BSONizer.convertJStoBSON(args[0], true);
        Long size = new Long(0);
        if (bsonObj != null) {
            BasicOutputBuffer byteBuffer = new BasicOutputBuffer();
            bsonEncoder.writeObject(byteBuffer, bsonObj);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byteBuffer.pipe(byteStream);
            size = new Long(byteStream.size());
        }
        return size;
    }

    public static final BinData UUID(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        if (args.length != 1)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "UUID needs 1 argument"));
        String uuidHex = Context.toString(args[0]);
        if (uuidHex.length() != 32)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "UUID string must have 32 characters"));
        String str = hexToBase64(uuidHex);
        BinData uuid = (BinData) MongoRuntime.call(new NewInstanceAction(
                (MongoScope) thisObj, "BinData", new Object[] { BSON.B_UUID,
                        str }));
        return uuid;
    }

    public static final BinData MD5(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        if (args.length != 1)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "MD5 needs 1 argument"));
        String md5Hex = Context.toString(args[0]);
        if (md5Hex.length() != 32)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "MD5 string must have 32 characters"));
        String str = hexToBase64(md5Hex);
        // MD5Type = 5 in bsontypes.h
        BinData md5 = (BinData) MongoRuntime.call(new NewInstanceAction(
                (MongoScope) thisObj, "BinData", new Object[] { 5, str }));
        return md5;
    }

    public static final BinData HexData(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        if (args.length != 2)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "HexData needs 2 arguments"));
        int type = Double.valueOf(Context.toNumber(args[0])).intValue();
        String str = hexToBase64(Context.toString(args[1]));
        BinData md5 = (BinData) MongoRuntime.call(new NewInstanceAction(
                (MongoScope) thisObj, "BinData", new Object[] { type, str }));
        return md5;
    }

    private static final String hexToBase64(final String hex) {
        String base64 = null;
        try {
            base64 = Base64
                    .encodeBase64String(Hex.decodeHex(hex.toCharArray()));
        } catch (DecoderException e) {
            Context.throwAsScriptRuntimeEx(e);
        }
        return base64;
    }

    public static Object print(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        if (thisObj instanceof MongoScope) {
            MongoScope mongoScope = (MongoScope) thisObj;
            if (mongoScope.getPrintHandler() != null)
                mongoScope.getPrintHandler().doPrint(cx, thisObj, args);
            else
                Global.print(cx, thisObj, args, funObj);
        } else {
            Global.print(cx, thisObj, args, funObj);
        }

        return Context.getUndefinedValue();
    }

    /**
     * Call the {@link ClearHandler} or noop.
     * 
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     */
    public static void clear(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        if (thisObj instanceof MongoScope) {
            MongoScope mongoScope = (MongoScope) thisObj;
            if (mongoScope.getClearHandler() != null)
                mongoScope.getClearHandler().doClear(cx, thisObj, args);
        }
    }

    /**
     * Call the {@link QuitHandler} or call the default
     * {@link Global#quit(Context, Scriptable, Object[], Function)}.
     */
    public static void quit(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        if (thisObj instanceof MongoScope) {
            MongoScope mongoScope = (MongoScope) thisObj;
            if (mongoScope.getQuitHandler() != null)
                mongoScope.getQuitHandler().doQuit(cx, thisObj, args);
            else
                Global.quit(cx, thisObj, args, funObj);
        } else
            Global.quit(cx, thisObj, args, funObj);
    }

    // *** extended shell functions ***

    public static Object ls(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        File path = null;
        MongoScope mongoScope = (MongoScope) thisObj;
        if (args.length == 0)
            path = mongoScope.getCwd();
        else if (args.length > 1)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "need to specify 1 argument to listFiles"));
        else {
            try {
                path = resolveFilePath(mongoScope, Context.toString(args[0]));
            } catch (IOException e) {
                Context.throwAsScriptRuntimeEx(new MongoScriptException(
                        "listFiles: " + e.getMessage()));
            }
        }

        // mongo only checks if the path exists, so we will do the same here
        // official mongo has ls() call listFiles()... im not doing that here
        // but i am honoring the error messages as they appear in the official
        // shell
        if (!path.exists())
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "listFiles: no such directory: " + path.getAbsolutePath()));
        if (!path.isDirectory())
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "listFiles: not a directory: " + path.getAbsolutePath()));

        List<String> files = new ArrayList<String>();
        for (File file : path.listFiles()) {
            String name = file.getPath();
            if (file.isDirectory())
                name += "/";
            files.add(name);
        }

        Object jsResult = BSONizer.convertBSONtoJS((MongoScope) thisObj, files);

        return jsResult;
    }

    public static Object cd(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        assertSingleArgument(args);
        MongoScope mongoScope = (MongoScope) thisObj;
        String newDirPath = Context.toString(args[0]);
        String result = null;
        try {
            File newCwd = resolveFilePath(mongoScope, newDirPath);
            if (newCwd.isDirectory()) {
                mongoScope.setCwd(newCwd.getCanonicalFile());
            } else
                result = "change directory failed";
        } catch (IOException e) {
            result = "change directory failed: " + e.getMessage();
        }
        return result;
    }

    public static Object mkdir(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        assertSingleArgument(args);
        boolean success = false;
        File newDir;
        try {
            newDir = resolveFilePath((MongoScope)thisObj, Context.toString(args[0])).getCanonicalFile();
            success = newDir.mkdirs();
        } catch (IOException e) {
        }
        // despite what the official shell does, i want to return if this fails
        return success;
    }

    public static Object pwd(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        return ((MongoScope) thisObj).getCwd().getAbsolutePath();
    }

    public static Object listFiles(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        File path = null;
        if (args.length == 0)
            path = ((MongoScope) thisObj).getCwd();
        else if (args.length > 1)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "need to specify 1 argument to listFiles"));
        else {
            try {
                path = resolveFilePath((MongoScope)thisObj, Context.toString(args[0]));
            } catch (IOException e) {
                Context.throwAsScriptRuntimeEx(new MongoScriptException(
                        "listFiles: " + e.getMessage()));
            }
        }

        // mongo only checks if the path exists, so we will do the same here
        if (!path.exists())
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "listFiles: no such directory: " + path.getAbsolutePath()));
        if (!path.isDirectory())
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "listFiles: not a directory: " + path.getAbsolutePath()));

        List<DBObject> files = new ArrayList<DBObject>();
        for (File file : path.listFiles()) {
            BasicDBObjectBuilder fileObj = new BasicDBObjectBuilder();
            fileObj.append("name", file.getPath()).append("isDirectory",
                    file.isDirectory());
            if (!file.isDirectory())
                fileObj.append("size", file.length());
            files.add(fileObj.get());
        }

        Object jsResult = BSONizer.convertBSONtoJS((MongoScope) thisObj, files);

        return jsResult;
    }

    public static Object hostname(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Context.throwAsScriptRuntimeEx(e);
            return Undefined.instance;
        }
    }

    public static Object cat(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        assertSingleArgument(args);
        try {
            File toRead = resolveFilePath((MongoScope) thisObj,
                    Context.toString(args[0])).getAbsoluteFile();
            return FileUtils.readFileToString(toRead);
        } catch (IOException e) {
            Context.throwAsScriptRuntimeEx(e);
            return Undefined.instance;
        }
    }

    public static Object removeFile(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        assertSingleArgument(args);
        boolean success = false;
        File toRemove;
        try {
            toRemove = resolveFilePath((MongoScope) thisObj,
                    Context.toString(args[0])).getCanonicalFile();
            success = FileUtils.deleteQuietly(toRemove);
        } catch (IOException e) {
        }
        return success;
    }

    public static Object md5sumFile(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        assertSingleArgument(args);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Context.throwAsScriptRuntimeEx(e);
        }
        File inFile;
        try {
            inFile = resolveFilePath((MongoScope) thisObj,
                    Context.toString(args[0])).getCanonicalFile();
        } catch (IOException e) {
            Context.throwAsScriptRuntimeEx(e);
            return null;
        }
        InputStream in = null;
        DigestInputStream dis = null;
        try {
            in = new BufferedInputStream(new FileInputStream(inFile));
            dis = new DigestInputStream(in, md);
            while (dis.available() > 0)
                dis.read();
            byte[] digest = md.digest();
            String hexStr = Hex.encodeHexString(digest);
            return hexStr;
        } catch (FileNotFoundException e) {
            Context.throwAsScriptRuntimeEx(e);
        } catch (IOException e) {
            Context.throwAsScriptRuntimeEx(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                }
            }
        }
        return Undefined.instance;
    }

    public static Object fuzzFile(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        if (args.length != 2)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "fuzzFile takes 2 arguments"));
        File fileToFuzz;
        try {
            fileToFuzz = resolveFilePath((MongoScope) thisObj,
                    Context.toString(args[0])).getCanonicalFile();
        } catch (IOException e) {
            Context.throwAsScriptRuntimeEx(new MongoScriptException(e));
            return null;
        }
        RandomAccessFile fuzzFile = null;
        try {
            fuzzFile = new RandomAccessFile(fileToFuzz, "rw");
            long fuzzPosition = Double.valueOf(Context.toNumber(args[1]))
                    .longValue();
            fuzzFile.seek(fuzzPosition);
            int byteToFuzz = fuzzFile.readByte();
            byteToFuzz = ~byteToFuzz;
            fuzzFile.seek(fuzzPosition);
            fuzzFile.write(byteToFuzz);
            fuzzFile.close();
        } catch (FileNotFoundException e) {
            Context.throwAsScriptRuntimeEx(e);
        } catch (IOException e) {
            Context.throwAsScriptRuntimeEx(e);
        } finally {
            if (fuzzFile != null) {
                try {
                    fuzzFile.close();
                } catch (IOException e) {
                }
            }
        }

        return Undefined.instance;
    }

    public static Object run(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        return "run(...) not supported";
    }

    public static Object runProgram(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        return "runProgram(...) not supported";
    }

    public static void sleep(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) throws NumberFormatException, InterruptedException {
        Thread.sleep(Double.valueOf(args[0].toString()).longValue());
    }

    public static Object getMemInfo(Context cx, Scriptable thisObj,
            Object[] args, Function funObj) {
        return "getMemInfo() not supported";
    }

    // *** ******************** ***

    public static final void assertSingleArgument(final Object[] args) {
        if (args.length != 1)
            Context.throwAsScriptRuntimeEx(new MongoScriptException(
                    "need to specify 1 argument"));
    }

    public static final class InitMongoScopeAction extends MongoAction {

        public InitMongoScopeAction() {
            super(null);
        }

        @Override
        public Object doRun(Context cx) {
            try {
                return new MongoScope(cx);
            } catch (IllegalAccessException e) {
                throw new MongoScopeException(
                        "caught when attempting to create a new MongoScope", e);
            } catch (InstantiationException e) {
                throw new MongoScopeException(
                        "caught when attempting to create a new MongoScope", e);
            } catch (InvocationTargetException e) {
                throw new MongoScopeException(
                        "caught when attempting to create a new MongoScope", e);
            }
        }

    }

    private static Reader loadFile(MongoScope scope, String filePath)
            throws IOException {
        Reader reader = null;
        File file = resolveFilePath(scope, filePath);
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                file)));
        return reader;
    }

    /**
     * Load and execute a set of JavaScript source files.
     * 
     * Overrides Global.load()
     * 
     */
    public static void load(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) {
        for (int i = 0; i < args.length; i++) {
            String filename = Context.toString(args[i]);
            try {
                cx.evaluateReader(thisObj,
                        loadFile((MongoScope) thisObj, filename), filename, 0,
                        null);
            } catch (Exception e) {
                throw new MongoScopeException("error loading js file: "
                        + filename, e);
            }
        }
    }

    private static File resolveFilePath(MongoScope scope, String filePath)
            throws IOException {
        File resolvedFile = null;
        if (scope != null)
            resolvedFile = scope.getCurrentDirHandler().resolveFilePath(
                    filePath);
        else
            throw new MongoRuntimeException(
                    "resolveFilePath() called with a null MongoScope!");
        return resolvedFile;
    }
}
