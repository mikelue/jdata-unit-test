package guru.mikelue.jdut;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.SQLExceptionConvert;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.OperatorFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class ConductorConfigTest {
	public ConductorConfigTest() {}

	/**
	 * Tests the getting of resource loader.
	 */
	@ParameterizedTest
	@MethodSource
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
		assertEquals(expectedResourceLoader, testedLoader.isPresent());

		if (testedLoader.isPresent()) {
			assertEquals(
				expectedValueOfResourceLoader,
				((SampleResourceLoader)testedLoader.get()).v
			);
		}
	}
	static Arguments[] getResourceLoader()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.resourceLoader(new SampleResourceLoader(2))
		);

		return new Arguments[] {
			arguments(null, null, false, 0), // No resource loader
			arguments(null, new SampleResourceLoader(1), true, 1), // Has resource loader
			arguments(parent, new SampleResourceLoader(1), true, 1), // Overrides parent's loader
			arguments(parent, null, true, 2), // Use parent's resource loader
		};
	}

	/**
	 * Tests the getting of resource loader.
	 */
	@ParameterizedTest
	@MethodSource
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
		assertEquals(expectedSqlExceptionConvert, testedLoader.isPresent());

		if (testedLoader.isPresent()) {
			assertEquals(
				expectedValueOfSqlExceptionConvert,
				((SampleSqlExceptionConvert)testedLoader.get()).v
			);
		}
	}
	static Arguments[] getSqlExceptionConvert()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.sqlExceptionConvert(new SampleSqlExceptionConvert(2))
		);

		return new Arguments[] {
			arguments(null, null, false, 0), // No resource loader
			arguments(null, new SampleSqlExceptionConvert(1), true, 1), // Has resource loader
			arguments(parent, new SampleSqlExceptionConvert(1), true, 1), // Overrides parent's loader
			arguments(parent, null, true, 2), // Use parent's resource loader
		};
	}

	/**
	 * Tests the getting of decorator.
	 */
	@ParameterizedTest
	@MethodSource
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
		assertEquals(expectedDecorator, testedDecorator.isPresent());

		if (testedDecorator.isPresent()) {
			assertEquals(
				expectedValueOfDecorator,
				((SampleDecorator)testedDecorator.get()).v
			);
		}
	}
	static Arguments[] getDecorator()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedDecorator("p-existing-1", new SampleDecorator(2))
				.namedDecorator("existing-1", new SampleDecorator(3)) // Overrode
		);

		return new Arguments[] {
			arguments(null, "not-existing-1", false, 1), // No decorator
			arguments(null, "existing-1", true, 1), // Has decorator
			arguments(parent, "existing-1", true, 1), // Overrides decorator of parent
			arguments(parent, "p-existing-1", true, 2), // Use parent's decorator
		};
	}

	/**
	 * Tests the getting of JDBC function.
	 */
	@ParameterizedTest
	@MethodSource
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
		assertEquals(expectedJdbcFunction, testedJdbcFunction.isPresent());

		if (testedJdbcFunction.isPresent()) {
			assertEquals(
				expectedValueOfJdbcFunction,
				((SampleJdbcFunction)testedJdbcFunction.get()).v
			);
		}
	}
	static Arguments[] getJdbcFunction()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedJdbcFunction("f-existing-1", new SampleJdbcFunction(2))
				.namedJdbcFunction("existing-1", new SampleJdbcFunction(3)) // Overrode
		);

		return new Arguments[] {
			arguments(null, "not-existing-1", false, 1), // No JDBC function
			arguments(null, "existing-1", true, 1), // Has JDBC function
			arguments(parent, "existing-1", true, 1), // Overrides JDBC function of parent
			arguments(parent, "f-existing-1", true, 2), // Use parent's JDBC function
		};
	}

	/**
	 * Tests the getting of operator.
	 */
	@ParameterizedTest
	@MethodSource
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
		assertEquals(expectedOperator, testedOperator.isPresent());

		if (testedOperator.isPresent()) {
			assertEquals(
				expectedValueOfOperator,
				((SampleOperator)testedOperator.get()).v
			);
		}
	}
	static Arguments[] getOperator()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedOperator("p-existing-1", new SampleOperator(2))
				.namedOperator("of-existing-1", new SampleOperator(3))
				.namedOperator("existing-1", new SampleOperator(3)) // Overrode
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

		return new Arguments[] {
			arguments(null, null, "not-existing-1", false, 1), // No operator
			arguments(null, null, "existing-1", true, 1), // Has operator
			arguments(null, sampleOperatorFactory, "existing-1", true, 1), // Has operator(overrides factory)
			arguments(null, sampleOperatorFactory, "of-existing-1", true, 13), // Has operator(by factory)
			arguments(null, sampleOperatorFactory, "not-existing-1", false, 1), // No operator
			arguments(parent, null, "existing-1", true, 1), // Overrides operator of parent
			arguments(parent, sampleOperatorFactory, "of-existing-1", true, 13), // Overrides operator of parent
			arguments(parent, sampleOperatorFactory, "p-existing-1", true, 2), // Use operator of parent
		};
	}

	/**
	 * Tests the getting of supplier.
	 */
	@ParameterizedTest
	@MethodSource
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
		assertEquals(expectedSupplier, testedSupplier.isPresent());

		if (testedSupplier.isPresent()) {
			assertEquals(
				expectedValueOfSupplier,
				((SampleSupplier)testedSupplier.get()).v
			);
		}
	}
	static Arguments[] getSupplier()
	{
		ConductorConfig parent = ConductorConfig.build(
			builder -> builder
				.namedSupplier("p-existing-1", new SampleSupplier(2))
				.namedSupplier("existing-1", new SampleSupplier(3)) // Overrode
		);

		return new Arguments[] {
			arguments(null, "not-existing-1", false, 1), // No supplier
			arguments(null, "existing-1", true, 1), // Has supplier
			arguments(parent, "existing-1", true, 1), // Overrides supplier of parent
			arguments(parent, "p-existing-1", true, 2), // Use parent's supplier
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
