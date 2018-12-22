# Examples

This page demonstrates example from real testing code of JDUT.

## Database schema
[Database Schema](xref-test/guru/mikelue/jdut/example/SchemaSetup.html)

```sql
CREATE TABLE IF NOT EXISTS ex_artist(
    at_id IDENTITY PRIMARY KEY,
    at_name VARCHAR(512) NOT NULL,
    at_gender TINYINT NOT NULL DEFAULT 3,
    at_birthday DATE
);

CREATE TABLE IF NOT EXISTS ex_album(
    ab_id IDENTITY PRIMARY KEY,
    ab_name VARCHAR(512) NOT NULL,
    ab_release_date DATE NOT NULL,
    ab_duration_seconds SMALLINT NOT NULL,
    ab_type TINYINT NOT NULL DEFAULT 1,
    ab_at_id INTEGER NOT NULL
);
```

---

## Java API

The file [JdbcExampleTest.java] contains runnable code for examples of using JDUT by pure-Java API.

---

### Building data

Following code shows how to build data for method of `countAlbumsByType` in [JdbcExampleTest.java]

```java
private DataGrain dataGrain_1ForListing;
private DataGrain dataGrain_2ForListing;

int idOfArtistForListing = 9081;

/**
 * Random data supplier
 */
Supplier<Date> randomDate = JdbcExampleTest::randomDate;
Supplier<Integer> randomDuration = JdbcExampleTest::randomDuration;
// :~)

/**
 * Insertion of data for table "ex_artist"
 */
dataConductor.conduct(
    dataGrain_1ForListing = DataGrain.build(
        builder -> builder.name("ex_artist"),
        rowsBuilder -> rowsBuilder
            .implicitColumns("at_id", "at_name")
            .addValues(idOfArtistForListing, "Sonny Rollins")
    ),
    operatorFactory.get(DefaultOperators.INSERT)
);
// :~)
/**
 * Insertion of data for table "ex_album"
 */
dataConductor.conduct(
    dataGrain_2ForListing = DataGrain.build(
        builder -> builder.name("ex_album"),
        rowsBuilder -> rowsBuilder
            .implicitColumns(
                "ab_id", "ab_name", "ab_release_date", "ab_duration_seconds", "ab_type", "ab_at_id"
            )
            .addValues(
                4051, "No. 1", randomDate, randomDuration,
                1,
                idOfArtistForListing
            )
            .addValues(
                4052, "No. 2", randomDate, randomDuration,
                2,
                idOfArtistForListing
            )
            .addValues(
                4053, "No. 3", randomDate, randomDuration,
                3,
                idOfArtistForListing
            )
            .addValues(
                4054, "No. 4", randomDate, randomDuration,
                1,
                idOfArtistForListing
            )
    ),
    operatorFactory.get(DefaultOperators.INSERT)
);
// :~)
```

### Cleaning data

Following code shows how to clean data for method of `countAlbumsByType` in [JdbcExampleTest.java]

```java
dataConductor.conduct(
    dataGrain_2ForListing,
    operatorFactory.get(DefaultOperators.DELETE)
);
dataConductor.conduct(
    dataGrain_1ForListing,
    operatorFactory.get(DefaultOperators.DELETE)
);
break;
```

[JdbcExampleTest.java]: xref-test/guru/mikelue/jdut/example/JdbcExampleTest.html
[YamlExampleTest.java]: xref-test/guru/mikelue/jdut/example/YamlExampleTest.html

---

## YAML API

The file [YamlExampleTest.java] contains runnable code for examples of using JDUT by YAML.

