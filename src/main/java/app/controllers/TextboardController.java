package app.controllers;

import static app.Application.DATA_SOURCE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.util.Reference;
import app.util.ViewUtil;
import app.util.Tools;

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
	public static Route serveTextboard_HOME = (Request request, Response response) -> {
		Tools.print("FROM:TextboardsController.java:START:serveTextboard_HOME");
		Map<String, Object> model = new HashMap<>();
		/**
		 * 1. Get the list of boards from database 2. Populate it into an
		 * arraylist 3. Put the arraylist to the VTL
		 */
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfBoardsFromDatabase = new ArrayList<Map>();
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();

			// If the table does not exist for whatever reason, create them
			Tools.print("Executing script:" + Reference.CommonStrings.SCRIPT_CREATE_BOARDS);
			stmt.executeUpdate(Reference.CommonStrings.SCRIPT_CREATE_BOARDS);

			// Just a simple SELECT ALL script
			Tools.print("Executing script:" + Reference.CommonStrings.SCRIPT_SELECT_BOARDS);
			ResultSet rs = stmt.executeQuery(Reference.CommonStrings.SCRIPT_SELECT_BOARDS);

			while (rs.next()) {
				Map<String, String> board = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				board.put(Reference.CommonStrings.BOARDNAME, rs.getString(Reference.CommonStrings.BOARDNAME));
				board.put(Reference.CommonStrings.BOARDLINK, rs.getString(Reference.CommonStrings.BOARDLINK));
				board.put(Reference.CommonStrings.BOARDDESCRIPTION,
						rs.getString(Reference.CommonStrings.BOARDDESCRIPTION));

				arrayOfBoardsFromDatabase.add(board);
			}

			Tools.print("START:printing content of arrayOfBoardsFromDatabase:");
			for (int a = 0; a < arrayOfBoardsFromDatabase.size(); a++) {
				Tools.print(Reference.CommonStrings.BOARDNAME + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Reference.CommonStrings.BOARDNAME) + " "
						+ Reference.CommonStrings.BOARDLINK + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Reference.CommonStrings.BOARDLINK));
			}
			Tools.print("END:printing content of arrayOfBoardsFromDatabase");

		} catch (Exception e) {
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.ROOTLINK,
					Reference.CommonStrings.ROOT);
		}

		// Populate with list of boards
		model.put(Reference.VTLStatics.BOARDLIST, arrayOfBoardsFromDatabase);

		// Populate html-form
		model.put(Reference.VTLStatics.INPUT_BOARDLINK, Reference.VTLStatics.INPUT_BOARDLINK);
		model.put(Reference.VTLStatics.INPUT_BOARDNAME, Reference.VTLStatics.INPUT_BOARDNAME);
		model.put(Reference.VTLStatics.INPUT_BOARDDESCRIPTION, Reference.VTLStatics.INPUT_BOARDDESCRIPTION);

		Tools.print("END:serveTextboard_HOME");
		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD, Reference.Web.TEXTBOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD
	 */
	public static Route serveTextboard_BOARD = (Request request, Response response) -> {
		Tools.print("FROM:TextboardController:START:serveTextboard_BOARD");
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardlink = request.params(Reference.CommonStrings.BOARDLINK);

		// Put request parameters into the map
		model.put(Reference.CommonStrings.BOARDLINK, boardlink);

		// Verify result
		Tools.print(Reference.CommonStrings.BOARDLINK + ":" + boardlink);

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
			Tools.print("Executing script:" + Reference.CommonStrings.SCRIPT_CREATE_THREADS);
			stmt.executeUpdate(Reference.CommonStrings.SCRIPT_CREATE_THREADS);

			// Select all thread based on the given boardlink
			Tools.print("Executing script:" + SCRIPT_SELECT_BOARD_THREAD);
			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_BOARD_THREAD);
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

			Tools.print("START:printing content of arrayOfThreadsFromDatabase:");
			for (int a = 0; a < arrayOfThreadsFromDatabase.size(); a++) {
				Tools.print(Reference.CommonStrings.THREADID + ":"
						+ arrayOfThreadsFromDatabase.get(a).get(Reference.CommonStrings.THREADID));
			}
			Tools.print("END:printing content of arrayOfThreadsFromDatabase");

		} catch (Exception e) {
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.TEXTBOARDLINK,
					Reference.CommonStrings.TEXTBOARD);
		}

		// Populate with list of threads
		model.put(Reference.VTLStatics.THREADLIST, arrayOfThreadsFromDatabase);

		// Populate html-forms
		model.put(Reference.VTLStatics.INPUT_THREADTEXT, Reference.VTLStatics.INPUT_THREADTEXT);
		Tools.print("END:serveTextboard_BOARD");
		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD_BOARD, Reference.Web.TEXTBOARD_BOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD_THREAD
	 */
	public static Route serveTextboard_THREAD = (Request request, Response response) -> {
		Tools.print("FROM:TextboardController:START:serveTextboard_THREAD");
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardlink = request.params(Reference.CommonStrings.BOARDLINK);
		String threadid = request.params(Reference.CommonStrings.THREADID);

		// Put request parameters into the map
		model.put(Reference.CommonStrings.BOARDLINK, boardlink);
		model.put(Reference.CommonStrings.THREADID, threadid);

		// Verify result
		Tools.print(Reference.CommonStrings.BOARDLINK + ":" + boardlink);
		Tools.print(Reference.CommonStrings.THREADID + ":" + threadid);

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
			Tools.print("Executing script:" + Reference.CommonStrings.SCRIPT_CREATE_POSTS);
			stmt.executeUpdate(Reference.CommonStrings.SCRIPT_CREATE_POSTS);

			// Get the threadtext from the database
			Tools.print("Executing script:" + Reference.CommonStrings.getSCRIPT_GET_THREADTEXT_BY_ID(threadid));
			ResultSet rs = stmt.executeQuery(Reference.CommonStrings.getSCRIPT_GET_THREADTEXT_BY_ID(threadid));
			rs.next();
			threadtext = rs.getString(Reference.CommonStrings.THREADTEXT);
			model.put(Reference.CommonStrings.THREADID, threadid);
			model.put(Reference.CommonStrings.THREADTEXT, threadtext);

			// Select all thread based on the given boardlink
			Tools.print("Executing script:" + SCRIPT_SELECT_BOARD_THREAD_POST);
			rs = stmt.executeQuery(SCRIPT_SELECT_BOARD_THREAD_POST);

			while (rs.next()) {
				Map<String, String> post = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				post.put(Reference.CommonStrings.POSTID, rs.getString(Reference.CommonStrings.POSTID));
				post.put(Reference.CommonStrings.POSTTEXT, rs.getString(Reference.CommonStrings.POSTTEXT));

				arrayOfPostsFromDatabase.add(post);
			}

			Tools.print("START:printing content of arrayOfPostsFromDatabase:");
			for (int a = 0; a < arrayOfPostsFromDatabase.size(); a++) {
				Tools.print(Reference.CommonStrings.THREADID + ":"
						+ arrayOfPostsFromDatabase.get(a).get(Reference.CommonStrings.THREADID) + " "
						+ Reference.CommonStrings.POSTTEXT + ":"
						+ arrayOfPostsFromDatabase.get(a).get(Reference.CommonStrings.POSTTEXT));
			}
			Tools.print("END:printing content of arrayOfPostsFromDatabase");

		} catch (Exception e) {
			return ViewUtil.renderErrorMessage(request, e.getMessage(),
					Reference.CommonStrings.getPREVIOUSBOARDLINK(boardlink),
					Reference.CommonStrings.TEXTBOARD + "/" + boardlink);
		}

		// Populate with list of posts
		model.put(Reference.VTLStatics.POSTLIST, arrayOfPostsFromDatabase);

		// Populate html-form
		model.put(Reference.VTLStatics.INPUT_POSTTEXT, Reference.VTLStatics.INPUT_POSTTEXT);

		Tools.print("END:serveTextboard_THREAD");
		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD_BOARD_THREAD,
				Reference.Web.TEXTBOARD_BOARD_THREAD, "OK: default return");
	};

	///////////////////
	// POST HANDLERS //
	///////////////////

	/**
	 * HANDLES CREATE BOARD POST METHOD
	 */
	public static Route handleCreateBoard = (Request request, Response response) -> {
		Tools.print("FROM:TextboardController:START:handleCreateBoard");
		Map<String, Object> model = new HashMap<>();

		String requestedBoardLink = request.queryParams(Reference.VTLStatics.INPUT_BOARDLINK);
		String requestedBoardName = request.queryParams(Reference.VTLStatics.INPUT_BOARDNAME);
		String requestedBoardDescription = request.queryParams(Reference.VTLStatics.INPUT_BOARDDESCRIPTION);

		if (TextboardLogic.checkIfBoardIsAvailable(requestedBoardLink)) {
			Tools.print("The requested boardlink:" + requestedBoardLink + " is available!");

			try (Connection connection = DATA_SOURCE.getConnection()) {
				Statement stmt = connection.createStatement();

				// Execute insertion into database
				final String SCRIPT_INSERT_BOARD = "INSERT INTO boards (boardlink, boardname, boarddescription) VALUES ('"
						+ requestedBoardLink + "', '" + requestedBoardName + "', '" + requestedBoardDescription + "');";
				Tools.print("SCRIPT_INSERT_BOARD:" + SCRIPT_INSERT_BOARD);
				stmt.executeUpdate(SCRIPT_INSERT_BOARD);
			} catch (Exception e) {
				return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.TEXTBOARDLINK,
						Reference.CommonStrings.TEXTBOARD);
			}
		} else {
			Tools.print("The requested boardlink:" + requestedBoardLink + " is NOT available!");
		}

		return serveTextboard_HOME.handle(request, response);
	};

	/**
	 * HANDLES CREATE BOARD THREAD
	 */
	public static Route handleCreateThread = (Request request, Response response) -> {
		Tools.print("FROM:TextboardController:START:handleCreateBoard");
		Map<String, Object> model = new HashMap<>();

		String requestedThreadText = request.queryParams(Reference.VTLStatics.INPUT_THREADTEXT);
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);

		// Verify retrieved data
		Tools.print(Reference.VTLStatics.INPUT_THREADTEXT + ":" + requestedThreadText);
		Tools.print(Reference.CommonStrings.BOARDLINK + ":" + currentBoard);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedThreadText)) {
			Tools.print("The requested thread with post:" + requestedThreadText + " is acceptable!");

			try (Connection connection = DATA_SOURCE.getConnection()) {
				Statement stmt = connection.createStatement();

				// Create threads table if not exist
				stmt.executeUpdate(Reference.CommonStrings.SCRIPT_CREATE_THREADS);
				Tools.print("Executing script:" + Reference.CommonStrings.SCRIPT_CREATE_THREADS);

				// Create a new thread instance in the threads table
				final String SCRIPT_INSERT_THREAD = "INSERT INTO threads (boardlink, threadtext) VALUES ('"
						+ currentBoard + "', '" + requestedThreadText + "');";
				Tools.print("Executing script:" + SCRIPT_INSERT_THREAD);
				stmt.executeUpdate(SCRIPT_INSERT_THREAD);

			} catch (Exception e) {
				return ViewUtil.renderErrorMessage(request, e.getMessage(),
						Reference.CommonStrings.getPREVIOUSBOARDLINK(currentBoard),
						Reference.CommonStrings.TEXTBOARD + "/" + currentBoard);
			}
		} else {
			Tools.print("The requested thread with post:" + requestedThreadText + " is NOT acceptable!");
		}

		return serveTextboard_BOARD.handle(request, response);
	};

	/**
	 * HANDLES CREATE BOARD THREAD POST
	 */
	public static Route handleCreatePost = (Request request, Response response) -> {
		Tools.print("FROM:TextboardController:handleCreateBoardPost");
		Map<String, Object> model = new HashMap<>();

		String requestedPostText = request.queryParams(Reference.VTLStatics.INPUT_POSTTEXT);
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);
		String currentThread = request.params(Reference.CommonStrings.THREADID);

		// Verify retrieved data
		Tools.print(Reference.VTLStatics.INPUT_POSTTEXT + ":" + requestedPostText);
		Tools.print(Reference.CommonStrings.BOARDLINK + ":" + currentBoard);
		Tools.print(Reference.CommonStrings.THREADID + ":" + currentThread);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedPostText)) {
			Tools.print("The requested thread with post:" + requestedPostText + " is acceptable!");

			try (Connection connection = DATA_SOURCE.getConnection()) {
				Statement stmt = connection.createStatement();

				// Create threads table if not exist
				stmt.executeUpdate(Reference.CommonStrings.SCRIPT_CREATE_THREADS);
				Tools.print("Executing script:" + Reference.CommonStrings.SCRIPT_CREATE_THREADS);

				// Create a new thread instance in the threads table
				final String SCRIPT_INSERT_POST = "INSERT INTO posts (threadid, posttext) VALUES ('" + currentThread
						+ "', '" + requestedPostText + "');";
				Tools.print("Executing script:" + SCRIPT_INSERT_POST);
				stmt.executeUpdate(SCRIPT_INSERT_POST);

			} catch (Exception e) {
				return ViewUtil.renderErrorMessage(request, e.getMessage(),
						Reference.CommonStrings.getPREVIOUSTHREAD(currentBoard, currentThread), currentThread);
			}
		} else {
			Tools.print("The requested thread with post:" + requestedPostText + " is NOT acceptable!");
		}

		return serveTextboard_THREAD.handle(request, response);
	};
}
