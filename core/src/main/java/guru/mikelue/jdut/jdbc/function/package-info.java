/**
 * This package contains out-of-box builder for {@link guru.mikelue.jdut.jdbc.JdbcFunction JdbcSupplier} or {@link guru.mikelue.jdut.jdbc.JdbcRunnable} on
 * processing of {@link java.sql.ResultSet ResultSet}, {@link java.sql.Statement}, etc.<br>
 *
 * <h3>Database Transaction</h3>
 * <p>{@link guru.mikelue.jdut.jdbc.function.Transactional} is a implementation of {@link guru.mikelue.jdut.jdbc.JdbcFunction.SurroundOperator SurroundOperator}
 * for wrapping a {@link guru.mikelue.jdut.jdbc.JdbcFunction JdbcFunction} to be transactional.</p>
 *
 * @see guru.mikelue.jdut.jdbc.function.DbStatement
 * @see guru.mikelue.jdut.jdbc.function.DbResultSet
 * @see guru.mikelue.jdut.jdbc.function.DbRelease
 */
package guru.mikelue.jdut.jdbc.function;
