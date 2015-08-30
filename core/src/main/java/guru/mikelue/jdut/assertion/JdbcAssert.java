package guru.mikelue.jdut.assertion;

import java.sql.SQLException;

import guru.mikelue.jdut.jdbc.SQLExceptionConvert;

/**
 * Defines static methods for assertion of JDBC objects.
 */
public final class JdbcAssert {
	private JdbcAssert() {}

	/**
	 * As lambda of {@link SQLExceptionConvert}.
	 *
	 * @param sqlException The exception to be wrapped
	 *
	 * @return The assert exception wrapping sqlException
	 */
	public static AssertException assertException(
		SQLException sqlException
	) {
		return new AssertException(sqlException);
	}
}
