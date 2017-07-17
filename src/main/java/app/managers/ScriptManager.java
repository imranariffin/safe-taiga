package app.managers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import app.imageprocessing.ImageProcessing;
import app.util.Reference;
import app.util.Tools;
import app.util.ViewUtil;
import spark.Request;
import spark.Response;
import spark.Route;

public class ScriptManager {

	/**
	 * CREATE TEXTBOARD DATABASE
	 */
	private final static String CREATE_BOARDS = "CREATE TABLE IF NOT EXISTS boards ( boardlink TEXT, boardname TEXT, boarddescription TEXT, PRIMARY KEY(boardlink));";
	private final static String CREATE_THREADS = "CREATE TABLE IF NOT EXISTS threads (threadid SERIAL, boardlink TEXT, threadtext TEXT, PRIMARY KEY (threadid), FOREIGN KEY (boardlink) REFERENCES boards(boardlink));";
	private final static String CREATE_POSTS = "CREATE TABLE IF NOT EXISTS posts (postid SERIAL, threadid INTEGER, posttext TEXT, PRIMARY KEY (postid), FOREIGN KEY (threadid) REFERENCES threads(threadid));";

	/**
	 * CREATE IMAGE DATABASE
	 */
	private final static String CREATE_IMAGEDB_ANIME_RGB_INTEGER = "CREATE TABLE IF NOT EXISTS imagedb_anime_rgb_integer (name TEXT, episode INT, panel INT, pixel_rgb INT["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE
			+ "][3], PRIMARY KEY(name, episode, panel));";

	private final static String CREATE_IMAGEDB_ANIME_RGB_FLOAT = "CREATE TABLE IF NOT EXISTS imagedb_anime_rgb_float (name TEXT, episode INT, panel INT, pixel_rgb real["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE
			+ "][3], PRIMARY KEY(name, episode, panel));";

	private final static String CREATE_IMAGEDB_USER_IMAGE_REQUEST = "CREATE TABLE IF NOT EXISTS imagedb_user_image_request (image_id SERIAL, request_ip TEXT, pixel_rgb real["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE + "][3], PRIMARY KEY(image_id));";

	private final static String CREATE_IMAGEDB_USER_IMAGE_REQUEST_FLOAT = "CREATE TABLE IF NOT EXISTS imagedb_user_image_request_float (image_id SERIAL, request_ip TEXT, pixel_rgb real["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE + "][3], PRIMARY KEY(image_id));";

	private final static String CREATE_IMAGEDB_USER_IMAGE_REQUEST_BYTE = "CREATE TABLE IF NOT EXISTS imagedb_user_image_request_byte (image_id SERIAL, request_ip TEXT, imagefile bytea);";
	private final static String CREATE_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH = "CREATE TABLE IF NOT EXISTS imagedb_anime_basic_histogram_hash (name TEXT, episode INT, panel INT, hash TEXT, PRIMARY KEY(name, episode, panel));";

	/**
	 * DROP TEXTBOARD DATABASE
	 */
	private final static String DROP_BOARDS = "DROP TABLE IF EXISTS boards;";
	private final static String DROP_THREADS = "DROP TABLE IF EXISTS threads;";
	private final static String DROP_POSTS = "DROP TABLE IF EXISTS posts;";

	/**
	 * DROP IMAGE DATABASE
	 */
	private final static String DROP_IMAGEDB_ANIME_RGB_INTEGER = "DROP TABLE IF EXISTS imagedb_anime_rgb_integer;";
	private final static String DROP_IMAGEDB_ANIME_RGB_FLOAT = "DROP TABLE IF EXISTS imagedb_anime_rgb_float;";
	private final static String DROP_IMAGEDB_USER_IMAGE_REQUEST_INTEGER = "DROP TABLE IF EXISTS imagedb_user_image_request_integer;";
	private final static String DROP_IMAGEDB_USER_IMAGE_REQUEST_FLOAT = "DROP TABLE IF EXISTS imagedb_user_image_request_float;";
	private final static String DROP_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH = "DROP TABLE IF EXISTS imagedb_anime_basic_histogram_hash;";

	private final static String SELECT_ALL_FROM_BOARDS = "SELECT * FROM boards;";
	private final static String SELECT_ALL_FROM_THREADS = "SELECT * FROM threads;";
	private final static String SELCT_ALL_FROM_POSTS = "SELECT * FROM posts;";

	private final static String SELECT_ALL_FROM_IMAGEDB_INTEGER = "SELECT * FROM imagedb_anime_rgb_integer;";
	private final static String SELECT_ALL_FROM_IMAGEDB_USER_IMAGE_REQUEST_INTEGER = "SELECT * FROM imagedb_user_image_request_integer;";

