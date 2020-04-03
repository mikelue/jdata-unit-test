package guru.mikelue.jdut.jdbc.util;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

public class MetaDataWorker {
	private final boolean storesUpperCaseIdentifiers;
	private final boolean storesLowerCaseIdentifiers;
	private final boolean storesMixedCaseIdentifiers;
	private final boolean supportsMixedCaseIdentifiers;
	private final boolean supportsSchemasInTableDefinitions;
	private final boolean supportsSchemasInDataManipulation;
	private final String identifierQuoteString;

	public MetaDataWorker(DatabaseMetaData metaData)
	{
		try {
			/**
			 * Identifier configurations of database
			 */
			storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();
			storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
			storesMixedCaseIdentifiers = metaData.storesMixedCaseIdentifiers();
			supportsMixedCaseIdentifiers = metaData.supportsMixedCaseIdentifiers();
			// :~)

			/**
			 * Schema configuration of database
			 */
			supportsSchemasInTableDefinitions = metaData.supportsSchemasInTableDefinitions();
			supportsSchemasInDataManipulation = metaData.supportsSchemasInDataManipulation();
			// :~)

			String quoteString = metaData.getIdentifierQuoteString();
			identifierQuoteString = " ".equals(quoteString) ? null : quoteString;
		} catch (SQLException e) {
			throw new RuntimeException("Cannot load metadata", e);
		}
	}

	public String processIdentifier(String identifier)
	{
		identifier = StringUtils.trimToNull(identifier);
		if (identifier == null) {
			return null;
		}

		/**
		 * Changes the identifier to upper or lower case if the database is case insensitive
		 */
		if (storesLowerCaseIdentifiers || storesMixedCaseIdentifiers) {
			return identifier.toLowerCase();
		} else if (storesUpperCaseIdentifiers) {
			return identifier.toUpperCase();
		}
		// :~)

		if (!supportsMixedCaseIdentifiers) {
			identifier.toLowerCase();
		}

		// Case sensitive
		return identifier;
	}

	public String quoteIdentifier(String identifier)
	{
		return String.format("%s%s%s", identifierQuoteString, identifier, identifierQuoteString);
	}

	public boolean supportsSchemasInTableDefinitions()
	{
		return supportsSchemasInTableDefinitions;
	}

	public boolean supportsSchemasInDataManipulation()
	{
		return supportsSchemasInDataManipulation;
	}
}
