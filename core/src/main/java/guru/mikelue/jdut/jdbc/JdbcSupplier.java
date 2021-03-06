package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Likes the {@link java.util.function.Supplier Supplier} interface with throwing of {@link SQLException}.
 *
 * @param <R> The type of returned object
 *
 * @see JdbcRunnable
 * @see JdbcFunction
 * @see SQLExceptionConvert
 */
@FunctionalInterface
public interface JdbcSupplier<R> {
	/**
	 * Converts this expression to {@link Supplier}.
	 *
	 * @return The supplier with {@link SQLExceptionConvert#runtimeException SQLExceptionConvert::runtimeException}
	 *
	 * @see #asSupplier(SQLExceptionConvert)
	 */
	default Supplier<R> asSupplier()
	{
		return asSupplier(SQLExceptionConvert::runtimeException);
	}
	/**
	 * Converts this expression to {@link Supplier}, with customized {@link SQLExceptionConvert}.
	 *
	 * @param <E> The type of exception to be generated by
	 * @param exceptionConvert The instance for converting {@link SQLException} to exception of type {@literal <E>}
	 *
	 * @return The supplier with {@link SQLExceptionConvert}
	 *
	 * @see #asSupplier()
	 */
	default <E extends RuntimeException> Supplier<R> asSupplier(SQLExceptionConvert<E> exceptionConvert)
	{
		return () -> {
			try {
				return getJdbc();
			} catch (SQLException e) {
				throw exceptionConvert.apply(e);
			}
		};
	}

	/**
	 * Gets the supplied value by this interface.
	 *
	 * @return The value to be returned
	 *
	 * @throws SQLException eliminate the exception block of JDBC
	 */
	public R getJdbc() throws SQLException;
}
