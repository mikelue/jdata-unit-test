package guru.mikelue.jdut.yaml.node;

import java.util.Map;

/**
 * Represents the type of node
 */
public enum NodeType {
	Config, Defines, Table, Code, Unknown;

	/**
	 * Gets the type of node by object.
	 *
	 * @param unknownObject The object loaded by YAML
	 *
	 * @return The matched type of node
	 */
	public static NodeType getNodeType(Object unknownObject)
	{
		if (Map.class.isInstance(unknownObject)) {
			Map<?, ?> mapOfUnknown = (Map<?, ?>)unknownObject;
			for (Object key: mapOfUnknown.keySet()) {
				if (TableNode.TableName.class.isInstance(key)) {
					return Table;
				}
				if ("config".equals(key)) {
					return Config;
				}
				if ("defines".equals(key)) {
					return Defines;
				}
			}
			return Unknown;
		}

		if (CodeNode.class.isInstance(unknownObject)) {
			return Code;
		}

		return Unknown;
	}
}
