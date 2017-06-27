package app;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.debug.DebugScreen.enableDebugScreen;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import app.controllers.ImageProcessingController;
import app.controllers.RootController;
import app.controllers.TextboardController;
import app.util.Reference;
import app.util.Tools;
import app.util.ViewUtil;

public class Application {

	// public static HikariConfig config;

	public static File IMAGES_INPUT_DIR, IMAGES_OTHER_DIR, IMAGES_OUTPUT_PARTITION_DIR, TEXT_OUTPUT_PARTITION_DIR,
			IMAGES_OUTPUT_RESIZED_DIR;

	public static void main(String[] args) {
		enableDebugScreen();

		int portNumber;
		try {
			portNumber = Integer.valueOf(System.getenv("PORT"));
		} catch (Exception e) {
			Tools.println("System running locally, setting port to the default 5000");
			portNumber = 5000;
		}
		Tools.println("PORT:" + portNumber);
		port(portNumber);

		IMAGES_OTHER_DIR = new File("public/images/other");
		IMAGES_OUTPUT_PARTITION_DIR = new File("public/images/output/partition");
		IMAGES_OUTPUT_RESIZED_DIR = new File("public/images/output/resized");
		TEXT_OUTPUT_PARTITION_DIR = new File("public/texts/output/partition");
		IMAGES_INPUT_DIR = new File("public/images/input");

		IMAGES_OTHER_DIR.mkdirs();
		IMAGES_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_OUTPUT_RESIZED_DIR.mkdirs();
		TEXT_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_INPUT_DIR.mkdirs();

		staticFiles.externalLocation("public");
		staticFiles.expireTime(600L);

		/**
		 * config = new HikariConfig();
		 * config.setJdbcUrl(System.getenv("HEROKU_POSTGRESQL_BLUE_URL"));
		 * DATA_SOURCE = (config.getJdbcUrl() != null) ? new
		 * HikariDataSource(config) : new HikariDataSource();
		 */
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
		Tools.InsertTextDumpToDatabase();
		System.out.println("SERVER:END");
	}

	public static Connection getConnection() throws URISyntaxException, SQLException {
		URI dbUri = new URI(System.getenv("HEROKU_POSTGRESQL_BLUE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
	}

}
