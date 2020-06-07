# jdata-unit-test

A framework for testing code over relational databases.

This framework is mainly inspired by [DbUnit](http://dbunit.sourceforge.net/).

See <https://jdut.gh.mikelue.guru/> for documentations.

## Modules

* [core/](./core) - Core libraries of JDUT
* [junit4/](./junit4) - [JUnit4](https://junit.org/junit4/) extensions of JDUT.
* [junit5/](./junit5) - [JUnit5(Jupiter)](https://junit.org/junit5/docs/current/user-guide/) extensions of JDUT.
* [testng/](./testng) - [TestNG](https://testng.org/) extensions of JDUT.

# Development

## Deploy artifacts to [OSSRH](https://central.sonatype.org/pages/ossrh-guide.html)

```bash
mvn -P oss-deploy -DskipTests=true deploy
```

## Reporting

Local building of sites:

```bash
# Quick building for AsciiDoctor
mvn -P site-author -pl ':parent' site

# With aggregation of javadoc/jxr
mvn -P site-author -pl ':parent,junit5' -Dskip.aggregate=false site
```

----
Deploy to `gh-pages` of GitHub

```bash
# 1. Execute site:stage after building every modules
mvn -P gh-pages clean site site:stage

# 2. Push to gh-pages branch of GitHub
mvn -P gh-pages -pl ":parent" scm-publish:publish-scm
```

Property `scmpublish.dryRun` can be used to dry run for **scm-publish**

```
-Dscmpublish.dryRun=true
```

## Upgrade versions dependencies/plugins

Use [org.codehaus.mojo:versions-maven-plugin](https://www.mojohaus.org/versions-maven-plugin/index.html) to display/upgrade versions of dependencies/plugins:

```bash
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

The [version-ruleset.xml](src/main/resources/guru/mikelue/jdut/version-ruleset.xml) file defines some ignorances of version number.
* Skips `-alpha` versions
* Skips `-beta` versions
* Skips `-RC` versions
* Skips `-jre9/10/11/12/13/14/15` versions

# Testing

You can use `logging.level.jdut`(default: `warn`) to assign logging level for all of the logging level under<br/>
package `guru.mikelue.jdut`.

```bash
mvn test -Dlogging.level.jdut=debug
```
