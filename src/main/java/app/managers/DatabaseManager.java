package app.managers;

import java.net.URISyntaxException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.util.Reference;
import app.util.Tools;
import processing.ImageProcessing;
import app.util.ScriptCreator;

public class DatabaseManager {

	/**
	 * CREATE TEXTBOARD DATABASE
	 */
	public static void createTableBoards() throws SQLException, URISyntaxException {
		Tools.println("FROM:DatabaseManager:START:createTableBoards");
		Connection connection = app.Application.getConnection();

		String script = "CREATE TABLE IF NOT EXISTS boards ( boardlink TEXT, boardname TEXT, boarddescription TEXT, PRIMARY KEY(boardlink));";
		Statement stmt = connection.createStatement();

		stmt.executeUpdate(script);

		stmt.close();
	}

	public static void createTableThreads() throws SQLException, URISyntaxException {
		Tools.println("FROM:DatabaseManager:START:createTableThreads");
		Connection connection = app.Application.getConnection();

		String script = "CREATE TABLE IF NOT EXISTS threads (threadid SERIAL, boardlink TEXT, threadtext TEXT, PRIMARY KEY (threadid), FOREIGN KEY (boardlink) REFERENCES boards(boardlink));";
		Statement stmt = connection.createStatement();

		stmt.executeUpdate(script);

		stmt.close();
	}

	public static void createTablePosts() throws SQLException, URISyntaxException {
		Tools.println("FROM:DatabaseManager:START:createTablePosts");
		Connection connection = app.Application.getConnection();

		String script = "CREATE TABLE IF NOT EXISTS posts (postid SERIAL, threadid INTEGER, posttext TEXT, PRIMARY KEY (postid), FOREIGN KEY (threadid) REFERENCES threads(threadid));";
		Statement stmt = connection.createStatement();

		stmt.executeUpdate(script);

		stmt.close();
	}

	public static void createBoard(String boardLink, String boardName, String boardDescription)
			throws SQLException, URISyntaxException {
		Tools.println("FROM:DatabaseManager:START:createBoard");
		Connection connection = app.Application.getConnection();

		String script = "INSERT INTO boards (boardlink, boardname, boarddescription) VALUES (?,?,?);";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setString(1, boardLink);
		pstmt.setString(2, boardName);
		pstmt.setString(3, boardDescription);

		pstmt.executeUpdate();

		pstmt.close();
	}

	public static void createThread(String currentBoard, String requestedThreadText)
			throws SQLException, URISyntaxException {
		Tools.println("FROM:DatabaseManager:START:createThread");
		Connection connection = app.Application.getConnection();

		String script = "INSERT INTO threads (boardlink, threadtext) VALUES ( ?, ?);";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setString(1, currentBoard);
		pstmt.setString(2, requestedThreadText);

		pstmt.executeUpdate();

		pstmt.close();
	}

	public static void createPost(String threadId, String postText) throws SQLException, URISyntaxException {
		Tools.println("FROM:DatabaseManager:START:createPost");
		Connection connection = app.Application.getConnection();

		String script = "INSERT INTO posts (threadid, posttext) VALUES (?, ?);";
		PreparedStatement pstmt = connection.prepareStatement(script);

		pstmt.setInt(1, Integer.valueOf(threadId));
		pstmt.setString(2, postText);

		pstmt.executeUpdate();

		pstmt.close();
	}

	public static void getPostsGivenThreadId(String threadId, Map<String, Object> model)
			throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		String scriptOpText = "SELECT threadtext FROM threads WHERE threadid = ? LIMIT 1;";
		PreparedStatement pstmt = connection.prepareStatement(scriptOpText);

		pstmt.setInt(1, Integer.valueOf(threadId));

		ResultSet rs = pstmt.executeQuery();

		rs.next();

		String threadText = rs.getString(Reference.CommonStrings.THREADTEXT);

		model.put(Reference.CommonStrings.THREADID, threadId);
		model.put(Reference.CommonStrings.THREADTEXT, threadText);

		String scriptThreadPosts = "SELECT * FROM posts WHERE threadid = ?;";
		pstmt = connection.prepareStatement(scriptThreadPosts);

		pstmt.setInt(1, Integer.valueOf(threadId));

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

		// Populate with list of posts
		model.put(Reference.VTL.POSTLIST, arrayOfPostsFromDatabase);

		pstmt.close();
	}

	public static void getAllBoards(Map<String, Object> model) throws SQLException, URISyntaxException {
		Connection connection = app.Application.getConnection();

		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfBoardsFromDatabase = new ArrayList<Map>();

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(ScriptCreator.SELECT_ALL_FROM_BOARDS);

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

	public static void getImageDbAverageRGB() {
		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(ScriptCreator.selectAverageOfImageDb());

			rs.next();
			String averageOfRGB = "";
			String result = "";
			for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 1; c <= 3; c++) {
						result = "{" + rs.getString("" + a + ":" + b + ":" + c) + "}";
						averageOfRGB += result;
						Tools.print(result);
					}
					averageOfRGB += System.lineSeparator();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void getImageDbMinMax() {
		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(ScriptCreator.getMinMaxOfImageDb());

			rs.next();
			String minMaxOfRGB = "";
			String result = "";
			for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 1; c <= 3; c++) {
						result = "{" + rs.getString("MIN:" + a + ":" + b + ":" + c) + ","
								+ rs.getString("MAX:" + a + ":" + b + ":" + c) + "}";
						minMaxOfRGB += result;
						Tools.print(result);
					}
					minMaxOfRGB += System.lineSeparator();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void insertPartitionHash(String animeName, int episode, int frame, int[] partitionHash) {
		try (Connection connection = app.Application.getConnection()) {

			String query = "INSERT INTO partition_hash (name, episode, frame, hash_red, hash_green, hash_blue) VALUES (?,?,?,?,?,?);";
			PreparedStatement pstmt = connection.prepareStatement(query);

			pstmt.setString(1, animeName);
			pstmt.setInt(2, episode);
			pstmt.setInt(3, frame);
			pstmt.setInt(4, partitionHash[0]);
			pstmt.setInt(5, partitionHash[1]);
			pstmt.setInt(6, partitionHash[2]);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			Tools.print("duplicate key ");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void partitionSearch(ArrayList<Integer> partitionHash, Map<String, Object> model) {
		try (Connection connection = app.Application.getConnection()) {

			String query = "SELECT * FROM partition_hash WHERE hash_red = ? AND hash_green = ? AND hash_blue = ?;";
			PreparedStatement pstmt = connection.prepareStatement(query);

			pstmt.setInt(1, partitionHash.get(0));
			pstmt.setInt(2, partitionHash.get(1));
			pstmt.setInt(3, partitionHash.get(2));

			ResultSet rs = pstmt.executeQuery();

			ArrayList<String> partitionHashResult = new ArrayList<String>();
			while (rs.next()) {
				partitionHashResult
						.add(rs.getString("name") + " " + rs.getString("episode") + " " + rs.getString("frame") + " "
								+ rs.getInt("hash_red") + " " + rs.getInt("hash_green") + " " + rs.getInt("hash_blue"));
			}

			model.put("partitionHashResult", partitionHashResult);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
