# jdata-unit-test
A framework for testing relational databases.

## Abstract
Inspired by [DbUnit][dbunit], this framework takes advantage of [Java 8 Lambda][java_8_lambda] and [YAML][yaml_1_1], providing [decorators][data_decorator] and [operators][data_operator] to ease the preparation of data while your are testing codes needing or affecting data in databases.

## APIs and YAML
* [API Guideline](https://github.com/mikelue/jdata-unit-test/wiki/API-Guideline) - a way by pure-java(lambda) to build/clean data for testing.
* [YAML API Guideline](https://github.com/mikelue/jdata-unit-test/wiki/API-Guideline-of-YAML) - a way to load YAML format of data for testing
* [YAML Syntax](https://github.com/mikelue/jdata-unit-test/wiki/YAML-syntax) - Syntax of YAML for data definition
 
## Features
* [Decorators][data_decorator] - A chainable object to decorate data or to change mapping between data and column.
* [Operators][data_operator] - The one to apply changes to database(by JDBC)

## Integration with testing framework
### TestNG
See [Integration with TestNG](https://github.com/mikelue/jdata-unit-test/wiki/Integration-with-TestNG)

### JUnit4
* Not supported yet, but you could use [YAML API Guideline](https://github.com/mikelue/jdata-unit-test/wiki/API-Guideline-of-YAML) to set-up your own building/cleaning data for testing.

## References
* [YAML 1.1][yaml_1_1],[SnakeYaml](https://bitbucket.org/asomov/snakeyaml)
* [DbUnit](dbunit)

[yaml_1_1]: http://yaml.org/spec/current.html
[dbunit]: http://dbunit.sourceforge.net/
[java_8_lambda]: https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
[data_decorator]: https://github.com/mikelue/jdata-unit-test/wiki/Provided-decorators-of-data-grain
[data_operator]: https://github.com/mikelue/jdata-unit-test/wiki/Provided-data-operations