	private final static String SELECT_ALL_FROM_IMAGEDB_FLOAT = "SELECT * FROM imagedb_anime_rgb_float;";
	private final static String SELECT_ALL_FROM_IMAGEDB_USER_IMAGE_REQUEST_FLOAT = "SELECT * FROM imagedb_user_image_request_float;";

	private final static String CLEAR_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH = "DELETE FROM imagedb_anime_basic_histogram_hash";

	public static String insertBasicHistogramHash(String name, int episode, int panel, String hash) {
		String script = "INSERT INTO imagedb_anime_basic_histogram_hash (name, episode, panel, hash) VALUES ('" + name
				+ "','" + Integer.toString(episode) + "','" + Integer.toString(panel) + "','" + hash + "');";
		return script;
	}

	public static String insertIntoImagedbAnimeRgbInteger(String name, int episode, int panel, int[][][] tripleArray) {
		String script = "INSERT INTO imagedb_anime_rgb_integer (name, episode, panel, pixel_rgb) VALUES ('" + name
				+ "','" + Integer.toString(episode) + "', '" + Integer.toString(panel) + "', ";
		String RGBArray = convertTripleArrayToQueryString(tripleArray);
		script += RGBArray + ");";
		return script;
	}

	public static String selectAverageOfImageDb() {

		String selectString = "SELECT ";
		for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
			for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
				for (int c = 1; c <= 3; c++) {
					selectString += "AVG(pixel_rgb[" + a + "][ " + b + "][" + c + "]) AS \"" + a + ":" + b + ":" + c
							+ "\"";
					if (a == ImageProcessing.DIVISOR_VALUE && b == ImageProcessing.DIVISOR_VALUE && c == 3) {
						selectString += "";
					} else {
						selectString += ", ";
					}
				}
			}
		}

		selectString += " FROM imagedb_anime_rgb_integer;";

		return selectString;
	}

	public static String getMinMaxOfImageDb() {

		String script = "SELECT ";
		for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
			for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
				for (int c = 1; c <= 3; c++) {
					script += "MIN(pixel_rgb[" + a + "][" + b + "][" + c + "]) AS \"MIN:" + a + ":" + b + ":" + c
							+ "\", ";
					script += "MAX(pixel_rgb[" + a + "][" + b + "][" + c + "]) AS \"MAX:" + a + ":" + b + ":" + c
							+ "\"";
					if (a == ImageProcessing.DIVISOR_VALUE && b == ImageProcessing.DIVISOR_VALUE && c == 3) {
						script += "";
					} else {
						script += ", ";
					}
				}
			}
		}

		script += " FROM imagedb_anime_rgb_integer;";
		return script;
	}

	public static String insertIntoImageDbUserImageRequest(String ipAddress, int[][][] tripleArray) {
		String script = "INSERT INTO imagedb_user_image_request (request_ip, pixel_rgb) VALUES ('" + ipAddress + "', "
				+ convertTripleArrayToQueryString(tripleArray) + ");";
		return script;
	}

	public static String findMatchingImageDataBruteForce(int[][][] tripleArray) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb_integer WHERE ";

		for (int a = 2; a <= (ImageProcessing.DIVISOR_VALUE - 1); a++) { // Y-axis
			for (int b = 2; b <= (ImageProcessing.DIVISOR_VALUE - 1); b++) { // X-axis
				for (int c = 1; c <= 3; c++) {
					script += "(pixel_rgb[" + a + "][" + b + "][" + c + "] BETWEEN " + "("
							+ tripleArray[a - 1][b - 1][c - 1] + " - " + ImageProcessing.BUFFER_VALUE + ") AND ("
							+ tripleArray[a - 1][b - 1][c - 1] + " + " + ImageProcessing.BUFFER_VALUE + "))";
					if (a == (ImageProcessing.DIVISOR_VALUE - 1) && b == (ImageProcessing.DIVISOR_VALUE - 1)
							&& c == 3) {
						script += "";
					} else {
						script += "AND ";
					}
				}
			}
		}

		script += ";";
		return script;
	}

	public static String findMatchingImageDataRandomized(int[][][] tripleArray) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb_integer WHERE ";

		for (int a = 1; a <= ImageProcessing.TRIAL_VALUE; a++) {

			int x = ThreadLocalRandom.current().nextInt(0, ImageProcessing.DIVISOR_VALUE);
			int y = ThreadLocalRandom.current().nextInt(0, ImageProcessing.DIVISOR_VALUE);

			for (int c = 0; c < 3; c++) {
				script += "(pixel_rgb[" + (x + 1) + "][" + (y + 1) + "][" + (c + 1) + "] BETWEEN " + "("
						+ tripleArray[x][y][c] + " - " + ImageProcessing.BUFFER_VALUE + ") AND (" + tripleArray[x][y][c]
						+ " + " + ImageProcessing.BUFFER_VALUE + "))";
				if (a == ImageProcessing.TRIAL_VALUE && c == 2) {
					script += "";
				} else {
					script += "AND ";
				}

			}
		}

		script += ";";
		return script;
	}

	public static String findMatchingImageDataRandomizedV2(int[][][] tripleArray) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb_integer WHERE ";

		int x = ThreadLocalRandom.current().nextInt(0, (ImageProcessing.DIVISOR_VALUE - ImageProcessing.TRIAL_VALUE));
		int y = ThreadLocalRandom.current().nextInt(0, (ImageProcessing.DIVISOR_VALUE - ImageProcessing.TRIAL_VALUE));

		for (int a = 0; a < ImageProcessing.TRIAL_VALUE; a++) {
			for (int b = 0; b < ImageProcessing.TRIAL_VALUE; b++) {
				for (int c = 0; c < 3; c++) {
					script += "(pixel_rgb[" + (x + a + 1) + "][" + (y + b + 1) + "][" + (c + 1) + "] BETWEEN " + "("
							+ tripleArray[x + a][y + b][c] + " - " + ImageProcessing.BUFFER_VALUE + ") AND ("
							+ tripleArray[x + a][y + b][c] + " + " + ImageProcessing.BUFFER_VALUE + "))";
					if (a == (ImageProcessing.TRIAL_VALUE - 1) && b == (ImageProcessing.TRIAL_VALUE - 1) && c == 2) {
						script += "";
					} else {
						script += "AND ";
					}

				}
			}
		}

		script += ";";
		return script;
	}

	public static String findMatchingImageDataIncremental(int x, int y, int z, int value) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb_integer WHERE ";

		script += "(pixel_rgb[" + (x + 1) + "][" + (y + 1) + "][" + (z + 1) + "] BETWEEN " + "(" + value + " - "
				+ ImageProcessing.BUFFER_VALUE + ") AND (" + value + " + " + ImageProcessing.BUFFER_VALUE + "))";

		script += ";";
		return script;
	}

	public static String findMatchingImageDataIncrementalRGB(int x, int y, int[] array) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb_integer WHERE ";

		for (int a = 0; a < 3; a++) {
			script += "(pixel_rgb[" + (x + 1) + "][" + (y + 1) + "][" + (a + 1) + "] BETWEEN " + "(" + array[a] + " - "
					+ ImageProcessing.BUFFER_VALUE + ") AND (" + array[a] + " + " + ImageProcessing.BUFFER_VALUE + "))";
			if (a < 2) {
				script += " AND ";
			}
		}
		script += ";";
		return script;
	}

	public static String convertTripleArrayToQueryString(int[][][] tripleArray) {
		/**
		 * create string for the RGBs
		 */

		String script = "'{";
		for (int a = 0; a < tripleArray.length; a++) { // y-axis
			script += "{";
			for (int b = 0; b < tripleArray[a].length; b++) { // x-axis
				script += "{";
				for (int c = 0; c < tripleArray[a][b].length; c++) {
					script += tripleArray[a][b][c];
					if (c < (tripleArray[a][b].length - 1)) {
						script += ",";
					}
				}
				if (b < (tripleArray[a].length - 1)) {
					script += "},";
				} else {
					script += "}";
				}
			}
			if (a < (tripleArray.length - 1)) {
				script += "},";
			} else {
				script += "}";
			}
		}
		script += "}'";
		return script;
	}

	public static void createThread(String currentBoard, String requestedThreadText)
			throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		String script = "INSERT INTO threads (boardlink, threadtext) VALUES ( ?, ?);";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setString(1, currentBoard);
		pstmt.setString(2, requestedThreadText);

		pstmt.executeUpdate();

		pstmt.close();
	}

	public static void createBoard(String boardlink, String boardname, String boarddescription)
			throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		String script = "INSERT INTO boards (boardlink, boardname, boarddescription) VALUES (?,?,?);";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setString(1, boardlink);
		pstmt.setString(2, boardname);
		pstmt.setString(3, boarddescription);

		pstmt.executeUpdate();

		pstmt.close();
	}

	public static String selectThreadFromThreadsGivenThreadid(String threadid) {
		return "SELECT * FROM threads WHERE threadid = '" + threadid + "' LIMIT 1;";
	}

	public static void getPostsGivenThreadId(String threadid, Map<String, Object> model)
			throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		String script = "SELECT threadtext FROM threads WHERE threadid = ? LIMIT 1;";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setString(1, threadid);

		ResultSet rs = pstmt.executeQuery();

		rs.next();

		String threadtext = rs.getString(Reference.CommonStrings.THREADTEXT);

		model.put(Reference.CommonStrings.THREADID, threadid);
		model.put(Reference.CommonStrings.THREADTEXT, threadtext);

		rs = pstmt.executeQuery();

		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfPostsFromDatabase = new ArrayList<Map>();

		while (rs.next()) {
			Map<String, String> post = new HashMap<String, String>();

			// populate board with the appropriate description of a board
			post.put(Reference.CommonStrings.POSTID, rs.getString(Reference.CommonStrings.POSTID));
			post.put(Reference.CommonStrings.POSTTEXT, rs.getString(Reference.CommonStrings.POSTTEXT));

			arrayOfPostsFromDatabase.add(post);
		}

		pstmt.close();
	}

	public static void selectAllPostFromPostsGivenThreadId(int threadId, Map<String, Object> model)
			throws SQLException, URISyntaxException {
		selectAllPostFromPostsGivenThreadId(Integer.toString(threadId), model);
	}

	public static void selectAllPostFromPostsGivenThreadId(String threadId, Map<String, Object> model)
			throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		String script = "SELECT * FROM posts AS post WHERE post.threadid =?;";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setInt(1, Integer.valueOf(threadId));

		ResultSet rs = pstmt.executeQuery();

		rs.next();

		String threadtext = rs.getString(Reference.CommonStrings.THREADTEXT);

		model.put(Reference.CommonStrings.THREADID, threadId);
		model.put(Reference.CommonStrings.THREADTEXT, threadtext);

		rs = pstmt.executeQuery();

		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfPostsFromDatabase = new ArrayList<Map>();

		while (rs.next()) {
			Map<String, String> post = new HashMap<String, String>();

			// populate board with the appropriate description of a board
			post.put(Reference.CommonStrings.POSTID, rs.getString(Reference.CommonStrings.POSTID));
			post.put(Reference.CommonStrings.POSTTEXT, rs.getString(Reference.CommonStrings.POSTTEXT));

			arrayOfPostsFromDatabase.add(post);
		}
	}

	public static void getAllBoards(Map<String, Object> model) throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfBoardsFromDatabase = new ArrayList<Map>();

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(ScriptManager.SELECT_ALL_FROM_BOARDS);

		while (rs.next()) {
			Map<String, String> board = new HashMap<String, String>();

			// populate board with the appropriate description of a board
			board.put(Reference.CommonStrings.BOARDNAME, rs.getString(Reference.CommonStrings.BOARDNAME));
			board.put(Reference.CommonStrings.BOARDLINK, rs.getString(Reference.CommonStrings.BOARDLINK));
			board.put(Reference.CommonStrings.BOARDDESCRIPTION, rs.getString(Reference.CommonStrings.BOARDDESCRIPTION));

			arrayOfBoardsFromDatabase.add(board);
		}

		// Populate with list of boards
		model.put(Reference.VTL.BOARDLIST, arrayOfBoardsFromDatabase);

		stmt.close();
	}

	public static String selectAllThreadFromThreadsGivenBoardLink(String boardLink) {
		return "SELECT * FROM threads AS thread WHERE thread.boardlink = '" + boardLink + "';";
	}

	public static void getThreadsGivenBoardLink(String boardLink, Map<String, Object> model)
			throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfThreadsFromDatabase = new ArrayList<Map>();

		// Select all thread based on the given boardlink
		String script = "SELECT * FROM threads AS thread WHERE thread.boardlink = ?;";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setString(1, boardLink);
		ResultSet rs = pstmt.executeQuery();
		// this is how you get a column given the colum name in string
		while (rs.next()) {
			// Prepare the map for threadid
			Map<String, String> thread = new HashMap<String, String>();

			// populate board with the appropriate description of a board
			thread.put(Reference.CommonStrings.THREADID, rs.getString(Reference.CommonStrings.THREADID));
			thread.put(Reference.CommonStrings.THREADTEXT, rs.getString(Reference.CommonStrings.THREADTEXT));
			// put board into the arrayOfThreadsFromDatabase
			arrayOfThreadsFromDatabase.add(thread);
		}

		// Populate with list of threads
		model.put(Reference.VTL.THREADLIST, arrayOfThreadsFromDatabase);

		pstmt.close();
	}

	public static void createPost(String threadId, String postText) throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		// Create a new thread instance in the threads table
		String script = "INSERT INTO posts (threadid, posttext) VALUES (?, ?);";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setString(1, threadId);
		pstmt.setString(2, postText);

		pstmt.executeUpdate();

		pstmt.close();
	}
}
