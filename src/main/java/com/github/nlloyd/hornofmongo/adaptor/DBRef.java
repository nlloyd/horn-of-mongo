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
package com.github.nlloyd.hornofmongo.adaptor;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;

/**
 * @author nlloyd
 * 
 */
public class DBRef extends ScriptableMongoObject {

    /**
     * 
     */
    private static final long serialVersionUID = 7845123364974187876L;

    private String ns;
    private Object id;

    public DBRef() {
        super();
    }

    @JSConstructor
    public DBRef(Object ref, Object id) {
        super();
        if (!(ref instanceof String))
            Context.throwAsScriptRuntimeEx(new IllegalArgumentException(
                    "Error: DBRef 1st parameter must be a string"));
        if ((ref instanceof Undefined) || (id instanceof Undefined))
            Context.throwAsScriptRuntimeEx(new IllegalArgumentException(
                    "Error: DBRef needs 2 arguments"));
        this.ns = Context.toString(ref);
        this.id = id;
        put("$ref", this, ref);
        put("$id", this, id);
    }

    @JSConstructor
    public DBRef(Object ref, Object id, Object invalid) {
        this(ref, id);
        if (!(invalid instanceof Undefined))
            Context.throwAsScriptRuntimeEx(new IllegalArgumentException(
                    "Error: DBRef needs 2 arguments"));
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return the ns
     */
    public String getNs() {
        return ns;
    }

    /**
     * @param ns
     *            the ns to set
     */
    public void setNs(String ns) {
        this.ns = ns;
        put("$ref", this, ns);
    }

    /**
     * @return the id
     */
    public Object getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Object id) {
        this.id = id;
        put("$id", this, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mozilla.javascript.ScriptableObject#put(java.lang.String,
     * org.mozilla.javascript.Scriptable, java.lang.Object)
     */
    @Override
    public void put(String name, Scriptable start, Object value) {
        if ("$id".equals(name))
            this.id = value;
        else if ("$ref".equals(name))
            this.ns = Context.toString(value);
        super.put(name, start, value);
    }

}
