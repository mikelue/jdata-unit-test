package guru.mikelue.jdut.yaml.node;

import java.sql.Connection;
import java.util.Map;
import java.util.Optional;

import guru.mikelue.jdut.DuetFunctions;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.yaml.LoadingYamlException;

/**
 * Represents the node of code.
 */
public class CodeNode implements NodeBase {
	private Optional<JdbcFunction<Connection, ?>> buildFunction = Optional.empty();
	private Optional<JdbcFunction<Connection, ?>> cleanFunction = Optional.empty();

	@SuppressWarnings("unchecked")
	public CodeNode(Object codeNode)
	{
		Map<String, JdbcFunction<Connection, ?>> sqlCode = (Map<String, JdbcFunction<Connection, ?>>)codeNode;

		sqlCode.forEach(
			(key, value) -> {
				switch (key) {
					case "build_operation":
						buildFunction = Optional.of(value);
						break;
					case "clean_operation":
						cleanFunction = Optional.of(value);
						break;
					default:
						throw new LoadingYamlException("Unknown property of !sql!code: \"%s\"", key);
				}
			}
		);
	}

	@Override
	public NodeType getNodeType()
	{
		return NodeType.Code;
	}

	public DuetFunctions toDuetFunctions()
	{
		return new DuetFunctions() {
			@Override
			public JdbcFunction<Connection, ?> getBuildFunction()
			{
				return buildFunction.orElse(conn -> null);
			}
			@Override
			public JdbcFunction<Connection, ?> getCleanFunction()
			{
				return cleanFunction.orElse(conn -> null);
			}
		};
	}
}
