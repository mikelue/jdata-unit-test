package guru.mikelue.jdut;

import java.sql.SQLException;

import guru.mikelue.jdut.jdbc.SQLExceptionConvert;

/**
 * Represents the runtime exception thrown by {@link DataConductor}.
 */
public class DataConductException extends RuntimeException {
	/**
	 * As the type of {@link SQLExceptionConvert}.
	 *
	 * @param e The sql exception to be converted
	 *
	 * @return data conduct exception
	 */
	public static DataConductException as(SQLException e)
	{
		return new DataConductException(e);
	}

	private final static long serialVersionUID = 1L;

	public DataConductException(String message)
	{
		super(message);
	}
	public DataConductException(Throwable throwable)
	{
		super(throwable);
	}

	public DataConductException(String format, Object... args)
	{
		super(String.format(format, args));
	}
}
