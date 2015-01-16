package nz.ac.waikato.its.irr.altmetrics.providers;

import nz.ac.waikato.its.irr.altmetrics.providers.ProviderLookup;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public abstract class AbstractProviderLookup implements ProviderLookup {
	private Map<String, String> parameters = new HashMap<>();

	@Override
	public void addParam(String name, String value) {
		parameters.put(name, value);
	}

	protected Map<String, String> getParams() {
		return parameters;
	}

}
