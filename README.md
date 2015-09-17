# jdata-unit-test
A framework for testing relational databases.

## Installation
Currently, this framework is on [SNAPSHOT of oss.sonatype.org](https://oss.sonatype.org/content/repositories/snapshots/guru/mikelue/jdut/).

You should put following setting of repository:
```xml
<project>
    <!-- Other configurations... -->

    <repositories>
        <repository>
            <id>oss.sonatype.org</id>
            <name>oss.sonatype.org</name>
            <snapshots>
                <enabled>true</enabled>
                <checksumPolicy>fail</checksumPolicy>
            </snapshots>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <layout>default</layout>
        </repository>
    </repositories>

    <!-- Other configurations... -->
<project>
```

Dependencies(for testing):
```xml
<dependency>
    <groupId>guru.mikelue.jdut</groupId>
    <artifactId>core</artifactId>
    <!-- If you like to integrate TestNG -->
    <!--<artifactId>testng</artifactId>-->
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Documentations
* [Web site](http://jdut.gh.mikelue.guru/) - Maven reports, API references
* [Wiki](https://github.com/mikelue/jdata-unit-test/wiki) - Tutorials, Guidelines
