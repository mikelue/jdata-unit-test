package guru.mikelue.jdut.vendor.oracle;

import org.testng.Assert;
import org.testng.annotations.Test;

import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;
import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.vendor.DatabaseVendor;

public class SequenceGetterTest extends AbstractDataSourceTestBase {
	public SequenceGetterTest() {}

	/**
	 * Tests the getting of "NEXTVAL".
	 */
	@Test @DoLiquibase @IfDatabaseVendor(match=DatabaseVendor.Oracle)
	public void nextValAsInt()
	{
		SequenceGetter testedGetter = new SequenceGetter(getDataSource());

		Assert.assertEquals(testedGetter.nextValAsInt("seq_test_1"), 5);
	}

	/**
	 * Tests the getting of "CURRENTVAL".
	 */
	@Test @DoLiquibase @IfDatabaseVendor(match=DatabaseVendor.Oracle)
	public void currentValAsInt()
	{
		SequenceGetter testedGetter = new SequenceGetter(getDataSource());
		testedGetter.nextValAsInt("seq_test_2");

		Assert.assertEquals(testedGetter.currentValAsInt("seq_test_2"), 10);
	}
}
