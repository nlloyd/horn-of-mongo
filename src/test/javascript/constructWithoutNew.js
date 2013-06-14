
var s1 = new Timestamp();
var s2 = Timestamp();
assert.eq(nl1, nl2, "Timestamp mismatch");

var oid1 = new ObjectId();
var oid2 = ObjectId(oid1.valueOf());
assert.eq(nl1, nl2, "ObjectId mismatch");

var nl1 = new NumberLong(1);
var nl2 = NumberLong(1);
assert.eq(nl1, nl2, "NumberLong mismatch");

var tstamp1 = ObjectId("519f19a784aeefdf38bff2df").getTimestamp();
var tstamp2 = ISODate("2013-05-24T07:41:27Z");
assert.eq(tstamp1, tstamp2, "ObjectId.getTimestamp() and ISODate mismatch");
