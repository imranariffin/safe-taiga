package Managers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import app.util.ImageProcessing;

public class ScriptManager {

	public final static String CREATE_BOARDS = "CREATE TABLE IF NOT EXISTS boards ( boardlink TEXT, boardname TEXT, boarddescription TEXT, PRIMARY KEY(boardlink));";
	public final static String CREATE_THREADS = "CREATE TABLE IF NOT EXISTS threads (threadid SERIAL, boardlink TEXT, threadtext TEXT, PRIMARY KEY (threadid), FOREIGN KEY (boardlink) REFERENCES boards(boardlink));";
	public final static String CREATE_POSTS = "CREATE TABLE IF NOT EXISTS posts (postid SERIAL, threadid INTEGER, posttext TEXT, PRIMARY KEY (postid), FOREIGN KEY (threadid) REFERENCES threads(threadid));";

	public final static String CREATE_IMAGEDB_ANIME_RGB = "CREATE TABLE IF NOT EXISTS imagedb_anime_rgb_integer (name TEXT, episode INT, panel INT, pixel_rgb real["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE
			+ "][3], PRIMARY KEY(name, episode, panel));";

	public final static String CREATE_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH = "CREATE TABLE IF NOT EXISTS imagedb_anime_basic_histogram_hash (name TEXT, episode INT, panel INT, hash TEXT, PRIMARY KEY(name, episode, panel));";

	public final static String CREATE_IMAGEDB_ANIME_RGB_FLOAT = "CREATE TABLE IF NOT EXISTS imagedb_anime_rgb_float (name TEXT, episode INT, panel INT, pixel_rgb real["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE
			+ "][3], PRIMARY KEY(name, episode, panel));";

	public final static String CREATE_IMAGEDB_USER_IMAGE_REQUEST = "CREATE TABLE IF NOT EXISTS imagedb_user_image_request (image_id SERIAL, request_ip TEXT, pixel_rgb real["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE + "][3], PRIMARY KEY(image_id));";
	public final static String CREATE_IMAGEDB_USER_IMAGE_REQUEST_FLOAT = "CREATE TABLE IF NOT EXISTS imagedb_user_image_request_float (image_id SERIAL, request_ip TEXT, pixel_rgb real["
			+ ImageProcessing.DIVISOR_VALUE + "][" + ImageProcessing.DIVISOR_VALUE + "][3], PRIMARY KEY(image_id));";
	public final static String CREATE_IMAGEDB_USER_IMAGE_REQUEST_BYTE = "CREATE TABLE IF NOT EXISTS imagedb_user_image_request_byte (image_id SERIAL, request_ip TEXT, imagefile bytea);";

	public final static String DROP_BOARDS = "DROP TABLE IF EXISTS boards;";
	public final static String DROP_THREADS = "DROP TABLE IF EXISTS threads;";
	public final static String DROP_POSTS = "DROP TABLE IF EXISTS posts;";

	public final static String DROP_IMAGEDB_ANIME_RGB_INTEGER = "DROP TABLE IF EXISTS imagedb_anime_rgb_integer;";
	public final static String DROP_IMAGEDB_ANIME_RGB_FLOAT = "DROP TABLE IF EXISTS imagedb_anime_rgb_float;";
	public final static String DROP_IMAGEDB_USER_IMAGE_REQUEST_INTEGER = "DROP TABLE IF EXISTS imagedb_user_image_request_integer;";
	public final static String DROP_IMAGEDB_USER_IMAGE_REQUEST_FLOAT = "DROP TABLE IF EXISTS imagedb_user_image_request_float;";
	public final static String DROP_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH = "DROP TABLE IF EXISTS imagedb_anime_basic_histogram_hash;";

	public final static String SELECT_ALL_FROM_BOARDS = "SELECT * FROM boards;";
	public final static String SELECT_ALL_FROM_THREADS = "SELECT * FROM threads;";
	public final static String SELCT_ALL_FROM_POSTS = "SELECT * FROM posts;";

	public final static String SELECT_ALL_FROM_IMAGEDB_INTEGER = "SELECT * FROM imagedb_anime_rgb_integer;";
	public final static String SELECT_ALL_FROM_IMAGEDB_USER_IMAGE_REQUEST_INTEGER = "SELECT * FROM imagedb_user_image_request_integer;";

	public final static String SELECT_ALL_FROM_IMAGEDB_FLOAT = "SELECT * FROM imagedb_anime_rgb_float;";
	public final static String SELECT_ALL_FROM_IMAGEDB_USER_IMAGE_REQUEST_FLOAT = "SELECT * FROM imagedb_user_image_request_float;";

	public final static String CLEAR_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH = "DELETE FROM imagedb_anime_basic_histogram_hash";

	public static String selectThreadFromThreadsGivenThreadid(String threadid) {
		return "SELECT * FROM threads WHERE threadid = '" + threadid + "' LIMIT 1;";
	}

	public static String selectAllPostFromPostsGivenThreadId(int threadid) {
		return "SELECT * FROM posts AS post WHERE post.threadid ='" + threadid + "';";
	}

