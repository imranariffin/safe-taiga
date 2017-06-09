package app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import static spark.Spark.*;
import app.util.*;
import app.controllers.*;

public class Application {

	public static HikariDataSource DATA_SOURCE;
	public static HikariConfig config;
	public static boolean devmode = false;

	public static void main(String[] args) {
		System.out.println("SERVER:STARTs");
		System.out.println("Server is running at port:" + Integer.valueOf(System.getenv("PORT")));
		port(Integer.valueOf(System.getenv("PORT")));
		staticFileLocation("/public");
		
		config = new HikariConfig();
		config.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
		DATA_SOURCE = (config.getJdbcUrl() != null) ? new HikariDataSource(config) : new HikariDataSource();

		// Set up before-filters (called before each get/post)
		before("*", Filters.addTrailingSlashes);

		/**
		 * GET ROUTES
		 */
		// get(Path.Web.LOGIN, LoginController.serveLoginPage);
		get(Reference.Web.ROOT, RootController.serveRootPage);
		get(Reference.Web.TEXTBOARD, TextboardController.serveTextboard_HOME);
		get(Reference.Web.TEXTBOARD_BOARD, TextboardController.serveTextboard_BOARD);
		get(Reference.Web.TEXTBOARD_BOARD_THREAD, TextboardController.serveTextboard_THREAD);

		/**
		 * POST ROUTES
		 */
		post(Reference.Web.CREATE_BOARD, TextboardController.handleCreateBoard);
		post(Reference.Web.CREATE_THREAD, TextboardController.handleCreateThread);
		post(Reference.Web.CREATE_POST, TextboardController.handleCreatePost);

		/**
		 * NOT FOUND
		 */
		get("*", ViewUtil.notFound);

		// Set up after-filters (called after each get/post)
		after("*", Filters.addGzipHeader);
		System.out.println("SERVER:END");
	}
}
