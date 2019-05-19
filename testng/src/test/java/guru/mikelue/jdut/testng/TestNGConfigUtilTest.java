package guru.mikelue.jdut.testng;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestNGConfigUtilTest {
	public TestNGConfigUtilTest() {}

	private Map<Integer, Boolean> recordBeforeTimes = new HashMap<>();
	private Map<Integer, Boolean> recordAfterTimes = new HashMap<>();

	@BeforeMethod
	void increaseCount(ITestResult result, Method m)
	{
		if (!m.getName().equals("recordSample")) {
			return;
		}

		ITestNGMethod recordMethod = result.getMethod();
		recordBeforeTimes.put(recordBeforeTimes.size(), TestNGConfigUtil.firstTime(recordMethod));
	}
	@AfterMethod
	void decreaseCount(ITestResult result, Method m)
	{
		if (!m.getName().equals("recordSample")) {
			return;
		}

		ITestNGMethod recordMethod = result.getMethod();
		recordAfterTimes.put(recordAfterTimes.size(), TestNGConfigUtil.lastTime(recordMethod));
	}

	/**
	 * Test sample of method for recording result of tested method.
	 */
	@Test(dataProvider="firstTime", groups="recorder")
	public void recordSample(int v) {}
	@DataProvider
	Object[][] firstTime()
	{
		return new Object[][] {
			{ 1 }, { 2 }, { 3 },
		};
	}

	/**
	 * Tests the checking of first time.
	 */
	@Test(dataProvider="assertBoundary", dependsOnGroups="recorder")
	public void assertBoundary(int index, boolean expectedFirstTime, boolean expectedLastTime)
	{
		Assert.assertEquals(recordBeforeTimes.get(index).booleanValue(), expectedFirstTime,
			String.format("Index[%d] expected value of \"First Time\" is not matched", index)
		);
		Assert.assertEquals(recordAfterTimes.get(index).booleanValue(), expectedLastTime,
			String.format("Index[%d] expected value of \"Last Time\" is not matched", index)
		);
	}
	@DataProvider
	Object[][] assertBoundary()
	{
		return new Object[][] {
			{ 0, true, false },
			{ 1, false, false },
			{ 2, false, true },
		};
	}
}
