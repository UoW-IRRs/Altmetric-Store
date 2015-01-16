package nz.ac.waikato.its.irr.altmetrics;

import nz.ac.waikato.its.irr.altmetrics.model.ProviderMetrics;
import nz.ac.waikato.its.irr.altmetrics.providers.*;
import nz.ac.waikato.its.irr.altmetrics.storage.SolrStorageService;
import nz.ac.waikato.its.irr.altmetrics.storage.StorageException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class AltmetricsController {
	private static final Logger log = Logger.getLogger(AltmetricsController.class);

	public static ProviderMetrics getProviderMetrics(String handle, String provider, Map<String, String> filteredParameters, Properties properties) throws ProviderException, LookupException {
		ProviderMetrics storedMetrics = null;
		try {
			log.info("Retrieving stored metrics for handle=" + handle + ", provider=" + provider);
			storedMetrics = getStoredMetrics(handle, provider);
		} catch (StorageException e) {
			e.printStackTrace();
		}
		if (!refreshNeeded(storedMetrics)) {
			return storedMetrics;
		}

		log.info("Stored metrics need refreshing, querying provider");
		ProviderMetrics result = queryProvider(handle, provider, filteredParameters, properties);

		if (result != null) {
			try {
				log.info("Got metrics from provider, storing them for future use");
				SolrStorageService.addOrUpdate(result);
			} catch (StorageException e) {
				log.error("Could not store metrics: " + e.getMessage(), e);
			}
		}

		return result;
	}

	public static ProviderMetrics getStoredMetrics(String handle, String provider) throws StorageException {
		return SolrStorageService.findByHandleAndProvider(handle, provider);
	}

	public static void deleteStoredMetrics(String handle, String provider) throws StorageException {
		if (StringUtils.isNotBlank(handle) || StringUtils.isNotBlank(provider)) {
			SolrStorageService.deleteByHandleAndProvider(handle, provider);
		} else {
			// delete all metrics where last updated is older than cut-off date
			Date cutOff = DateTime.now().minusWeeks(2).toDate();
			SolrStorageService.deleteLastUpdatedBefore(cutOff);
		}
	}

	private static ProviderMetrics queryProvider(String handle, String provider, Map<String, String> filteredParameters, Properties properties) throws ProviderException, LookupException {
		ProviderService service = ProviderServiceFactory.forName(properties, provider);

		ProviderLookup lookup = service.makeLookup(handle);
		for (String filteredParameter : filteredParameters.keySet()) {
			lookup.addParam(filteredParameter, filteredParameters.get(filteredParameter));
		}
		return lookup.doLookup();
	}

	private static boolean refreshNeeded(ProviderMetrics metrics) {
		if (metrics == null) {
			return true;
		}
		Date lastUpdated = metrics.getLastUpdated();
		return lastUpdated == null || Days.daysBetween(new DateTime(lastUpdated), new DateTime(new Date())).getDays() >= 7;
	}

}
