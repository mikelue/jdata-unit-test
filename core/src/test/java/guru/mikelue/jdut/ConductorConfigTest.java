package guru.mikelue.jdut;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.SQLExceptionConvert;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.OperatorFactory;

public class ConductorConfigTest {
	public ConductorConfigTest() {}

	/**
	 * Tests the getting of resource loader.
	 */
	@Test(dataProvider="GetResourceLoader")
	public void getResourceLoader(
		ConductorConfig sampleParent, Function<String, Reader> resourceLoader,
		boolean expectedResourceLoader, int expectedValueOfResourceLoader
	) {
		ConductorConfig testedConfig = ConductorConfig.build(
			builder -> builder
				.parent(sampleParent)
				.resourceLoader(resourceLoader)
		);

		Optional<Function<String, Reader>> testedLoader = testedConfig.getResourceLoader();
		Assert.assertEquals(testedLoader.isPresent(), expectedResourceLoader);

		if (testedLoader.isPresent()) {
			Assert.assertEquals(
				((SampleResourceLoader)testedLoader.get()).v,
				expectedValueOfResourceLoader
			);
		}
	}
	@DataProvider(name="GetResourceLoader")
	private Object[][] getGetResourceLoader()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.resourceLoader(new SampleResourceLoader(2))
		);

		return new Object[][] {
			{ null, null, false, 0 }, // No resource loader
			{ null, new SampleResourceLoader(1), true, 1 }, // Has resource loader
			{ parent, new SampleResourceLoader(1), true, 1 }, // Overrides parent's loader
			{ parent, null, true, 2 }, // Use parent's resource loader
		};
	}

	/**
	 * Tests the getting of resource loader.
	 */
	@Test(dataProvider="GetSqlExceptionConvert")
	public void getSqlExceptionConvert(
		ConductorConfig sampleParent, SQLExceptionConvert<?> sqlExceptionConvert,
		boolean expectedSqlExceptionConvert, int expectedValueOfSqlExceptionConvert
	) {
		ConductorConfig testedConfig = ConductorConfig.build(
			builder -> builder
				.parent(sampleParent)
				.sqlExceptionConvert(sqlExceptionConvert)
		);

		Optional<SQLExceptionConvert<?>> testedLoader = testedConfig.getSqlExceptionConvert();
		Assert.assertEquals(testedLoader.isPresent(), expectedSqlExceptionConvert);

		if (testedLoader.isPresent()) {
			Assert.assertEquals(
				((SampleSqlExceptionConvert)testedLoader.get()).v,
				expectedValueOfSqlExceptionConvert
			);
		}
	}
	@DataProvider(name="GetSqlExceptionConvert")
	private Object[][] getGetSqlExceptionConvert()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.sqlExceptionConvert(new SampleSqlExceptionConvert(2))
		);

		return new Object[][] {
			{ null, null, false, 0 }, // No resource loader
			{ null, new SampleSqlExceptionConvert(1), true, 1 }, // Has resource loader
			{ parent, new SampleSqlExceptionConvert(1), true, 1 }, // Overrides parent's loader
			{ parent, null, true, 2 }, // Use parent's resource loader
		};
	}

	/**
	 * Tests the getting of decorator.
	 */
	@Test(dataProvider="GetDecorator")
	public void getDecorator(
		ConductorConfig sampleParent, String nameOfDecorator,
		boolean expectedDecorator, int expectedValueOfDecorator
	) {
		ConductorConfig testedConfig = ConductorConfig.build(
			builder -> builder
				.parent(sampleParent)
				.namedDecorator("existing-1", new SampleDecorator(1))
		);

		Optional<DataGrainDecorator> testedDecorator = testedConfig.getDecorator(nameOfDecorator);
		Assert.assertEquals(testedDecorator.isPresent(), expectedDecorator);

		if (testedDecorator.isPresent()) {
			Assert.assertEquals(
				((SampleDecorator)testedDecorator.get()).v,
				expectedValueOfDecorator
			);
		}
	}
	@DataProvider(name="GetDecorator")
	private Object[][] getGetDecorator()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedDecorator("p-existing-1", new SampleDecorator(2))
				.namedDecorator("existing-1", new SampleDecorator(3)) // Overrided
		);

		return new Object[][] {
			{ null, "not-existing-1", false, 1 }, // No decorator
			{ null, "existing-1", true, 1 }, // Has decorator
			{ parent, "existing-1", true, 1 }, // Overrides decorator of parent
			{ parent, "p-existing-1", true, 2 }, // Use parent's decorator
		};
	}

	/**
	 * Tests the getting of JDBC function.
	 */
	@Test(dataProvider="GetJdbcFunction")
	public void getJdbcFunction(
		ConductorConfig sampleParent, String nameOfJdbcFunction,
		boolean expectedJdbcFunction, int expectedValueOfJdbcFunction
	) {
		ConductorConfig testedConfig = ConductorConfig.build(
			builder -> builder
				.parent(sampleParent)
				.namedJdbcFunction("existing-1", new SampleJdbcFunction(1))
		);

		Optional<JdbcFunction<Connection, ?>> testedJdbcFunction = testedConfig.getJdbcFunction(nameOfJdbcFunction);
		Assert.assertEquals(testedJdbcFunction.isPresent(), expectedJdbcFunction);

		if (testedJdbcFunction.isPresent()) {
			Assert.assertEquals(
				((SampleJdbcFunction)testedJdbcFunction.get()).v,
				expectedValueOfJdbcFunction
			);
		}
	}
	@DataProvider(name="GetJdbcFunction")
	private Object[][] getGetJdbcFunction()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedJdbcFunction("f-existing-1", new SampleJdbcFunction(2))
				.namedJdbcFunction("existing-1", new SampleJdbcFunction(3)) // Overrided
		);

		return new Object[][] {
			{ null, "not-existing-1", false, 1 }, // No JDBC function
			{ null, "existing-1", true, 1 }, // Has JDBC function
			{ parent, "existing-1", true, 1 }, // Overrides JDBC function of parent
			{ parent, "f-existing-1", true, 2 }, // Use parent's JDBC function
		};
	}

	/**
	 * Tests the getting of operator.
	 */
	@Test(dataProvider="GetOperator")
	public void getOperator(
		ConductorConfig sampleParent, OperatorFactory sampleOperatorFactory,
		String nameOfOperator,
		boolean expectedOperator, int expectedValueOfOperator
	) {
		ConductorConfig testedConfig = ConductorConfig.build(
			builder -> builder
				.parent(sampleParent)
				.operatorFactory(sampleOperatorFactory)
				.namedOperator("existing-1", new SampleOperator(1))
		);

		Optional<DataGrainOperator> testedOperator = testedConfig.getOperator(nameOfOperator);
		Assert.assertEquals(testedOperator.isPresent(), expectedOperator);

		if (testedOperator.isPresent()) {
			Assert.assertEquals(
				((SampleOperator)testedOperator.get()).v,
				expectedValueOfOperator
			);
		}
	}
	@DataProvider(name="GetOperator")
	private Object[][] getGetOperator()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedOperator("p-existing-1", new SampleOperator(2))
				.namedOperator("of-existing-1", new SampleOperator(3))
				.namedOperator("existing-1", new SampleOperator(3)) // Overrided
		);

		OperatorFactory sampleOperatorFactory =
			new OperatorFactory() {
				@Override
				public DataGrainOperator get(String name)
				{
					if ("of-existing-1".equals(name)) {
						return new SampleOperator(13);
					}

					return null;
				}
			};

		return new Object[][] {
			{ null, null, "not-existing-1", false, 1 }, // No operator
			{ null, null, "existing-1", true, 1 }, // Has operator
			{ null, sampleOperatorFactory, "existing-1", true, 1 }, // Has operator(overrides factory)
			{ null, sampleOperatorFactory, "of-existing-1", true, 13 }, // Has operator(by factory)
			{ null, sampleOperatorFactory, "not-existing-1", false, 1 }, // No operator
			{ parent, null, "existing-1", true, 1 }, // Overrides operator of parent
			{ parent, sampleOperatorFactory, "of-existing-1", true, 13 }, // Overrides operator of parent
			{ parent, sampleOperatorFactory, "p-existing-1", true, 2 }, // Use operator of parent
		};
	}

	/**
	 * Tests the getting of supplier.
	 */
	@Test(dataProvider="GetSupplier")
	public void getSupplier(
		ConductorConfig sampleParent, String nameOfSupplier,
		boolean expectedSupplier, int expectedValueOfSupplier
	) {
		ConductorConfig testedConfig = ConductorConfig.build(
			builder -> builder
				.parent(sampleParent)
				.namedSupplier("existing-1", new SampleSupplier(1))
		);

		Optional<Supplier<?>> testedSupplier = testedConfig.getSupplier(nameOfSupplier);
		Assert.assertEquals(testedSupplier.isPresent(), expectedSupplier);

		if (testedSupplier.isPresent()) {
			Assert.assertEquals(
				((SampleSupplier)testedSupplier.get()).v,
				expectedValueOfSupplier
			);
		}
	}
	@DataProvider(name="GetSupplier")
	private Object[][] getGetSupplier()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedSupplier("p-existing-1", new SampleSupplier(2))
				.namedSupplier("existing-1", new SampleSupplier(3)) // Overrided
		);

		return new Object[][] {
			{ null, "not-existing-1", false, 1 }, // No supplier
			{ null, "existing-1", true, 1 }, // Has supplier
			{ parent, "existing-1", true, 1 }, // Overrides supplier of parent
			{ parent, "p-existing-1", true, 2 }, // Use parent's supplier
		};
	}
}