	public static String selectAllPostFromPostsGivenThreadId(String threadid) {
		return selectAllPostFromPostsGivenThreadId(Integer.valueOf(threadid));
	}

	public static String selectAllThreadFromThreadsGivenBoardLink(String boardlink) {
		return "SELECT * FROM threads AS thread WHERE thread.boardlink = '" + boardlink + "';";
	}

	public static String insertBasicHistogramHash(String name, int episode, int panel, String hash) {
		String script = "INSERT INTO imagedb_anime_basic_histogram_hash (name, episode, panel, hash) VALUES ('" + name
				+ "','" + Integer.toString(episode) + "','" + Integer.toString(panel) + "','" + hash + "');";
		return script;
	}

	public static String insertIntoImagedbAnimeRgb(String name, int episode, int panel,
			int[][][] partitioningRGBArray) {
		String script = "INSERT INTO imagedb_anime_rgb (name, episode, panel, pixel_rgb) VALUES ('" + name + "','"
				+ Integer.toString(episode) + "', '" + Integer.toString(panel) + "', ";
		String RGBArray = convertTripleArrayToQueryString(partitioningRGBArray);
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

		selectString += " FROM imagedb_anime_rgb;";

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

		script += " FROM imagedb_anime_rgb;";
		return script;
	}

	public static String insertIntoImageDbUserImageRequest(String ipAddress, int[][][] partitioningRGBArray) {
		String script = "INSERT INTO imagedb_user_image_request (request_ip, pixel_rgb) VALUES ('" + ipAddress + "', "
				+ convertTripleArrayToQueryString(partitioningRGBArray) + ");";
		return script;
	}

	public static String findMatchingImageDataBruteForce(int[][][] partitioningRGBArray) {
		// sample
		// SELECT pixel_rgb[1][1][1] FROM imagedb_user_image_request WHERE
		// pixel_rgb[1][1][1] BETWEEN (100-25) AND (100+25) ORDER BY
		// pixel_rgb[1][1][1];

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb WHERE ";

		for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
			for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
				for (int c = 1; c <= 3; c++) {
					script += "(pixel_rgb[" + a + "][" + b + "][" + c + "] BETWEEN " + "("
							+ partitioningRGBArray[a - 1][b - 1][c - 1] + " - " + ImageProcessing.BUFFER_VALUE
							+ ") AND (" + partitioningRGBArray[a - 1][b - 1][c - 1] + " + "
							+ ImageProcessing.BUFFER_VALUE + "))";
					if (a == ImageProcessing.DIVISOR_VALUE && b == ImageProcessing.DIVISOR_VALUE && c == 3) {
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

	public static String findMatchingImageDataRandomized(int[][][] partitioningRGBArray) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb WHERE ";

		for (int a = 1; a <= ImageProcessing.TRIAL_VALUE; a++) {

			int x = ThreadLocalRandom.current().nextInt(0, ImageProcessing.DIVISOR_VALUE);
			int y = ThreadLocalRandom.current().nextInt(0, ImageProcessing.DIVISOR_VALUE);

			for (int c = 0; c < 3; c++) {
				script += "(pixel_rgb[" + (x + 1) + "][" + (y + 1) + "][" + (c + 1) + "] BETWEEN " + "("
						+ partitioningRGBArray[x][y][c] + " - " + ImageProcessing.BUFFER_VALUE + ") AND ("
						+ partitioningRGBArray[x][y][c] + " + " + ImageProcessing.BUFFER_VALUE + "))";
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

	public static String findMatchingImageDataRandomizedV2(int[][][] partitioningRGBArray) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb WHERE ";

		int x = ThreadLocalRandom.current().nextInt(0, (ImageProcessing.DIVISOR_VALUE - ImageProcessing.TRIAL_VALUE));
		int y = ThreadLocalRandom.current().nextInt(0, (ImageProcessing.DIVISOR_VALUE - ImageProcessing.TRIAL_VALUE));

		for (int a = 0; a < ImageProcessing.TRIAL_VALUE; a++) {
			for (int b = 0; b < ImageProcessing.TRIAL_VALUE; b++) {
				for (int c = 0; c < 3; c++) {
					script += "(pixel_rgb[" + (x + a + 1) + "][" + (y + b + 1) + "][" + (c + 1) + "] BETWEEN " + "("
							+ partitioningRGBArray[x + a][y + b][c] + " - " + ImageProcessing.BUFFER_VALUE + ") AND ("
							+ partitioningRGBArray[x + a][y + b][c] + " + " + ImageProcessing.BUFFER_VALUE + "))";
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

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb WHERE ";

		script += "(pixel_rgb[" + (x + 1) + "][" + (y + 1) + "][" + (z + 1) + "] BETWEEN " + "(" + value + " - "
				+ ImageProcessing.BUFFER_VALUE + ") AND (" + value + " + " + ImageProcessing.BUFFER_VALUE + "))";

		script += ";";
		return script;
	}

	public static String findMatchingImageDataIncrementalRGB(int x, int y, int[] array) {

		String script = "SELECT name, episode, panel FROM imagedb_anime_rgb WHERE ";

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
}
