package org.transmart.plugin.gnome

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.ResultSetOutParameter
import groovy.sql.Sql
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import oracle.jdbc.OracleTypes
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.HttpResponseException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource
import java.sql.ResultSet

import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_OK

@CompileStatic
@Slf4j('logger')
class GnomeService {

	private static final ResultSetOutParameter REF_CURSOR = Sql.resultSet(OracleTypes.CURSOR)

	@Autowired private DataSource dataSource

	@CompileDynamic
	List<String> getListOfUuidForSubset(String resultInstanceId) {

		String sql = 'BEGIN TM_QUERY_UTILITY.GET_SOURCE_ID_BY_RI(?, ?); END;'

		List<String> uuids = []
		new Sql(dataSource).call(sql, [resultInstanceId, REF_CURSOR]) { cursorResults ->
			ResultSet rs = (ResultSet) cursorResults
			rs.eachRow { row ->
				uuids << row[0].toString().replace('-', '_')
			}
		}

		uuids
	}

	/**
	 * Authenticate with gNome and receive a token.
	 * @return the token
	 */
	String authenticateWithGnome(String gNomeHost, String username, String password) throws HttpResponseException {
		try {
			HttpPost post = new HttpPost(new URI(gNomeHost + '/auth/auth.cgi'))
			post.setHeader 'Content-type', 'application/x-www-form-urlencoded'

			//Execute and get the response.
			HttpResponse response = createHttpClient(username, password).execute(post)
			int statusCode = response.statusLine.statusCode
			if (statusCode != SC_OK) {
				logger.error 'Gnome responded with Status: {} Response: {}', statusCode, response.statusLine
				throw new HttpResponseException(statusCode, response.statusLine.reasonPhrase)
			}

			new ObjectMapper().readValue(response.entity.content, Map).token
		}
		catch (URISyntaxException e) {
			logger.error e.message, e
			throw new HttpResponseException(SC_BAD_REQUEST, 'Gnome is not configured properly. Please contact administrator.')
		}
		catch (e) {
			logger.error e.message, e
			throw new HttpResponseException(SC_BAD_REQUEST, 'Gnome is unavailable. Contact administrator.')
		}
	}

	/**
	 * Returns Gnome subset URL based on provided list of UUIDs.
	 * The POST request has the previously obtained authentication token in the header,
	 * and the other project/type/idList values in a JSON, as the request body.
	 */
	String getSubsetUrl(String gNomeHost, String username, String password, String token, String projectName,
	                    String subsetType, List<String> ids) throws HttpResponseException {

		try {
			JSONArray jsonIdList = new JSONArray()
			for (String id in ids) {
				jsonIdList.put id
			}

			HttpPost post = new HttpPost(new URI(gNomeHost + '/subset_api.cgi'))
			post.setHeader('Content-Type', 'application/json')
			post.setEntity(new StringEntity(new JSONObject()
					.put('token', token)
					.put('project', projectName)
					.put('type', subsetType)
					.put('list', jsonIdList).toString()))

			HttpResponse response = createHttpClient(username, password).execute(post)

			Map<String, String> responseMap = new ObjectMapper().readValue(response.entity.content, Map)
			int status = response.statusLine.statusCode
			if (status != SC_OK) {
				logger.error('getSubsetURL() HttpRequest returned status: ' + status + '. ' + response.statusLine.reasonPhrase)
				throw new HttpResponseException(status, response.statusLine.reasonPhrase)
			}

			gNomeHost + responseMap.path +
					'?project_type_A=' + responseMap.project_type_A +
					'&project_type_B=' + responseMap.project_type_B +
					'&token=' + token
		}
		catch (URISyntaxException e) {
			logger.error e.message, e
			throw new HttpResponseException(SC_BAD_REQUEST, 'Gnome is not configured properly. Please contact administrator.')
		}
		catch (e) {
			logger.error e.message, e
			throw new HttpResponseException(SC_BAD_REQUEST, 'Gnome is unavailable. Contact administrator.')
		}
	}

	private HttpClient createHttpClient(String username, String password) {

		CredentialsProvider provider = new BasicCredentialsProvider()
		provider.setCredentials AuthScope.ANY, new UsernamePasswordCredentials(username, password)

		HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()
	}
}
