// tag::package[]
package guru.mikelue.jdut.junit5.example;
// end::package[]

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.junit5.JdutYamlFactory;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

// tag::test_class[]
// Looking for:
// 	for guru/mikelue/jdut/junit5/example/BroccoliTest.yaml
@TestInstance(PER_CLASS) @JdutResource // <1>
public class BroccoliTest {
	public BroccoliTest() {}

	// Looking for:
	// 	guru/mikelue/jdut/junit5/example/BroccoliTest-grow.yaml
	@Test @JdutResource // <2>
	void grow()
	{
		// 1) Execute tested code
		// 2) Assert the result of database
	}
}
// end::test_class[]

// tag::factory[]
class InitYamlFactory extends JdutYamlFactory {
	public InitYamlFactory() {}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		// When you override the callback, don't forget to call the parent's implementation
		super.beforeAll(context);
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
		// When you override the callback, don't forget to call the parent's implementation
		super.afterAll(context);
	}

	@Override
	protected YamlConductorFactory getYamlConductorFactory(ExtensionContext context, Event event)
	{
		Random r = new Random();
		Supplier<Object> randomSize = () -> r.nextInt(101) + 1;

		// You can customize ConductorConfig
		return YamlConductorFactory.build(
			DataSourceGetter.get(),
			builder -> builder.namedSupplier("random_size", randomSize) // <1>
		);
	}
}
// end::factory[]
