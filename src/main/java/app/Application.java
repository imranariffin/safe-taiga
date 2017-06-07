package app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import static spark.Spark.*;
import app.util.*;
import app.controllers.*;

public class Application {

	public static HikariDataSource DATA_SOURCE;
	public static HikariConfig config;
	public final static String DATABASE_URL = DatabaseController.DATABASE_URL;

	public static void main(String[] args) {
		System.out.println("SERVER:START");
		System.out.println("Server is running at port:" + Integer.valueOf(System.getenv("PORT")));
		port(Integer.valueOf(System.getenv("PORT")));
		staticFileLocation("/public");

		// Instantiate your dependencies

		// Initiate databaseHikariConfig config = new HikariConfig();
		config = new HikariConfig();
		config.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
		DATA_SOURCE = (config.getJdbcUrl() != null) ? new HikariDataSource(config) : new HikariDataSource();

		// Set up before-filters (called before each get/post)
		before("*", Filters.addTrailingSlashes);

		/**
		 * GET ROUTES
		 */
		// get(Path.Web.LOGIN, LoginController.serveLoginPage);
		get(Path.Web.ROOT, RootController.serveRootPage);
		get(Path.Web.DATABASE, DatabaseController.serveDatabasePage);
		get(Path.Web.HOME, HomeController.serveHomePage);
		get(Path.Web.TEXTBOARD, TextboardController.serveTextboardHome);
		get(Path.Web.TEXTBOARD_BOARD, TextboardController.serveTextboardBoard);
		get(Path.Web.TEXTBOARD_BOARD_THREAD, TextboardController.serveTextboardBoardThread);

		/**
		 * POST ROUTES
		 */
		post(Path.Web.CREATE_BOARD, TextboardController.handleCreateBoard);

		/**
		 * NOT FOUND
		 */
		get("*", ViewUtil.notFound);

		// Set up after-filters (called after each get/post)
		after("*", Filters.addGzipHeader);
		System.out.println("SERVER:END");
	}
}
