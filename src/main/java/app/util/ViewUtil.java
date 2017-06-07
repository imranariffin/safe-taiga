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
		// model.put("message", new MessageBundle(getSessionLocale(request)));
		// model.put("currentuser", getSessionCurrentUser(request));

		model.put("where", where);
		model.put("message", message);
		model.put("WebPath", Path.Web.class); // Access application URLs from
												// templates
		return strictVelocityEngine().render(new ModelAndView(model, templatePath));
	}

	public static Route notFound = (Request request, Response response) -> {
		response.status(HttpStatus.NOT_FOUND_404);
		Map<String, Object> model = new HashMap<>();
		return render(request, model, Path.Template.NOT_FOUND, Path.StaticStrings.ERROR, "404 NOT FOUND");
	};

	private static VelocityTemplateEngine strictVelocityEngine() {
		VelocityEngine configuredEngine = new VelocityEngine();
		configuredEngine.setProperty("runtime.references.strict", true);
		configuredEngine.setProperty("resource.loader", "class");
		configuredEngine.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		return new VelocityTemplateEngine(configuredEngine);
	}
}
