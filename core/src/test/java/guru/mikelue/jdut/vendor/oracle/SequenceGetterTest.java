package guru.mikelue.jdut.vendor.oracle;

import org.junit.jupiter.api.Test;

import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;
import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.vendor.DatabaseVendor;

import static org.junit.jupiter.api.Assertions.*;

public class SequenceGetterTest extends AbstractDataSourceTestBase {
	public SequenceGetterTest() {}

	/**
	 * Tests the getting of "NEXTVAL".
	 */
	@Test @DoLiquibase @IfDatabaseVendor(match=DatabaseVendor.Oracle)
	public void nextValAsInt()
	{
		SequenceGetter testedGetter = new SequenceGetter(getDataSource());

		assertEquals(5, testedGetter.nextValAsInt("seq_test_1"));
	}

	/**
	 * Tests the getting of "CURRENTVAL".
	 */
	@Test @DoLiquibase @IfDatabaseVendor(match=DatabaseVendor.Oracle)
	public void currentValAsInt()
	{
		SequenceGetter testedGetter = new SequenceGetter(getDataSource());
		testedGetter.nextValAsInt("seq_test_2");

		assertEquals(10, testedGetter.currentValAsInt("seq_test_2"));
	}
}
