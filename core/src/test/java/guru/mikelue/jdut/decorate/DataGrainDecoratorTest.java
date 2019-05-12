package guru.mikelue.jdut.decorate;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

		assertEquals(2, calledTimes.intValue());
	}
}
