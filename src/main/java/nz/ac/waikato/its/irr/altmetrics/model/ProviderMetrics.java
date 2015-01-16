package nz.ac.waikato.its.irr.altmetrics.model;

import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ProviderMetrics {
	private String handle;
	private String provider;
	private Map<String, Object> metricsMap = new TreeMap<>();
	private Date lastUpdated;

	public void setMetricsMap(Map<String, Object> metricsMap) {
		this.metricsMap = metricsMap;
	}

	public ProviderMetrics() {
	}

	public ProviderMetrics(String handle, String provider) {
		setHandle(handle);
		setProvider(provider);
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getProvider() {
		return provider;
	}

	public String getHandle() {
		return handle;
	}

	public Map<String, Object> getMetrics() {
		return metricsMap;
	}

	public void putMetric(String metricName, Object metricValue) {
		metricsMap.put(metricName, metricValue);
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public void touchLastUpdated() {
		setLastUpdated(new Date());
	}

	@Override
	public String toString() {
		return "ProviderMetrics{" +
				       ", handle='" + handle + '\'' +
				       ", provider='" + provider + '\'' +
				       ", metricsMap=" + metricsMap +
				       ", lastUpdated=" + lastUpdated +
				       '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ProviderMetrics that = (ProviderMetrics) o;

		if (!handle.equals(that.handle)) return false;
		if (lastUpdated != null ? !lastUpdated.equals(that.lastUpdated) : that.lastUpdated != null) return false;
		if (!metricsMap.equals(that.metricsMap)) return false;
		if (!provider.equals(that.provider)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = 0;
		result = 31 * result + handle.hashCode();
		result = 31 * result + provider.hashCode();
		result = 31 * result + metricsMap.hashCode();
		result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
		return result;
	}
}
