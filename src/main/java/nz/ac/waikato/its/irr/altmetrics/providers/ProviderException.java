package nz.ac.waikato.its.irr.altmetrics.providers;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ProviderException extends Exception {
	public ProviderException(String message) {
		super(message);
	}

	public ProviderException(String message, Throwable cause) {
		super(message, cause);
	}
}
