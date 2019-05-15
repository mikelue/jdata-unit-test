package guru.mikelue.jdut.junit5.test;

import java.io.Reader;
import java.sql.Date;
import java.time.LocalDate;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.junit5.JdutYamlFactory;
import guru.mikelue.jdut.junit5.example.SchemaSetup;
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.yaml.ReaderFunctions;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

@ExtendWith({ AppContextExtension.class, SampleJdutYamlExtension.class })
public abstract class AbstractDataSourceTestBase {
	public AbstractDataSourceTestBase() {}

	@RegisterExtension
	static TestInstancePostProcessor appContextSetup = new TestInstancePostProcessor() {
		@Override
		public void postProcessTestInstance(Object instance, ExtensionContext context) throws Exception
		{
			AbstractDataSourceTestBase testBase = (AbstractDataSourceTestBase)instance;
			testBase.appContext = AppContextExtension.getAppContext(context);
		}
	};

	private ApplicationContext appContext;

	public DataSource getDataSource()
	{
		return appContext.getBean(DataSource.class);
	}
}

class SampleJdutYamlExtension extends JdutYamlFactory {
	private Logger logger = LoggerFactory.getLogger(SampleJdutYamlExtension.class);
	private YamlConductorFactory factory;

	SampleJdutYamlExtension() {}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		Class<?> targetClass = context.getRequiredTestClass();
		Function<String, Reader> readerByClass = ReaderFunctions.loadByClass(targetClass);
		DataSource dataSoruce = AppContextExtension.getAppContext(context).getBean(DataSource.class);

		factory = YamlConductorFactory.build(
			dataSoruce,
			builder -> builder
				.resourceLoader(readerByClass)
				.namedSupplier(
					"random_date", SampleJdutYamlExtension::randomDate
				)
				.namedSupplier(
					"random_duration", SampleJdutYamlExtension::randomDuration
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

		SchemaSetup.buildSchema(dataSoruce);

		super.beforeAll(context);
	}
	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
		factory = null;
		super.afterAll(context);
	}

	@Override
	protected YamlConductorFactory getYamlConductorFactory(ExtensionContext context, Event event)
	{
		return factory;
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
