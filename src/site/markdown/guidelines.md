# Guidelines

This article contains the main building blocks for usage of JDUT.

For quick example, see [Examples](examples.html).

---

## 1. Java API

This section describes the usage of pure-Java API for building/cleaning data

---

### Set-up data grain

[DataGrain]
:   Used to set-up values and definitions of data
[Supplier<?>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html)
:   The value of field could be a supplier, which working as lazy-evaluation

See [DataGrainTest](xref-test/guru/mikelue/jdut/datagrain/DataGrainTest.html) for full example

Quick example:

```java
import idv.mikelue.jdut.datagrain.DataGrain;

DataGrain dataGrainByDefine = DataGrain.build(
    /**
     * Defines the table meta-data
     * The columns would be indexed by sequence of builds
     */
    tableSchemaBuilder -> {
        tableSchemaBuilder
            .name("tab_1") // Mandatory
            .keys("col_1", "col_2"); // Used by DELETE operation
    },
    // :~)
    /**
     * Adds the data of table
     */
    rowsBuilder -> {
        rowsBuilder.implicitColumns(
            "col_1", "col_2", "col_3", "col_4"
        )
        /**
         * Implicit data
         */
        .addValues(10, "CC-1", () -> "AC-2", null)
        .addValues(11, "CC-1", () -> "AC-2", null)
        // :~)
        /**
         * Explicit data
         */
        .addFields(
            rowsBuilder.newField("col_1", 12),
            rowsBuilder.newField("col_2", 13),
            rowsBuilder.newField("col_3", () -> "AN-78")
            rowsBuilder.newField("col_4", () -> "AN-78")
        );
        // :~)
    }
    // :~)
);
```

---

### Execute operator with data grain

[DataConductor]
:   The conductor depending on [DataSource]

[DataGrainOperator]
:   The code(SQL) executes on database with [DataGrain]

[DefaultOperatorFactory]
:   The factory for fetching operator(may be [vendor-specific](apidocs/guru/mikelue/jdut/vendor/package-summary.html)) by name.

```java
import guru.mikelue.jdut.DataConductor;
import guru.mikelue.jdut.operation.DefaultOperators;

/**
 * dataSource - The initialized data source
 *
 * This conductor would load schema of database to complement the complete information of column.
 */
DataConductor dataConductor = DataConductor.build(dataSource);

/**
 * Executes the "INSERT"(defined in OperatorFactory) with data grain
 */
dataConductor.conduct(dataGrainByDefine, operatorFactory.get(DefaultOperators.INSERT));
```

---

