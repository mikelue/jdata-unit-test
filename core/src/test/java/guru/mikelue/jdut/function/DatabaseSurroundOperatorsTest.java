package guru.mikelue.jdut.function;

import java.sql.Connection;
import java.sql.SQLException;

import mockit.Injectable;
import mockit.Verifications;
import org.testng.annotations.Test;

import guru.mikelue.jdut.operation.DataGrainOperator;

public class DatabaseSurroundOperatorsTest {
	@Injectable
	private Connection mockConn;

	public DatabaseSurroundOperatorsTest() {}

	/**
	 * Tests the auto-closed operator.
	 */
	@Test
	public void autoClose() throws SQLException
	{
		DataGrainOperator testedOperator = DataGrainOperator::none;
		testedOperator = testedOperator.surroundedBy(DatabaseSurroundOperators::autoClose);

		testedOperator.operate(mockConn, null);

		new Verifications() {{
			mockConn.close();
			times = 1;
		}};
	}
}
