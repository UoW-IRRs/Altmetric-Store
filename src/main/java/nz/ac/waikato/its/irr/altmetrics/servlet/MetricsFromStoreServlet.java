package nz.ac.waikato.its.irr.altmetrics.servlet;

import nz.ac.waikato.its.irr.altmetrics.AltmetricsController;
import nz.ac.waikato.its.irr.altmetrics.model.ProviderMetrics;
import nz.ac.waikato.its.irr.altmetrics.storage.StorageException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class MetricsFromStoreServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MetricsFromStoreServlet.class);
	private static final DateFormat ISO_DATE_FORMAT;

	static {
		ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String command = request.getParameter("command");
		if ("delete".equals(command)) {
			doDelete(request, response);
			return;
		}
		response.addHeader("Allow", "GET,DELETE");
		response.sendError(405, "You must use get or delete, not post");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean isMachineUser = request.getRequestURI().endsWith(".xml") || request.getRequestURI().endsWith(".json");

		String handle = request.getParameter("handle");
		String provider = request.getParameter("provider");

		ProviderMetrics result = null;
		try {
			result = AltmetricsController.getStoredMetrics(handle, provider);
			request.setAttribute("result", result);
			if (result == null) {
				String message = "No metrics found";
				if (isMachineUser) {
					response.sendError(404, message);
					return;
				} else {
					request.setAttribute("notice", message);
				}
			}
		} catch (StorageException e) {
			String message = "An error occurred when looking up metrics: " + e.getMessage();
			log.error(message, e);
			if (isMachineUser) {
				response.sendError(500, message);
				return;
			} else {
				request.setAttribute("error", message);
			}
		}

		request.setAttribute("handle", handle);
		request.setAttribute("provider", provider);

		RequestDispatcher view;
		if (request.getRequestURI().endsWith(".xml")) {
			view = request.getRequestDispatcher("/pages-xml/handle-metrics-single-provider.jsp");
		} else if (request.getRequestURI().endsWith(".json")) {
			ObjectMapper mapper = new ObjectMapper();
			ByteArrayOutputStream serialised = new ByteArrayOutputStream();
			ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter().withDateFormat(ISO_DATE_FORMAT);
			writer.writeValue(serialised, result);
			request.setAttribute("object", serialised.toString("UTF8"));
			view = request.getRequestDispatcher("/pages-json/json.jsp");
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

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean isMachineUser = request.getRequestURI().endsWith(".xml") || request.getRequestURI().endsWith(".json");
		if (!"127.0.0.1".equals(request.getRemoteAddr()) && !"::1".equals(request.getRemoteAddr())) {
			String message = "DELETE is available only for localhost, remote host was " + request.getRemoteAddr();
			if (isMachineUser) {
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

		try {
			AltmetricsController.deleteStoredMetrics(handle, provider);

			if (isMachineUser) {
				response.setStatus(202); // 202 = "marked for deletion"
				return;
			} else {
				request.setAttribute("success", "Matching altmetrics were marked for deletion");
			}
		} catch (StorageException e) {
			String message = "Could not delete as requested: " + e.getMessage();
			if (isMachineUser) {
				response.sendError(403, message);
				return;
			} else {
				request.setAttribute("error", message);
			}
		}

		RequestDispatcher view = request.getRequestDispatcher("/");
		view.forward(request, response);
	}

	@Override
	protected long getLastModified(HttpServletRequest request) {
		String handle = request.getParameter("handle");
		String provider = request.getParameter("provider");

		try {
			ProviderMetrics result = AltmetricsController.getStoredMetrics(handle, provider);
			if (result != null) {
				Date lastUpdated = result.getLastUpdated();
				if (lastUpdated != null) {
					return lastUpdated.getTime();
				}
			}
		} catch (StorageException e) {
			// ignore and fall back on super
		}
		return super.getLastModified(request);
	}
}
