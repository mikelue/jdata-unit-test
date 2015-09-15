/**
 * Main objects to convert <a href="http://yaml.org/spec/current.html">YAML</a> format of data grain to {@link guru.mikelue.jdut.DuetConductor DuetConductor} for testing.<br>
 *
 * <h3>{@link guru.mikelue.jdut.yaml.YamlConductorFactory YamlConductorFactory}</h3>
 * This factory is the most important object to load YAML and convert the content to {@link guru.mikelue.jdut.DuetConductor DuetConductor}.<br>
 *
 * You may customize several building block by {@link guru.mikelue.jdut.ConductorConfig ConductorConfig}:
 * <ul>
 * 	<li>The loader of resource</li>
 * 	<li>The {@link guru.mikelue.jdut.jdbc.SQLExceptionConvert}</li>
 * 	<li>The {@link guru.mikelue.jdut.operation.OperatorFactory operation factory}</li>
 * 	<li>The named {@link guru.mikelue.jdut.operation.DataGrainOperator operators}</li>
 * 	<li>The named {@link guru.mikelue.jdut.decorate.DataGrainDecorator decorations}</li>
 * 	<li>The named {@link guru.mikelue.jdut.jdbc.JdbcFunction JdbcFunction}</li>
 * </ul>
 *
 * <h3>Reader functions</h3>
 * <p>The {@link guru.mikelue.jdut.yaml.YamlConductorFactory#conductResource YamlConductorFactory.conductResource} method
 * accepts a string value of resource, which converts the value to a {@link java.io.Reader Reader} object.<br>
 * The conversion is performed by instance of {@link java.util.function.Function Function&lt;String, Reader&gt;}.<br>
 * You could set conversion of your own by {@link guru.mikelue.jdut.ConductorConfig ConductorConfig}.</p>
 *
 * <p>{@link guru.mikelue.jdut.yaml.ReaderFunctions ReaderFunctions} provides default functions to load resource by various {@link java.lang.ClassLoader ClassLoader}</p>
 *
 * @see <a href="https://bitbucket.org/asomov/snakeyaml">SnakeYaml</a>
 */
package guru.mikelue.jdut.yaml;
