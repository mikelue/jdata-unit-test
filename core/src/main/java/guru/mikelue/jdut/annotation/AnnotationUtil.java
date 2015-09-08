package guru.mikelue.jdut.annotation;

import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.vendor.DatabaseVendor;

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
}
