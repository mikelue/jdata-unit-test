package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Likes the {@link Function} inteface with throwing of {@link SQLException}.<p>
 *
 * See {@link guru.mikelue.jdut.jdbc JDBC Function} for usage of this lambda expresssion.
 *
 * @param <T> The type of fed object
 * @param <R> The type of returned object
 *
 * @see JdbcRunnable
 * @see JdbcSupplier
 * @see JdbcVoidFunction
 * @see SQLExceptionConvert
*/
@FunctionalInterface
public interface JdbcFunction<T, R> {
	/**
	 * This operator is used to surround fed {@link JdbcFunction} by the implementation of surrounding.<p>
	 *
	 * For example:
	 * <pre>{@code
	 * static <T, R> JdbcFunction.SurroundOperator<T, R> buildLogging(Logger logger)
	 * {
	 *     return surroundedFunc -> v -> {
	 *         logger.info("Before Invoke: {}", v);
	 *
	 *         R result = surroundedFunc.apply(v);
	 *
	 *         logger.info("After Invoke: {}", v);
	 *
	 *         return result;
	 *     };
	 * }
	 *
	 * JdbcFunction<Integer, Integer> loggedFunc = sourceFunc.surroundedBy(buildLogging(logger));
	 * }</pre>
	 *
 	 * @param <T> The type of fed object
 	 * @param <R> The type of returned object
	 *
	 * @see JdbcFunction#surroundedBy
	 */
	@FunctionalInterface
	public interface SurroundOperator<T, R> extends UnaryOperator<JdbcFunction<T, R>> {}

	/**
	 * Converts this expression to {@link Function}.
	 *
	 * @return The function with {@link SQLExceptionConvert#runtimeException SQLExceptionConvert::runtimeException}
	 *
	 * @see #asFunction(SQLExceptionConvert)
	 */
	default Function<T, R> asFunction()
	{
		return asFunction(SQLExceptionConvert::runtimeException);
	}
	/**
	 * Converts this expression to {@link Function}, with customized {@link SQLExceptionConvert}.
	 *
	 * @param <E> The type of exception to be generated by
	 * @param exceptionConvert The instance for converting {@link SQLException} to exception of type {@literal <E>}
	 *
	 * @return The function with {@link SQLExceptionConvert}
	 *
	 * @see #asFunction()
	 */
	default <E extends RuntimeException> Function<T, R> asFunction(SQLExceptionConvert<E> exceptionConvert)
	{
		return t -> {
			try {
				return apply(t);
			} catch (SQLException e) {
				throw exceptionConvert.apply(e);
			}
		};
	}

	/**
	 * Surrounds this function by {@link SurroundOperator}.
	 *
	 * @param surroundOperator The operator to build a new function to surround current function
	 *
	 * @return The surround function of result
	 *
	 * @see SurroundOperator
	 */
	default JdbcFunction<T, R> surroundedBy(SurroundOperator<T, R> surroundOperator)
	{
		return surroundOperator.apply(this);
	}

	/**
	 * Gives object of type {@literal <R>} object by fed object of type {@literal <T>}.<p>
	 *
	 * @param jdbcObject The object to be fed
	 *
	 * @return The generated object
	 *
	 * @throws SQLException eliminate the exception block of JDBC
	 */
	public R apply(T jdbcObject) throws SQLException;
}
