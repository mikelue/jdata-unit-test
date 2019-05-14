package guru.mikelue.jdut.jdbc.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.jdbc.JdbcFunction;

/**
 * Functions for {@link AutoCloseable}, which are used to wrap existing {@link JdbcFunction} for closing feature.
 *
 * You may use {@code DbRelease::autoClose} to surrounding your {@link JdbcFunction}.
 * <pre><code class="java">
 * yourJdbcFunction = yourJdbcFunction.surroundedBy(DbRelease::autoClose);
 * </code></pre>
 *
 * @see JdbcFunction.SurroundOperator
 */
public final class DbRelease {
	private static Logger logger = LoggerFactory.getLogger(DbRelease.class);
	private DbRelease() {}

	/**
	 * Surrounds surroundedFunction to surrounding closing block for object of {@link AutoCloseable}.
	 *
	 * @param <T> The fed object must be type of {@link AutoCloseable}
 	 * @param <R> The type of returned object
 	 * @param surroundedFunction The function to be surrounded
	 *
	 * @return The function making {@link AutoCloseable} to be closed after the surrounded function is completed.
	 *
	 * @see #autoClose(JdbcFunction)
	 */
	public static <T extends AutoCloseable, R> JdbcFunction<T, R> autoClose(
		JdbcFunction<T, R> surroundedFunction
	) {
		return jdbcObject -> {
			try {
				return surroundedFunction.applyJdbc(jdbcObject);
			} finally {
				try {
					logger.debug("Close resource: {}", jdbcObject);
					jdbcObject.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
