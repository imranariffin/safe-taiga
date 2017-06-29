package app.controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.util.Reference;
import app.util.ScriptCreator;
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
		Tools.println("\nFROM:TextboardsController:START:serveTextboard_HOME");
		Map<String, Object> model = new HashMap<>();
		/**
		 * 1. Get the list of boards from database 2. Populate it into an
		 * arraylist 3. Put the arraylist to the VTL
		 */
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfBoardsFromDatabase = new ArrayList<Map>();
		try (Connection connection = app.Application.getConnection()) {

			Statement stmt = connection.createStatement();

			// If the table does not exist for whatever reason, create them
			Tools.println("Executing script:" + ScriptCreator.CREATE_BOARDS);
			stmt.executeUpdate(ScriptCreator.CREATE_BOARDS);

			// Just a simple SELECT ALL script
			Tools.println("Executing script:" + ScriptCreator.SELECT_ALL_FROM_BOARDS);
			ResultSet rs = stmt.executeQuery(ScriptCreator.SELECT_ALL_FROM_BOARDS);

			while (rs.next()) {
				Map<String, String> board = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				board.put(Reference.CommonStrings.BOARDNAME, rs.getString(Reference.CommonStrings.BOARDNAME));
				board.put(Reference.CommonStrings.BOARDLINK, rs.getString(Reference.CommonStrings.BOARDLINK));
				board.put(Reference.CommonStrings.BOARDDESCRIPTION,
						rs.getString(Reference.CommonStrings.BOARDDESCRIPTION));

				arrayOfBoardsFromDatabase.add(board);
			}

			Tools.println("START:printing content of arrayOfBoardsFromDatabase:");
			for (int a = 0; a < arrayOfBoardsFromDatabase.size(); a++) {
				Tools.println(Reference.CommonStrings.BOARDNAME + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Reference.CommonStrings.BOARDNAME) + " "
						+ Reference.CommonStrings.BOARDLINK + ":"
						+ arrayOfBoardsFromDatabase.get(a).get(Reference.CommonStrings.BOARDLINK));
			}
			Tools.println("END:printing content of arrayOfBoardsFromDatabase");

		} catch (Exception e) {
			e.printStackTrace();
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_ROOT,
					Reference.CommonStrings.NAME_ROOT);
		}

		// Populate with list of boards
		model.put(Reference.VTL.BOARDLIST, arrayOfBoardsFromDatabase);

		// Populate html-form
		model.put(Reference.VTL.INPUT_BOARDLINK, Reference.VTL.INPUT_BOARDLINK);
		model.put(Reference.VTL.INPUT_BOARDNAME, Reference.VTL.INPUT_BOARDNAME);
		model.put(Reference.VTL.INPUT_BOARDDESCRIPTION, Reference.VTL.INPUT_BOARDDESCRIPTION);

		Tools.println("END:serveTextboard_HOME\n");
		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD, Reference.Web.TEXTBOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD
	 */
	public static Route serveTextboard_BOARD = (Request request, Response response) -> {
		Tools.println("FROM:TextboardController:START:serveTextboard_BOARD");
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardlink = request.params(Reference.CommonStrings.BOARDLINK);

		// Put request parameters into the map
		model.put(Reference.CommonStrings.BOARDLINK, boardlink);

		// Verify result
		Tools.println(Reference.CommonStrings.BOARDLINK + ":" + boardlink);

		/**
		 * Get objects from database
		 */
		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfThreadsFromDatabase = new ArrayList<Map>();

		try (Connection connection = app.Application.getConnection()) {

			Statement stmt = connection.createStatement();

			/**
			 * Create threads if not exist
			 */
			Tools.println("Executing script:" + ScriptCreator.CREATE_THREADS);
			stmt.executeUpdate(ScriptCreator.CREATE_THREADS);

			// Select all thread based on the given boardlink
			String selectAllThreadFromBoardGivenBoardlink = ScriptCreator
					.selectAllThreadFromThreadsGivenBoardLink(boardlink);
			Tools.println("Executing script:" + selectAllThreadFromBoardGivenBoardlink);
			ResultSet rs = stmt.executeQuery(selectAllThreadFromBoardGivenBoardlink);
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

			Tools.println("START:printing content of arrayOfThreadsFromDatabase:");
			for (int a = 0; a < arrayOfThreadsFromDatabase.size(); a++) {
				Tools.println(Reference.CommonStrings.THREADID + ":"
						+ arrayOfThreadsFromDatabase.get(a).get(Reference.CommonStrings.THREADID));
			}
			Tools.println("END:printing content of arrayOfThreadsFromDatabase");

		} catch (Exception e) {
			e.printStackTrace();
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_TEXTBOARD,
					Reference.CommonStrings.NAME_TEXTBOARD);
		}

		// Populate with list of threads
		model.put(Reference.VTL.THREADLIST, arrayOfThreadsFromDatabase);

		// Populate html-forms
		model.put(Reference.VTL.INPUT_THREADTEXT, Reference.VTL.INPUT_THREADTEXT);
		Tools.println("END:serveTextboard_BOARD\n");
		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD_BOARD, Reference.Web.TEXTBOARD_BOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD_THREAD
	 */
	public static Route serveTextboard_THREAD = (Request request, Response response) -> {
		Tools.println("\nFROM:TextboardController:START:serveTextboard_THREAD");
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardlink = request.params(Reference.CommonStrings.BOARDLINK);
		String threadid = request.params(Reference.CommonStrings.THREADID);

		// Put request parameters into the map
		model.put(Reference.CommonStrings.BOARDLINK, boardlink);
		model.put(Reference.CommonStrings.THREADID, threadid);

		// Verify result
		Tools.println(Reference.CommonStrings.BOARDLINK + ":" + boardlink);
		Tools.println(Reference.CommonStrings.THREADID + ":" + threadid);

		/**
		 * Get objects from database
		 */
		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfPostsFromDatabase = new ArrayList<Map>();
		String threadtext = "NULL_THREADTEXT_DOES_NOT_EXIST";

		try (Connection connection = app.Application.getConnection()) {

			Statement stmt = connection.createStatement();

			/**
			 * Create posts table if not exist
			 */
			Tools.println("Executing script:" + ScriptCreator.CREATE_POSTS);
			stmt.executeUpdate(ScriptCreator.CREATE_POSTS);

			/**
			 * Get threadtext from the table
			 */
			Tools.println("Executing script:" + ScriptCreator.selectThreadFromThreadsGivenThreadid(threadid));
			ResultSet rs = stmt.executeQuery(ScriptCreator.selectThreadFromThreadsGivenThreadid(threadid));
			rs.next();
			threadtext = rs.getString(Reference.CommonStrings.THREADTEXT);
			model.put(Reference.CommonStrings.THREADID, threadid);
			model.put(Reference.CommonStrings.THREADTEXT, threadtext);

			// Select all thread based on the given boardlink
			Tools.println("Executing script:" + ScriptCreator.selectAllPostFromPostsGivenThreadId(threadid));
			rs = stmt.executeQuery(ScriptCreator.selectAllPostFromPostsGivenThreadId(threadid));

			while (rs.next()) {
				Map<String, String> post = new HashMap<String, String>();

				// populate board with the appropriate description of a board
				post.put(Reference.CommonStrings.POSTID, rs.getString(Reference.CommonStrings.POSTID));
				post.put(Reference.CommonStrings.POSTTEXT, rs.getString(Reference.CommonStrings.POSTTEXT));

				arrayOfPostsFromDatabase.add(post);
			}

			Tools.println("START:printing content of arrayOfPostsFromDatabase:");
			for (int a = 0; a < arrayOfPostsFromDatabase.size(); a++) {
				Tools.println(Reference.CommonStrings.POSTID + ":"
						+ arrayOfPostsFromDatabase.get(a).get(Reference.CommonStrings.POSTID) + " "
						+ Reference.CommonStrings.POSTTEXT + ":"
						+ arrayOfPostsFromDatabase.get(a).get(Reference.CommonStrings.POSTTEXT));
			}
			Tools.println("END:printing content of arrayOfPostsFromDatabase");

		} catch (Exception e) {
			return ViewUtil.renderErrorMessage(request, e.getMessage(),
					Reference.CommonStrings.getPREVIOUSBOARDLINK(boardlink), Reference.Web.TEXTBOARD + "/" + boardlink);
		}

		// Populate with list of posts
		model.put(Reference.VTL.POSTLIST, arrayOfPostsFromDatabase);

		// Populate html-form
		model.put(Reference.VTL.INPUT_POSTTEXT, Reference.VTL.INPUT_POSTTEXT);

		Tools.println("END:serveTextboard_THREAD\n");
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
		Tools.println("\nFROM:TextboardController:START:handleCreateBoard");
		// Map<String, Object> model = new HashMap<>();

		String requestedBoardLink = request.queryParams(Reference.VTL.INPUT_BOARDLINK);
		String requestedBoardName = request.queryParams(Reference.VTL.INPUT_BOARDNAME);
		String requestedBoardDescription = request.queryParams(Reference.VTL.INPUT_BOARDDESCRIPTION);

		if (TextboardLogic.checkIfBoardIsAvailable(requestedBoardLink)) {
			Tools.println("The requested boardlink:" + requestedBoardLink + " is available!");

			try (Connection connection = app.Application.getConnection()) {
				Statement stmt = connection.createStatement();

				/**
				 * Create boards table if not exist
				 */
				stmt.executeUpdate(ScriptCreator.CREATE_BOARDS);

				/**
				 * Insert value into the table
				 */
				final String SCRIPT_INSERT_BOARD = "INSERT INTO boards (boardlink, boardname, boarddescription) VALUES ('"
						+ requestedBoardLink + "', '" + requestedBoardName + "', '" + requestedBoardDescription + "');";
				Tools.println("SCRIPT_INSERT_BOARD:" + SCRIPT_INSERT_BOARD);
				stmt.executeUpdate(SCRIPT_INSERT_BOARD);
			} catch (Exception e) {
				e.printStackTrace();
				return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_TEXTBOARD,
						Reference.CommonStrings.NAME_TEXTBOARD);
			}
		} else {
			Tools.println("The requested boardlink:" + requestedBoardLink + " is NOT available!");
		}

		Tools.println("END:handleCreateBoard\n");
		return serveTextboard_HOME.handle(request, response);
	};

	/**
	 * HANDLES CREATE BOARD THREAD
	 */
	public static Route handleCreateThread = (Request request, Response response) -> {
		Tools.println("\nFROM:TextboardController:START:handleCreateBoard");
		// Map<String, Object> model = new HashMap<>();

		String requestedThreadText = request.queryParams(Reference.VTL.INPUT_THREADTEXT);
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);

		// Verify retrieved data
		Tools.println(Reference.VTL.INPUT_THREADTEXT + ":" + requestedThreadText);
		Tools.println(Reference.CommonStrings.BOARDLINK + ":" + currentBoard);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedThreadText)) {
			Tools.println("The requested thread with post:" + requestedThreadText + " is acceptable!");

			try (Connection connection = app.Application.getConnection()) {
				Statement stmt = connection.createStatement();

				/**
				 * Create threads table if not exist
				 */
				stmt.executeUpdate(ScriptCreator.CREATE_THREADS);
				Tools.println("Executing script:" + ScriptCreator.CREATE_THREADS);

				// Create a new thread instance in the threads table
				final String SCRIPT_INSERT_THREAD = "INSERT INTO threads (boardlink, threadtext) VALUES ('"
						+ currentBoard + "', '" + requestedThreadText + "');";
				Tools.println("Executing script:" + SCRIPT_INSERT_THREAD);
				stmt.executeUpdate(SCRIPT_INSERT_THREAD);

			} catch (Exception e) {
				e.printStackTrace();
				return ViewUtil.renderErrorMessage(request, e.getMessage(),
						Reference.CommonStrings.getPREVIOUSBOARDLINK(currentBoard),
						Reference.CommonStrings.NAME_TEXTBOARD + "/" + currentBoard);
			}
		} else {
			Tools.println("The requested thread with post:" + requestedThreadText + " is NOT acceptable!");
		}
		Tools.println("END:handleCreateThread\n");
		return serveTextboard_BOARD.handle(request, response);
	};

	/**
	 * HANDLES CREATE BOARD THREAD POST
	 */
	public static Route handleCreatePost = (Request request, Response response) -> {
		Tools.println("\nFROM:TextboardController:handleCreateBoardPost");
		// Map<String, Object> model = new HashMap<>();

		String requestedPostText = request.queryParams(Reference.VTL.INPUT_POSTTEXT);
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);
		String currentThread = request.params(Reference.CommonStrings.THREADID);

		// Verify retrieved data
		Tools.println(Reference.VTL.INPUT_POSTTEXT + ":" + requestedPostText);
		Tools.println(Reference.CommonStrings.BOARDLINK + ":" + currentBoard);
		Tools.println(Reference.CommonStrings.THREADID + ":" + currentThread);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedPostText)) {
			Tools.println("The requested thread with post:" + requestedPostText + " is acceptable!");

			try (Connection connection = app.Application.getConnection()) {
				Statement stmt = connection.createStatement();

				/**
				 * Create threads table if not exist
				 */
				stmt.executeUpdate(ScriptCreator.CREATE_THREADS);
				Tools.println("Executing script:" + ScriptCreator.CREATE_THREADS);

				// Create a new thread instance in the threads table
				final String SCRIPT_INSERT_POST = "INSERT INTO posts (threadid, posttext) VALUES ('" + currentThread
						+ "', '" + requestedPostText + "');";
				Tools.println("Executing script:" + SCRIPT_INSERT_POST);
				stmt.executeUpdate(SCRIPT_INSERT_POST);

			} catch (Exception e) {
				e.printStackTrace();
				return ViewUtil.renderErrorMessage(request, e.getMessage(),
						Reference.CommonStrings.getPREVIOUSTHREAD(currentBoard, currentThread), currentThread);
			}
		} else {
			Tools.println("The requested thread with post:" + requestedPostText + " is NOT acceptable!");
		}
		Tools.println("END:handleCreatePost\n");
		return serveTextboard_THREAD.handle(request, response);
	};
}
