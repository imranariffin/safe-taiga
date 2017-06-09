package app.util;

import spark.*;

public class Filters {

	// If a user manually manipulates paths and forgets to add
	// a trailing slash, redirect the user to the correct path
	public static Filter addTrailingSlashes = (Request request, Response response) -> {
		//System.out.println("FROM:Filters:START:addTrailingSlashes");
		if (!request.pathInfo().endsWith("/")) {
			response.redirect(request.pathInfo() + "/");
		}
		//System.out.println("END:addTrailingSlashes");
	};

	// Enable GZIP for all responses
	public static Filter addGzipHeader = (Request request, Response response) -> {
		//System.out.println("FROM:Filters:START:addGzipHeader");
		response.header("Content-Encoding", "gzip");
		//System.out.println("END:addGzipHeader");
	};

}
