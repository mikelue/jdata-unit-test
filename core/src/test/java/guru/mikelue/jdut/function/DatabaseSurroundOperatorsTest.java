package guru.mikelue.jdut.function;

import java.sql.Connection;
import java.sql.SQLException;

import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.Test;

import guru.mikelue.jdut.operation.DataGrainOperator;

public class DatabaseSurroundOperatorsTest {
	@Mocked
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
