package guru.mikelue.jdut.yaml;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

/**
 * Defines the functions for reader.
 */
public final class ReaderFunctions {
	private ReaderFunctions() {}

	/**
	 * Builds {@code java.util.Function<String, Reader>}, uses {@link Class#getResourceAsStream}
	 * for loading reading resource.
	 *
	 * @param classForLoadResource The class for loading resource
	 *
	 * @return The reader
	 */
    public static Function<String, Reader> loadByClass(Class<?> classForLoadResource)
    {
        return resourceName -> new InputStreamReader(
            classForLoadResource.getResourceAsStream(resourceName)
		);
    }

	/**
	 * As {@code java.util.Function<String, Reader>}, uses {@link Thread#getContextClassLoader} of {@link Thread#currentThread}
	 * for loading reading resource.
	 *
	 * @param resourceName The name of resource
	 *
	 * @return The reader
	 */
    public static Reader currentThreadContext(String resourceName)
    {
        return new InputStreamReader(
            Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)
		);
    }

	/**
	 * As {@code java.util.Function<String, Reader>}, used for converting the {@link String} to {@link Reader}.
	 *
	 * @param content The YAML content as string
	 *
	 * @return The reader
	 */
    public static Reader stringReader(String content)
    {
        return new StringReader(content);
    }
}
