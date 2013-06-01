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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

/**
 * @author nlloyd
 *
 */
public class BinData extends ScriptableMongoObject {
	
	private int type;
	private String data;

	/**
	 * 
	 */
	private static final long serialVersionUID = 8887293438121607724L;
	
	public BinData() {}
	
	@JSConstructor
	public BinData(int type, Object obj) {
        super();
		if((type < 0) ||(type > 255)) {
			throw new IllegalArgumentException(
					"invalid BinData subtype -- range is 0..255 see bsonspec.org");
		}
		this.type = type;
		this.data = Context.toString(obj);
		byte[] tmpData = Base64.decodeBase64(this.data);
		put("type", this, type);
		put("len", this, tmpData.length);
	}

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
	
	@JSFunction
	public String toString() {
		return "BinData(" + type + ",\"" + data + "\")";
	}
	
	@JSFunction
	public String base64() {
		return data;
	}
	
	@JSFunction
	public String hex() {
	    String hexStr = Hex.encodeHexString(Base64.decodeBase64(this.data));
		return hexStr;
	}
	
	public int getType() {
	    return type;
	}
	
	public String getData() {
	    return data;
	}

}
