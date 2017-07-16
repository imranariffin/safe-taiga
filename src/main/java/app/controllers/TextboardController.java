package app.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.managers.ScriptManager;
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
	public static Route serveTextboardHome = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfBoardsFromDatabase = new ArrayList<Map>();
		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();

			// Just a simple SELECT ALL script
			Tools.println("Executing script:" + ScriptManager.SELECT_ALL_FROM_BOARDS);
			ResultSet rs = stmt.executeQuery(ScriptManager.SELECT_ALL_FROM_BOARDS);

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

		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD, Reference.Web.TEXTBOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD
	 */
	public static Route serveTextboardBoard = (Request request, Response response) -> {
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

			// Select all thread based on the given boardlink
			String selectAllThreadFromBoardGivenBoardlink = ScriptManager
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

		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD_BOARD, Reference.Web.TEXTBOARD_BOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD_THREAD
	 */
	public static Route serveTextboard_THREAD = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardlink = request.params(Reference.CommonStrings.BOARDLINK);
		String threadid = request.params(Reference.CommonStrings.THREADID);

		// Put request parameters into the map
		model.put(Reference.CommonStrings.BOARDLINK, boardlink);
		model.put(Reference.CommonStrings.THREADID, threadid);

		/**
		 * Get objects from database
		 */
		// Prepare arraylist for output from database
		@SuppressWarnings("rawtypes")
		ArrayList<Map> arrayOfPostsFromDatabase = new ArrayList<Map>();

		try {
			ScriptManager.serveTextboardThread(threadid, model);
		} catch (SQLException | URISyntaxException e) {
			return ViewUtil.renderErrorMessage(request, e.getMessage(),
					Reference.CommonStrings.getPREVIOUSBOARDLINK(boardlink), Reference.Web.TEXTBOARD + "/" + boardlink);
		}

		// Populate with list of posts
		model.put(Reference.VTL.POSTLIST, arrayOfPostsFromDatabase);

		// Populate html-form
		model.put(Reference.VTL.INPUT_POSTTEXT, Reference.VTL.INPUT_POSTTEXT);

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
		// Map<String, Object> model = new HashMap<>();

		String requestedBoardLink = request.queryParams(Reference.VTL.INPUT_BOARDLINK);
		String requestedBoardName = request.queryParams(Reference.VTL.INPUT_BOARDNAME);
		String requestedBoardDescription = request.queryParams(Reference.VTL.INPUT_BOARDDESCRIPTION);

		if (TextboardLogic.checkIfBoardIsAvailable(requestedBoardLink)) {
			try {
				ScriptManager.createBoard(requestedBoardLink, requestedBoardName, requestedBoardDescription);
			} catch (SQLException | URISyntaxException e) {
				e.printStackTrace();
				return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_TEXTBOARD,
						Reference.CommonStrings.NAME_TEXTBOARD);
			}
		}

		return serveTextboardHome.handle(request, response);
	};

	/**
	 * HANDLES CREATE BOARD THREAD
	 */
	public static Route handleCreateThread = (Request request, Response response) -> {
		// Map<String, Object> model = new HashMap<>();

		// Retrieve data from the form
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);
		String requestedThreadText = request.queryParams(Reference.VTL.INPUT_THREADTEXT);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedThreadText)) {
			try {
				ScriptManager.createThread(currentBoard, requestedThreadText);
			} catch (SQLException | URISyntaxException e) {
				e.printStackTrace();
				return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_TEXTBOARD,
						Reference.CommonStrings.NAME_TEXTBOARD);
			}
		}

		return serveTextboardBoard.handle(request, response);
	};

	/**
	 * HANDLES CREATE BOARD THREAD POST
	 */
	public static Route handleCreatePost = (Request request, Response response) -> {
		// Map<String, Object> model = new HashMap<>();

		String requestedPostText = request.queryParams(Reference.VTL.INPUT_POSTTEXT);
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);
		String currentThread = request.params(Reference.CommonStrings.THREADID);

		// Validate requested names
		requestedPostText = Tools.convertToQuerySafe(requestedPostText);

		// Verify retrieved data
		Tools.println(Reference.VTL.INPUT_POSTTEXT + ":" + requestedPostText);
		Tools.println(Reference.CommonStrings.BOARDLINK + ":" + currentBoard);
		Tools.println(Reference.CommonStrings.THREADID + ":" + currentThread);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedPostText)) {
			Tools.println("The requested thread with post:" + requestedPostText + " is acceptable!");

			try (Connection connection = app.Application.getConnection()) {
				Statement stmt = connection.createStatement();

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

		return serveTextboard_THREAD.handle(request, response);
	};
}
