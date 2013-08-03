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
package com.github.nlloyd.hornofmongo.util;

import java.io.File;
import java.io.IOException;

/**
 * Very basic implementation of CurrentDirectoryHandler that just wraps a single
 * member variable and a {@link File} constructor call.
 * 
 * @author nlloyd
 * 
 */
public class DefaultCurrentDirectoryHandler implements CurrentDirectoryHandler {

    private File cwd = new File(".");

    /**
     * @see com.github.nlloyd.hornofmongo.util.CurrentDirectoryHandler#getCurrentDirectory()
     */
    @Override
    public File getCurrentDirectory() {
        return cwd;
    }

    /**
     * @see com.github.nlloyd.hornofmongo.util.CurrentDirectoryHandler#setCurrentDirectory(java.io.File)
     */
    @Override
    public void setCurrentDirectory(File f) {
        cwd = f;
    }

    /**
     * @see com.github.nlloyd.hornofmongo.util.CurrentDirectoryHandler#resolveFilePath(java.lang.String)
     */
    @Override
    public File resolveFilePath(String path) throws IOException {
        File resolvedFile;
        // if this is a relative path, use cwd instead of system property
        // user.dir
        if (path.matches("^(/|([a-zA-Z]{1}:\\\\)).*$"))
            resolvedFile = new File(path);
        else
            resolvedFile = new File(cwd, path);
        return resolvedFile;
    }

}
