import org.springframework.context.ApplicationContext
import org.transmart.plugin.gnome.GnomeConfig

class TransmartGnomeGrailsPlugin {

	private static final String TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME = 'transmartExtensionsRegistry'

	String version = '18.1-SNAPSHOT'
	String grailsVersion = '2.5 > *'
	String author = 'Burt Beckwith'
	String authorEmail = 'burt_beckwith@hms.harvard.edu'
	String description = 'gNOME Integration'
	String documentation = 'TODO'
	String title = 'Transmart gNOME Plugin'
	String license = 'APACHE'
	def organization = [name: 'TODO', url: 'TODO']
	def issueManagement = [url: 'TODO']
	def scm = [url: 'TODO']

	def doWithSpring = {
		gnomeConfig(GnomeConfig)
	}

	def doWithApplicationContext = { ApplicationContext ctx ->
		GnomeConfig gnomeConfig = ctx.gnomeConfig
		if (!gnomeConfig.enabled) {
			return
		}

		if (!ctx.containsBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)) {
			return
		}

		String extensionId = 'transmart-gnome'
		String resourcesUrl = '/gnome/loadScripts'
		String bootstrapFunction = 'gnomeBootstrap'

		ctx.getBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME).registerAnalysisTabExtension(
				extensionId, resourcesUrl, bootstrapFunction)
	}
}
