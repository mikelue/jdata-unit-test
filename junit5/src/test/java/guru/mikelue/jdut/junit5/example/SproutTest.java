// tag::package[]
package guru.mikelue.jdut.junit5.example;
// end::package[]

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.junit5.JdutYamlFactory;

// tag::test_class[]
// File: classpath:guru/mikelue/jdut/junit5/example
// 		-> SproutTest.yaml
@TestInstance(PER_CLASS) @JdutResource // <1>
public class SproutTest {
	public SproutTest() {}

	// tag::impl_factory[]
	@RegisterExtension // <2>
	static JdutYamlFactory myDataSourceFactory = JdutYamlFactory.buildByDataSource(
		DataSourceGetter::get
	);
	// end::impl_factory[]

	// File: classpath:guru/mikelue/jdut/junit5/example
	// 		-> SproutTest-grow.yaml
	@Test @JdutResource // <3>
	void grow()
	{
		// 1) Execute tested code
		// 2) Assert the result of database
	}
}
// end::test_class[]
