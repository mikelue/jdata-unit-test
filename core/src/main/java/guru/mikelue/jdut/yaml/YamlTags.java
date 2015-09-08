package guru.mikelue.jdut.yaml;

/**
 * Defines the tags of JDUT used in YAML.
 */
public final class YamlTags {
	/**
	 * Defines the type of jdbc data.
	 */
	public final static String NAMESPACE_DB_TYPE = "tag:jdut.mikelue.guru:jdbcType:1.8/";
	/**
	 * Defines the jdut objects.
	 */
	public final static String NAMESPACE_JDUT = "tag:jdut.mikelue.guru:1.0/";
	/**
	 * Defines the sql objects.
	 */
	public final static String NAMESPACE_SQL = "tag:jdut.mikelue.guru:sql:1.0/";

	/**
	 * Defins the default shorthand for {@link #NAMESPACE_DB_TYPE}.
	 */
	public final static String SHORTHAND_DB_TYPE = "!dbtype!";
	/**
	 * Defins the default shorthand for {@link #NAMESPACE_JDUT}.
	 */
	public final static String SHORTHAND_JDUT = "!jdut!";
	/**
	 * Defins the default shorthand for {@link #NAMESPACE_SQL}.
	 */
	public final static String SHORTHAND_SQL = "!sql!";

	/**
	 * Deftaul tags could be used in header of YAML document.
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
