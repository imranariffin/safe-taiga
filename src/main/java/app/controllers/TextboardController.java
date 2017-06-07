package app.controllers;

import static app.Application.DATA_SOURCE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.util.Path;
import app.util.ViewUtil;

import spark.Request;
import spark.Response;
import spark.Route;

public class TextboardController {

	/**
	 * GET HANDLERS
	 */
	public static Route serveTextboardHome = (Request request, Response response) -> {
		System.out.println("FROM:TextboardsController.java:START:serveTextboardHome");
		Map<String, Object> model = new HashMap<>();

		/**
		 * Get objects from database
		 */
		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfBoardsFromDatabase = new ArrayList<Map>();
		final String SCRIPT_SELECT_ALL_BOARD = "SELECT * FROM boards;";
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();

			// Create table if it does not exist
			System.out.println("Executing script:" + Path.StaticStrings.SCRIPT_CREATE_BOARDS);
			stmt.executeUpdate(Path.StaticStrings.SCRIPT_CREATE_BOARDS);

			// Select all the board in the table
			System.out.println("Executing script:" + SCRIPT_SELECT_ALL_BOARD);
			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_ALL_BOARD);

			// this is how you get a column given the colum name in string
			// rs.getString(columnLabel)
			while (rs.next()) {
				// Prepare the map for boardlink, boardname and boarddescription
				Map<String, String> board = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				board.put(Path.StaticStrings.BOARDNAME, rs.getString(Path.StaticStrings.BOARDNAME));
				board.put(Path.StaticStrings.BOARDLINK, rs.getString(Path.StaticStrings.BOARDLINK));
				board.put(Path.StaticStrings.BOARDDESCRIPTION, rs.getString(Path.StaticStrings.BOARDDESCRIPTION));

				// put board into the arrayOfBoardsFromDatabase
				arrayOfBoardsFromDatabase.add(board);
			}

