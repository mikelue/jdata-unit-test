package guru.mikelue.jdut.annotation;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.vendor.DatabaseVendor;
import guru.mikelue.jdut.yaml.ReaderFunctions;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

/**
 * Utilities for processing defined annotations.
 */
public final class AnnotationUtil {
	private AnnotationUtil() {}

	/**
	 * Checks whether or not the vendor of database match the value of annotation.<br>
	 *
	 * If <em>vendor</em> parameter is null, the result would be <b>true</b>.
	 *
	 * @param vendor The vendor to be checked, <b>must not be null</b>
	 * @param annotationValue The instance of annotation
	 *
	 * @return true if any matched or nothing is not matched
	 */
	public static boolean matchDatabaseVendor(final DatabaseVendor vendor, IfDatabaseVendor annotationValue)
	{
		Validate.notNull(vendor, "The vendor is null");

		if (annotationValue == null) {
			return true;
		}

		boolean matched = Stream.of(annotationValue.match())
			.anyMatch(
				matchVendor -> matchVendor.equals(vendor) || matchVendor == DatabaseVendor.Unknown
			);

		boolean notMatched = Stream.of(annotationValue.notMatch())
			.filter(notMatchVendor -> notMatchVendor != DatabaseVendor.Unknown)
			.anyMatch(
				notMatchVendor -> notMatchVendor.equals(vendor) && notMatchVendor != DatabaseVendor.Unknown
			);

		return matched && !notMatched;
	}

	/**
	 * Checks whether or not the class has declare {@link JdutResource} on it(directly).
	 *
	 * @param sourceClass The class to be checked
	 *
	 * @return true if it is declaring the annotation
	 */
	public static boolean hasJdutResourceAnnotation(Class<?> sourceClass)
	{
		return sourceClass.getDeclaredAnnotation(JdutResource.class) != null;
	}
	/**
	 * Checks whether or not the method has declare {@link JdutResource} on it(directly).
	 *
	 * @param sourceMethod The method to be checked
	 *
	 * @return true if it is declaring the annotation
	 */
	public static boolean hasJdutResourceAnnotation(Method sourceMethod)
	{
		return sourceMethod.getDeclaredAnnotation(JdutResource.class) != null;
	}

	/**
	 * Builds {@link DuetConductor} by naming(using convention) of class and method,
	 * the file name would be {@code <class_name>-<method_name>.yaml} which should be as same as package of method's class.<br>
	 *
	 * The method must be annotated with {@link JdutResource}.
	 *
	 * @param conductorFactory The factory of conductor
	 * @param sourceMethod The method to be tested(recommended)
	 *
	 * @return The conductor for data
	 */
	public static Optional<DuetConductor> buildConductorByConvention(YamlConductorFactory conductorFactory, Method sourceMethod)
	{
		if (!hasJdutResourceAnnotation(sourceMethod)) {
			return Optional.empty();
		}

		return Optional.of(conductorFactory.conductResource(
			JdutResourceNaming.naming(
				"{1}-{4}", sourceMethod, ".yaml"
			),
			builder -> builder.resourceLoader(
				ReaderFunctions.loadByClass(sourceMethod.getDeclaringClass())
			)
		));
	}
	/**
	 * Builds {@link DuetConductor} by naming(using convention) of class and method,
	 * the file name would be {@code <class_name>.yaml} which should be as same as package of class.<br>
	 *
	 * The class must be annotated with {@link JdutResource}.
	 *
	 * @param conductorFactory The factory of conductor
	 * @param sourceClass The source of class
	 *
	 * @return The conductor for data
	 */
	public static Optional<DuetConductor> buildConductorByConvention(YamlConductorFactory conductorFactory, Class<?> sourceClass)
	{
		if (!hasJdutResourceAnnotation(sourceClass)) {
			return Optional.empty();
		}

		return Optional.of(conductorFactory.conductResource(
			JdutResourceNaming.naming(
				"{1}", sourceClass, ".yaml"
			),
			builder -> builder.resourceLoader(
				ReaderFunctions.loadByClass(sourceClass)
			)
		));
	}
}
