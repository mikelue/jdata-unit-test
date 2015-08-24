package guru.mikelue.jdut.datagrain;

/**
 * Represents the excpetion thrown by {@link DataRow#validate}.
 */
public class DataRowException extends Exception {
	private final static long serialVersionUID = 1L;

	public DataRowException(Throwable t)
	{
		super(t);
	}
	public DataRowException(String message)
	{
		super(message);
	}
}
