package nz.ac.waikato.its.irr.altmetrics.providers;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class LookupException extends Exception {
	public LookupException(String message) {
		super(message);
	}

	public LookupException(String message, Throwable cause) {
		super(message, cause);
	}
}
