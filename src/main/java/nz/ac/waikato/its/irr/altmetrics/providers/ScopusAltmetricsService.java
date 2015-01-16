package nz.ac.waikato.its.irr.altmetrics.providers;

import nz.ac.waikato.its.irr.altmetrics.model.ProviderMetrics;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ScopusAltmetricsService implements ProviderService {
	static enum MetricNames {
		CITEDBY_COUNT("citedby-count"),
		SCOPUS_LINK("scopus-link"),
		SCOPUS_CITEDBY_LINK("scopus-citedby-link");

		private final String key;

		MetricNames(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	};


	private static final String ENDPOINT = "http://api.elsevier.com/content/search/scopus";
	private static final int TIMEOUT = 5000;

	public static final String PROVIDER_NAME = "scopus";
	public static final String DOI_KEY = "doi";

	private final String apiKey;

	public ScopusAltmetricsService(Properties properties) {
		apiKey = properties.getProperty("scopus.apiKey", "");
	}

	@Override
	public ProviderLookup makeLookup(final String handle) {
		return new AbstractProviderLookup() {
			@Override
			public ProviderMetrics doLookup() throws LookupException {
				if (StringUtils.isBlank(apiKey)) {
					throw new LookupException("API key is required for Scopus but not set");
				}
				Map<String, String> params = getParams();
				if (!params.containsKey(DOI_KEY)) {
					throw new LookupException("DOI is required for lookup");
				}

				ProviderMetrics metrics = null;

				GetMethod get = null;
				try {
					String requestUrlString = makeRequest(params);
					HttpClient httpClient = new HttpClient();
					httpClient.getParams().setConnectionManagerTimeout(TIMEOUT);
					get = new GetMethod(requestUrlString);

					int result = httpClient.executeMethod(get);
					if (result == 200) {
						metrics = makeProviderMetricsFromXml(handle, get.getResponseBodyAsStream());
						metrics.touchLastUpdated();
					}
				} catch (IOException e) {
					throw new LookupException("Problem retrieving information from Scopus: " + e.getMessage(), e);
				} finally {
					if (get != null) {
						get.releaseConnection();
					}
				}

				return metrics;
			}
		};
	}

	ProviderMetrics makeProviderMetricsFromXml(String handle, InputStream xml) throws IOException {
		ProviderMetrics result = new ProviderMetrics(handle, PROVIDER_NAME);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			Document tree = documentBuilder.parse(xml);
			XPath xPath = XPathFactory.newInstance().newXPath();

			Double citedByCount = (Double) xPath.evaluate("//*[local-name()='citedby-count']", tree.getDocumentElement(), XPathConstants.NUMBER);
			if (citedByCount != null && citedByCount >= 0) {
				result.putMetric(MetricNames.CITEDBY_COUNT.key, (int) Math.floor(citedByCount));
			}

			String scopusLink = Objects.toString(xPath.evaluate("//*[local-name()='link'][@ref='scopus']/@href", tree.getDocumentElement(), XPathConstants.STRING), "");
			if (StringUtils.isNotBlank(scopusLink)) {
				result.putMetric(MetricNames.SCOPUS_LINK.key, scopusLink.replace("&", "&amp;"));
			}

			String scopusCitedByLink = Objects.toString(xPath.evaluate("//*[local-name()='link'][@ref='scopus-citedby']/@href", tree.getDocumentElement(), XPathConstants.STRING), "");
			if (StringUtils.isNotBlank(scopusCitedByLink)) {
				result.putMetric(MetricNames.SCOPUS_CITEDBY_LINK.key, scopusCitedByLink.replace("&", "&amp;"));
			}
		} catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}

	String makeRequest(Map<String, String> params) throws UnsupportedEncodingException {
		return ENDPOINT + "?" + "httpAccept=application/xml&apiKey=" + apiKey + "&count=1&suppressNavLinks=true&query=" + paramsToQuery(params);
	}

	String paramsToQuery(Map<String, String> params) throws UnsupportedEncodingException {
		String doi = params.get(DOI_KEY);
		return URLEncoder.encode("DOI(\"" + doi + "\")", "UTF8");
	}
}
