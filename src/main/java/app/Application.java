package app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;

import app.util.*;
import app.controllers.*;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

public class Application {

	public static HikariDataSource DATA_SOURCE;
	public static HikariConfig config;
	public static boolean devmode = true;

	public static File uploadDir;

	public static void main(String[] args) {
		enableDebugScreen();

		Tools.print("SERVER:START:" + Integer.valueOf(System.getenv("PORT")));
		port(Integer.valueOf(System.getenv("PORT")));

		new File("public/images/other").mkdirs();
		new File("public/images/input").mkdirs();
		new File("public/images/output/partition").mkdirs();
		new File("public/texts/output").mkdirs();

		uploadDir = new File("public/images/input");

		// staticFiles.location("src/main/resources/public");
		staticFiles.externalLocation("public");
		staticFiles.expireTime(600L);

		config = new HikariConfig();
		config.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
		DATA_SOURCE = (config.getJdbcUrl() != null) ? new HikariDataSource(config) : new HikariDataSource();

		// Set up before-filters (called before each get/post)
		// before("*", Filters.addTrailingSlashes);

		/**
		 * GET ROUTES
		 */
		get(Reference.Web.ROOT, RootController.serveRootPage);
		get(Reference.Web.TEXTBOARD, TextboardController.serveTextboard_HOME);
		get(Reference.Web.TEXTBOARD_BOARD, TextboardController.serveTextboard_BOARD);
		get(Reference.Web.TEXTBOARD_BOARD_THREAD, TextboardController.serveTextboard_THREAD);
		get(Reference.Web.IMAGEPROCESSING, ImageProcessingController.serveImageUpload);

		/**
		 * POST ROUTES
		 */
		post(Reference.Web.TEXTBOARD, TextboardController.handleCreateBoard);
		post(Reference.Web.TEXTBOARD_BOARD, TextboardController.handleCreateThread);
		post(Reference.Web.TEXTBOARD_BOARD_THREAD, TextboardController.handleCreatePost);
		post(Reference.Web.IMAGEPROCESSING, ImageProcessingController.handleImageUpload);
		/**
		 * NOT FOUND
		 */
		get("*", ViewUtil.notFound);

		// Set up after-filters (called after each get/post)
		// after("*", Filters.addGzipHeader);
		System.out.println("SERVER:END");
	}
}
