dataSource {
	dbCreate = 'update'
	driverClassName = 'org.h2.Driver'
	jmxExport = true
	password = ''
	pooled = true
	url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
	username = 'sa'
}

hibernate {
	cache {
		use_query_cache = false
		use_second_level_cache = false
	}
	format_sql = true
	use_sql_comments = true
}
