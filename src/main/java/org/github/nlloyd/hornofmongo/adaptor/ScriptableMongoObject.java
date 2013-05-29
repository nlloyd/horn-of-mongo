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
package org.github.nlloyd.hornofmongo.adaptor;

import org.github.nlloyd.hornofmongo.MongoScope;
import org.github.nlloyd.hornofmongo.exception.MongoRuntimeException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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

//    /**
//     * @see org.mozilla.javascript.ScriptableObject#getPrototype()
//     */
//    @Override
//    public Scriptable getPrototype() {
//        if (super.getPrototype() == null) {
//            if ((mongoScope != null)
//                    && (mongoScope.getChildPrototypeRegistry().containsKey(this
//                            .getClass())))
//                setPrototype(mongoScope.getChildPrototypeRegistry().get(
//                        this.getClass()));
//        }
//        return super.getPrototype();
//    }

    /**
     * Overrides {@link ScriptableObject}
     */
    @Override
    public void setParentScope(Scriptable m) {
        super.setParentScope(m);
        Scriptable topScope = ScriptableObject.getTopLevelScope(m);
        if (topScope instanceof MongoScope) {
            mongoScope = (MongoScope) topScope;
            if (!mongoScope.getChildPrototypeRegistry().containsKey(
                    this.getClass()))
                mongoScope.getChildPrototypeRegistry().put(this.getClass(),
                        this);
        } else
            throw new MongoRuntimeException(this.getClass().getName()
                    + " was not created within a MongoScope!");
    }
}
