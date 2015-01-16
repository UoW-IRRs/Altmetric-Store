package nz.ac.waikato.its.irr.altmetrics.storage;

import nz.ac.waikato.its.irr.altmetrics.model.ProviderMetrics;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class SolrStorageService {
	private static final DateFormat ISO_DATE_FORMAT;

	static {
		ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private static final SolrServer solr;
	static {
		String solrUrl = "http://localhost:8080/solr/altmetrics";
		solr = new HttpSolrServer(solrUrl);
	}

	public static final int COMMIT_DELAY = 500;

	public static void addOrUpdate(ProviderMetrics providerMetrics) throws StorageException {
		try {
			SolrInputDocument solrDoc = toSolrDoc(providerMetrics);
			if (solrDoc != null) {
				solr.add(solrDoc, COMMIT_DELAY);
			}
		} catch (IOException | SolrServerException e) {
			throw new StorageException("Problem saving metrics", e);
		}
	}

	public static ProviderMetrics findByHandleAndProvider(String handle, String provider) throws StorageException {
		NamedList<String> queryParams = new NamedList<>();
		queryParams.add("q", String.format("handle:%s AND provider:%s", wildcardOrEscape(handle), wildcardOrEscape(provider)));
		queryParams.add("sort", "last-updated DESC");
		queryParams.add("rows", "1");
		try {
			QueryResponse response = solr.query(SolrParams.toSolrParams(queryParams));
			SolrDocumentList results = response.getResults();
			for (SolrDocument result : results) {
				ProviderMetrics metrics = fromSolrDoc(result);
				if (metrics != null) {
					return metrics; // return first non-null result
				}
			}
			return null;
		} catch (SolrServerException e) {
			throw new StorageException("Could not do query", e);
		}
	}

	static SolrInputDocument toSolrDoc(ProviderMetrics metrics) {
		if (metrics == null) {
			return null;
		}
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("provider", metrics.getProvider());
		doc.setField("handle", metrics.getHandle());
		doc.setField("last-updated", metrics.getLastUpdated());
		doc.setField("id", metrics.getProvider() + "-" + metrics.getHandle());
		for (String key : metrics.getMetrics().keySet()) {
			Object value = metrics.getMetrics().get(key);
			if (value instanceof Integer) {
				doc.setField(key + "_i_metric", value);
			} else {
				doc.setField(key + "_s_metric", Objects.toString(value, ""));
			}
		}
		return doc;
	}

	static ProviderMetrics fromSolrDoc(SolrDocument doc) {
		if (doc == null) {
			return null;
		}
		ProviderMetrics metrics = new ProviderMetrics();
		metrics.setProvider(Objects.toString(doc.getFieldValue("provider"), ""));
		metrics.setHandle(Objects.toString(doc.getFieldValue("handle"), ""));
		try {
			metrics.setLastUpdated((Date) doc.getFieldValue("last-updated"));
		} catch (ClassCastException e) {
			// ignore
		}
		Collection<String> fieldNames = doc.getFieldNames();
		for (String fieldName : fieldNames) {
			if (fieldName.endsWith("_i_metric")) {
				String key = StringUtils.removeEnd(fieldName, "_i_metric");
				Integer value;
				try {
					value = (Integer) doc.getFieldValue(fieldName);
					metrics.putMetric(key, value);
				} catch (ClassCastException e) {
					// ignore
				}
			} else if (fieldName.endsWith("_s_metric")) {
				String key = StringUtils.removeEnd(fieldName, "_s_metric");
				Object value = doc.getFieldValue(fieldName);
				metrics.putMetric(key, Objects.toString(value, ""));
			}
		}
		return metrics;
	}

	public static void deleteLastUpdatedBefore(Date cutOff) throws StorageException {
		if (cutOff == null || cutOff.before(new Date())) {
			throw new StorageException("Cut-off needs to be non-null date in the past, is " + cutOff);
		}
		try {
			solr.deleteByQuery(String.format("last-updated:[* TO %s]", ISO_DATE_FORMAT.format(cutOff)));
		} catch (SolrServerException | IOException e) {
			throw new StorageException("Could not delete older than cut-off", e);
		}
	}

	public static void deleteByHandleAndProvider(String handle, String provider) throws StorageException {
		handle = wildcardOrEscape(handle);
		provider = wildcardOrEscape(provider);

		try {
			solr.deleteByQuery(String.format("handle:%s AND provider:%s", handle, provider), COMMIT_DELAY);
		} catch (SolrServerException | IOException e) {
			throw new StorageException("Could not delete by handle/provider", e);
		}
	}

	private static String wildcardOrEscape(String string) {
		if (StringUtils.isBlank(string)) {
			string = "*";
		} else {
			string = ClientUtils.escapeQueryChars(string);
		}
		return string;
	}
}
