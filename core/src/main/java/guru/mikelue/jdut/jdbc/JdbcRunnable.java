package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;

/**
 * Likes the {@link Runnable} interface with throwing of {@link SQLException}.
 *
 * @see JdbcSupplier
 * @see JdbcFunction
 * @see SQLExceptionConvert
 */
@FunctionalInterface
public interface JdbcRunnable {
	/**
	 * Converts this expression to {@link Runnable}.
	 *
	 * @return The runnable with {@link SQLExceptionConvert#runtimeException SQLExceptionConvert::runtimeException}
	 *
	 * @see #asRunnable(SQLExceptionConvert)
	 */
	default Runnable asRunnable()
	{
		return asRunnable(SQLExceptionConvert::runtimeException);
	}
	/**
	 * Converts this expression to {@link Runnable}, with customized {@link SQLExceptionConvert}.
	 *
	 * @param <E> The type of exception to be generated by
	 * @param exceptionConvert The instance for converting {@link SQLException} to exception of type {@literal <E>}
	 *
	 * @return The runnable with {@link SQLExceptionConvert}
	 *
	 * @see #asRunnable()
	 */
	default <E extends RuntimeException> Runnable asRunnable(SQLExceptionConvert<E> exceptionConvert)
	{
		return () -> {
			try {
				runJdbc();
			} catch (SQLException e) {
				throw exceptionConvert.apply(e);
			}
		};
	}

	/**
 	 * Executes code of JDBC with throwing of {@link SQLException}.
	 *
	 * @throws SQLException eliminate the exception block of JDBC
	 */
    public void runJdbc() throws SQLException;
}
