package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.sql.DataSource;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Expectations;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataGrain;

public class DefaultOperatorFactoryTest {
	@Injectable
	private DatabaseMetaData mockMetaData;
	@Mocked
	private DataSource mockDataSource;

	public DefaultOperatorFactoryTest() {}

	/**
	 * Tests the building of object and get specific matched operators.<br>
	 *
	 * The testing of default ones is performed by {@link DefaultOperatorsTest}.
	 */
	@Test(dataProvider="Build")
	public void build(
		String sampleProductName, String sampleOperatorName,
		Consumer<CallingTracer> assertion
	) throws SQLException {
		new Expectations() {{
			mockDataSource.getConnection().getMetaData();
			result = mockMetaData;

			mockMetaData.getDriverName();
			result = "";

			mockMetaData.getDatabaseProductName();
			result = sampleProductName;
		}};

		CallingTracer tracer = new CallingTracer();

		/**
		 * Prepare factory with customized operators
		 */
		OperatorFactory testedFactory = DefaultOperatorFactory.build(
			mockDataSource,
			builder -> builder
				.add(
					metaData -> metaData.getDatabaseProductName().equals("match-1"),
					buildSampleOperators(tracer::grainInsert1, tracer::grainUpdate)
				)
				.add( // Not used predicate
					metaData -> metaData.getDatabaseProductName().equals("match-1"),
					buildSampleOperators(tracer::grainInsert2, tracer::grainUpdate)
				)
		);
		// :~)

		testedFactory.get(sampleOperatorName).operate(null, null);

		assertion.accept(tracer);
	}
	@DataProvider(name="Build")
	private Object[][] getBuild()
	{
		return new Object[][] {
			/**
			 * Asserts the matched operations
			 */
			{ "match-1", DefaultOperators.INSERT,
				(Consumer<CallingTracer>)tracer -> {
					Assert.assertTrue(tracer.insert1);
					Assert.assertFalse(tracer.insert2);
					Assert.assertFalse(tracer.update);
				}
			},
			{ "match-1", DefaultOperators.UPDATE,
				(Consumer<CallingTracer>)tracer -> {
					Assert.assertFalse(tracer.insert1);
					Assert.assertFalse(tracer.insert2);
					Assert.assertTrue(tracer.update);
				}
			},
			// :~)
		};
	}
	private Map<String, DataGrainOperator> buildSampleOperators(
		DataGrainOperator insertOperator, DataGrainOperator updateOperator
	) {
		Map<String, DataGrainOperator> operators = new HashMap<>();
		operators.put(DefaultOperators.INSERT, insertOperator);
		operators.put(DefaultOperators.UPDATE, updateOperator);
		return operators;
	}

}

/**
 * Used for tracing the calling of lambda of DataGrainOperator.
 */
class CallingTracer {
	boolean insert1 = false;
	boolean insert2 = false;
	boolean update = false;

	DataGrain grainInsert1(Connection conn, DataGrain dataGrain)
	{
		insert1 = true;
		return dataGrain;
	}
	DataGrain grainInsert2(Connection conn, DataGrain dataGrain)
	{
		insert2 = true;
		return dataGrain;
	}
	DataGrain grainUpdate(Connection conn, DataGrain dataGrain)
	{
		update = true;
		return dataGrain;
	}
}
