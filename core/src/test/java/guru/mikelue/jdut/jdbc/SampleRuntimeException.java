package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;

public class SampleRuntimeException extends RuntimeException {
	private final static long serialVersionUID = 1L;

	public SampleRuntimeException() {}
	public SampleRuntimeException(String message) { super(message); }
	public SampleRuntimeException(SQLException e) { super(e); }
}
