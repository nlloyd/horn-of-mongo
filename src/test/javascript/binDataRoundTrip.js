// Tests BinData type round tripping to/from the db

var coll = db.binDataRoundTrip
coll.drop();

var data = "iVBORw0K";

db.eval("db.binDataRoundTrip.insert({ '_id': 0, bindata: new BinData(0, '" + data + "') });")

var fromDbEval = coll.findOne({ '_id': 0 }).bindata;

var toDB = new BinData(0, "iVBORw0K");

coll.insert({ '_id': 1, bindata: toDB });

var fromDB = coll.findOne({ '_id': 1 }).bindata;

assert.eq(fromDB, toDB, "BinData fromDB != toDB");
assert.eq(fromDbEval, toDB, "BinData fromDbEval != toDB");
