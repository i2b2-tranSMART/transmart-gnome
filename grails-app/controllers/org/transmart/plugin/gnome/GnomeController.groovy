package org.transmart.plugin.gnome

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpResponseException
import org.springframework.beans.factory.annotation.Autowired

import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@Slf4j('logger')
class GnomeController {

	@Autowired private GnomeService gnomeService
	@Autowired private GnomeConfig gnomeConfig

	/**
	 * Called at startup to configure UI.
	 */
	def loadScripts() {

		List rows = [
			[path: resource(dir: 'js', file: 'gnome.js', plugin: 'transmart-gnome'), type: 'script']
		]

		render([success: true, totalCount: rows.size(), files: rows] as JSON)
	}

	/**
	 * Ajax call from gnome.js to get the project names.
	 */
	def projectNames() {
		render(gnomeConfig.gNomeProjects as JSON)
	}

	/**
	 * Authenticate with gnome, construct gnome subset URL and redirect to that URL.
	 */
	def analyzegnomeapi(String result_instance_id, String project, String type) {
		if (!gnomeConfig.baseUrl || !gnomeConfig.username || !gnomeConfig.password) {
			response.status = SC_BAD_REQUEST
			render 'Gnome is not fully configured. Please contact administrator.'
			return
		}

		try {
			List<String> uuidList = gnomeService.getListOfUuidForSubset(result_instance_id)
			if (!uuidList) {
				response.status = SC_BAD_REQUEST
				render 'No Patients UUID found for the selected Subset. Please try different Subset.'
				return
			}

			String token = gnomeService.authenticateWithGnome(gnomeConfig.baseUrl, gnomeConfig.username, gnomeConfig.password)
			render gnomeService.getSubsetUrl(gnomeConfig.baseUrl, gnomeConfig.username, gnomeConfig.password, token, project, type, uuidList)
		}
		catch (HttpResponseException e) {
			logger.error e.message, e
			response.status = e.statusCode
			render e.statusCode == SC_UNAUTHORIZED ? 'Could not authenticate with Gnome. Please contact administrator.' : e.message
		}
		catch (e) {
			logger.error e.message, e
			response.status = SC_BAD_REQUEST
			render 'Gnome is not responding, try again later. Contact administrator if error persists.'
		}
	}
}
