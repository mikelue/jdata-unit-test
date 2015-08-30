package guru.mikelue.jdut.vendor.mssql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.DefaultOperators;

/**
 * Pre-defined operations, features of MS SQL server.
 */
public final class MsSql {
	private MsSql() {}

	/**
	 * The default operators provided for MS SQL server.
	 */
	public final static Map<String, DataGrainOperator> DEFAULT_OPERATORS;

	static {
		Map<String, DataGrainOperator> operators = new HashMap<>(1);
		operators.put(
			DefaultOperators.INSERT,
			new IdentityInsertOperator(DefaultOperators::doInsert)
				.toDataGrainOperator()
		);
		operators.put(
			DefaultOperators.REFRESH,
			new IdentityInsertOperator(DefaultOperators::doRefresh)
				.toDataGrainOperator()
		);

		DEFAULT_OPERATORS = Collections.unmodifiableMap(operators);
	}
}
