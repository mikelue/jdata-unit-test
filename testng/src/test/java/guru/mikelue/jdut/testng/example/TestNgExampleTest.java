package guru.mikelue.jdut.testng.example;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.IAttributes;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.testng.IInvokedMethodYamlFactoryListener;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

@Listeners(TestNgExampleTest.ExampleMethodListener.class)
public class TestNgExampleTest extends AbstractDataSourceTestBase {
    /**
     * The listener with some configurations.
     */
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

		@Override
		protected boolean needConductData(IInvokedMethod method, ITestResult testResult)
		{
			return TestNgExampleTest.class.isAssignableFrom(
				method.getTestMethod().getTestClass().getRealClass()
			);
		}
    }

	private static ExampleDao testedDao;

    public TestNgExampleTest() {}

    /**
     * Tests the counting of data by TestNG
     */
    @Test @JdutResource
    public void countAlbumsByType() throws SQLException
    {
		Assert.assertEquals(
			testedDao.countAlbumsByType(1),
			2
		);
    }

    @BeforeTest(dependsOnMethods="initDataSourceTest")
    static void setupDataSource(ITestContext testContext)
    {
        IInvokedMethodYamlFactoryListener.setDataSource(testContext, getDataSource());
    }
    @AfterTest
    static void cleanDataSource(ITestContext testContext)
    {
        IInvokedMethodYamlFactoryListener.removeDataSource(testContext);
    }

    @BeforeClass
    static void setupSchema()
    {
        SchemaSetup.buildSchema(getDataSource());
		testedDao = new ExampleDao(getDataSource());
    }

	private static Date randomDate()
	{
		return Date.valueOf(
			LocalDate.of(
				RandomUtils.nextInt(1930, 1956),
				RandomUtils.nextInt(1, 13),
				RandomUtils.nextInt(1, 26)
			)
		);
	}

	private static int randomDuration()
	{
		return RandomUtils.nextInt(1900, 8801);
	}
}
