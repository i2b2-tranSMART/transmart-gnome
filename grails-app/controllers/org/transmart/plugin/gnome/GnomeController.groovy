package org.transmart.plugin.gnome

import groovy.util.logging.Slf4j
import org.apache.http.client.HttpResponseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED

@Slf4j('logger')
class GnomeController {

	@Autowired private GnomeService gnomeService

	@Value('${com.recomdata.gnome.url:}')
	private String baseUrl

	@Value('${com.recomdata.gnome.password:}')
	private String password

	@Value('${com.recomdata.gnome.username:}')
	private String username

	/**
	 * Authenticate with gnome, construct gnome subset URL and redirect to that URL.
	 */
	def analyzegnomeapi(String result_instance_id, String project, String type) {
		if (!baseUrl || !username || !password) {
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

			String token = gnomeService.authenticateWithGnome(baseUrl, username, password)
			render gnomeService.getSubsetUrl(baseUrl, username, password, token, project, type, uuidList)
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
