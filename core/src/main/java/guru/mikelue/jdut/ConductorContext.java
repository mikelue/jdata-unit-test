package guru.mikelue.jdut;

import java.sql.Connection;
import java.util.Optional;

/**
 * Used to be access by thread-local variable of current execution of conductor.
 */
public final class ConductorContext {
	private static ThreadLocal<Connection> connOfThreadLocal = new ThreadLocal<>();

	private ConductorContext() {}

	/**
	 * Gets current connection.
	 *
	 * @return current object of connection, my be empty
	 */
	public static Optional<Connection> getCurrentConnection()
	{
		return Optional.ofNullable(connOfThreadLocal.get());
	}

	/**
	 * Sets current connection.
	 *
	 * @param connection The connection object to be keeped in thread local
	 */
	static void setCurrentConnection(Connection connection)
	{
		connOfThreadLocal.set(connection);
	}

	/**
	 * Cleans the current connection.
	 */
	static void cleanCurrentConnection()
	{
		connOfThreadLocal.remove();
	}
}
