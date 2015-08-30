package guru.mikelue.jdut.vendor;

import guru.mikelue.jdut.function.OperatorPredicate;
import guru.mikelue.jdut.operation.DefaultOperatorFactory;

/**
 * The interface to represent type of vendor.
 *
 * @see DatabaseVendor
 */
public interface VendorType {
	/**
	 * Builds {@link OperatorPredicate} for check of {@link #getVendorName}.<br>
	 *
	 * This method find any match of {@link #getVendorName} in {@link java.sql.DatabaseMetaData#getDriverName()}
	 * (<em>case insensitive</em>).
	 *
	 * @param needVendor The vendor to be checked
	 *
	 * @return The predicate can be used in {@link DefaultOperatorFactory}
	 *
	 * @see DefaultOperatorFactory
	 */
	public static OperatorPredicate buildOperatorPredicate(VendorType needVendor)
	{
		return metaData -> metaData.getDriverName().toLowerCase()
			.contains(needVendor.getVendorName());
	}

	/**
	 * Gets the name of vendor.
	 *
	 * @return The name of vendor
	 */
	public String getVendorName();
}