You can find [YAML files](https://github.com/mikelue/jdata-unit-test/tree/master/core/src/test/resources/guru/mikelue/jdut/example/) in GitHub.

---

### Building data by YAML
Following code snippet demonstrates using of [DuetConductor]`.build` with YAML.

```java
private static Map<String, DuetConductor> duetConductors = new HashMap<>(6);

@BeforeMethod
private void buildData(Method method)
{
    duetConductors.put(
        method.getName(),
        yamlConductor.conductResource(
            "guru/mikelue/jdut/example/YamlExampleTest-" + method.getName() + ".yaml"
        )
    );

    duetConductors.get(method.getName()).build();
}
```

### Cleaning data by YAML
Following code snippet demonstrates using of [DuetConductor]`.clean` with YAML.

```java
@AfterMethod
private void cleanData(Method method)
{
    duetConductors.get(method.getName()).clean();
}
```

---

### Data definitions

```yaml
%TAG !jdut! tag:jdut.mikelue.guru:1.0/
%TAG !dbtype! tag:jdut.mikelue.guru:jdbcType:1.8/
%TAG !sql! tag:jdut.mikelue.guru:sql:1.0/

---
- !sql!table ex_artist : [
    { at_id: &artist_id 38997, at_name: "Charlie Parker" }
]

- !sql!table ex_album : {
    config: {
        build_operation: "insert_and_log",
        decorator: "decorator_album"
    },

    data: [
        {
            ab_id: 4051, ab_name: "No. 1",
            ab_duration_seconds: !jdut!supplier "random_duration", ab_release_date: !jdut!supplier "random_date",
            ab_type: 1, ab_at_id: *artist_id
        },
        {
            ab_id: 4052, ab_name: "No. 2",
            ab_duration_seconds: !jdut!supplier "random_duration", ab_release_date: !jdut!supplier "random_date",
            ab_type: 1, ab_at_id: *artist_id
        },
        {
            ab_id: 4053, ab_name: "No. 3",
            ab_duration_seconds: !jdut!supplier "random_duration", ab_release_date: !jdut!supplier "random_date",
            ab_type: 2, ab_at_id: *artist_id
        },
        {
            ab_id: 4054, ab_name: "No. 4",
            ab_duration_seconds: !jdut!supplier "random_duration", ab_release_date: !jdut!supplier "random_date",
            ab_type: 3, ab_at_id: *artist_id
        }
    ]
}
```

---

## TestNG

The file [TestNgExampleTest.java] contains runnable code for examples of using JDUT by YAML and build-in listeners of [TestNG].

You can find [YAML files](https://github.com/mikelue/jdata-unit-test/tree/master/testng/src/test/resources/guru/mikelue/jdut/testng/example/) in GitHub.

---

### Register listener(TestNG)
Following code snippet shows the registering listener to [TestNG].

```java
@Listeners(TestNgExampleTest.ExampleMethodListener.class)
public class TestNgExampleTest extends AbstractDataSourceTestBase {
    /* ... */
}

```

### Use of @JdutResource
Following code snippet demonstrates the usage of `@JdutResource` on your test methods.

```java
@Test @JdutResource
public void countAlbumsByType() throws SQLException
{
    Assert.assertEquals(
        testedDao.countAlbumsByType(1),
        2
    );
}
```

---

### Customize listener
Following code snippet demonstrates the listener with customized value supplier, decorator, operator, etc,.

```java
// lhjs:java
public static class ExampleMethodListener extends IInvokedMethodYamlFactoryListener {
    private Logger logger = LoggerFactory.getLogger(ExampleMethodListener.class);

    public ExampleMethodListener() {}

    private YamlConductorFactory yamlFactory = null;

    @Override
    protected YamlConductorFactory buildYamlConductorFactory(IAttributes attributes)
    {
        if (yamlFactory != null) {
            return yamlFactory;
        }

        yamlFactory = YamlConductorFactory.build(
            getDataSource(attributes),
            builder -> builder
                .namedSupplier(
                    "random_date", TestNgExampleTest::randomDate
                )
                .namedSupplier(
                    "random_duration", TestNgExampleTest::randomDuration
                )
                .namedOperator(
                    "insert_and_log",
                    (connection, dataGrain) -> {
                        logger.info("@@@ BEFORE BUILDING DATA @@@");

                        DataGrain result = DefaultOperators.insert(connection, dataGrain);

                        logger.info("@@@ AFTER BUILDING DATA @@@");

                        return result;
                    }
                )
                .namedDecorator(
                    "decorator_album",
                    (dataRowBuilder) -> {
                        dataRowBuilder.fieldOfValue(
                            "ab_name",
                            dataRowBuilder.getData("ab_name").get() + "(BlueNote)"
                        );
                    }
                )
        );

        return yamlFactory;
    }
}
```

[TestNG]: https://testng.org/
[TestNgExampleTest.java]: xref-test/guru/mikelue/jdut/testng/example/TestNgExampleTest.html

---

## JUnit 4

The module supports JUnit since version of **4.9**.

You can find [YAML files](https://github.com/mikelue/jdata-unit-test/tree/master/junit4/src/test/resources/guru/mikelue/jdut/junit4/) in GitHub.

---

### @Rule

You may use [@Rule](https://junit.org/junit4/javadoc/latest/org/junit/Rule.html) to use the conduction of data from YAML file.

See full example from [JdutYamlFactoryTest.java].

```java
import org.junit.Rule;
import org.junit.Test;

import guru.mikelue.jdut.junit4.JdutYamlFactoryTest;
import guru.mikelue.jdut.annotation.JdutResource;

public class JdutYamlFactoryTest extends AbstractDataSourceTestBase {
    @Rule
    public JdutYamlFactory jdutYamlFactoryForMethodLevel = new JdutYamlFactory(conductorFactory);

    @Test @JdutResource
    public void sampleTest()
    {
        /* Your tests... */
    }
}
```

[JdutYamlFactoryTest.java]: xref-test/guru/mikelue/jdut/junit4/JdutYamlFactoryTest.html

### @ClassRule

You may use [@ClassRule](https://junit.org/junit4/javadoc/latest/org/junit/ClassRule.html) to use the conduction of data from YAML file.

See full example from [JdutYamlFactoryForClassRuleTest.java].

```java
import org.junit.Rule;
import org.junit.Test;

import guru.mikelue.jdut.junit4.JdutYamlFactoryTest;
import guru.mikelue.jdut.annotation.JdutResource;

@JdutResource
public class JdutYamlFactoryForClassRuleTest extends AbstractDataSourceTestBase {
    @ClassRule
    public static TestRule rule = new TestRule() {
        @Override
        public Statement apply(Statement base, Description description)
        {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable
                {
                    new JdutYamlFactory(getDataSource())
                        .apply(base, description)
                        .evaluate();
                }
            };
        }
    };

    public JdutYamlFactoryForClassRuleTest() {}

    @Test
    public void sampleTest()
    {
        /* Your tests... */
    }
}
```

[JdutYamlFactoryForClassRuleTest.java]: xref-test/guru/mikelue/jdut/junit4/JdutYamlFactoryForClassRuleTest.html

[DuetConductor]: apidocs/guru/mikelue/jdut/DuetConductor.html
