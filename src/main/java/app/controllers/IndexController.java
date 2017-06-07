package app.controllers;

import java.util.HashMap;
import java.util.Map;

import app.util.Path;
import app.util.ViewUtil;

import spark.*;

public class IndexController {
	public static Route serveHomePage = (Request request, Response response) -> {
		System.out.println("FROM:IndexController.java:START:serveHomePage");
		Map<String, Object> model = new HashMap<>();
		System.out.println("END:serveHomePage");

		return ViewUtil.render(request, model, Path.Template.INDEX, Path.Web.INDEX, "OK: default return");
	};
}
