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
import org.mozilla.javascript.ScriptableObject;

import com.github.nlloyd.hornofmongo.MongoContext;
import com.github.nlloyd.hornofmongo.MongoScope;
import com.github.nlloyd.hornofmongo.exception.MongoRuntimeException;
import com.github.nlloyd.hornofmongo.exception.MongoScopeException;

/**
 * @author nlloyd
 * 
 */
public abstract class ScriptableMongoObject extends ScriptableObject {

    /**
     * 
     */
    private static final long serialVersionUID = 839135097878723000L;

    /**
     * Reference to the owning {@link MongoScope} to check for certain
     * scope-level behavior flags.
     */
    protected MongoScope mongoScope;

    public ScriptableMongoObject() {
        super();
    }

    /**
     * Intercepts {@link ScriptableObject#get(String, Scriptable)} calls in
     * order to determine if protoype and parent scope has been set.
     * 
     * @see org.mozilla.javascript.ScriptableObject#get(java.lang.String,
     *      org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(String name, Scriptable start) {
        resolveParentAndPrototype();
        return super.get(name, start);
    }

    /**
     * Intercepts {@link ScriptableObject#get(int, Scriptable)} calls in order
     * to determine if protoype and parent scope has been set.
     * 
     * @see org.mozilla.javascript.ScriptableObject#get(int,
     *      org.mozilla.javascript.Scriptable)
     */
    @Override
    public Object get(int index, Scriptable start) {
        resolveParentAndPrototype();
        return super.get(index, start);
    }

    /**
     * Special handling for situations where the "new" keyword isn't used when
     * calling a constructor. This results in a strange situation in Rhino where
     * a ScriptableObject is created without the parent scope or prototype being
     * set. This will check for {@link ScriptableMongoObject} instances for that
     * particular situation and leverage previously created {@link ThreadLocal
     * <MongoScope>} to grab the current {@link MongoScope} for the currently
     * executing thread.
     */
    private void resolveParentAndPrototype() {
        // if we dont have a parent scope or prototype yet, grab from
        // threadlocal
        // threadlocal is setup by MongoRuntime.call() method and the
        // MongoScope's
        // ContextFactory.Listener implementation
        if (getParentScope() == null) {
            Context context = Context.getCurrentContext();
            if (context instanceof MongoContext) {
                MongoScope executionScope = ((MongoContext) context)
                        .getExecutingMongoScope();
                setParentScope(executionScope);
            }
        }
        if (getPrototype() == null) {
            Scriptable classPrototype = ScriptableObject.getClassPrototype(
                    mongoScope, this.getClassName());
            if (classPrototype == null)
                Context.throwAsScriptRuntimeEx(new MongoScopeException(
                        "could not resolve prototype for class: "
                                + this.getClassName()));
            setPrototype(classPrototype);
        }
    }

    /**
     * Overrides {@link ScriptableObject} to capture the top level
     * {@link MongoScope} instance in a local member variable.
     */
    @Override
    public void setParentScope(Scriptable m) {
        super.setParentScope(m);
        Scriptable topScope = ScriptableObject.getTopLevelScope(this);
        if (topScope instanceof MongoScope)
            mongoScope = (MongoScope) topScope;
        else
            throw new MongoRuntimeException(this.getClass().getName()
                    + " was not created within a MongoScope!");
    }

}
