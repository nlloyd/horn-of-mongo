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
package com.github.nlloyd.hornofmongo.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import com.github.nlloyd.hornofmongo.MongoScope;
import com.mongodb.MongoException;

/**
 * A concrete implementation of {@link MongoAction} that wraps a JavaScript
 * script for execution in either String, Reader, or File form.
 * 
 * @author nlloyd
 * 
 */
public class MongoScriptAction extends MongoAction {

    protected String scriptName = "anon";
    protected String scriptString = null;
    protected Reader scriptReader = null;

    public MongoScriptAction(MongoScope mongoScope, String script) {
        super(mongoScope);
        this.scriptString = script;
    }

    public MongoScriptAction(MongoScope mongoScope, File script) {
        super(mongoScope);
        this.scriptName = script.getName();
        try {
            this.scriptReader = new BufferedReader(new FileReader(script));
        } catch (FileNotFoundException e) {
            throw new MongoException("Attempted to execute non-existent file "
                    + script.getAbsolutePath(), e);
        }
    }

    public MongoScriptAction(MongoScope mongoScope, Reader script) {
        super(mongoScope);
        this.scriptReader = script;
    }

    public MongoScriptAction(MongoScope mongoScope, String name, String script) {
        super(mongoScope);
        this.scriptName = name;
        this.scriptString = script;
    }

    public MongoScriptAction(MongoScope mongoScope, String name, File script) {
        super(mongoScope);
        this.scriptName = name;
        try {
            this.scriptReader = new BufferedReader(new FileReader(script));
        } catch (FileNotFoundException e) {
            throw new MongoException("Attempted to execute non-existent file "
                    + script.getAbsolutePath(), e);
        }
    }

    public MongoScriptAction(MongoScope mongoScope, String name, Reader script) {
        super(mongoScope);
        this.scriptName = name;
        this.scriptReader = script;
    }

    /**
     * Uses the provided context to execute either a String script,
     * 
     * @see org.mozilla.javascript.ContextAction#run(org.mozilla.javascript.Context)
     */
    @Override
    public Object doRun(Context cx) {
        Object result = null;
        String script;
        if (StringUtils.isNotBlank(scriptString))
            script = scriptString;
        else {
            try {
                script = readFully(scriptReader);
            } catch (IOException e) {
                throw new MongoException(
                        "IOException when executing a script in Reader form.",
                        e);
            }
        }

        // this could be a command like "use", "show", or "help" which must be a
        // one-liner without a semi-colon line terminator
        boolean wasCmd = false;
        if (isOneLine(script)) {
            script = script.trim();
            String cmd = script;
            if (cmd.indexOf(' ') > 0)
                cmd = script.substring(0, cmd.indexOf(' '));
            if (cmd.indexOf('"') == -1) {
                StringBuilder cmdCheck = new StringBuilder();
                cmdCheck.append("__iscmd__ = shellHelper[\"");
                cmdCheck.append(cmd);
                cmdCheck.append("\"];");
                cx.evaluateString(mongoScope, cmdCheck.toString(),
                        "(shellhelp1)", 0, null);
                if (Context.toBoolean(ScriptableObject.getProperty(
                        mongoScope, "__iscmd__"))) {
                    StringBuilder cmdScript = new StringBuilder();
                    cmdScript.append("shellHelper( \"");
                    cmdScript.append(cmd);
                    cmdScript.append("\" , \"");
                    cmdScript.append(script.substring(cmd.length()));
                    cmdScript.append("\");");
                    try {
                    cx.evaluateString(mongoScope, cmdScript.toString(),
                            "(shellhelp2)", 0, null);
                    } finally {
                        wasCmd = true;
                    }
                }
            }
        }

        if (!wasCmd) {
            result = cx.evaluateString(mongoScope, script, scriptName, 0, null);
        }

        return result;
    }

    /**
     * Borrowed from Rhino's Parser class.
     * 
     * @param reader
     * @return
     * @throws IOException
     */
    private String readFully(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        try {
            char[] cbuf = new char[1024];
            StringBuilder sb = new StringBuilder(1024);
            int bytes_read;
            while ((bytes_read = in.read(cbuf, 0, 1024)) != -1) {
                sb.append(cbuf, 0, bytes_read);
            }
            return sb.toString();
        } finally {
            in.close();
        }
    }

    private boolean isOneLine(final String script) {
        LineNumberReader lnr = new LineNumberReader(new StringReader(script));
        try {
            @SuppressWarnings("unused")
            int lastRead;
            while ((lastRead = lnr.read()) != -1) {
            }
        } catch (IOException e) {
        }
        // 0 lines just means no line terminator in the string
        return lnr.getLineNumber() <= 1;
    }

}
