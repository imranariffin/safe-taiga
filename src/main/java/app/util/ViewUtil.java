package app.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.http.HttpStatus;

import spark.Filter;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;

public class ViewUtil {

	// Renders a template given a model and a request
	// The request is needed to check the user session for language settings
	// and to see if the user is logged in
	public static String render(Request request, Map<String, Object> model, String templatePath, String where,
			String message) {

		// Basic links that are not dynamic like :boardlink or :threadid
		model.put(Reference.VTL.ROOT_LINK, Reference.Web.ROOT);
		model.put(Reference.VTL.ROOT_NAME, Reference.CommonStrings.NAME_ROOT);

		model.put(Reference.VTL.TEXTBOARD_LINK, Reference.Web.TEXTBOARD);
		model.put(Reference.VTL.TEXTBOARD_NAME, Reference.CommonStrings.NAME_TEXTBOARD);

		model.put(Reference.VTL.IMAGEPROCESSING_LINK, Reference.Web.IMAGEPROCESSING);
		model.put(Reference.VTL.IMAGEPROCESSING_NAME, Reference.CommonStrings.NAME_IMAGEPROCESSING);

		model.put(Reference.VTL.WHERE_NAME, where);
		model.put(Reference.VTL.WHERE_TEXT, message);
		return strictVelocityEngine().render(new ModelAndView(model, templatePath));
	}

	public static Route notFound = (Request request, Response response) -> {
		response.status(HttpStatus.NOT_FOUND_404);
		Map<String, Object> model = new HashMap<>();
		return render(request, model, Reference.Templates.NOT_FOUND, Reference.CommonStrings.ERROR, "404 NOT FOUND");
	};

	private static VelocityTemplateEngine strictVelocityEngine() {
		VelocityEngine configuredEngine = new VelocityEngine();
		configuredEngine.setProperty("runtime.references.strict", true);
		configuredEngine.setProperty("resource.loader", "class");
		configuredEngine.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		return new VelocityTemplateEngine(configuredEngine);
	}

	// Renders a template given a model and a request
	// The request is needed to know where the user is
	public static String renderErrorMessage(Request request, String errorMessage, String returnLink,
			String returnName) {
		Tools.println("\nAn error rendering page has occured\n");

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(Reference.CommonStrings.ERROR, errorMessage);
		model.put(Reference.CommonStrings.RETURNLINK, returnLink);
		model.put(Reference.CommonStrings.RETURNNAME, returnName);
		return render(request, model, Reference.Templates.ERROR, "Error", errorMessage);
	}

	// If a user manually manipulates paths and forgets to add
	// a trailing slash, redirect the user to the correct path
	public static Filter addTrailingSlashes = (Request request, Response response) -> {
		// Tools.print("FROM:Filters:START:addTrailingSlashes");
		if (!request.pathInfo().endsWith("/")) {
			response.redirect(request.pathInfo() + "/");
		}
		// Tools.print("END:addTrailingSlashes");
	};

	// Enable GZIP for all responses
	public static Filter addGzipHeader = (Request request, Response response) -> {
		// Tools.print("FROM:Filters:START:addGzipHeader");
		response.header("Content-Encoding", "gzip");
		// Tools.print("END:addGzipHeader");
	};
}
