package guru.mikelue.jdut.junit5;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.jdbc.function.DbStatement;
import guru.mikelue.jdut.junit5.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.yaml.YamlConductorFactory;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;

@JdutResource
public class JdutYamlFactoryTest extends AbstractDataSourceTestBase {
	private final JdutYamlFactory testedExtension = new JdutYamlFactory() {
		@Override
		protected YamlConductorFactory getYamlConductorFactory(ExtensionContext context, Event event)
		{
			return YamlConductorFactory.build(getDataSource());
		}
	};

	public JdutYamlFactoryTest() {}

	@BeforeEach
	void setupTable() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn, stmt -> {
					stmt.execute("DROP TABLE IF EXISTS method_t1");
					stmt.execute("CREATE TABLE method_t1(t1_id INTEGER)");
				}
			).runJdbc()
		).runJdbc();
	}

	@Injectable
	private ExtensionContext mockExtContext;

	/**
	 * Tests build/clean data on method level
	 */
	@ParameterizedTest
	@MethodSource("allFactories") @JdutResource
	void sampleTestByMethod(JdutYamlFactory factory) throws Exception, SQLException
	{
		Method thisMethod = getClass().getDeclaredMethod("sampleTestByMethod", JdutYamlFactory.class);

		expectationsOfExtensionContext(thisMethod);
		new Expectations() {{
			mockExtContext.getRequiredTestMethod();
			result = thisMethod;
		}};

		factory.beforeEach(mockExtContext);
		assertData(2);

		factory.afterEach(mockExtContext);
		assertData(0);
	}

	/**
	 * Tests build/clean data on class level
	 */
	@ParameterizedTest
	@MethodSource("allFactories")
	void sampleTestByClass(JdutYamlFactory factory) throws Exception, SQLException
	{
		expectationsOfExtensionContext(getClass());

		factory.beforeAll(mockExtContext);
		assertData(2);

		factory.afterAll(mockExtContext);
		assertData(0);
	}

	Arguments[] allFactories()
	{
		return new Arguments[] {
			arguments(testedExtension),
			arguments(JdutYamlFactory.buildByDataSource(this::getDataSource)),
			arguments(JdutYamlFactory.buildByFactory(() -> YamlConductorFactory.build(getDataSource()))),
		};
	}

	/**
	 * Tests build/clean without @JdutResource on method level(nothing happended).
	 */
	@Test
	void sampleTestByMethodWithoutAnnotation() throws Exception, SQLException
	{
		Method thisMethod = getClass().getDeclaredMethod("sampleTestByMethodWithoutAnnotation");

		expectationsOfExtensionContext(thisMethod);
		new Expectations() {{
			mockExtContext.getRequiredTestMethod();
			result = thisMethod;
		}};

		testedExtension.beforeEach(mockExtContext);
		assertData(0);

		testedExtension.afterEach(mockExtContext);
		assertData(0);
	}

	/**
	 * Tests build/clean without @JdutResource on class level(nothing happended).
	 */
	@Test
	void samleTestByClassWithoutAnnotation() throws Exception, SQLException
	{
		expectationsOfExtensionContext(new WithouAnnotation(), WithouAnnotation.class);

		testedExtension.beforeAll(mockExtContext);
		assertData(0);

		testedExtension.afterAll(mockExtContext);
		assertData(0);
	}

	private void expectationsOfExtensionContext(Object targetContext)
	{
		expectationsOfExtensionContext(this, targetContext);
	}
	private void expectationsOfExtensionContext(Object targetObject, Object targetContext)
	{
		new Expectations() {{
			mockExtContext.getRequiredTestInstance(); minTimes = 0;
			result = targetObject;
			mockExtContext.getRequiredTestClass(); minTimes = 0;
			result = targetObject.getClass(); minTimes = 0;

			MutableObject<DuetConductor> conductorRef = new MutableObject<>();
			mockExtContext.getStore(Namespace.create(targetContext)).put(anyString, withInstanceOf(DuetConductor.class)); minTimes = 0;
			result = new Delegate<Void>() {
				@SuppressWarnings("unused")
				void delegate(String name, DuetConductor conductor)
				{
					conductorRef.setValue(conductor);
				}
			};
			mockExtContext.getStore(Namespace.create(targetContext)).remove(anyString, DuetConductor.class); minTimes = 0;
			result = new Delegate<Void>() {
				@SuppressWarnings("unused")
				DuetConductor delegate(String name, Class<?> objectType)
				{
					return conductorRef.getValue();
				}
			};
		}};
	}

	private void assertData(int expectedCount) throws SQLException
	{
		String checkData = "SELECT COUNT(*) FROM method_t1 WHERE t1_id = 52";
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, checkData,
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, expectedCount)
			).runJdbc()
		).runJdbc();
	}
}

class WithouAnnotation {}
