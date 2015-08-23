package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * Converts the {@link SQLException} into another instance of {@link RuntimeException}.<p>
 *
 * @param <E> The type of exception to be generated
 */
@FunctionalInterface
public interface SQLExceptionConvert<E extends RuntimeException> extends Function<SQLException, E> {
	/**
	 * The pre-defined instance for converting {@link SQLException} to {@link RuntimeException}.
	 *
	 * @param e The sql exception to be converted
	 *
	 * @return runtime exception
	 */
	public static RuntimeException runtimeException(SQLException e)
	{
		return new RuntimeException(e);
	}
}
