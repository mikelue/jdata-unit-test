package guru.mikelue.jdut.operation;

import java.sql.SQLException;

import org.apache.commons.lang3.mutable.MutableInt;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataGrainOperatorTest {
	public DataGrainOperatorTest() {}

	/**
	 * Tests the surrouning of operator.
	 */
	@Test
	public void surroundedBy() throws SQLException
	{
		MutableInt executeTimes = new MutableInt(0);

		DataGrainOperator testedOperator = (connection, dataGrain) -> {
			executeTimes.increment();
			return dataGrain;
		};

		testedOperator = testedOperator.surroundedBy(
			o -> (connection, dataGrain) -> {
				executeTimes.increment();
				o.operate(connection, dataGrain);
				return dataGrain;
			}
		);

		testedOperator.operate(null, null);

		Assert.assertEquals(executeTimes.intValue(), 2);
	}
}
