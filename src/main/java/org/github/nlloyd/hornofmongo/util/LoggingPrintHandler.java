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
package org.github.nlloyd.hornofmongo.util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PrintHandler} implementation that wraps a {@link Logger}.
 * Logger is either a default info-level logger for the package path
 * org.github.nlloyd.hornofmongo, or is a logger and logging level
 * passed in a constructor.
 * 
 * @author nlloyd
 *
 */
public class LoggingPrintHandler implements PrintHandler {

    /**
     * slf4j doesn't provide an enum set like this so here we are!
     *
     */
    public static enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR;
    }
    
    protected Logger printLogger = LoggerFactory.getLogger("org.github.nlloyd.hornofmongo");
    protected Level printLogLevel = Level.INFO;
    
    public LoggingPrintHandler() {}
    
    public LoggingPrintHandler(Logger printLogger) {
        if(printLogger == null)
            throw new IllegalArgumentException("printLogger cannot be null");
        this.printLogger = printLogger;
    }
    
    public LoggingPrintHandler(Level printLogLevel) {
        if(printLogLevel == null)
            throw new IllegalArgumentException("printLogLevel cannot be null");
        this.printLogLevel = printLogLevel;
    }
    
    public LoggingPrintHandler(Logger printLogger, Level printLogLevel) {
        if(printLogger == null)
            throw new IllegalArgumentException("printLogger cannot be null");
        if(printLogLevel == null)
            throw new IllegalArgumentException("printLogLevel cannot be null");
        this.printLogger = printLogger;
        this.printLogLevel = printLogLevel;
    }

    /**
     * @see org.github.nlloyd.hornofmongo.util.PrintHandler#doPrint(org.mozilla.javascript.Context, org.mozilla.javascript.Scriptable, java.lang.Object[])
     */
    @Override
    public void doPrint(Context cx, Scriptable s, Object[] args) {
        StringBuilder sb = new StringBuilder();
        for(Object arg : args) {
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(Context.toString(arg));
        }
        
        String msg = sb.toString();
        
        switch(printLogLevel) {
        case ERROR:
            printLogger.error(msg);
            break;
        case WARN:
            printLogger.warn(msg);
            break;
        case INFO:
            printLogger.info(msg);
            break;
        case DEBUG:
            printLogger.debug(msg);
            break;
        case TRACE:
            printLogger.trace(msg);
            break;
        }
    }

}
