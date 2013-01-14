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
package org.github.nlloyd.hornofmongo.adaptor;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;

/**
 * @author nlloyd
 *
 */
public class ObjectId extends org.bson.types.ObjectId implements Scriptable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5594412197023274036L;

	private Scriptable wrappedHostInstance;
	
	@JSConstructor
	public ObjectId() {
		super();
		wrappedHostInstance = new ScriptableObjectId();
	}
	
	@JSConstructor
	public ObjectId(Scriptable scope, Scriptable prototype) {
		super();
		wrappedHostInstance = new ScriptableObjectId(scope, prototype);
	}

	@JSConstructor
	public ObjectId(String str) {
		super(str);
		wrappedHostInstance = new ScriptableObjectId();
	}
	
	public void delete(String name) {
		wrappedHostInstance.delete(name);
	}

	public void delete(int index) {
		wrappedHostInstance.delete(index);
	}

	public Object get(String name, Scriptable start) {
		if(name.equals("str")) {
			return super.toString();
		} else {
			return wrappedHostInstance.get(name, start);
		}
	}

	public Object get(int index, Scriptable start) {
		return wrappedHostInstance.get(index, start);
	}

	public String getClassName() {
		return wrappedHostInstance.getClassName();
	}

	public Object getDefaultValue(Class<?> hint) {
		return wrappedHostInstance.getDefaultValue(hint);
	}

	public Object[] getIds() {
		return wrappedHostInstance.getIds();
	}

	public Scriptable getParentScope() {
		return wrappedHostInstance.getParentScope();
	}

	public Scriptable getPrototype() {
		return wrappedHostInstance.getPrototype();
	}

	public boolean has(String name, Scriptable start) {
		if(name.equals("str")) {
			return true;
		} else {
			return wrappedHostInstance.has(name, start);
		}
	}

	public boolean has(int index, Scriptable start) {
		return wrappedHostInstance.has(index, start);
	}

	public boolean hasInstance(Scriptable instance) {
		return wrappedHostInstance.hasInstance(instance);
	}

	public void put(String name, Scriptable start, Object value) {
		wrappedHostInstance.put(name, start, value);
	}

	public void put(int index, Scriptable start, Object value) {
		wrappedHostInstance.put(index, start, value);
	}

	public void setParentScope(Scriptable parent) {
		wrappedHostInstance.setParentScope(parent);
	}

	public void setPrototype(Scriptable prototype) {
		wrappedHostInstance.setPrototype(prototype);
	}
	
	/**
	 * Nested ScriptableObject implementation bound to this ObjectId type to 
	 * act as the root of the JavaScript host object core of the ObjectId type which
	 * already extends {@link org.bson.types.ObjectId}.
	 * 
	 * @author nlloyd
	 *
	 */
	private static class ScriptableObjectId extends ScriptableObject {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1337766591881558897L;
		
		public ScriptableObjectId() {
			super();
		}
		
		public ScriptableObjectId(Scriptable scope, Scriptable prototype) {
			super(scope, prototype);
		}

		@Override
		public String getClassName() {
			return ObjectId.class.getSimpleName();
		}
		
	}

}
