# YAML Syntax

**Why choose YAML as external representation of sample data for testing:**

1. The format could be edit by any of your favor editors.
1. The format could be maintained in [VCS](https://en.wikipedia.org/wiki/Version_control).
1. The [alias](http://yaml.org/spec/1.1/#alias/information%20model) of YAML, which benefits editing data of PK and FK on relational-database.

You may reference the [sample YAML](https://github.com/mikelue/jdata-unit-test/tree/master/core/src/test/resources/guru/mikelue/jdut/yaml) used in testing code of this framework

---

### Tags

Every document should define namespace(tag) of YAML:

```yaml
# hljs:yaml

%TAG !jdut! tag:jdut.mikelue.guru:1.0/
%TAG !dbtype! tag:jdut.mikelue.guru:jdbcType:1.8/
%TAG !sql! tag:jdut.mikelue.guru:sql:1.0/

---
# 1st document

---
# 2nd document
```

By the implementation of [YamlConductorFactory](xref/guru/mikelue/jdut/yaml/YamlConductorFactory.html),  
**every document in YAML would be an instance of [DuetConductor]**.

[DuetConductor]: apidocs/guru/mikelue/jdut/DuetConductor.html

---

### Structure
A document is defined by:

`defines`(optional)
:   You could defines the aliases with this map(the engine would ignore this node).

`config`(optional)
:   Configuration for processing of data defined in this document

`!sql!table! <table_name>`(multiple)
:   Data definitions
    * For example: `!sql!table yp_book`, `!sql!table yp_vendor`

`!sql!code`(multiple)
:   Native SQL statement if you like to execute SQL to database directly

---

#### `defines`
In this node, there is nothing to be processed, you may use this node to define alias.

```yaml
# hljs:yaml

- defines : [
    &id_1 1,
    &id_2 2
]
```

---

#### config
The configuration for data processing.

* `build_operation`(**`!!str`**): The operation for building data.
    1. build-in operators: See [Guidelines](guidelines.html)
    1. default value: `INSERT`
* `clean_operation`(**`!!str`**): The operation for cleaning data.
    1. value domain:
        * Is same as `build_operation`
    1. default value: `DELETE`
* `decorator`(**`!!str`**) - The name of decorator for every data grain
* `transaction`(**`!!bool`**): Use the transaction function of JDBC for processing data
    1. value domain:
        * `true` - Use transaction
        * `false` - Not use transaction
    1. default value: `false`
* `transaction_isolation`(**`!!str`**): Set the transaction isolation over JDBC
    * `READ_COMMITTED`, `READ_UNCOMMITTED`, `REPEATABLE_READ`, `SERIALIZABLE`
    * *default value*: as default value of JDBC driver

```yaml
# hljs:yaml

- config : {
    build_operation: "REFRESH",
    clean_operation: "DELETE",
    decorator: "decorator_name",
    transaction: false,
    transaction_isolation: "REPEATABLE_READ"
}
```

---

#### !sql!table

This node represents the data to be update into database.

1. Override the configuration defined at document-level
1. Set default name of columns
1. Set default usage of key to target row to be updated or deleted

`config`(optional)
:   The configuration section for this table
  * `build_operation` - The name of operator for building
  * `clean_operation` - The name of operator for cleaning
  * `decorator`: The decorator chains after the global one to decorate this data grain

`columns`(mandatory if you like to use implicit data row)
:   The columns for implicit data of row(by **`!!seq`**)

`keys`(optional)
:   The explicit definition for key(used by `UPDATE`, `DELETE` operator)

`data`(mandatory)
:   The data section for this table, as **`!!seq`** of YAML, see simple definition

```yaml
# hljs:yaml

- !sql!table tab_2 {
  # build/clean name of operation
  config : {
      build_operation: "INSERT",
      clean_operation: "DELETE",
      decorator: "tab_2_decorator"
  },
  # define keys for UPDATE/REFRESH/DELETE
  keys : [ "id_1" ]
  # defines implicit columns
  columns: [ "col_1", "col_2", "col_3", "col_4" ]
  # defines data(implicit or explicit definition are both working)
  data : [
      { col_1: 20, col_2: 40, col_3: "Thing" },
      [ "c1", 20, 40, 50 ]
  ]
}
```

##### !jdut!supplier

The engine supports the value of column from lambda expression([Supplier]).

`!jdata-unit-test:value,1.0:supplier`(`!jdut!supplier`)
:   The name which is registered with a instance of [Supplier]

[Supplier]: https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html

```yaml
# hljs:yaml

%TAG !jdut! tag:jdut.mikelue.guru:1.0/
%TAG !dbtype! tag:jdut.mikelue.guru:jdbcType:1.8/
%TAG !sql! tag:jdut.mikelue.guru:sql:1.0/

---
- !sql!table tab_3 : [
  {
    col_2: !jdut!supplier "value_1",
  }
]
```

```java
// hljs:java

YamlConductor.build(
    builder -> builder
        .namedSupplier("value_1", () -> 30);
);
```

---

#### !sql!code

`build_operation`
:   The operation while executing [DuetConductor.build](apidocs/guru/mikelue/jdut/DuetConductor.html#build--)

`clean_operation`
:   The operation while executing [DuetConductor.clean](apidocs/guru/mikelue/jdut/DuetConductor.html#clean--)

`!sql!statement`
:   indicates that the text is a SQL statement to be executed by [Statement.executeUpdate()](https://docs.oracle.com/javase/8/docs/api/java/sql/Statement.html#executeUpdate-java.lang.String-).

`!sql!jdbcfunction`
:   indicates that the text is the name of a [JdbcFunction]

**<span style="color:green">You don't have to provide both of the two operations in `!sql!code` node</span>**

[JdbcFunction]: apidocs/guru/mikelue/jdut/jdbc/JdbcFunction.html

```yaml
# hljs:yaml

- !sql!code
  build_operation: !sql!statement
    INSERT INTO tab_1(col_1, col_2, col_3)
    VALUES(10, '3323', 20)
  clean_operation: !sql!statement
    DELETE tab_1 WHERE col_1 = 10

- !sql!code
  build_operation: !sql!jdbcfunction "func_1"
  clean_operation: !sql!jdbcfunction "func_1"
```

```java
// hljs:java

YamlConductor.build(
    builder -> builder
        .namedJdbcFunction(
            "func_1",
            (connection) -> {
                /* Your JDBC operations */
            }
        );
);
```

---

## Named Operator

Uses [namedOperator(...)](apidocs/guru/mikelue/jdut/ConductorConfig.Builder.html#namedOperator-java.lang.String-guru.mikelue.jdut.operation.DataGrainOperator-) of [YamlConductorFactory.build(...)](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#build-javax.sql.DataSource-java.util.function.Consumer-) to set named [DataGrainDecorator].

Also, following methods can override the configurations of factory:

* [YamlConductorFactory.conductResource(String, Consumer<ConductorConfig.Builder>)](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#conductResource-java.lang.String-java.util.function.Consumer-)
* [YamlConductorFactory.conductYaml(String, Consumer<ConductorConfig.Builder>)](file:///D:/Code/jdata-unit-test/target/site/apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#conductYaml-java.io.Reader-java.util.function.Consumer-)


```yaml
# hljs:yaml

# Document level configuration
- config : {
    build_operation: "insert_and_check"
}

- !sql!table tab_1 {
    config : {
        // Overrides the one assigned in document level
        build_operation: "insert_and_check"
    }
    data : [
        # data ....
    ]
}
```

```java
// hljs:java

YamlConductorFactory.build(
    dataSource,
    builder -> builder
        .namedOperation(
            "insert_and_check",
            (connection, dataRows) -> {
                /* Your JDBC operations */
            }
        );
);
```

---

## Named Decorator

You could use [namedDecorator(...)](apidocs/guru/mikelue/jdut/ConductorConfig.Builder.html#namedDecorator-java.lang.String-guru.mikelue.jdut.decorate.DataGrainDecorator-) of [YamlConductorFactory.build(...)](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#build-javax.sql.DataSource-java.util.function.Consumer-) to set named [DataGrainDecorator].

Also, following methods can override the configurations of factory:

* [YamlConductorFactory.conductResource(String, Consumer<ConductorConfig.Builder>)](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#conductResource-java.lang.String-java.util.function.Consumer-)
* [YamlConductorFactory.conductYaml(String, Consumer<ConductorConfig.Builder>)](file:///D:/Code/jdata-unit-test/target/site/apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#conductYaml-java.io.Reader-java.util.function.Consumer-)

```yaml
# hljs:yaml

- config : {
    decorator: "coffee_decorate",
}
```

```java
// hljs:java

YamlConductorFactory.build(
    dataSource,
    builder -> builder
        .namedDecorator(
            "value_1",
            (tableDef, data) -> {
                /* Your decoration of data grain */
            }
        );
);
```

[DataGrainDecorator]: apidocs/guru/mikelue/jdut/decorate/DataGrainDecorator.html

---

## Data Types
See [Guidelines](guidelines.haml)

---

## Quick example

```yaml
# hljs:yaml

%TAG !jdut! tag:jdut.mikelue.guru:1.0/
%TAG !dbtype! tag:jdut.mikelue.guru:jdbcType:1.8/
%TAG !sql! tag:jdut.mikelue.guru:sql:1.0/

---
# Skipped processing for alias usage
- defines : [
    &v1 20,
    &v2 40
]

- config : {
    build_operation: "INSERT",
    clean_operation: "DELETE"
}

- !sql!table tab_1 [
  { col_1: 10, col_2: "String Value", col_3: !!Timestamp "2010-05-05 10:20:35+08" },
]

- !sql!table tab_2 {
  columns : [ "col_id", "col_name" ]
  data : [
    [10, "v1"],
    [20, "v2"],
    { col_id: 30, col_name: "new-name"},
  ]
}

- !sql!code
  build_operation: !sql!statement
    INSERT INTO tab_1(col_1, col_2, col_3)
    VALUES(10, '3323', 20)
  clean_operation: !sql!statement
    DELETE tab_1 WHERE col_1 = 10

- !sql!code {
    build_operation: !sql!jdbcfunction "func_1",
    clean_operation: !sql!jdbcfunction "func_2"
}
```

---

## References
* [YAML 1.1](http://yaml.org/spec/1.1/)
* [SnakeYaml](https://bitbucket.org/asomov/snakeyaml)
* [YAML Tag Repository](http://yaml.org/type/index.html)
