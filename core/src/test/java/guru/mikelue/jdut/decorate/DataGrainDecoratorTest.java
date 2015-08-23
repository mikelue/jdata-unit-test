package guru.mikelue.jdut.decorate;

import org.apache.commons.lang3.mutable.MutableInt;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataGrainDecoratorTest {
	public DataGrainDecoratorTest() {}

	/**
	 * Tests the chaining of two decorates.
	 */
	@Test
	public void chain()
	{
		final MutableInt calledTimes = new MutableInt(0);

		DataGrainDecorator testedDecorator = rowBuilder -> calledTimes.increment();
		testedDecorator = testedDecorator.chain(rowBuilder -> calledTimes.increment());

		testedDecorator.decorate(null);

		Assert.assertEquals(calledTimes.intValue(), 2);
	}
}