			System.out.println("START:printing content of arrayOfBoardsFromDatabase:");
			for (int a = 0; a < arrayOfBoardsFromDatabase.size(); a++) {
				System.out.println(Path.StaticStrings.BOARDNAME + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Path.StaticStrings.BOARDNAME) + " "
						+ Path.StaticStrings.BOARDLINK + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Path.StaticStrings.BOARDLINK));
			}
			System.out.println("END:printing content of arrayOfBoardsFromDatabase");

		} catch (Exception e) {
			return ViewUtil.render(request, model, Path.Template.ERROR, Path.StaticStrings.ERROR, e.getMessage());
		}

		// Assign appropriate objects
		model.put("boardList", arrayOfBoardsFromDatabase);
		System.out.println("END:serveTextboardHome");
		return ViewUtil.render(request, model, Path.Template.TEXTBOARD, Path.Web.TEXTBOARD, "OK: default return");
	};

	public static Route serveTextboardBoard = (Request request, Response response) -> {
		System.out.println("FROM:TextboardController:serveTextboardBoard");
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardlink = request.params(Path.StaticStrings.BOARDLINK);
		// Put request parameters into the map
		model.put(Path.StaticStrings.BOARDLINK, boardlink);

		// Verify result
		System.out.println(Path.StaticStrings.BOARDLINK + ":" + boardlink);
		/**
		 * Get objects from database
		 */
		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfThreadsFromDatabase = new ArrayList<Map>();
		final String SCRIPT_SELECT_BOARD_THREAD = "SELECT * FROM threads AS thread WHERE thread.boardlink = '"
				+ boardlink + "';";
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();

			// Create table if it does not exist
			System.out.println("Executing script:" + Path.StaticStrings.SCRIPT_CREATE_THREADS);
			stmt.executeUpdate(Path.StaticStrings.SCRIPT_CREATE_THREADS);

			// Select all thread based on the given boardlink
			System.out.println("Executing script:" + SCRIPT_SELECT_BOARD_THREAD);
			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_BOARD_THREAD);
			// this is how you get a column given the colum name in string
			while (rs.next()) {
				// Prepare the map for threadid
				Map<String, String> thread = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				thread.put(Path.StaticStrings.THREADID, rs.getString(Path.StaticStrings.THREADID));
				// put board into the arrayOfThreadsFromDatabase
				arrayOfThreadsFromDatabase.add(thread);
			}

			System.out.println("START:printing content of arrayOfThreadsFromDatabase:");
			for (int a = 0; a < arrayOfThreadsFromDatabase.size(); a++) {
				System.out.println(Path.StaticStrings.THREADID + ":"
						+ arrayOfThreadsFromDatabase.get(a).get(Path.StaticStrings.THREADID));
			}
			System.out.println("END:printing content of arrayOfThreadsFromDatabase");

		} catch (Exception e) {
			return ViewUtil.render(request, model, Path.Template.ERROR, Path.StaticStrings.ERROR, e.getMessage());
		}

		// Assign appropriate objects
		model.put("threadList", arrayOfThreadsFromDatabase);

		System.out.println("END:serveTextboardBoard");
		return ViewUtil.render(request, model, Path.Template.TEXTBOARD_BOARD, Path.Web.TEXTBOARD_BOARD,
				"OK: default return");
	};

	public static Route serveTextboardBoardThread = (Request request, Response response) -> {
		System.out.println("FROM:TextboardController:serveTextboardBoardThread");
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardlink = request.params(Path.StaticStrings.BOARDLINK);
		String threadid = request.params(Path.StaticStrings.THREADID);

		// Put request parameters into the map
		model.put(Path.StaticStrings.BOARDLINK, boardlink);
		model.put(Path.StaticStrings.THREADID, threadid);

		// Verify result
		System.out.println(Path.StaticStrings.BOARDLINK + ":" + boardlink);
		System.out.println(Path.StaticStrings.THREADID + ":" + threadid);

		/**
		 * Get objects from database
		 */
		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfPostsFromDatabase = new ArrayList<Map>();
		final String SCRIPT_SELECT_BOARD_THREAD_POST = "SELECT * FROM posts AS post WHERE post.threadid ='" + threadid
				+ "';";
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();

			// Create table if it does not exist
			System.out.println("Executing script:" + Path.StaticStrings.SCRIPT_CREATE_POSTS);
			stmt.executeUpdate(Path.StaticStrings.SCRIPT_CREATE_POSTS);

			// Select all thread based on the given boardlink
			System.out.println("Executing script:" + SCRIPT_SELECT_BOARD_THREAD_POST);

			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_BOARD_THREAD_POST);
			// this is how you get a column given the colum name in string
			while (rs.next()) {
				// Prepare the map for threadid
				Map<String, String> post = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				post.put(Path.StaticStrings.POSTID, rs.getString(Path.StaticStrings.POSTID));
				post.put(Path.StaticStrings.POSTTEXT, rs.getString(Path.StaticStrings.POSTTEXT));

				// put board into the arrayOfPostsFromDatabase
				arrayOfPostsFromDatabase.add(post);
			}

			System.out.println("START:printing content of arrayOfPostsFromDatabase:");
			for (int a = 0; a < arrayOfPostsFromDatabase.size(); a++) {
				System.out.println(Path.StaticStrings.THREADID + ":"
						+ arrayOfPostsFromDatabase.get(a).get(Path.StaticStrings.THREADID) + " "
						+ Path.StaticStrings.POSTTEXT + ":"
						+ arrayOfPostsFromDatabase.get(a).get(Path.StaticStrings.POSTTEXT));
			}
			System.out.println("END:printing content of arrayOfPostsFromDatabase");

		} catch (Exception e) {
			return ViewUtil.render(request, model, Path.Template.ERROR, Path.StaticStrings.ERROR, e.getMessage());
		}

		// Assign appropriate objects
		model.put("postList", arrayOfPostsFromDatabase);

		System.out.println("END:serveTextboardBoardThread");
		return ViewUtil.render(request, model, Path.Template.TEXTBOARD_BOARD_THREAD, Path.Web.TEXTBOARD_BOARD_THREAD,
				"OK: default return");
	};

	/**
	 * POST HANDLERS
	 */

	public static Route handleCreateBoard = (Request request, Response response) -> {
		System.out.println("FROM:TextboardController:handleCreateBoard");
		Map<String, Object> model = new HashMap<>();

		String requestedBoardLink = request.queryParams("boardLinkText");
		String requestedBoardName = request.queryParams("boardNameText");
		String requestedBoardDescription = request.queryParams("boardDescriptionText");

		if (TextboardLogic.checkIfBoardIsAvailable(requestedBoardLink)) {
			System.out.println("The requested boardlink:" + requestedBoardLink + " is available!");

			try (Connection connection = DATA_SOURCE.getConnection()) {
				Statement stmt = connection.createStatement();

				// Execute insertion into database
				final String SCRIPT_INSERT_BOARD = "INSERT INTO boards (boardlink, boardname, boarddescription) VALUES ('"
						+ requestedBoardLink + "', '" + requestedBoardName + "', '" + requestedBoardDescription + "');";
				System.out.println("SCRIPT_INSERT_BOARD:" + SCRIPT_INSERT_BOARD);
				stmt.executeUpdate(SCRIPT_INSERT_BOARD);
			} catch (Exception e) {
				model.put("ERROR", "There was an error: " + e.getMessage());
				return ViewUtil.render(request, model, Path.Template.ERROR, Path.StaticStrings.ERROR, e.getMessage());
			}
		} else {
			System.out.println("The requested boardlink:" + requestedBoardLink + " is NOT available!");
		}

		/**
		 * COPIED FROM serveHomePage
		 */
		System.out.println("FROM:TextboardsController.java:START:serveTextboardHome");
		// Map<String, Object> model = new HashMap<>();

		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfBoardsFromDatabase = new ArrayList<Map>();
		final String SCRIPT_SELECT_ALL_BOARD = "SELECT * FROM boards;";
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();

			// create table if it does not exist
			System.out.println("Executing script:" + Path.StaticStrings.SCRIPT_CREATE_BOARDS);
			stmt.executeUpdate(Path.StaticStrings.SCRIPT_CREATE_BOARDS);

			// select all the boards
			System.out.println("Executing script:" + SCRIPT_SELECT_ALL_BOARD);
			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_ALL_BOARD);

			// this is how you get a column given the colum name in string
			// rs.getString(columnLabel)
			while (rs.next()) {
				// Prepare the map for boardlink, boardname and boarddescription
				Map<String, String> board = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				board.put(Path.StaticStrings.BOARDNAME, rs.getString(Path.StaticStrings.BOARDNAME));
				board.put(Path.StaticStrings.BOARDLINK, rs.getString(Path.StaticStrings.BOARDLINK));
				board.put(Path.StaticStrings.BOARDDESCRIPTION, rs.getString(Path.StaticStrings.BOARDDESCRIPTION));

				// put board into the arrayOfBoardsFromDatabase
				arrayOfBoardsFromDatabase.add(board);
			}

			System.out.println("START:printing content of output:");
			for (int a = 0; a < arrayOfBoardsFromDatabase.size(); a++) {
				System.out.println(Path.StaticStrings.BOARDNAME + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Path.StaticStrings.BOARDNAME) + " "
						+ Path.StaticStrings.BOARDLINK + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Path.StaticStrings.BOARDLINK));
			}
			System.out.println("END:printing content of output");

		} catch (Exception e) {
			return ViewUtil.render(request, model, Path.Template.ERROR, Path.StaticStrings.ERROR, e.getMessage());
		}

		// Assign appropriate objects
		model.put("boardList", arrayOfBoardsFromDatabase);
		System.out.println("END:serveTextboardHome");
		/**
		 * END OF COPY
		 */
		return ViewUtil.render(request, model, Path.Template.TEXTBOARD, Path.Web.TEXTBOARD,
				"OK: returned from a post call");
	};
}
