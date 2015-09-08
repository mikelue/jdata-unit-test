package guru.mikelue.jdut.yaml.node;

/**
 * Defines the base interface for nodes of YAML.
 */
public interface NodeBase {
	/**
	 * Gets type of node.
	 *
	 * @return The type of node
	 */
	public NodeType getNodeType();
}
