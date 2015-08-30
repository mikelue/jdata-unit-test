package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import mockit.Injectable;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.JdbcRunnable;
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
		JdbcFunction.SurroundOperator<Connection, Integer> surroundingFunc,
		Optional<Integer> expectedTransactionIsolation
	) throws SQLException {
		final int sampleValue = 20;

		JdbcFunction<Connection, Integer> sampleFunc = conn -> {
			return sampleValue;
		};

		sampleFunc = sampleFunc.surroundedBy(surroundingFunc);

		Assert.assertEquals(sampleFunc.applyJdbc(mockedConn), new Integer(sampleValue));

		new Verifications() {{
			mockedConn.setAutoCommit(false);
			times = 1;

			/**
			 * Asserts the setting of transaction isolation
			 */
			mockedConn.setTransactionIsolation(anyInt);
			times = expectedTransactionIsolation.isPresent() ? 1 : 0;

			expectedTransactionIsolation.ifPresent(
				transactionIsolation -> {
					((JdbcRunnable)() -> {
						mockedConn.setTransactionIsolation(transactionIsolation);
						times = 1;
					}).asRunnable().run();
				}
			);
			// :~)

			mockedConn.commit();
			times = 1;
		}};
	}
	@DataProvider(name="OperateTransactional")
	private Object[][] getOperateTransactional()
	{
		return new Object[][] {
			{ (JdbcFunction.SurroundOperator<Connection, Integer>)Transactional::simple,
				Optional.empty()
			},
			{ new Transactional<Connection, Integer>(Connection.TRANSACTION_READ_COMMITTED),
				Connection.TRANSACTION_READ_COMMITTED
			},
			{ new Transactional<Connection, Integer>(
				Optional.of(Connection.TRANSACTION_REPEATABLE_READ)),
				Connection.TRANSACTION_REPEATABLE_READ
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
		JdbcFunction<Connection, Integer> sampleFunc = conn -> {
			throw new SQLException("Rollback");
		};
		sampleFunc = sampleFunc.surroundedBy(Transactional::simple);

		try {
			sampleFunc.applyJdbc(mockedConn);
		} catch (SQLException e) {
			new Verifications() {{
				mockedConn.rollback();
				times = 1;
			}};

			throw e;
		}
	}
}