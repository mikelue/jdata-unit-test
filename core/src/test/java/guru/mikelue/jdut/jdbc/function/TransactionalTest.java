package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.test.OptionalParameterListener;

@Listeners(OptionalParameterListener.class)
public class TransactionalTest {
	@Injectable
	private Connection mockedConn;

	public TransactionalTest() {}

	/**
	 * Tests the operator for transaction surrounding.<p>
	 */
	@Test(dataProvider="OperateTransactional")
	public void operateTransactional(
		boolean oldValueAutoCommit, Integer oldValueOfIsolation,
		JdbcFunction.SurroundOperator<Connection, Integer> surroundingFunc,
		boolean expectedSetAutoCommit, Optional<Integer> expectedTransactionIsolation
	) throws SQLException {
		final int sampleValue = 20;

		new Expectations() {{
			mockedConn.getAutoCommit();
			result = oldValueAutoCommit;

			mockedConn.getTransactionIsolation();
			result = oldValueOfIsolation;
		}};

		JdbcFunction<Connection, Integer> sampleFunc = conn -> {
			return sampleValue;
		};
		sampleFunc = sampleFunc.surroundedBy(surroundingFunc);

		Assert.assertEquals(sampleFunc.applyJdbc(mockedConn), Integer.valueOf(sampleValue));

		new Verifications() {{
			mockedConn.commit();
			times = 1;
		}};

		if (oldValueAutoCommit) {
			new Verifications() {{
				List<Boolean> setAutoCommit = new ArrayList<>(2);
				mockedConn.setAutoCommit(withCapture(setAutoCommit));

				Assert.assertFalse(setAutoCommit.get(0));
				Assert.assertTrue(setAutoCommit.get(1));
			}};
		} else {
			new Verifications() {{
				mockedConn.setAutoCommit(anyBoolean);
				times = 0;
			}};
		}

		if (expectedTransactionIsolation.isPresent()) {
			new Verifications() {{
				List<Integer> isolations = new ArrayList<>(2);

				mockedConn.setTransactionIsolation(withCapture(isolations));

				Assert.assertEquals(isolations.get(0), expectedTransactionIsolation.get());
				Assert.assertEquals(isolations.get(1), oldValueOfIsolation);
			}};
		} else {
			new Verifications() {{
				mockedConn.setTransactionIsolation(anyInt);
				times = 0;
			}};
		}
	}

	@DataProvider(name="OperateTransactional")
	private Object[][] getOperateTransactional()
	{
		return new Object[][] {
			{ // Set has effect
				true, Connection.TRANSACTION_SERIALIZABLE,
				(JdbcFunction.SurroundOperator<Connection, Integer>)new Transactional<Connection, Integer>(Connection.TRANSACTION_READ_COMMITTED),
				true, Optional.of(Connection.TRANSACTION_READ_COMMITTED)
			},
			{ // Doesn't have to set
				false, Connection.TRANSACTION_READ_COMMITTED,
				(JdbcFunction.SurroundOperator<Connection, Integer>)new Transactional<Connection, Integer>(Connection.TRANSACTION_READ_COMMITTED),
				true, Optional.empty()
			},
			{ // Simple transaction
				false, Connection.TRANSACTION_READ_COMMITTED,
				(JdbcFunction.SurroundOperator<Connection, Integer>)(JdbcFunction.SurroundOperator<Connection, Integer>)Transactional::simple,
				true, Optional.empty()
			},
		};
	}

	/**
	 * Tests the rollback of transaction surrounding.<p>
	 */
	@Test(expectedExceptions=SQLException.class)
	public void operateTransactionalWithRollback()
		throws SQLException
	{
		new Expectations() {{
			mockedConn.getAutoCommit();
			result = true;

			mockedConn.getTransactionIsolation();
			result = Connection.TRANSACTION_READ_COMMITTED;

			mockedConn.rollback();
			times = 1;
		}};

		JdbcFunction<Connection, Integer> sampleFunc = conn -> {
			throw new SQLException("Rollback");
		};
		sampleFunc = sampleFunc.surroundedBy(new Transactional<Connection, Integer>(Connection.TRANSACTION_SERIALIZABLE));

		sampleFunc.applyJdbc(mockedConn);

		new Verifications() {{
			/**
			 * Asserts the setting of auto commit
			 */
			List<Boolean> setAutoCommit = new ArrayList<>(2);
			mockedConn.setAutoCommit(withCapture(setAutoCommit));

			Assert.assertFalse(setAutoCommit.get(0));
			Assert.assertTrue(setAutoCommit.get(1));
			// :~)

			/**
			 * Asserts the setting of isolation
			 */
			List<Integer> isolations = new ArrayList<>(2);
			mockedConn.setTransactionIsolation(withCapture(isolations));

			Assert.assertEquals(isolations.get(0).intValue(), Connection.TRANSACTION_SERIALIZABLE);
			Assert.assertEquals(isolations.get(1).intValue(), Connection.TRANSACTION_READ_COMMITTED);
			// :~)
		}};
	}
}
