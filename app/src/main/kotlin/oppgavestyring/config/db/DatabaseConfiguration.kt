package oppgavestyring.config.db

val DB_CONFIG_PREFIX = "DB_OPPGAVESTYRING"

data class DatabaseConfiguration(
    val connectionURL: String = System.getenv("${DB_CONFIG_PREFIX}_JDBC_URL") ?:
        System.getProperty("${DB_CONFIG_PREFIX}_JDBC_URL"),
    val username: String = System.getenv("${DB_CONFIG_PREFIX}_USERNAME") ?:
        System.getProperty("${DB_CONFIG_PREFIX}_USERNAME"),
    val password: String = System.getenv("${DB_CONFIG_PREFIX}_PASSWORD") ?:
        System.getProperty("${DB_CONFIG_PREFIX}_PASSWORD"),
    val driver: String = "org.postgresql.Driver"
)