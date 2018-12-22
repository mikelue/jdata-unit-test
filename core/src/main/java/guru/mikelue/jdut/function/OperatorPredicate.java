package guru.mikelue.jdut.function;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Predicate;

import guru.mikelue.jdut.jdbc.SQLExceptionConvert;
import guru.mikelue.jdut.operation.DefaultOperatorFactory;

/**
 * Used with {@link DefaultOperatorFactory.Builder} to check the matching condition for operators of vendor.<br>
 *
 * <p>Example:</p>
 * <pre class="java">{@code
 * // dataSource - Initialized instance of data source
 * // your_map_of_operators - The map of operators by String key
 * OperatorFactory yourFactory = DefaultOperatorFactory.build(
 *     dataSource,
 *     builder -> builder
 *         .add(metaData -> { metaData.getXXX().equals("XXX") }, your_map_of_operators)
 * )
 * }</pre>
 */
@FunctionalInterface
public interface OperatorPredicate {
    /**
     * Turns this predicate to {@link Predicate}.
     *
     * @return The predicate with default runtime exception handling
     */
    default Predicate<DatabaseMetaData> asPredicate()
    {
        return metaData -> {
            try {
                return testMetaData(metaData);
            } catch (SQLException e) {
                throw SQLExceptionConvert.runtimeException(e);
            }
        };
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another.
     * When evaluating the composed predicate, if this predicate is false, then the other predicate is not evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     *
     * @return A composed predicate
     */
    default OperatorPredicate and(OperatorPredicate other)
    {
        return metaData -> {
            return testMetaData(metaData) && other.testMetaData(metaData);
        };
    }
    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another.
     * When evaluating the composed predicate, if this predicate is true, then the other predicate is not evaluated.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     *
     * @return A composed predicate
     */
    default OperatorPredicate or(OperatorPredicate other)
    {
        return metaData -> {
            return testMetaData(metaData) || other.testMetaData(metaData);
        };
    }
    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    default OperatorPredicate negate()
    {
        return metaData -> {
            return !testMetaData(metaData);
        };
    }

	/**
	 * Checks the condition by name of operator and meta data of database.
	 *
	 * @param metaData The meta data
	 *
	 * @return true if the condition is matched
	 *
	 * @throws SQLException The exception to simplify implementation
	 */
	public boolean testMetaData(DatabaseMetaData metaData) throws SQLException;
}
