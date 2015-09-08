package guru.mikelue.jdut;

import java.sql.Connection;
import guru.mikelue.jdut.jdbc.JdbcFunction;

/**
 * Represents the function provider for building/clean.
 */
public interface DuetFunctions {
	/**
	 * Gets the function for building data.
	 *
	 * @return The function with accepting {@link Connection}
	 */
	public JdbcFunction<Connection, ?> getBuildFunction();
	/**
	 * Gets the function for cleaning data.
	 *
	 * @return The function with accepting {@link Connection}
	 */
	public JdbcFunction<Connection, ?> getCleanFunction();
}
