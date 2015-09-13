package guru.mikelue.jdut.yaml;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.Validate;

/**
 * Defines the functions for reader.
 */
public final class ReaderFunctions {
	private ReaderFunctions() {}

	/**
	 * Builds {@code java.util.Function<String, Reader>}, uses fed {@link ClassLoader}.
	 *
	 * @param classLoader the class loader used to loading resources
	 *
	 * @return The function to generate reader
	 *
	 * @see #loadByClass
	 */
	public static Function<String, Reader> loadByClassLoader(ClassLoader classLoader)
	{
        return buildFunctionOfReader(
			classLoader,
			resourceName -> String.format("The file is not existing: \"%s\". Class Loader: [{}]", resourceName, classLoader)
		);
	}

	/**
	 * Builds {@code java.util.Function<String, Reader>}, uses {@link Class#getResourceAsStream}
	 * for loading resource.
	 *
	 * @param classForLoadResource The class for loading resource
	 *
	 * @return The function to generate reader
	 *
	 * @see #loadByClassLoader
	 */
    public static Function<String, Reader> loadByClass(Class<?> classForLoadResource)
    {
        return resourceName -> {
			URL fileUrl = classForLoadResource.getResource(resourceName);
			Validate.notNull(fileUrl, "The file is not existing: \"%s\". Package: \"%s\"", resourceName, classForLoadResource.getPackage().getName());

			return new InputStreamReader(
				classForLoadResource.getResourceAsStream(resourceName)
			);
		};
    }

	/**
	 * As {@code java.util.Function<String, Reader>}, uses {@link Thread#getContextClassLoader} of {@link Thread#currentThread}
	 * for loading resource.
	 *
	 * @param resourceName The name of resource
	 *
	 * @return The reader
	 */
    public static Reader currentThreadContext(String resourceName)
    {
		return buildFunctionOfReader(
			Thread.currentThread().getContextClassLoader(),
			localResourceName -> String.format("The file is not existing(ClassLoader of current thread): \"%s\"", localResourceName)
		).apply(resourceName);
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

	private static Function<String, Reader> buildFunctionOfReader(
		ClassLoader classLoader, UnaryOperator<String> nullMessage
	) {
        return resourceName -> {
			URL fileUrl = classLoader.getResource(resourceName);
			if (fileUrl == null) {
				Validate.notNull(fileUrl, nullMessage.apply(resourceName));
			}

			return new InputStreamReader(
				classLoader.getResourceAsStream(resourceName)
			);
		};
	}
}
