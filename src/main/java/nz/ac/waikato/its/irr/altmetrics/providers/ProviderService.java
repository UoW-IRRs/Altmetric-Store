package nz.ac.waikato.its.irr.altmetrics.providers;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public interface ProviderService {
	ProviderLookup makeLookup(String handle);
}
