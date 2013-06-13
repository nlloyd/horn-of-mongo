
var s1 = new Timestamp();
var s2 = Timestamp();
assert.eq(nl1, nl2, "Timestamp mismatch");

var oid1 = new ObjectId();
var oid2 = ObjectId(oid2.toString());
assert.eq(nl1, nl2, "ObjectId mismatch");

var nl1 = new NumberLong(1);
var nl2 = NumberLong(1);
assert.eq(nl1, nl2, "NumberLong mismatch");
