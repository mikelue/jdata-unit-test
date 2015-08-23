package guru.mikelue.jdut;

/**
 * Represents the runtime exception thrown by {@link DataConductor}.
 */
public class DataConducteException extends RuntimeException {
	private final static long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	public DataConducteException(String message)
	{
		super(message);
	}
	/**
	 * {@inheritDoc}
	 */
	public DataConducteException(Throwable throwable)
	{
		super(throwable);
	}
}
