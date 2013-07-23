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
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

/**
 * @author nlloyd
 *
 */
public class DBPointer extends ScriptableMongoObject {

    /**
     * 
     */
    private static final long serialVersionUID = -9106146988587782504L;
    
    private String ns;
    private ObjectId id;
    
    public DBPointer() {
        super();
    }
    
    @JSConstructor
    public DBPointer(Object ns, Object id) {
        super();
        if (!(ns instanceof String))
            Context.throwAsScriptRuntimeEx(new IllegalArgumentException(
                    "Error: DBPointer 1st parameter must be a string"));
        if (!(id instanceof ObjectId))
            Context.throwAsScriptRuntimeEx(new IllegalArgumentException(
                    "DBPointer 2nd parameter must be an ObjectId"));
        if ((ns instanceof Undefined) || (id instanceof Undefined))
            Context.throwAsScriptRuntimeEx(new IllegalArgumentException(
                    "Error: DBPointer needs 2 arguments"));
        this.ns = Context.toString(ns);
        this.id = (ObjectId)id;
    }

    @JSConstructor
    public DBPointer(Object ns, Object id, Object invalid) {
        this(ns, id);
        if (!(invalid instanceof Undefined))
            Context.throwAsScriptRuntimeEx(new IllegalArgumentException(
                    "Error: DBPointer needs 2 arguments"));
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
    @JSGetter
    public String getNs() {
        return ns;
    }

    /**
     * @param ns the ns to set
     */
    @JSSetter
    public void setNs(String ns) {
        this.ns = ns;
    }

    /**
     * @return the id
     */
    @JSGetter
    public ObjectId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @JSSetter
    public void setId(ObjectId id) {
        this.id = id;
    }

}