### Aggregation of data grains
[DataGrain.aggregate](apidocs/guru/mikelue/jdut/datagrain/DataGrain.html#aggregate-guru.mikelue.jdut.datagrain.DataGrain-) - Used to aggregate multiple data grains in sequence

See detail sample of [DataGrainTest.aggregate](xref-test/guru/mikelue/jdut/datagrain/DataGrainTest.html)

```java
import guru.mikelue.jdut.datagrain.DataGrain;

// dg_1 - The object of DataGrain
// dg_2 - Another object of DataGrain

// The data of dg_1 with data of dg_2....
DataGrain aggregatedDataGrains = dg_1.aggregate(dg_2);
```

---

### Data grain decoration

A decorator is a lambda to modify a [DataRow], which is the internal data of a [DataGrain].

#### Decorate data grain(decorator)
You could implement functional interface of [DataGrainDecorator](apidocs/guru/mikelue/jdut/decorate/DataGrainDecorator.html) to decorate an instance of [DataGrain].

```java
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.decorate.DataGrainDecorator;

DataGrainDecorator dataGrainDecorate = rowBuilder -> rowBuilder
    .field("col_1", Types.INTEGER, 55)
    .field("col_2", rowBuilder<Integer>.getData("col_1").orElse(0) + 1)
    .field("col_3", () -> "EXP-01");

DataGrain decoratedDataGrain = dataGrain.decorate(dataGrainDecorate);
```

#### Chaining of decorator
You could use method of [DataGrainDecoration.chain()](apidocs/guru/mikelue/jdut/decorate/DataGrainDecorator.html#chain-guru.mikelue.jdut.decorate.DataGrainDecorator-) to chain multiple decorations.

```java
import guru.mikelue.jdut.decorate.DataGrainDecorator;

DataGrainDecorator dataGrainDecorate_1 = (rowBuilder) -> {
    /* Your decoration...*/
}
DataGrainDecorator dataGrainDecorate_2 = (rowBuilder) -> {
    /* Your decoration...*/
}

DataGrainDecoration chainedDecorate = dataGrainDecorate_1.chain(dataGrainDecorate_2);
```

---

### Data operator

You may use [DataGrainOperator] to implement your own operations to database.

[DataRowsOperator](apidocs/guru/mikelue/jdut/operation/DataRowsOperator.html)
:   Accepts a list of [DataRow]s

[DataRowOperator](apidocs/guru/mikelue/jdut/operation/DataRowOperator.html)
:   Accepts a [DataRow]

#### Composition of operations
You could composite operations for interception of data operation.

A [SurroundOperator](apidocs/guru/mikelue/jdut/operation/DataGrainOperator.SurroundOperator.html) is a lambda to surround a [DataGrainOperator]. The returned object is another [DataGrainOperator] with same signature.

```java
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.operation.DataGrainOperator;

DataGrainOperator specialInsert = DefaultOperators.INSERT.surroundedBy(
    surroundedOp -> (conn, dataGrain) -> {
        /* Your surrounding before the calling of surrounded operation */

        surroundedOp.operate(conn, dataGrain)

        /* Your surrounding after the calling of surrounded operation */
    };
)
```

#### Operate data in transaction
See [DatabaseTransactional](apidocs/guru/mikelue/jdut/function/DatabaseTransactional.html) and
[DatabaseSurroundOperators](apidocs/guru/mikelue/jdut/function/DatabaseSurroundOperators.html)

```java
/**
 * By DataSourceConsumer
 */
import guru.mikelue.jdut.function.DatabaseTransactional;
import guru.mikelue.jdut.operation.DefaultOperators;

dataConductor.conduct(
    DefaultOperators.INSERT.surroundedBy(
        DatabaseTransactional::simple
    )
);
```

[DataConductor]: apidocs/guru/mikelue/jdut/DataConductor.html
[DataRow]: apidocs/guru/mikelue/jdut/datagrain/DataRow.html
[DataGrain]: apidocs/guru/mikelue/jdut/datagrain/DataGrain.html

---

### Data types

#### For Java API

The build-in operators of JDUT would use the type of value
to decide which method of `setXXX` in [PreparedStatement](https://docs.oracle.com/javase/8/docs/api/java/sql/PreparedStatement.html)
to be called for setting parameter of SQL.

[java.util.Date](https://docs.oracle.com/javase/8/docs/api/java/util/Date.html)
:   the engine would use [PreparedStatement.setTimestamp()](https://docs.oracle.com/javase/8/docs/api/java/sql/PreparedStatement.html#setTimestamp-int-java.sql.Timestamp-).

**Type cannot be decided**
:   the engine would use [PreparedStatement.setObject()](https://docs.oracle.com/javase/8/docs/api/java/sql/PreparedStatement.html#setObject-int-java.lang.Object-)

#### For YAML
1. The engine for loading of YAML would build the data into [DataGrain],
    so the target type is the decided by how the YAML engine converts value to Java object.
1. You may assign desired type of data in YAML tagging

```yaml

%TAG !jdut! tag:jdut.mikelue.guru:1.0/
%TAG !dbtype! tag:jdut.mikelue.guru:jdbcType:1.8/
%TAG !sql! tag:jdut.mikelue.guru:sql:1.0/

---
- !sql!table tab_1 [
  # With column name
  {
      col_1: !dbtype!smallint 10, col_2: "String Value",
      col_3: !dbtype!timestamp "2010-05-05 10:20:35+08"
  },
  # Other rows...
]
```

#### Scalar Value
| YAML Type : JDBC type   | Accept type of JDBC                                                             | Misc                         |
|-------------|---------------------------------------------------------------------------------|------------------------------|
| `!!binary` : `VARBINARY`    | BINARY, BLOB, LONGVARBINARY, VARBINARY                                          | *1 as base64 |
| `!!bool` : `BOOLEAN`      | BIT, BOOLEAN,<br/>TINYINT, SMALLINT, INTEGER, BIGINT,<br/>DECIMAL, NUMERIC, REAL,<br/>DOUBLE, FLOAT | True - 1<br/>False - 0           |
| `!!float` : `DOUBLE`     | DECIMAL, DOUBLE, FLOAT, REAL, NUMERIC                                           | *1           |
| `!!int` : `INTEGER`       | TINYINT, SMALLINT, INTEGER, BIGINT,<br/> NUMERIC, DECIMAL, REAL,<br/> DOUBLE, FLOAT                           | *1           |
| `!!str` : `VARCHAR`       | CHAR, VARCHAR, LONGVARCHAR,<br/> NCHAR, NVARCHAR, LONGNVARCHAR,<br/> CLOB, NCLOB          |                              |
| `!!timestamp` : `TIMESTAMP` | DATE, TIME, TIMESTAMP                                                           | *1          |
| `!!null` : `null value`      | doesn't matter

**\*1** - Could be converted to text-type of SQL

`NUMERIC` and `DECIMAL`
:   the value would be converted into [BigDecimal](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html).

#### Complex Value
For type of complex value, you could use [Supplier<?>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html) to create data of these types:
**ARRAY**, **DISTINCT**, **JAVA_OBJECT**, **OTHER**, **REF**, **REF_CURSOR**, **ROWID**, **STRUCT**

#### References
* [Data types provided by SnakeYaml](https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-yaml-tags-and-java-types)
* [SnakeYaml Documentation](https://bitbucket.org/asomov/snakeyaml/wiki/Documentation)

---

## 2. YAML API

The configuration of YAML condcutor:

[ConductorConfig]
:   Bean as facade of configuration for conducting data.
    The practices of the config are defined by implementation of engine.
    The
    [YamlConductorFactory.build()](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#build-javax.sql.DataSource-java.util.function.Consumer-)
    or
    [YamlConductorFactory.conductResource()](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#conductResource-java.lang.String-java.util.function.Consumer-)
    method let you set-up the instance of [ConductorConfig].

[ConductorConfig.Builder](apidocs/guru/mikelue/jdut/ConductorConfig.Builder.html)
:   The fed object of [Consumer] to set-up an instance of [ConductorConfig]

---

### Set-up YamlConductorFactory

[YamlConductorFactory]
:   The most important service to build an instance of [DuetConductor].
    This factory depends on [DataSource] given to [build()](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#build-javax.sql.DataSource-) method.
    You can define [ConductorConfig] to customize [DataGrainDecorator] or [JdbcFunction], etc,.

[DefaultOperatorFactory]
:   The default factory used by [YamlConductorFactory].
    You may customize the factory by using [ConductorConfig.Builder.operatorFactory()](apidocs/guru/mikelue/jdut/ConductorConfig.Builder.html#operatorFactory-guru.mikelue.jdut.operation.OperatorFactory-) to set the instance.

[DefaultOperatorFactory]: apidocs/guru/mikelue/jdut/operation/DefaultOperatorFactory.html
[ConductorConfig]: apidocs/guru/mikelue/jdut/ConductorConfig.html
[DuetConductor]: apidocs/guru/mikelue/jdut/DuetConductor.html
[JdbcFunction]: apidocs/guru/mikelue/jdut/jdbc/JdbcFunction.html
[DataGrainDecorator]: apidocs/guru/mikelue/jdut/decorate/DataGrainDecorator.html

[YamlConductorFactory]: apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html

```java
// dataSource - The initialized data source

YamlConductorFactory yamlConductor = YamlConductorFactory.build(
    dataSource,
    builder -> builder
        // The factory of operator
        .operatorFactory(defaultOperationFactory)
        // The optional file loader for convertion string of file name to InputStream or Reader
        .resourceLoader((fileName) -> new FileReader(fileName))
        .namedOperator(
            "INSERT_AND_CHECK",
            (connection, dataRows) -> { /* Your database operations */ }
        )
        .namedDecorator(
            "[V1]",
            (dataRowBuilder) -> { /* Your data decoration */ }
        )
        .namedJdbcFunction(
            "func_1",
            connection -> { /* Your code of JDBC */ }
        )
);
```

---

### Build DuetConductor

[DuetConductor]
:   This interface defines build/clean data for [unit test](https://en.wikipedia.org/wiki/Unit_testing).

```java
import guru.mikelue.jdut.DuetConductor;

DuetConductor testConductor_1 = yamlFactory.conductNamedResource(
    "org/your/package/CarDaoTest-addNew.yaml",
    /**
     * You could overrides configuration of YamlConductorFactory
     */
    configure -> configure
        .namedDecorator("add_value", your_decorator)
    // :~)
);

// Consturct a content of YAML with Yaml.DEFAULT_TAGS directly(instead of file)
DuetConductor testConductor_2 = yamlConductor.conduct(
   YamlTags.DEFAULT_TAGS +
   "---\n" +
   "- !sql!table tab_1: {" +
   "  col_1: 40, col_2: \"VGA-1\"" +
   "}"
);
```

---

#### Resource loading

While using [YamlConductorFactory.conductResource(String, Consuemr<ConductorConfig.Builder>)](apidocs/guru/mikelue/jdut/yaml/YamlConductorFactory.html#conductResource-java.lang.String-java.util.function.Consumer-) to load YAML file,
you could configure how to convert a String to a [Reader] by [ConductorConfig.Builder.resourceLoader()](apidocs/guru/mikelue/jdut/ConductorConfig.Builder.html#resourceLoader-java.util.function.Function-).


See [ReaderFunctions](apidocs/guru/mikelue/jdut/yaml/ReaderFunctions.html) for build-in functions.

---

#### Execute build/clean
The [DuetConductor] should be used in proper event defined by [testing framework](https://en.wikipedia.org/wiki/List_of_unit_testing_frameworks#Java).

For [JUnit]
:   * [build()](apidocs/guru/mikelue/jdut/DuetConductor.html#build--) - Could be used in [@Before](https://junit.org/junit4/javadoc/latest/org/junit/Before.html) annotated method.
    * [clean()](apidocs/guru/mikelue/jdut/DuetConductor.html#clean--) - Could be used in [@After](https://junit.org/junit4/javadoc/latest/org/junit/After.html) annotated method.

For [TestNG]
:   * [build()](apidocs/guru/mikelue/jdut/DuetConductor.html#build--) - Could be used in [@BeforeMethod](https://jitpack.io/com/github/cbeust/testng/master/javadoc/org/testng/annotations/BeforeMethod.html) annotated method.
    * [clean()](apidocs/guru/mikelue/jdut/DuetConductor.html#clean--) - Could be used in [@AfterMethod](https://jitpack.io/com/github/cbeust/testng/master/javadoc/org/testng/annotations/AfterMethod.html) annotated method.

Following example to execute the two methods when testing:

```java
testConductor_1.build();
testConductor_2.build();

try {
    /* Your testing code */
} finally {
    testConductor_2.clean();
    testConductor_1.clean();
}
```

---

### YAML Syntax

See [YAML Syntax](yaml-syntax.html)

---

## 3. Operators/Decorators

---

### Build-in operators

The [DefaultOperators] constructs operator-inspection of vendor-specific, uses it would be a good idea.

* INSERT
    * As `DefaultOperators::insert`
    * Use of SQL `INSERT <table_name>`, directly.
* UPDATE
    * As `DefaultOperators::update`
    * Use of SQL `UPDATE <table_name> SET <col_1> = <v_1>, ... WHERE <conditions>`, directly
* REFRESH
    * As `DefaultOperators::refresh`
    * Gets the data, if the data is existing, then updates the data
    * Otherwise, inserts the data
* DELETE
    * As `DefaultOperators::delete`
    * Use of SQL `DELETE FROM <table_name> WHERE <conditions>`, directly.
* DELETE_ALL
    * As `DefaultOperators::deleteAll`
    * Use of SQL `DELETE FROM <table_name>`, directly.
* TRUNCATE
    * As `DefaultOperators::truncate`
    * Use of SQL `TRUNCATE TABLE <table_name>`, directly.
* NONE
    * As `DefaultOperators::none`
    * This operation has no effect to database.

```java

import guru.mikelue.jdut.operation.DefaultOperatorFactory;
import guru.mikelue.jdut.operation.DefaultOperators;

DefaultOperatorFactory factory = DefaultDataOperatorFactory.build(
    dataSource, builder -> {}
);

dataConductor.conduct(
    factory.get(DefaultOperators.INSERT),
    dataGrain
);
```

---

### Build-in decorators

---

#### Value replacement

```java

import guru.mikelue.jdut.decorate.ReplaceFieldDataDecorator;
import guru.mikelue.jdut.decorate.DataGrainDecorator;

/**
 * The decorate to replace string by assigned value
 */
DataGrainDecorator replaceDecorate = ReplaceFieldDataDecorator.build(
    builder -> builder
        .replaceWith(30, 90); // Replacement by object
        .replaceWith("[V1]", () -> 10); // Replacement by supplier
        .replaceWith(dataField -> dataField.getColumnName().equals("col_9"), "Another Value"); // Replacement by predict of datafield
);
```

##### Default value
You can replace value with null-value checking

* **DataFieldPredicates.nullValue(tableName, columnName)** - Builds a predicate to check null value for a column on table
* **DataFieldPredicates::nonSupplier** - A predicate to check if the data of field doesn't comes from [Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html)
  * This is used if you don't want to load data before the processing of operator

```java
// builder - builder of ReplaceFieldDataDecorator.build()

builder -> builder
    .replaceWith(DataFieldPredicates.nullValue("tab_1", "col_1"), "DEFAULT-VALUE")
```

---

#### Not defined column
You may use [DataRowBuilderPredicates::notExistingColumn](apidocs/guru/mikelue/jdut/function/DataRowBuilderPredicates.html#notExistingColumn-java.lang.String-java.lang.String-) to predicate
a decorator [a row][DataRow] with not-defined column.

```java

import guru.mikelue.jdut.function.DataRowBuilderPredicates;
import guru.mikelue.jdut.decorate.DataGrainDecorator;

DataGrainDecorator decorator = (rowBuilder -> rowBuilder.field("col_1", -1))
    .predicate(DataRowBuilderPredicates.notExistingColumn("tabl_1", "col_1"));
```

See [Predicate.and(), .or()](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html)

---

### Conductor Context

While the [DataConductor] is executing, you may access its current working object of [Connection] provided from [ConductorContext], which keeps the connection in [ThreadLocal].

A good example to use conductor context is **value function**, you may access current session of database while generating data of a field.

Lazy loading of value function
:   Since conductor context is available only when it is executing,
    you should keep in mind that [DataGrainDecorator] shouldn't access [DataRow.Builder.getData(`<field_name>`)](apidocs/guru/mikelue/jdut/datagrain/DataRow.Builder.html#getData-java.lang.String-) to trigger the value function.

    Instead, using [DataRow.Builder.getDataSupplier(`<field_name>`)](apidocs/guru/mikelue/jdut/datagrain/DataRow.Builder.html#getDataSupplier-java.lang.String-).isPresent() to check whether or not the value comes from a value function.

**You must not close the object of [Connection] from the context**.

```java

Connection conn = ConductorContext.getCurrentConnection()
    .orElseThrow(() -> new Exception("NoContext"));

/* 1) Use of connection */
/* 2) Do not close it!! */
```

[ConductorContext]: apidocs/guru/mikelue/jdut/ConductorContext.html
[ThreadLocal]: https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html

#### Threading
Any execution of [DataConductor.conduct(...)](apidocs/guru/mikelue/jdut/DataConductor.html#conduct-guru.mikelue.jdut.datagrain.DataGrain-guru.mikelue.jdut.operation.DataGrainOperator-) would initialize a new object of [Connection], which is put into [ConductorContext].

---

## 4. TestNG

You should add dependency of `guru.mikelue.jdut:testng` before using this module.

---

### Listeners
There are three main listeners to be used with

[IInvokedMethodYamlFactoryListener]
:   Default listener for building/cleaning data per-method

[ISuiteYamlFactoryListener]
:   Default listener for building/cleaning data per-suite

[ITestContextYamlFactoryListener]
:   Default listeners for building/clean data per-test

You may extend the listeners to customize usage of YAML API

### Multiple listeners

If you would use multiple listeners for different tests, the [TestNG] engine **would
executes all of the listeners even if you don't annotate the class**.

Every listener provided by JDUT has `needConductData` method to let you write your own logic that is
whether or not to use the listener in certain context.

[IInvokedMethodYamlFactoryListener]: apidocs/guru/mikelue/jdut/testng/IInvokedMethodYamlFactoryListener.html
[ISuiteYamlFactoryListener]: apidocs/guru/mikelue/jdut/testng/ISuiteYamlFactoryListener.html
[ITestContextYamlFactoryListener]: apidocs/guru/mikelue/jdut/testng/ITestContextYamlFactoryListener.html

---

### Example
Following example demonstrates the integration with [TestNG].

[@JdutResource](apidocs/guru/mikelue/jdut/annotation/JdutResource.html)
:   you may build your own processor to process the content of JDUT resources.

[@TestNGConfig](apidocs/guru/mikelue/jdut/testng/TestNGConfig.html)
:   defines behaviour of TestNG.

```java
import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.testng.IInvokedMethodYamlFactoryListener;
import guru.mikelue.jdut.testng.TestNGConfig;

@Listeners(IInvokedMethodYamlFactoryListener.class)
public class YourTest {
    /**
     * This method would look for YourTest-do1-yaml in folder idv/you/sample of classpath
     */
    @Test @JdutResource
    public void do1() {}

    /**
     * 1. This method would look for YourTest-do2-yaml in folder idv/you/sample of classpath.
     * 2. The building and cleaning of data would be at first and last testing on the method with data provider, correspondingly.
     */
    @Test(dataProvider="Do2")
    @JdutResource @TestNGConfig(oneTimeOnly=true)
    public void do2(int value) {}
    private Object[][] getDo2()
    {
        return new Object[][] {
            { 1 },
            { 2 },
        };
    }

    /**
     * Nothing would be built
     */
    @Test
    public void do3() {}
}
```

## 5. JUnit 4

You should add dependency of `guru.mikelue.jdut:junit4` before using this module.

This module supports JUnit4 since version of **4.9**.

---

### Rule

The only rule provided by this module is [JdutYamlFactory].
You may override `buildDuetConductor` method to load your own YAML file.

[JdutYamlFactory]: apidocs/guru/mikelue/jdut/junit4/JdutYamlFactory.html

```java
import org.junit.Rule;
import org.junit.Test;

import guru.mikelue.jdut.junit4.JdutYamlFactoryTest;
import guru.mikelue.jdut.annotation.JdutResource;

public class YourSomethingTest {
    @Rule
    public JdutYamlFactory jdutYamlFactoryForMethodLevel = new JdutYamlFactory(conductorFactory);

    @Test @JdutResource
    public void sampleTest()
    {
        /* Your tests... */
    }
}
```

---

## 6. Database Vendor

---

### Vendor-specific operators

[DefaultOperatorFactory] would try to fetch best-implementation for vendor-specific operations.

You can override the operators implemented by JDUT.

The method of [DefaultOperatorFactory.Builder.add](apidocs/guru/mikelue/jdut/operation/DefaultOperatorFactory.Builder.html) accepts two arguments:

[OperatorPredicate]
:   The method of [testMetaData(DatabaseMetaData)](apidocs/guru/mikelue/jdut/function/OperatorPredicate.html#testMetaData-java.sql.DatabaseMetaData-) is used to check
    the meta data of current connection to database.

    This predicate returning **true value** means to use your mapping of operators.

[Map<String, DataGrainOperator>][Map]
:   The mapping is used to fetch operator by name for
    for fisrt matched [OperatorPredicate].

You don't have to define all of the names defined in [DefaultOperators] in the [Map] of operators.
[DefaultOperatorFactory] would fetch a default one if your mapping doesn't contain the name.

See [DatabaseMetaData](https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html).

[OperatorPredicate]: apidocs/guru/mikelue/jdut/function/OperatorPredicate.html
[DefaultOperators]: apidocs/guru/mikelue/jdut/operation/DefaultOperators.html

**The first matched predicte would be selected for the vendor-specific operation**

```java
import guru.mikelue.jdut.operation.DefaultOperatorFactory;
import guru.mikelue.jdut.operation.OperatorFactory;

// dataSource - The initialized data source
// yourMapOfOperators - Your customized mapping of operators
OperatorFactory yourFactory = DefaultOperatorFactory.build(
    dataSource,
    builder -> builder
        .add(
            metaData -> metaData.getDriverName().contains("something"),
            yourMapOfOperators
        )
);

```

---

### Notes
Following list states the databases and JDBC drivers which have been tested:

| Database Vendor          | Database Version | JDBC Version   | Misc           |
| :-------------           | :-------------   | :------------- | :------------- |
| [PostgreSql][postgresql] | 9.4              | jdbc41         |                |
| [MySql][mysql]           | 5.6              | 5.1            |                |
| [Oracle][oracle]         | 11g express      | 11.2.0(thin)   |                |
| [MS SQL Server][mssql]   | 2013 express     | 4.2            |                |
| [H2][h2]                 | 1.4              | 1.4            |                |
| [HsqlDb][hsqldb]         | 2.3              | 2.3            |                |
| [Derby][derby]           | 10.11            | 10.11          |                |

[postgresql]: https://www.postgresql.org/
[mysql]: https://www.mysql.com/
[oracle]: https://www.oracle.com/database/index.html
[mssql]: https://www.microsoft.com/en-cy/sql-server/sql-server-downloads
[h2]: https://www.h2database.com/
[hsqldb]: https://hsqldb.org/
[derby]: https://db.apache.org/derby/

[DataGrainOperator]: apidocs/guru/mikelue/jdut/operation/DataGrainOperator.html

[Consumer]: https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html
[DataSource]: https://docs.oracle.com/javase/8/docs/api/javax/sql/DataSource.html
[Connection]: https://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html
[Reader]: https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html
[Map]: https://docs.oracle.com/javase/8/docs/api/java/util/Map.html

[JUnit4]: https://junit.org/junit4/
[TestNG]: https://testng.org/

#### Keys of row
In order to execute `DELETE` or `UPDATE` more precisely, following priority for looking up one or multiple columns in a table is applied to build `WHERE` statement:

1. Setting of keys in [SchemaTable.Builder.keys(String...)](apidocs/guru/mikelue/jdut/datagrain/SchemaTable.Builder.html#keys-java.lang.String...-) or `keys` of YAML
1. **primary key**
1. **unique index** with least number of columns and all of their value have non-null value
    * Choose first constraint(as order of [DatabaseMetaData.getIndexInfo(...)](https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html#getIndexInfo-java.lang.String-java.lang.String-java.lang.String-boolean-boolean-))
