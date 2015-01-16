package nz.ac.waikato.its.irr.altmetrics.providers;

import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ProviderServiceFactory {
	public static ProviderService forName(Properties properties, String provider) throws ProviderException {
		if (ScopusAltmetricsService.PROVIDER_NAME.equals(provider)) {
			return new ScopusAltmetricsService(properties);
		}
		throw new ProviderException("No provider found for " + provider);
	}
}
