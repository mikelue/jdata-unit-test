package guru.mikelue.jdut.annotation;

import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * <p>Provides generating of resource name by format string({@link MessageFormat}) and properties of class/method.</p>
 *
 * For example:<br>
 * <pre>{@code
 * // objMethod - The object of java.lang.reflect.Method
 * String fileName = JdutResourceNaming.naming("{1}-{4}", objMethod, ".yaml");
 * }</pre>
 *
 * @see MessageFormat
 */
public final class JdutResourceNaming {
	private JdutResourceNaming() {}

	/**
	 * Generating naming by {@link MessageFormat}.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link Class#getName()}</li>
	 * 	<li>1 - {@link Class#getSimpleName()}</li>
	 * 	<li>2 - {@link Class#getTypeName()}</li>
	 * 	<li>3 - {@link Class#getCanonicalName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param sourceClass The source class used for naming
	 *
	 * @return formatted string
	 */
	public static String naming(String format, Class<?> sourceClass)
	{
		return MessageFormat.format(
			format,
			sourceClass.getName(),
			sourceClass.getSimpleName(),
			sourceClass.getCanonicalName(),
			sourceClass.getTypeName()
		);
	}
	/**
	 * Generating naming by {@link MessageFormat}.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link Class#getName()}</li>
	 * 	<li>1 - {@link Class#getSimpleName()}</li>
	 * 	<li>2 - {@link Class#getTypeName()}</li>
	 * 	<li>3 - {@link Class#getCanonicalName()}</li>
	 * 	<li>4 - {@link Method#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param sourceMethod The method used for naming
	 *
	 * @return formatted string
	 */
	public static String naming(String format, Method sourceMethod)
	{
		Class<?> sourceClass = sourceMethod.getDeclaringClass();

		return MessageFormat.format(
			format,
			sourceClass.getName(),
			sourceClass.getSimpleName(),
			sourceClass.getCanonicalName(),
			sourceClass.getTypeName(),
			sourceMethod.getName()
		);
	}

	/**
	 * Generating naming by {@link MessageFormat} and suffix.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link Class#getName()}</li>
	 * 	<li>1 - {@link Class#getSimpleName()}</li>
	 * 	<li>2 - {@link Class#getTypeName()}</li>
	 * 	<li>3 - {@link Class#getCanonicalName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param sourceClass The source class used for naming
	 * @param suffix The suffix of resource
	 *
	 * @return formatted string
	 */
	public static String naming(String format, Class<?> sourceClass, String suffix)
	{
		return naming(format + suffix, sourceClass);
	}
	/**
	 * Generating naming by {@link MessageFormat}.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link Class#getName()}</li>
	 * 	<li>1 - {@link Class#getSimpleName()}</li>
	 * 	<li>2 - {@link Class#getTypeName()}</li>
	 * 	<li>3 - {@link Class#getCanonicalName()}</li>
	 * 	<li>4 - {@link Method#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param sourceMethod The method used for naming
	 * @param suffix The suffix of resource
	 *
	 * @return formatted string
	 */
	public static String naming(String format, Method sourceMethod, String suffix)
	{
		return naming(format + suffix, sourceMethod);
	}
}
