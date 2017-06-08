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

	//////////////////
	// GET HANDLERS //
	//////////////////

	/**
	 * SERVE TEXTBOARD
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
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Path.StaticStrings.ROOTLINK,
					Path.StaticStrings.ROOT);
		}

		// Populate with list of boards
		model.put(Path.VTLStatics.BOARDLIST, arrayOfBoardsFromDatabase);

		// Populate html-form
		model.put(Path.VTLStatics.INPUT_BOARDLINK, Path.VTLStatics.INPUT_BOARDLINK);
		model.put(Path.VTLStatics.INPUT_BOARDNAME, Path.VTLStatics.INPUT_BOARDNAME);
		model.put(Path.VTLStatics.INPUT_BOARDDESCRIPTION, Path.VTLStatics.INPUT_BOARDDESCRIPTION);

		System.out.println("END:serveTextboardHome");
		return ViewUtil.render(request, model, Path.Templates.TEXTBOARD, Path.Web.TEXTBOARD, "OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD
	 */
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
				thread.put(Path.StaticStrings.THREADTEXT, rs.getString(Path.StaticStrings.THREADTEXT));
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
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Path.StaticStrings.TEXTBOARDLINK,
					Path.StaticStrings.TEXTBOARD);
		}

		// Populate with list of threads
		model.put(Path.VTLStatics.THREADLIST, arrayOfThreadsFromDatabase);

		// Populate html-forms
		model.put(Path.VTLStatics.INPUT_THREADTEXT, Path.VTLStatics.INPUT_THREADTEXT);
		System.out.println("END:serveTextboardBoard");
		return ViewUtil.render(request, model, Path.Templates.TEXTBOARD_BOARD, Path.Web.TEXTBOARD_BOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD_THREAD
	 */
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
		String threadtext = "NULL_THREADTEXT_DOES_NOT_EXIST";
		final String SCRIPT_SELECT_BOARD_THREAD_POST = "SELECT * FROM posts AS post WHERE post.threadid ='" + threadid
				+ "';";
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();

			// Create table if it does not exist
			System.out.println("Executing script:" + Path.StaticStrings.SCRIPT_CREATE_POSTS);
			stmt.executeUpdate(Path.StaticStrings.SCRIPT_CREATE_POSTS);

			// Get the threadtext from the database
			System.out.println("Executing script:" + Path.StaticStrings.getSCRIPT_GET_THREADTEXT_BY_ID(threadid));
			ResultSet rs = stmt.executeQuery(Path.StaticStrings.getSCRIPT_GET_THREADTEXT_BY_ID(threadid));
			rs.next();
			threadtext = rs.getString(Path.StaticStrings.THREADTEXT);
			model.put(Path.StaticStrings.THREADID, threadid);
			model.put(Path.StaticStrings.THREADTEXT, threadtext);

			// Select all thread based on the given boardlink
			System.out.println("Executing script:" + SCRIPT_SELECT_BOARD_THREAD_POST);
			rs = stmt.executeQuery(SCRIPT_SELECT_BOARD_THREAD_POST);

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
			return ViewUtil.renderErrorMessage(request, e.getMessage(),
					Path.StaticStrings.getPREVIOUSBOARDLINK(boardlink), Path.StaticStrings.TEXTBOARD + "/" + boardlink);
		}

		// Populate with list of posts
		model.put(Path.VTLStatics.POSTLIST, arrayOfPostsFromDatabase);

		// Populate html-form
		model.put(Path.VTLStatics.INPUT_POSTTEXT, Path.VTLStatics.INPUT_POSTTEXT);

		System.out.println("END:serveTextboardBoardThread");
		return ViewUtil.render(request, model, Path.Templates.TEXTBOARD_BOARD_THREAD, Path.Web.TEXTBOARD_BOARD_THREAD,
				"OK: default return");
	};

	///////////////////
	// POST HANDLERS //
	///////////////////

	/**
	 * HANDLES CREATE BOARD POST METHOD
	 */
	public static Route handleCreateBoard = (Request request, Response response) -> {
		System.out.println("FROM:TextboardController:handleCreateBoard");
		Map<String, Object> model = new HashMap<>();

		String requestedBoardLink = request.queryParams(Path.VTLStatics.INPUT_BOARDLINK);
		String requestedBoardName = request.queryParams(Path.VTLStatics.INPUT_BOARDNAME);
		String requestedBoardDescription = request.queryParams(Path.VTLStatics.INPUT_BOARDDESCRIPTION);

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
				return ViewUtil.renderErrorMessage(request, e.getMessage(), Path.StaticStrings.TEXTBOARDLINK,
						Path.StaticStrings.TEXTBOARD);
			}
		} else {
			System.out.println("The requested boardlink:" + requestedBoardLink + " is NOT available!");
		}

		/**
		 * COPIED FROM serveTextboardHome
		 */
		System.out.println("FROM:TextboardsController.java:START:serveTextboardHome");

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
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Path.StaticStrings.ROOTLINK,
					Path.StaticStrings.ROOT);
		}

		// Populate with list of boards
		model.put(Path.VTLStatics.BOARDLIST, arrayOfBoardsFromDatabase);

		// Populate html-form
		model.put(Path.VTLStatics.INPUT_BOARDLINK, Path.VTLStatics.INPUT_BOARDLINK);
		model.put(Path.VTLStatics.INPUT_BOARDNAME, Path.VTLStatics.INPUT_BOARDNAME);
		model.put(Path.VTLStatics.INPUT_BOARDDESCRIPTION, Path.VTLStatics.INPUT_BOARDDESCRIPTION);

		System.out.println("END:serveTextboardHome");
		return ViewUtil.render(request, model, Path.Templates.TEXTBOARD, Path.Web.TEXTBOARD,
				"OK: returned from call to create board");
	};

	/**
	 * HANDLES CREATE THREAD POST METHOD
	 */
	public static Route handleCreateBoardThread = (Request request, Response response) -> {
		System.out.println("FROM:TextboardController:handleCreateBoard");
		Map<String, Object> model = new HashMap<>();

		String requestedThreadText = request.queryParams(Path.VTLStatics.INPUT_THREADTEXT);
		String currentBoard = request.params(Path.StaticStrings.BOARDLINK);

		// Verify retrieved data
		System.out.println(Path.VTLStatics.INPUT_THREADTEXT + ":" + requestedThreadText);
		System.out.println(Path.StaticStrings.BOARDLINK + ":" + currentBoard);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedThreadText)) {
			System.out.println("The requested thread with post:" + requestedThreadText + " is acceptable!");

			try (Connection connection = DATA_SOURCE.getConnection()) {
				Statement stmt = connection.createStatement();

				// Create threads table if not exist
				stmt.executeQuery(Path.StaticStrings.SCRIPT_CREATE_THREADS);

				// Create a new thread instance in the threads table
				final String SCRIPT_INSERT_THREAD = "INSERT INTO threads (boardlink, threadtext) VALUES ('"
						+ currentBoard + "', '" + requestedThreadText + "');";
				System.out.println("SCRIPT_INSERT_THREAD:" + SCRIPT_INSERT_THREAD);
				stmt.executeUpdate(SCRIPT_INSERT_THREAD);

			} catch (Exception e) {
				return ViewUtil.renderErrorMessage(request, e.getMessage(),
						Path.StaticStrings.getPREVIOUSBOARDLINK(currentBoard),
						Path.StaticStrings.TEXTBOARD + "/" + currentBoard);
			}
		} else {
			System.out.println("The requested thread with post:" + requestedThreadText + " is NOT acceptable!");
		}

		/**
		 * COPY from serveTextboardBoard
		 */
		System.out.println("FROM:TextboardController:serveTextboardBoard");

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
				thread.put(Path.StaticStrings.THREADTEXT, rs.getString(Path.StaticStrings.THREADTEXT));
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
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Path.StaticStrings.TEXTBOARDLINK,
					Path.StaticStrings.TEXTBOARD);
		}

		// Populate with list of threads
		model.put(Path.VTLStatics.THREADLIST, arrayOfThreadsFromDatabase);

		// Populate html-forms
		model.put(Path.VTLStatics.INPUT_THREADTEXT, Path.VTLStatics.INPUT_THREADTEXT);
		System.out.println("END:serveTextboardBoard");
		return ViewUtil.render(request, model, Path.Templates.TEXTBOARD_BOARD, Path.Web.TEXTBOARD_BOARD,
				"OK: returned from a post call of creating thread");
	};
}
