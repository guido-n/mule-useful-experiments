# mule-useful-experiments
Three simple projects that can help a beginner.

### autowired-inside-mule-component

This is a basic project that shows how to wire Spring beans into Mule components and then use these components in a Mule flow. It is also possible to pass configuration using properties. This assumes that both the _Spring bean_ and the _Mule component_ are singletons.

The overall project structure can also be used as a reference (almost like an archetype) for a Mule application. It involves two types of integration testing design. The first one is the classic TCK test structure described in the _Mule in action Second Edition_ book. The second uses the _mule-maven-plugin_ and it involves a real deployment of a Mule standalone server during the Maven build (it all happens unde the _target/_ folder).

The TCK style integration testing allows us to programmatically set up the environment (create files, directory trees etc) for integration testing inside the test class. Whereas the other style, although more realistic, has to invole some scripts that run before/after the integration tests (you can hook scripts to _build phases_ in Maven with the _exec-maven-plugin_). The truth is there are a lot of Maven plugins out there that allow you to bring up and down pretty much anything you need for your integration testing: databases, docker images, web containers etc. You just have to hook them to the _[pre/post]-integration-test_ phases in your POM.

There are two Maven _profiles_ in this project. You can run them like this
```
mvn -P local clean verify
mvn -P integration-test clean verify
```
The _local_ profile executes only the xTest (run by Maven _Surefire_, ie. TCK style) tests.

The _integration-test_ profile executes both the xTest and the xIT (run by Maven _Failsafe_, ie. mule-maven-plugin style) tests.

In the real world you would run the _local_ profile (or similarly the _live_ profile) to package your Mule app in a deployable ZIP file. You would run the _integration-test_ profile when you want/need to (as it's normally fairly slower). And you would also set different configuration properties for the various profiles.

Two important considerations:
* You need to have the mule standalone distribution file (the mule zip file) in your local Maven repo
* If something goes wrong during any of the xIT tests with the standalone server, things are not cleaned up properly. Particularly the _target/_ directory might be left in an inconsistent state and the Mule server process might still be running. Clean up manually, or it may affect subsequent Maven runs.

In order to build and install Mule standalone in your local Maven repo:
 
 ```
git clone https://github.com/mulesoft/apikit.git
cd apikit
git checkout 3.8.3
mvn clean install
cd ..

git clone https://github.com/mulesoft/mule.git
cd mule
git checkout mule-3.8.3
mvn clean install -Pdistributions,release -DskipGpg=true -DxDocLint='-Xdoclint:none'
cd ..

ls -lh ~/.m2/repository/org/mule/distributions/mule-standalone/3.8.3/mule-standalone-3.8.3.zip
```
In order to kill Mule process easily:
 ```
 #!/bin/bash

sudo pkill -f MuleContainerBootstrap
sudo pkill -f wrapper-linux-x86-64
 ```
 
### reliability-pattern-mule

This is a basic project that shows a real implementation and test case of the _reliability design pattern_ described here https://docs.mulesoft.com/mule-user-guide/v/3.8/reliability-patterns

There are two test cases in this project: the _http-listener_ with a synchronous and non-blocking _flow processing strategy_. Note that the _queued async_ processing strategy is not allowed on an http-listener in Mule.

This project is a good example of SEDA design. The _VM queue_ inbetween the _acquisition flow_ and the _consumer flow_ can be configured with either a _<default-persistent-queue-store/>_ or a _<default-in-memory-queue-store/>_. Although the in-memory queue defeats the purpose of reliability, it's important to note that I was able to achieve deadlocks with the _VM persistent queuestore_ by beefing up the load on the app (the number of HTTP requests sent in parallel). Mule's persistent VM transport works fine for situations where not much load is involved, whereas for HA situations it is recommended to use a DBMS to persist _acquired_ messages: it will be slower, but it will cope with any load requirement (and works well with clustering).

Just to state the obvious, it is important to note that any persistent queue solution based on file system directly, will not work well in high load scenarios. As soon as you start to read and write too frequently, your system is going to freak out. In such situations it's best to let a DB server to cope with the load of data persistency and data retrieval operations.

### data-stream-acquisition

This is a basic project that shows a way to cope with big XML files (that don't fit in memory) or if you want, continuous XML streams. The idea is to have a tool that you can feed with any amount of data, and memory usage will remain constant. A tool that is able to split XML data streams based on an XPATH expression and that can process the splitted chunks in parallel (or in a single-threaded fashion should we need to preserve order).

Basically, this project wraps a smooks-based SAX parser in a Mule application. The parser splits any XML InputStream into chunks based on an XPATH expression, and then it sends them in a streaming fashion to a VM queue. A consumer flow then picks up the chunks from the queue and processes them. The VM queue has a fixed capacity (defined as number of elements like this _<vm:queue-profile maxOutstandingMessages="256">_). When the queue is full, the parser/splitter waits, the stream slows down, and so does the upstream connection (in case for example we are receiving the stream from a HTTP request with _chunked transfer encoding_).

My view is that there is little point in persisting the splitted chunks before consuming them. This means that this project does not technically adhere to the _reliability pattern_. Therefore the VM queue is set to use the default _in-memory_ queuestore. If you really need persistence, a better design would be to get the raw stream persisted to disk first, and then stream & process it, so that the sender will experience maximum speed. Should the stream be open-ended, then the actual consuming of the splitted chunks should be considered as the persistence operation.
 
A simple example of how to dump the raw body of an HTTP POST request to disk is also included.

By running the test cases in this project I found what I believe to be a bug in _mule-maven-plugin_ 2.1. Essentially when you deploy an application which has some custom libs included (Maven dependencies with scope compile get packaged within the Mule app ZIP file), the deploy goal fails because it cannot delete a directory. I have a theory on that but for now I will not speculate. For the moment I've just commented out the _undeploy goal_, hence after you run the integration tests, the mule standalone will still be running and you'll have to take it down manually. 
 