grails.project.work.dir = 'target'

grails.project.source.level = 1.7
grails.project.target.level = 1.7

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenLocal()
		grailsCentral()
		mavenCentral()
		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}

	dependencies {
		compile 'com.fasterxml.jackson.core:jackson-databind:2.9.6'
		compile 'com.oracle:ojdbc7:12.1.0.1'
		compile 'org.apache.httpcomponents:httpclient:4.4.1'
		compile 'org.apache.httpcomponents:httpcore:4.4.1'

		test 'com.github.tomakehurst:wiremock:2.7.0'
		test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
	}

	plugins {
		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
