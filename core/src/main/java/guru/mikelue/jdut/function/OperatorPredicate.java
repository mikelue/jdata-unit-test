package guru.mikelue.jdut.function;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import guru.mikelue.jdut.operation.DefaultOperatorFactory;

/**
 * Used with {@link DefaultOperatorFactory.Builder} to check the matching condition for operators of vendor.
 */
@FunctionalInterface
public interface OperatorPredicate {
	/**
	 * Checks the condition by name of operator and meta data of database.
	 *
	 * @param metaData The meta data
	 *
	 * @return true if the condition is matched
	 *
	 * @throws SQLException The exception to simplify implementation
	 */
	public boolean test(DatabaseMetaData metaData) throws SQLException;
}
