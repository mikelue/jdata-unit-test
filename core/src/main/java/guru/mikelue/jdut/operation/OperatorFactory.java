package guru.mikelue.jdut.operation;

/**
 * Defines the factory to retrieve the target operator by name.
 */
@FunctionalInterface
public interface OperatorFactory {
	/**
	 * Gets operator of data grain by name.
	 *
	 * @param name The name of operator
	 *
	 * @return The matched operator
	 */
	public DataGrainOperator get(String name);
}
