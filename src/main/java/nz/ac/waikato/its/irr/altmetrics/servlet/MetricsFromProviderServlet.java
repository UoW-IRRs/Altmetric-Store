package nz.ac.waikato.its.irr.altmetrics.servlet;

import nz.ac.waikato.its.irr.altmetrics.AltmetricsController;
import nz.ac.waikato.its.irr.altmetrics.model.ProviderMetrics;
import nz.ac.waikato.its.irr.altmetrics.providers.LookupException;
import nz.ac.waikato.its.irr.altmetrics.providers.ProviderException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class MetricsFromProviderServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MetricsFromProviderServlet.class);

	private Properties properties;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Allow", "GET");
		response.sendError(405, "You must use get not post");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean isXml = request.getRequestURI().endsWith(".xml");

		if (!"127.0.0.1".equals(request.getRemoteAddr()) && !"::1".equals(request.getRemoteAddr())) {
			String message = "This page is available only for localhost, remote host was " + request.getRemoteAddr();
			if (isXml) {
				response.sendError(403, message);
			} else {
				request.setAttribute("error", message);
				RequestDispatcher view = request.getRequestDispatcher("/");
				view.forward(request, response);
			}
			return;
		}

		String handle = request.getParameter("handle");
		String provider = request.getParameter("provider");

		ProviderMetrics result = null;
		try {
			Map<String, String> filteredParameters = filterParameters(request);
			result = AltmetricsController.getProviderMetrics(handle, provider, filteredParameters, properties);
		} catch (ProviderException e) {
			String message = "No provider found with name " + URLEncoder.encode(provider, "UTF8");
			log.error(message, e);
			if (isXml) {
				response.sendError(404, message);
				return;
			} else {
				request.setAttribute("error", message);
			}
		} catch (LookupException e) {
			String message = "Problem looking up metric: " + e.getMessage();
			log.error(message, e);
			if (isXml) {
				response.sendError(500, message);
				return;
			} else {
				request.setAttribute("error", message);
			}
		}

		request.setAttribute("result", result);

		if (result == null) {
			if (isXml) {
				response.sendError(404, "No metrics found");
				return;
			} else {
				request.setAttribute("notice", "No metrics found");
			}
		}

		request.setAttribute("handle", handle);
		request.setAttribute("provider", provider);

		RequestDispatcher view;
		if (isXml) {
			view = request.getRequestDispatcher("/pages-xml/handle-metrics-single-provider.jsp");
		} else {
			if (result != null) {
				request.setAttribute("page", "/pages-html/handle-metrics-single-provider");
				view = request.getRequestDispatcher("/layout/base.jsp");
			} else {
				view = request.getRequestDispatcher("/");
			}
		}
		view.forward(request, response);
	}

	private Map<String, String> filterParameters(HttpServletRequest request) {
		Map<String, String> filteredParameters = new HashMap<>();
		Enumeration parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameter = Objects.toString(parameterNames.nextElement(), "");
			if (parameter.startsWith("param_")) {
				filteredParameters.put(parameter.substring("param_".length()), request.getParameter(parameter));
			}
		}
		return filteredParameters;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		properties = new Properties();
		String apiKeyInitParam = config.getServletContext().getInitParameter("scopus.apiKey");
		if (StringUtils.isNotBlank(apiKeyInitParam)) {
			properties.put("scopus.apiKey", apiKeyInitParam);
		}
	}
}
