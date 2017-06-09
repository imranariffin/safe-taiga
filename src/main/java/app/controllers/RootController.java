package app.controllers;

import java.util.HashMap;
import java.util.Map;

import app.util.Reference;
import app.util.ViewUtil;
import spark.Request;
import spark.Response;
import spark.Route;

public class RootController {

	public static Route serveRootPage = (Request request, Response response) -> {
		System.out.println("FROM:RootController.java:START:serveRootPage");
		Map<String, Object> model = new HashMap<>();
		
		System.out.println("END:serveRootPage");
		return ViewUtil.render(request, model, Reference.Templates.ROOT, "ROOT PAGE", "");
	};
}
