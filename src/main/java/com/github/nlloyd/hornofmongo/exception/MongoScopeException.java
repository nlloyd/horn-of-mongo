package com.github.nlloyd.hornofmongo.exception;


/**
 * Exception to be thrown in the event of an unexpected issue with the
 * used MongoScope (or if the scope used is not a MongoScope).
 * 
 * This typically means the MongoScope was not properly initialized or
 * another scope without the MongoDB API is being used by the 
 * {@link org.mozilla.javascript.Context}.
 * 
 * @author nlloyd
 *
 */
public class MongoScopeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6521910127243538885L;
	
	public MongoScopeException(String message) {
		super(message);
	}
	
	public MongoScopeException(String message, Throwable cause) {
		super(message, cause);
	}

}
