package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.jdbc.JdbcFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class TransactionalTest {
	@Mocked
	private Connection mockedConn;

	public TransactionalTest() {}

	/**
	 * Tests the operator for transaction surrounding.<p>
	 */
	@ParameterizedTest
	@MethodSource
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

		assertEquals(Integer.valueOf(sampleValue), sampleFunc.applyJdbc(mockedConn));

		new Verifications() {{
			mockedConn.commit();
			times = 1;
		}};

		if (oldValueAutoCommit) {
			new Verifications() {{
				List<Boolean> setAutoCommit = new ArrayList<>(2);
				mockedConn.setAutoCommit(withCapture(setAutoCommit));

				assertFalse(setAutoCommit.get(0));
				assertTrue(setAutoCommit.get(1));
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

				assertEquals(expectedTransactionIsolation.get(), isolations.get(0));
				assertEquals(oldValueOfIsolation, isolations.get(1));
			}};
		} else {
			new Verifications() {{
				mockedConn.setTransactionIsolation(anyInt);
				times = 0;
			}};
		}
	}

	static Arguments[] operateTransactional()
	{
		return new Arguments[] {
			arguments(// Set has effect
				true, Connection.TRANSACTION_SERIALIZABLE,
				(JdbcFunction.SurroundOperator<Connection, Integer>)new Transactional<Connection, Integer>(Connection.TRANSACTION_READ_COMMITTED),
				true, Optional.of(Connection.TRANSACTION_READ_COMMITTED)
			),
			arguments(// Doesn't have to set
				false, Connection.TRANSACTION_READ_COMMITTED,
				(JdbcFunction.SurroundOperator<Connection, Integer>)new Transactional<Connection, Integer>(Connection.TRANSACTION_READ_COMMITTED),
				true, Optional.empty()
			),
			arguments(// Simple transaction
				false, Connection.TRANSACTION_READ_COMMITTED,
				(JdbcFunction.SurroundOperator<Connection, Integer>)(JdbcFunction.SurroundOperator<Connection, Integer>)Transactional::simple,
				true, Optional.empty()
			),
		};
	}

	/**
	 * Tests the rollback of transaction surrounding.<p>
	 */
	@Test
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

		JdbcFunction<Connection, Integer> rollbackFunc = conn -> {
			throw new SQLException("Rollback");
		};
		JdbcFunction<Connection, Integer> sampleFunc = rollbackFunc.surroundedBy(
			new Transactional<Connection, Integer>(Connection.TRANSACTION_SERIALIZABLE)
		);

		assertThrows(SQLException.class,
			() -> sampleFunc.applyJdbc(mockedConn)
		);

		new Verifications() {{
			/**
			 * Asserts the setting of auto commit
			 */
			List<Boolean> setAutoCommit = new ArrayList<>(2);
			mockedConn.setAutoCommit(withCapture(setAutoCommit));

			assertFalse(setAutoCommit.get(0));
			assertTrue(setAutoCommit.get(1));
			// :~)

			/**
			 * Asserts the setting of isolation
			 */
			List<Integer> isolations = new ArrayList<>(2);
			mockedConn.setTransactionIsolation(withCapture(isolations));

			assertEquals(Connection.TRANSACTION_SERIALIZABLE, isolations.get(0).intValue());
			assertEquals(Connection.TRANSACTION_READ_COMMITTED, isolations.get(1).intValue());
			// :~)
		}};
	}
}