class SampleResourceLoader implements Function<String, Reader> {
	int v;

	SampleResourceLoader(int newValue)
	{
		v = newValue;
	}

	@Override
	public Reader apply(String name)
	{
		return null;
	}
}
class SampleDecorator implements DataGrainDecorator {
	int v;

	SampleDecorator(int value)
	{
		v = value;
	}

	@Override
	public void decorate(DataRow.Builder rowBuilder) {}
}
class SampleJdbcFunction implements JdbcFunction<Connection, Void> {
	int v;

	SampleJdbcFunction(int value)
	{
		v = value;
	}

	@Override
	public Void applyJdbc(Connection conn)
	{
		return null;
	}
}
class SampleOperator implements DataGrainOperator {
	int v;

	SampleOperator(int value)
	{
		v = value;
	}

	@Override
	public DataGrain operate(Connection conn, DataGrain dataGrain)
	{
		return dataGrain;
	}
}
class SampleSupplier implements Supplier<Integer> {
	int v;

	SampleSupplier(int value)
	{
		v = value;
	}

	@Override
	public Integer get() { return v; }
}

class SampleSqlExceptionConvert implements SQLExceptionConvert<RuntimeException> {
	int v;

	SampleSqlExceptionConvert(int value)
	{
		v = value;
	}

	@Override
	public RuntimeException apply(SQLException e)
	{
		return new RuntimeException(e);
	}
}
