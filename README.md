Horn of Mongo
=============
[![Build Status](https://travis-ci.org/nlloyd/horn-of-mongo.png?branch=master)](https://travis-ci.org/nlloyd/horn-of-mongo)

MongoDB Shell built on the Rhino JavaScript Engine for Java.

Currently tested against mongodb release: <strong>2.4.4</strong>

## Usage

All actions within the a mongodb-enabled Rhino `Context` happen through `MongoRuntime`.  Before anything can happen, however, you need to first create a `MongoScope` to work from using static methods within `MongoRuntime`.  After you have a `MongoScope` you can execute just about any mongodb script you want from the convenient `MongoRuntime.call(..)` method!

There are two ways to create a `MongoScope`.  The first will create an environment without the `db` global variable.  The second will create a connected client for you but expects more information (ex. hostname(s) and a database name in the form of a `MongoClientURI`).

#### Disconnected MongoScope
```java
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.MongoScope;

...

MongoScope myMongoScope = MongoRuntime.createMongoScope();

// then to create the 'db' global variable and open a connection to a mongod instance...
MongoRuntime.call(new MongoScriptAction(myMongoScope, "connect", "db = connect('someHost:27017/someDb',myUserName,myPassword);"));

// or if no authentication is required...

MongoRuntime.call(new MongoScriptAction(myMongoScope, "connect", "db = connect('someHost:27017/someDb',null,null);"));

...

MongoRuntime.call(new MongoScriptAction(myMongoScope, "your special script, or a Reader instead of this string"));
```

#### Connected MongoScope
```java
import org.github.nlloyd.hornofmongo.MongoRuntime;
import org.github.nlloyd.hornofmongo.MongoScope;

...

MongoScope myMongoScope = MongoRuntime.createMongoScope(new MongoClientURI("mongodb://localhost/test"), true, true);

...

MongoRuntime.call(new MongoScriptAction(myMongoScope, "your special script, or a Reader instead of this string"));
```

After that you can feed scripts as strings or `Reader` instances through the MongoRuntime.call(..) method wrapped in `MongoScriptAction` objects.

### Additional Configuration Options

The `MongoScope` instance can be configured with two additional flags that can closely replicate the behavior observed in the official mongo shell client.

`myMongoScope.setUseMongoShellWriteConcern(true)` will configure the wrapped mongo client to use the WriteConcern used by the official mongo shell client which is send-and-forget.

`myMongoScope.setMimicShellExceptionBehavior(true)` will cause the Horn of Mongo environment to intercept some exception types from the wrapped mongo client and instead of throwing an exception in the mongodb-enabled Rhino environment will print an error to stdout.  Which exceptions are intercepted have been determined based on the behavior of the official mongo shell client.

The functionality of this project has been evaluated using the official JavaScript tests from the <a href='https://github.com/mongodb/mongo'>mongodb project</a>.  Most of the tests are used with some exceptions indicated below. 

## Known Limitations

The following official mongodb JavaScript tests have been excluded.  Tests that are excluded due to unsupported functionality simply reference the current lack of interest in implementing the support in this project and have nothing to do with the <a href='https://github.com/mongodb/mongo-java-driver'>mongo java driver</a> project.

* **_run_program1.js_** is excluded due to the currently unsupported runProgram() function.
* **_mr_noscripting.js_** and logpath.js are excluded due to the currently
unsupported MongoRunner js class.
* **_evalf.js_** is excluded due to a locking issue that has yet to be resolved, however the complexity of the test scenario makes this a safe-to-exclude for now.
* **_indexOtherNamespace.js_** excluded because I can't for the life of me get the failure scenario to actually fail using the mongo java driver :-(
* **_memory.js_** is excluded because it is testing the mongod rather than the
client api behavior (and it takes a while to run on slower machines).
This test does actually pass, however.
* **_fts_blogwild.js_** and **_fts_mix.js_** excluded for now due to invalid operator: $** issue https://jira.mongodb.org/browse/JAVA-814
* **_remove_justone.js_** excluded until the mongo java driver supports that feature: https://jira.mongodb.org/browse/JAVA-759
* **_basicc.js_**, **_bench_test1.js_**
**_bench_test2.js_**, **_bench_test3.js_**
**_connections_opened.js_**, **_count8.js_**
**_coveredIndex3.js_**, **_currentop.js_**
**_cursora.js_**, **_distinct3.js_**
**_drop2.js_**, **_evalc.js_**
**_evald.js_**, **_explain3.js_**
**_group7.js_**, **_index12.js_**
**_killop.js_**, **_loadserverscripts.js_**
**_logpath.js_**, **_mr_drop.js_**
**_mr_killop.js_**, **_orm.js_**
**_orn.js_**, **_queryoptimizer3.js_**
**_queryoptimizer5.js_**, **_remove9.js_**
**_removeb.js_**, **_removec.js_**
**_shellkillop.js_**, **_shellstartparallel.js_**
**_shellspawn.js_**, and **_updatef.js_** are excluded because they rely on the following functions `startMongoProgramNoConnect()` and `startParallelShell()` which there are no plans to implement

## Changelog

### v1.0

First release!  Highly compatible release with mongodb version 2.4.3 (see Known Limitations).

## Roadmap

[Planned Milestones](https://github.com/nlloyd/horn-of-mongo/issues/milestones)<br/>
[Issues](https://github.com/nlloyd/horn-of-mongo/issues?labels=&milestone=&page=1&state=open)

## Contributors

<a href="http://www.aquafold.com"><img src="http://www.aquafold.com/images/s_aquadatastudio_130x34.gif" /></a>  AquaFold, patch contributors, horn-of-mongo is the core engine behind the MongoDB features in their upcoming release.

<br/>
<strong>Show your support by donating!</strong>

<a href='http://www.pledgie.com/campaigns/20520'><img alt='Click here to lend your support to: Horn of Mongo and make a donation at www.pledgie.com !' src='http://www.pledgie.com/campaigns/20520.png?skin_name=chrome' border='0' /></a>

## License

All of Horn of Mongo is licensed under the MIT license.

  Copyright (c) 2012 Nick Lloyd

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.