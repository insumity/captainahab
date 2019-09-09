# CaptainAhab

CaptainAhab is a tool to inject **network partitions** in a cluster of servers. Goal of CaptainAhab is to be used as a tool to verify the consistency guarantees of a distributed system. In this regard, CaptainAhab is similar to [Jepsen](https://github.com/jepsen-io/jepsen), albeit simpler, since CaptainAhab does not provide as much functionaliy as Jepsen.

To see how CaptainAhab is used in practice to test the consistency guarantees of a distributed system, such as [ZooKeeper](https://github.com/apache/zookeeper), have a look at [CaptainAhabRunner](https://github.com/insumity/captainahabrunner).

### How does CaptainAhab work?
Assume we want to use CaptainAhab to inject network partitions in a cluster of servers `S`. Then, CaptainAhab performs the following steps:
- Start a "nemesis" server listening to port 5005 in each of the servers in `S`.
- Then issue a `changeTopology` request using CaptainAhab. Such a request issues `iptable` commands in order to change the topology of the cluster and inject network partitions. 

### How to use CaptainAhab?
CaptainAhab is used to generate two JAR files:
- The nemesis server executable JAR. Just do:
`mvn clean compile assembly:single`.
The generated JAR can be used by doing `java -jar captainahab.jar 5005` to start a nemesis server at port 5005.
- Comment out these lines in the `pom.xml`
```
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.twitter.captainahab.server.CaptainAhabServer</mainClass>
            </manifest>
        </archive>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
    </configuration>
</plugin>
```
and re-execute `mvn clean compile assembly:single`.
The generated JAR is used by applications such as [CaptainAhabRunner](https://github.com/insumity/captainahabrunner) for accessing the CaptainAhab API.
