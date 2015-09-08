package guru.mikelue.jdut.yaml;

/**
 * Represents the exception while loading YAML to conductor.
 */
public class LoadingYamlException extends RuntimeException {
	private final static long serialVersionUID = 1L;

	public LoadingYamlException(String format, Object... args)
	{
		super(String.format(format, args));
	}
}
