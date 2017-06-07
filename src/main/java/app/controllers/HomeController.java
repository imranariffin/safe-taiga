package app.controllers;

import java.util.HashMap;
import java.util.Map;
import app.util.Path;
import app.util.ViewUtil;

import spark.Request;
import spark.Response;
import spark.Route;

public class HomeController {

	public static Route serveHomePage = (Request request, Response response) -> {
		System.out.println("FROM:HomeController.java:START:serveHomePage");
		Map<String, Object> model = new HashMap<>();
		System.out.println("END:serveHomePage");
		return ViewUtil.render(request, model, Path.Template.HOME, Path.Web.HOME, "OK : default return");
	};
}
