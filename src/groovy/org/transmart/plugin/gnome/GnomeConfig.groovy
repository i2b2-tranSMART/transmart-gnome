package org.transmart.plugin.gnome

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class GnomeConfig {

	@Value('${edu.harvard.transmart.gnome.baseUrl:}')
	String baseUrl

	@Value('${edu.harvard.transmart.gnome.enabled:false}')
	boolean enabled

	@Value('${edu.harvard.transmart.gnome.password:}')
	String password

	@Value('${edu.harvard.transmart.gnome.username:}')
	String username

	@Value('${edu.harvard.transmart.gnome.projects:}')
	List<String> gNomeProjects
}
