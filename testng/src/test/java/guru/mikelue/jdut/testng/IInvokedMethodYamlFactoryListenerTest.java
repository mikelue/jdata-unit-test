package guru.mikelue.jdut.testng;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.jdbc.function.DbStatement;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;

@Listeners(IInvokedMethodYamlFactoryListenerTest.TestMethodInfoKeeper.class)
public class IInvokedMethodYamlFactoryListenerTest extends AbstractDataSourceTestBase {
	public static class TestMethodInfoKeeper implements IInvokedMethodListener {
		private final static Map<String, Object[]> callbackArgs = new HashMap<>();

		@Override
		public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
		{
			if (!IInvokedMethodYamlFactoryListenerTest.class.isAssignableFrom(testResult.getTestClass().getRealClass())) {
				return;
			}

			callbackArgs.put(buildKey(method), new Object[] { method, testResult });
		}

		@Override
		public void afterInvocation(IInvokedMethod method, ITestResult testResult)
		{
			if (!IInvokedMethodYamlFactoryListenerTest.class.isAssignableFrom(testResult.getTestClass().getRealClass())) {
				return;
			}

			callbackArgs.remove(buildKey(method));
		}

		public static Object[] getCallbackArgs(Class<?> targetClass, String methodName)
		{
			return callbackArgs.get(buildKey(targetClass, methodName));
		}

		private static String buildKey(Class<?> targetClass, String methodName)
		{
			return String.format("%s#%s", targetClass.getCanonicalName(), methodName);
		}
		private static String buildKey(IInvokedMethod method)
		{
			ITestNGMethod testingMethod = method.getTestMethod();
			return buildKey(testingMethod.getRealClass(), testingMethod.getMethodName());
		}
	}

	private final IInvokedMethodYamlFactoryListener testedListener = new IInvokedMethodYamlFactoryListener();

	public IInvokedMethodYamlFactoryListenerTest() {}

	@BeforeMethod(firstTimeOnly=true)
	void prepareTable(Method method) throws SQLException
	{
		int tableId = -1;

		switch (method.getName()) {
			case "loadAndClean":
			case "withoutAnnotation":
				tableId = 22;
				break;
			case "loadAndCleanForOneTimeOnly":
				tableId = 45;
				break;
			case "loadAndCleanForMultipleTimes":
				tableId = 63;
				break;
			default:
				return;
		}

		final int decidedTableId = tableId;
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn, stmt -> {
					stmt.execute(String.format("DROP TABLE IF EXISTS tab_%d", decidedTableId));
					stmt.execute(String.format("CREATE TABLE tab_%d(t%d_id INTEGER)", decidedTableId, decidedTableId));
				}
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests building/clean on method level.
	 */
	@Test @JdutResource
	void loadAndClean() throws SQLException
	{
		Object[] callbackArgs = TestMethodInfoKeeper.getCallbackArgs(getClass(), "loadAndClean");
		ITestContext testContext = ((ITestResult)callbackArgs[1]).getTestContext();

		YamlFactoryListenerBase.setDataSource(testContext, getDataSource());
		invokeCallback(testedListener::beforeInvocation, callbackArgs);

		/**
		 * Asserts the building of data
		 */
		String checkData = "SELECT COUNT(*) FROM tab_22 WHERE t22_id = 33";
		assertData(checkData, 2);
		// :~)

		invokeCallback(testedListener::afterInvocation, callbackArgs);
		YamlFactoryListenerBase.removeDataSource(testContext);

		/**
		 * Asserts the clean of data
		 */
		assertData(checkData, 0);
		// :~)
	}

	/**
	 * Tests the build/clean on method without @JdutResource(nothing happened).
	 */
	@Test
	void withoutAnnotation() throws SQLException
	{
		Object[] callbackArgs = TestMethodInfoKeeper.getCallbackArgs(getClass(), "withoutAnnotation");
		ITestContext testContext = ((ITestResult)callbackArgs[1]).getTestContext();

		YamlFactoryListenerBase.setDataSource(testContext, getDataSource());
		invokeCallback(testedListener::beforeInvocation, callbackArgs);

		/**
		 * Asserts the building of data
		 */
		String checkData = "SELECT COUNT(*) FROM tab_22 WHERE t22_id = 33";
		assertData(checkData, 0);
		// :~)

		invokeCallback(testedListener::afterInvocation, callbackArgs);
		YamlFactoryListenerBase.removeDataSource(testContext);

		/**
		 * Asserts the clean of data
		 */
		assertData(checkData, 0);
		// :~)
	}

	/**
	 * Tests one time only(as true) for multi-time calling of a testing method by @DataProvider.
	 */
	@Test(dataProvider="multiTimes")
	@JdutResource @TestNGConfig(oneTimeOnly=true)
	void loadAndCleanForOneTimeOnly(int v) throws SQLException
	{
		Object[] callbackArgs = TestMethodInfoKeeper.getCallbackArgs(getClass(), "loadAndCleanForOneTimeOnly");
		ITestContext testContext = ((ITestResult)callbackArgs[1]).getTestContext();

		YamlFactoryListenerBase.setDataSource(testContext, getDataSource());
		invokeCallback(testedListener::beforeInvocation, callbackArgs);

		/**
		 * Asserts the building of data
		 */
		String checkData = "SELECT COUNT(*) FROM tab_45 WHERE t45_id = 77";
		if (v == FIRST_TIME) {
			assertData(checkData, 1);
		}
		// :~)

		invokeCallback(testedListener::afterInvocation, callbackArgs);
		YamlFactoryListenerBase.removeDataSource(testContext);

		/**
		 * Asserts the clean of data
		 */
		if (v == LAST_TIME) {
			assertData(checkData, 2);
		}
		// :~)
	}

	/**
	 * Tests one time only(as false) for multi-time calling of a testing method by @DataProvider.
	 */
	@Test(dataProvider="multiTimes")
	@JdutResource @TestNGConfig(oneTimeOnly=false)
	void loadAndCleanForMultipleTimes(int v) throws SQLException
	{
		Object[] callbackArgs = TestMethodInfoKeeper.getCallbackArgs(getClass(), "loadAndCleanForMultipleTimes");
		ITestContext testContext = ((ITestResult)callbackArgs[1]).getTestContext();

		YamlFactoryListenerBase.setDataSource(testContext, getDataSource());
		invokeCallback(testedListener::beforeInvocation, callbackArgs);

		/**
		 * Asserts the building of data
		 */
		String checkData = "SELECT COUNT(*) FROM tab_63 WHERE t63_id = 89";
		if (v == FIRST_TIME) {
			assertData(checkData, 1);
		}
		// :~)

		invokeCallback(testedListener::afterInvocation, callbackArgs);
		YamlFactoryListenerBase.removeDataSource(testContext);

		/**
		 * Asserts the clean of data
		 */
		if (v == LAST_TIME) {
			assertData(checkData, 6);
		}
		// :~)
	}

	private final static int FIRST_TIME = 1;
	private final static int LAST_TIME = 3;
	@DataProvider
	Object[][] multiTimes()
	{
		return new Object[][] {
			{ 1 }, { 2 }, { 3 },
		};
	}

	private static void assertData(String sql, int expectedCount) throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, sql,
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, expectedCount)
			).runJdbc()
		).runJdbc();
	}
	private static void invokeCallback(MethodCallBack callback, Object[] args)
	{
		callback.execute((IInvokedMethod)args[0], (ITestResult)args[1]);
	}
}

@FunctionalInterface
interface MethodCallBack {
	void execute(IInvokedMethod m, ITestResult r);
}
