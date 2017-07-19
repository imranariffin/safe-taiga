package app.controllers;

import java.sql.SQLException;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.Map;

import app.managers.DatabaseManager;
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

		try {
			DatabaseManager.getAllBoards(model);
		} catch (SQLException | URISyntaxException e) {
			e.printStackTrace();
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_ROOT,
					Reference.CommonStrings.NAME_ROOT);
		}

		// Populate the rest of the html-forms
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
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardLink = request.params(Reference.CommonStrings.BOARDLINK);

		// Put request parameters into the map
		model.put(Reference.CommonStrings.BOARDLINK, boardLink);

		// Retrieve the list of threads of a given board from the database
		try {
			DatabaseManager.getThreadsGivenBoardLink(boardLink, model);
		} catch (SQLException | URISyntaxException e) {
			e.printStackTrace();
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_TEXTBOARD,
					Reference.CommonStrings.NAME_TEXTBOARD);
		}

		// Populate the rest of the html-forms
		model.put(Reference.VTL.INPUT_THREADTEXT, Reference.VTL.INPUT_THREADTEXT);

		return ViewUtil.render(request, model, Reference.Templates.TEXTBOARD_BOARD, Reference.Web.TEXTBOARD_BOARD,
				"OK: default return");
	};

	/**
	 * SERVE TEXTBOARD_BOARD_THREAD
	 */
	public static Route serveTextboardThread = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		// Obtain the request parameters
		String boardLink = request.params(Reference.CommonStrings.BOARDLINK);
		String threadId = request.params(Reference.CommonStrings.THREADID);

		// Put request parameters into the map
		model.put(Reference.CommonStrings.BOARDLINK, boardLink);
		model.put(Reference.CommonStrings.THREADID, threadId);

		try {
			DatabaseManager.getPostsGivenThreadId(threadId, model);
		} catch (SQLException | URISyntaxException e) {
			e.printStackTrace();
			return ViewUtil.renderErrorMessage(request, e.getMessage(),
					Reference.CommonStrings.getPREVIOUSBOARDLINK(boardLink), Reference.Web.TEXTBOARD + "/" + boardLink);
		}

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

		String requestedBoardLink = request.queryParams(Reference.VTL.INPUT_BOARDLINK);
		String requestedBoardName = request.queryParams(Reference.VTL.INPUT_BOARDNAME);
		String requestedBoardDescription = request.queryParams(Reference.VTL.INPUT_BOARDDESCRIPTION);

		if (TextboardLogic.checkIfBoardIsAvailable(requestedBoardLink)) {
			try {
				DatabaseManager.createBoard(requestedBoardLink, requestedBoardName, requestedBoardDescription);
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

		// Retrieve data from the form
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);
		String requestedThreadText = request.queryParams(Reference.VTL.INPUT_THREADTEXT);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedThreadText)) {
			try {
				DatabaseManager.createThread(currentBoard, requestedThreadText);
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

		String requestedPostText = request.queryParams(Reference.VTL.INPUT_POSTTEXT);
		String currentBoard = request.params(Reference.CommonStrings.BOARDLINK);
		String currentThread = request.params(Reference.CommonStrings.THREADID);

		if (TextboardLogic.checkIfTextIsAcceptable(requestedPostText)) {
			try {
				DatabaseManager.createPost(currentThread, requestedPostText);

			} catch (Exception e) {
				e.printStackTrace();
				return ViewUtil.renderErrorMessage(request, e.getMessage(),
						Reference.CommonStrings.getPREVIOUSTHREAD(currentBoard, currentThread), currentThread);
			}
		} else {
			Tools.println("Rejected post with the text:" + requestedPostText);
		}

		return serveTextboardThread.handle(request, response);
	};
}
