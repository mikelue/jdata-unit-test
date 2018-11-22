package guru.mikelue.jdut.function;

import java.sql.Connection;
import java.sql.SQLException;

import mockit.Mocked;
import mockit.Expectations;
import mockit.Verifications;

import org.testng.annotations.Test;

import guru.mikelue.jdut.operation.DataGrainOperator;

public class DatabaseTransactionalTest {
	@Mocked
	private Connection mockConn;

	public DatabaseTransactionalTest() {}

	/**
	 * Tests the transactional operator.
	 */
	@Test
	public void transactional() throws SQLException
	{
		new Expectations() {{
			mockConn.getAutoCommit();
			result = true;
		}};

		DataGrainOperator testedOperator = DataGrainOperator::none;
		testedOperator = testedOperator.surroundedBy(new DatabaseTransactional(2));

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
}
