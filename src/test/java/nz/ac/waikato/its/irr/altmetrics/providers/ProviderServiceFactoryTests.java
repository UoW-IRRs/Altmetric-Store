package nz.ac.waikato.its.irr.altmetrics.providers;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ProviderServiceFactoryTests {
	private static final Logger log = Logger.getLogger(ProviderServiceFactoryTests.class);
	@Test
	public void testMakeScopusAltmetricsService() {
		Properties properties = new Properties();
		ProviderService service = null;
		try {
			service = ProviderServiceFactory.forName(properties, ScopusAltmetricsService.PROVIDER_NAME);
		} catch (ProviderException e) {
			log.error("Exception when creating provider service object", e);
			Assert.fail("Exception when creating provider service object");
		}
		Assert.assertNotNull("Non-null service object", service);
		Assert.assertTrue("Service object instance of correct class", service instanceof ScopusAltmetricsService);
	}
}
