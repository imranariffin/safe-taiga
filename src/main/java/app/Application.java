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

import app.controllers.ImageProcessingController;
import app.controllers.RootController;
import app.controllers.TextboardController;
import app.util.Reference;
import app.util.SettingUp;
import app.util.Tools;
import app.util.ViewUtil;

public class Application {

	public static File IMAGES_INPUT_DIR, IMAGES_OTHER_DIR, IMAGES_OUTPUT_PARTITION_DIR, TEXT_OUTPUT_PARTITION_DIR,
			IMAGES_OUTPUT_RESIZED_DIR, IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR, TEXT_OUTPUT_GLOBALDIFFERENCE_DIR,
			IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR, IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR;

	public static void main(String[] args) {
		long tStart = System.currentTimeMillis();

		if (System.getenv("IS_HEROKU") == null) {
			enableDebugScreen();
			Tools.println("Debug screen enabled");
			// SettingUp.createDatabases();
			SettingUp.createImageInfo();
			// SettingUp.InsertTextDumpToDatabase();

			Tools.println("No .env file specified, defaulting to port:5000");
			port(5000);
		} else {
			Tools.println("Debug screen disabled");
			port(Integer.valueOf(System.getenv("PORT")));
		}

		/**
		 * Required Directory
		 */
		IMAGES_OTHER_DIR = new File("public/images/other");
		IMAGES_INPUT_DIR = new File("public/images/input");
		IMAGES_OUTPUT_RESIZED_DIR = new File("public/images/output/resized");

		/**
		 * Partition Directory
		 */
		IMAGES_OUTPUT_PARTITION_DIR = new File("public/images/output/partition");
		TEXT_OUTPUT_PARTITION_DIR = new File("public/texts/output/partition");

		/**
		 * Global Difference Directory
		 */
		IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR = new File("public/images/output/globaldifference");
		TEXT_OUTPUT_GLOBALDIFFERENCE_DIR = new File("public/texts/output/globaldifference");

		/**
		 * Global Difference Binary Directory
		 */
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR = new File("public/images/output/globaldifferencebinary");

		/**
		 * Global Difference Binary RGB Directory
		 */
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR = new File("public/images/output/globaldifferencebinaryRGB");

		IMAGES_OTHER_DIR.mkdirs();
		IMAGES_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_OUTPUT_RESIZED_DIR.mkdirs();
		TEXT_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_INPUT_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR.mkdirs();
		TEXT_OUTPUT_GLOBALDIFFERENCE_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR.mkdirs();

		staticFiles.externalLocation("public");
		staticFiles.expireTime(600L);

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

		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;

		Tools.println("SERVER READY, it took " + elapsedSeconds + " seconds");
	}

	public static Connection getConnection() throws URISyntaxException, SQLException {
		String username, password, dbUrl;

		if (System.getenv("HEROKU_POSTGRESQL_BLUE_URL") != null) {

			/**
			 * If .env file is not provided, try to use a local psql database
			 * instead
			 */
			URI dbUri = new URI(System.getenv("HEROKU_POSTGRESQL_BLUE_URL"));
			username = dbUri.getUserInfo().split(":")[0];
			password = dbUri.getUserInfo().split(":")[1];
			dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
		} else {
			username = "postgres";
			password = "5771";
			dbUrl = "jdbc:postgresql://" + "localhost" + ":" + "5432" + "/" + username;
		}

		return DriverManager.getConnection(dbUrl, username, password);
	}

}
