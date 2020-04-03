package guru.mikelue.jdut.jdbc.util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mockit.Expectations;
import mockit.Mocked;

public class MetaDataWorkerTest {
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(MetaDataWorkerTest.class);

	@Mocked
	private DatabaseMetaData mockMetaData;

	public MetaDataWorkerTest() {}

	/**
	 * Tests the process of identifier(case of identifier)
	 */
	@ParameterizedTest
	@MethodSource
	void processIdentifier(
		boolean storesUpperCaseIdentifiers,
		boolean storesLowerCaseIdentifiers,
		boolean storesMixedCaseIdentifiers,
		boolean supportsMixedCaseIdentifiers,
		String sampleName, String expectedName
	) throws SQLException {
		/**
		 * Mocks the value from meta data
		 */
		new Expectations() {{
			mockMetaData.storesUpperCaseIdentifiers();
			result = storesUpperCaseIdentifiers;
			mockMetaData.storesLowerCaseIdentifiers();
			result = storesLowerCaseIdentifiers;
			mockMetaData.storesMixedCaseIdentifiers();
			result = storesMixedCaseIdentifiers;
			mockMetaData.supportsMixedCaseIdentifiers();
			result = supportsMixedCaseIdentifiers;
			mockMetaData.supportsSchemasInTableDefinitions();
			result = true;
			mockMetaData.supportsSchemasInDataManipulation();
			result = true;
		}};
		// :~)

		MetaDataWorker testedWorker = new MetaDataWorker(mockMetaData);

		assertThat(testedWorker.processIdentifier(sampleName))
			.isEqualTo(expectedName);
	}
	static Arguments[] processIdentifier()
	{
		return new Arguments[] {
			/**
			 * Store mixed case identifiers(case insensitive)
			 */
			arguments(false, false, true, false,
				"gc_1", "gc_1"
			),
			arguments(false, false, true, false,
				"GC_1", "gc_1"
			),
			// :~)
			/**
			 * Store lower case identifiers(case insensitive)
			 */
			arguments(false, true, false, false,
				"gc_1", "gc_1"
			),
			arguments(false, true, false, false,
				"GC_1", "gc_1"
			),
			// :~)
			/**
			 * Store upper case identifiers(case insensitive)
			 */
			arguments(true, false, false, false,
				"GC_1", "GC_1"
			),
			arguments(true, false, false, false,
				"gc_1", "GC_1"
			),
			// :~)
			/**
			 * Support mixed case identifiers(case sensitive)
			 */
			arguments(false, false, false, true,
				"gc_1", "gc_1"
			),
			arguments(false, false, false, true,
				"GC_1", "GC_1"
			),
			arguments(false, false, false, true,
				"gc_1", "gc_1"
			),
			// :~)
		};
	}
}
