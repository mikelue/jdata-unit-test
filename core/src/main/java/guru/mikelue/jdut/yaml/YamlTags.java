package guru.mikelue.jdut.yaml;

/**
 * Defines the tags of JDUT used in YAML.<br>
 */
public final class YamlTags {
	/**
	 * Defines the type of jdbc data.<br>
	 *
	 * <em style="color:blue">{@value #NAMESPACE_DB_TYPE}</em>
	 */
	public final static String NAMESPACE_DB_TYPE = "tag:jdut.mikelue.guru:jdbcType:1.8/";
	/**
	 * Defines the jdut objects.<br>
	 *
	 * <em style="color:blue">{@value #NAMESPACE_JDUT}</em>
	 */
	public final static String NAMESPACE_JDUT = "tag:jdut.mikelue.guru:1.0/";
	/**
	 * Defines the sql objects.<br>
	 *
	 * <em style="color:blue">{@value #NAMESPACE_SQL}</em>
	 */
	public final static String NAMESPACE_SQL = "tag:jdut.mikelue.guru:sql:1.0/";

	/**
	 * Defines the default shorthand for {@link #NAMESPACE_DB_TYPE}.<br>
	 *
	 * <em style="color:blue">{@value #SHORTHAND_DB_TYPE}</em>
	 */
	public final static String SHORTHAND_DB_TYPE = "!dbtype!";
	/**
	 * Defines the default shorthand for {@link #NAMESPACE_JDUT}.<br>
	 *
	 * <em style="color:blue">{@value #SHORTHAND_JDUT}</em>
	 */
	public final static String SHORTHAND_JDUT = "!jdut!";
	/**
	 * Defines the default shorthand for {@link #NAMESPACE_SQL}.<br>
	 *
	 * <em style="color:blue">{@value #SHORTHAND_SQL}</em>
	 */
	public final static String SHORTHAND_SQL = "!sql!";

	/**
	 * Default tags could be used in header of YAML document.<br>
	 *
	 * <p><code style="color:blue">
	 * %TAG !jdut! tag:jdut.mikelue.guru:1.0/<br>
	 * %TAG !dbtype! tag:jdut.mikelue.guru:jdbcType:1.8/<br>
	 * %TAG !sql! tag:jdut.mikelue.guru:sql:1.0/<br>
	 * </code></p>
	 */
	public final static String DEFAULT_TAGS = String.format(
		"%%TAG %s %s\n" +
		"%%TAG %s %s\n" +
		"%%TAG %s %s\n",
		SHORTHAND_JDUT, NAMESPACE_JDUT,
		SHORTHAND_SQL, NAMESPACE_SQL,
		SHORTHAND_DB_TYPE, NAMESPACE_DB_TYPE
	);

	private YamlTags() {}
}
