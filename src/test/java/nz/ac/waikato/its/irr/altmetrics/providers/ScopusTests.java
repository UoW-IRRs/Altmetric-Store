package nz.ac.waikato.its.irr.altmetrics.providers;

import nz.ac.waikato.its.irr.altmetrics.model.ProviderMetrics;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ScopusTests {
	private static final Logger log = Logger.getLogger(ScopusTests.class);

	public static final String TEST_HANDLE = "123456789/1";
	public static final String TEST_DOI = "10.1002/jqs.1016";

	private ProviderLookup lookup;
	private ScopusAltmetricsService service;

	@Before
	public void makeLookup() {
		Properties testProperties = new Properties();
		try {
			testProperties.load(ClassLoader.getSystemResourceAsStream("altmetrics-store.properties"));
		} catch (IOException e) {
			log.error("Exception when trying to load properties", e);
			Assert.fail("Exception when trying to load properties");
		}
		service = new ScopusAltmetricsService(testProperties);
		lookup = service.makeLookup(TEST_HANDLE);
	}

	@Test
	public void testComplainsAboutNoDOI() {
		boolean exception = false;
		try {
			lookup.doLookup();
			Assert.fail("Should complain about missing DOI");
		} catch (LookupException e) {
			exception = true;
		}
		Assert.assertTrue("Should complain about missing DOI", exception);
	}

	@Test
	public void testHandleAndProviderCorrect() {
		setTestDOI(lookup);
		ProviderMetrics metrics = null;
		try {
			metrics = lookup.doLookup();
		} catch (LookupException e) {
			log.error("Exception when doing lookup", e);
			Assert.fail("Exception when doing lookup");
		}
		Assert.assertNotNull("Non-null lookup result", metrics);
		Assert.assertEquals("Provider correct", ScopusAltmetricsService.PROVIDER_NAME, metrics.getProvider());
		Assert.assertEquals("Handle correct", TEST_HANDLE, metrics.getHandle());
	}

	@Test
	public void testActualLookupByDOINotNull() {
		setTestDOI(lookup);
		ProviderMetrics metrics = null;
		try {
			metrics = lookup.doLookup();
		} catch (LookupException e) {
			log.error("Exception when doing lookup", e);
			Assert.fail("Exception when doing lookup");
		}
		Assert.assertNotNull("Non-null lookup result", metrics);
		Map<String, Object> actualMetrics = metrics.getMetrics();
		Assert.assertNotNull("Non-null actual metrics", actualMetrics);
		Assert.assertTrue("Non-empty actual metrics", !actualMetrics.isEmpty());
	}

	@Test
	public void testActualLookupByDOIDataMatches() {
		setTestDOI(lookup);
		ProviderMetrics metricsFromScopus = null;
		try {
			metricsFromScopus = lookup.doLookup();
		} catch (LookupException e) {
			log.error("Exception when doing lookup", e);
			Assert.fail("Exception when doing lookup");
		}
		Assert.assertNotNull("Non-null lookup result", metricsFromScopus);


		ProviderMetrics metricsFromFile = null;
		try {
			InputStream file = ClassLoader.getSystemResourceAsStream("scopus-test-response.xml");
			metricsFromFile = service.makeProviderMetricsFromXml(TEST_HANDLE, file);
			if (file != null) { file.close(); }
		} catch (IOException e) {
			log.error("Exception when creating metrics object from XML", e);
			Assert.fail("Exception when creating metrics object from XML");
		}

		Assert.assertEquals("Same Scopus URL",
				                   metricsFromFile.getMetrics().get(ScopusAltmetricsService.MetricNames.SCOPUS_LINK.getKey()),
				                   metricsFromScopus.getMetrics().get(ScopusAltmetricsService.MetricNames.SCOPUS_LINK.getKey()));

		Assert.assertEquals("Same Scopus cited-by link",
				                   metricsFromFile.getMetrics().get(ScopusAltmetricsService.MetricNames.SCOPUS_CITEDBY_LINK.getKey()),
				                   metricsFromScopus.getMetrics().get(ScopusAltmetricsService.MetricNames.SCOPUS_CITEDBY_LINK.getKey()));

		Object citecountObjectFromFile = metricsFromFile.getMetrics().get(ScopusAltmetricsService.MetricNames.CITEDBY_COUNT.getKey());
		Object citecountObjectFromScopus = metricsFromScopus.getMetrics().get(ScopusAltmetricsService.MetricNames.CITEDBY_COUNT.getKey());
		if (citecountObjectFromFile != null) {
			Integer citecountFromFile = (Integer) citecountObjectFromFile;
			Integer citecountFromScopus = (Integer) citecountObjectFromScopus;
			Assert.assertTrue("Count from Scopus >= count from file", citecountFromScopus >= citecountFromFile);
		}
	}

	@Test
	public void testParamsToQuery() {
		Map<String, String> params = new HashMap<>();
		params.put(ScopusAltmetricsService.DOI_KEY, TEST_DOI);
		try {
			String query = service.paramsToQuery(params);
			Assert.assertEquals("Query from params", "DOI%28%2210.1002%2Fjqs.1016%22%29", query);
		} catch (UnsupportedEncodingException e) {
			log.error("Exception when converting params to query", e);
			Assert.fail("Exception when converting params to query");
		}
	}

	@Test
	public void testMakeProviderMetricsFromXml() {
		ProviderMetrics expectedMetrics = new ProviderMetrics(TEST_HANDLE, ScopusAltmetricsService.PROVIDER_NAME);
		expectedMetrics.putMetric("citedby-count", 23);
		expectedMetrics.putMetric("scopus-link", "http://www.scopus.com/inward/record.url?partnerID=HzOxMe3b&scp=33846320043");
		expectedMetrics.putMetric("scopus-citedby-link", "http://www.scopus.com/inward/citedby.url?partnerID=HzOxMe3b&scp=33846320043");

		try {
			InputStream file = ClassLoader.getSystemResourceAsStream("scopus-test-response.xml");
			ProviderMetrics actualMetrics = service.makeProviderMetricsFromXml(TEST_HANDLE, file);
			Assert.assertEquals("Metrics from JSON match expectations", expectedMetrics, actualMetrics);
			if (file != null) { file.close(); }
		} catch (IOException e) {
			log.error("Exception when creating metrics object from XML", e);
			Assert.fail("Exception when creating metrics object from XML");
		}
	}

	private static void setTestDOI(ProviderLookup lookup) {
		lookup.addParam("doi", TEST_DOI);
	}
}
