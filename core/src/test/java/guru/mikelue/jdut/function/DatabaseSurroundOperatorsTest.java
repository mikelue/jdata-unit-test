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
	 * Tests the transactional operator.
	 */
	@Test
	public void transactional() throws SQLException
	{
		DataGrainOperator testedOperator = DataGrainOperator::none;
		testedOperator = testedOperator.surroundedBy(DatabaseSurroundOperators.operateTransactional(2));

		testedOperator.operate(mockConn, null);

		new Verifications() {{
			mockConn.setAutoCommit(false);
			times = 1;

			mockConn.setTransactionIsolation(2);
			times = 1;

			mockConn.commit();
			times = 1;
		}};
	}

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
