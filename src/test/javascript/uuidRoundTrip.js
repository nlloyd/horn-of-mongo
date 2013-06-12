// Tests BinData UUID type round tripping to/from the db

var coll = db.uuidDataRoundTrip
coll.drop();

var uuid = '0123456789abcdeffedcba9876543210';
db.eval("db.uuidDataRoundTrip.insert({ '_id': 0, uuid: UUID('" + uuid + "') });")

var fromDbEval = coll.findOne({ '_id': 0 }).uuid;

var toDB = UUID(uuid);

coll.insert({ '_id': 1, uuid: toDB });

var fromDB = coll.findOne({ '_id': 1 }).uuid;

assert.eq(fromDB, toDB, "UUID fromDB != toDB");
assert.eq(fromDbEval, toDB, "UUID fromDbEval != toDB");
