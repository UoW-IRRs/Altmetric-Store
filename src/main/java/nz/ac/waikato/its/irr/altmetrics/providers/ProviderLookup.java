package nz.ac.waikato.its.irr.altmetrics.providers;

import nz.ac.waikato.its.irr.altmetrics.model.ProviderMetrics;
import nz.ac.waikato.its.irr.altmetrics.providers.LookupException;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public interface ProviderLookup {
	void addParam(String name, String value);

	ProviderMetrics doLookup() throws LookupException;
}
