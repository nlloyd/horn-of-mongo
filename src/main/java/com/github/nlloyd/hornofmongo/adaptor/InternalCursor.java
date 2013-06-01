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

import java.util.List;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

import com.github.nlloyd.hornofmongo.MongoScope;
import com.github.nlloyd.hornofmongo.util.BSONizer;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * @author nlloyd
 * 
 */
public class InternalCursor extends ScriptableMongoObject {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8770272501991840064L;

    private DBCursor cursor;

    /**
     * A mock result in cases where we need to simulate a findOne call to the
     * $cmd collection. Since we are not reimplementing the wire protocol and
     * are leveraging the mongo js api directly we need to do some hackery.
     */
    private Object fauxFindOneResult;
    private boolean fauxFindOneReturned = false;

    public InternalCursor() {
        super();
    }

    @JSConstructor
    public InternalCursor(Object obj) {
        super();
        if (obj instanceof DBCursor)
            this.cursor = (DBCursor) obj;
        else if (!(obj instanceof Undefined))
            this.fauxFindOneResult = obj;
    }

    /**
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @JSFunction
    public boolean hasNext() {
        boolean haveNext = false;
        if (cursor == null) {
            haveNext = !fauxFindOneReturned;
        } else {
            try {
                haveNext = cursor.hasNext();
            } catch (MongoException me) {
                handleMongoException(me);
            }
        }

        return haveNext;
    }

    @JSFunction
    public Object next() {
        Object next = null;
        if (cursor == null) {
            if (!fauxFindOneReturned) {
                fauxFindOneReturned = true;
                next = fauxFindOneResult;
            }
        } else {
            try {
                DBObject bsonNext = cursor.next();
                next = BSONizer.convertBSONtoJS(mongoScope, bsonNext);
            } catch (MongoException me) {
                handleMongoException(me);
            }
        }
        return next;
    }

    @JSFunction
    public int objsLeftInBatch() {
        int leftInBatch = 0;
        if (cursor == null) {
            leftInBatch = fauxFindOneReturned ? 0 : 1;
        } else {
            if (cursor.hasNext()) {
                List<Integer> batchSizes = cursor.getSizes();
                int currentBatchIdx = cursor.numGetMores();
                int currentBatchSize = batchSizes.get(currentBatchIdx);
                int countUpToCurBatch = 0;
                for (int i = 0; i < currentBatchIdx; i++)
                    countUpToCurBatch += batchSizes.get(i);
                leftInBatch = currentBatchSize
                        - (cursor.numSeen() - countUpToCurBatch);
            }
        }
        return leftInBatch;
    }

    public void setCursor(DBCursor cursor) {
        this.cursor = cursor;
    }

    private void handleMongoException(MongoException me) {
        if (mongoScope == null)
            mongoScope = (MongoScope) ScriptableObject.getTopLevelScope(this);
        mongoScope.handleMongoException(me);
    }

}
