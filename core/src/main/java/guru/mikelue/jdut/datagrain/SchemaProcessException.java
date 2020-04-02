package guru.mikelue.jdut.datagrain;

/**
 * Represents the exception which defined data field cannot be found
 * in database schema.
 */
public class SchemaProcessException extends RuntimeException {
	private final static long serialVersionUID = 1L;

	public SchemaProcessException(String message)
	{
		super(message);
	}
}
