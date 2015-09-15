package guru.mikelue.jdut.assertion;

/**
 * The wrap exception for error of assertion.
 */
public class AssertException extends RuntimeException {
	private final static long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	public AssertException(Throwable throwable)
	{
		super(throwable);
	}
	/**
	 * {@inheritDoc}
	 */
	public AssertException(String message)
	{
		super(message);
	}
	/**
	 * Builds exception with {@link String#format} method.
	 *
	 * @param format The format of message
	 * @param args The arguments for formatting message
	 */
	public AssertException(String format, Object... args)
	{
		super(String.format(format, args));
	}
}
