package org.github.nlloyd.hornofmongo.adaptor;

import java.net.UnknownHostException;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;

/**
 * JavaScript host Mongo object that acts as an adaptor between the
 * JavaScript Mongo API and the {@link com.mongodb.Mongo} Java driver class.
 * 
 * @author nlloyd
 *
 */
public class Mongo extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6810309240609504412L;
	
	private com.mongodb.Mongo innerMongo;
	
	private String host;

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	@JSConstructor
	public Mongo() throws UnknownHostException {
		initMongo("127.0.0.1");
	}
	
	@JSConstructor
	public Mongo(String host) throws UnknownHostException {
		initMongo(host);
	}
	
	private void initMongo(String host) throws UnknownHostException {
		this.host = host;
		innerMongo = new com.mongodb.Mongo(this.host);
	}
	
	// --- Mongo js function implementation ---
	
	public Object insert(String ns, Object query) {
		System.out.println(ns);
		System.out.println(query);
//		innerMongo.getD
		return null;
	}
}
