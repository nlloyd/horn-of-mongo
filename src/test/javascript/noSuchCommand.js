// test behavior of executing db.runCommand() with a non-existent command

var res = db.runCommand('not-a-real-command');
assert.eq(0, res.ok);
assert(res.errmsg == 'no such cmd: not-a-real-command');

res = db.runCommand();
assert.eq(0, res.ok);
assert(res.errmsg == 'no such cmd: ');
