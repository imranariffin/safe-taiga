package app.util;

import org.apache.velocity.app.*;
import org.eclipse.jetty.http.*;
import spark.*;
import spark.template.velocity.*;
import java.util.*;

public class ViewUtil {

	// Renders a template given a model and a request
	// The request is needed to check the user session for language settings
	// and to see if the user is logged in
	public static String render(Request request, Map<String, Object> model, String templatePath, String where,
			String message) {

		model.put("where", where);
		model.put("message", message);
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
	public static String renderErrorMessage(Request request, String errorMessage, String returnLink, String returnName) {

		Map<String, String> model = new HashMap<String, String>();
		model.put(Reference.CommonStrings.ERROR, errorMessage);
		model.put(Reference.CommonStrings.RETURNLINK, returnLink);
		model.put(Reference.CommonStrings.RETURNNAME, returnName);
		return strictVelocityEngine().render(new ModelAndView(model, Reference.Templates.ERROR));
	}
}
