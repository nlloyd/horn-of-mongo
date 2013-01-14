package org.github.nlloyd.hornofmongo.adaptor;

import java.net.UnknownHostException;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;

import com.mongodb.DBCollection;

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
	
	protected com.mongodb.Mongo innerMongo;
	
	protected String host;

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	@JSConstructor
	public Mongo() throws UnknownHostException {
		super();
		initMongo("127.0.0.1");
	}
	
	@JSConstructor
	public Mongo(String host) throws UnknownHostException {
		super();
		initMongo(host);
	}
	
	private void initMongo(String host) throws UnknownHostException {
		this.host = host;
		this.innerMongo = new com.mongodb.Mongo(this.host);
	}
	
	// --- Mongo js function implementation ---
	
	public Object insert(String ns, Object query) {
		System.out.println(ns);
		System.out.println(query);
//		innerMongo.getD
		return null;
	}
	
	public Object find(final String ns , final Object query , final Object fields , int limit , int skip , int batchSize , int options) {
		String[] nsBits = ns.split(".");
		// TODO some sort of assertion that nsBits.length == 2?
		com.mongodb.DB db = innerMongo.getDB(nsBits[0]);
		DBCollection collection = db.getCollection(nsBits[1]);
		
		return null;
	}
	
	protected com.mongodb.DB getDB(String dbName) {
		return innerMongo.getDB(dbName);
	}
	
}
