package app.util;

public class Reference {

	public static class Web {
		public static final String ROOT = "/";
		public final static String TEXTBOARD = "/textboard/";
		public final static String TEXTBOARD_BOARD = "/textboard/:boardlink/";
		public final static String TEXTBOARD_BOARD_THREAD = "/textboard/:boardlink/:threadid/";
	}

	public static class Templates {

		/**
		 * My templates
		 */
		public final static String ROOT = "/velocity/root.vm";
		public final static String TEXTBOARD = "/velocity/textboard/textboard.vm";
		public final static String TEXTBOARD_BOARD = "/velocity/textboard/board.vm";
		public final static String TEXTBOARD_BOARD_THREAD = "/velocity/textboard/thread.vm";

		/**
		 * ERROR templates
		 */
		public static final String NOT_FOUND = "/velocity/notFound.vm";
		public final static String ERROR = "/velocity/error.vm";
	}

	public static class CommonStrings {

		/**
		 * WEBSITE MAIN VOCABULARIES
		 */

		public final static String ROOT = "ROOT";
		public final static String TEXTBOARD = "Textboard";

		/**
		 * ERROR HANDLER VOCABULARIES
		 */
		public final static String RETURNLINK = "returnLink";
		public final static String RETURNNAME = "returnName";
		public final static String ERROR = "ERROR";

		/**
		 * DATABASE TEXTBOARD VOCABULARIES
		 */

		public final static String BOARDLINK = "boardlink";
		public final static String BOARDNAME = "boardname";
		public final static String THREADID = "threadid";
		public final static String THREADTEXT = "threadtext";
		public final static String POSTID = "postid";
		public final static String POSTTEXT = "posttext";
		public final static String BOARDDESCRIPTION = "boarddescription";

		/**
		 * DATABASE TEXTBOARD SCRIPT
		 */
		public final static String SCRIPT_CREATE_BOARDS = "CREATE TABLE IF NOT EXISTS boards ( boardlink VARCHAR(5), boardname VARCHAR(25), boarddescription VARCHAR(100), PRIMARY KEY(boardlink));";
		public final static String SCRIPT_CREATE_THREADS = "CREATE TABLE IF NOT EXISTS threads (threadid SERIAL, boardlink VARCHAR(5), threadtext TEXT, PRIMARY KEY (threadid), FOREIGN KEY (boardlink) REFERENCES boards(boardlink));";
		public final static String SCRIPT_CREATE_POSTS = "CREATE TABLE IF NOT EXISTS posts (postid SERIAL, threadid INTEGER, posttext TEXT, PRIMARY KEY (postid), FOREIGN KEY (threadid) REFERENCES threads(threadid));";

		public final static String SCRIPT_SELECT_BOARDS = "SELECT * FROM boards;";

		public static String getSCRIPT_GET_THREADTEXT_BY_ID(String threadid) {
			return "SELECT * FROM threads WHERE threadid = '" + threadid + "' LIMIT 1;";
		}

		/**
		 * PATHS CONSTANTS
		 */
		public final static String ROOTLINK = "/";
		public final static String TEXTBOARDLINK = "/textboard/";

		public static String getPREVIOUSBOARDLINK(String previousBoardLink) {
			return TEXTBOARDLINK + previousBoardLink + "/";
		}

		public static String getPREVIOUSTHREAD(String previousBoardLink, String previousThreadId) {
			return TEXTBOARDLINK + previousBoardLink + "/" + previousThreadId + "/";
		}
	}

	public static class VTLStatics {

		public final static String BOARDLIST = "boardList";
		public final static String THREADLIST = "threadList";
		public final static String POSTLIST = "postList";
		public final static String INPUT_BOARDLINK = "INPUT_BOARDLINK";
		public final static String INPUT_BOARDNAME = "INPUT_BOARDNAME";
		public final static String INPUT_BOARDDESCRIPTION = "INPUT_BOARDDESCRIPTION";
		public final static String INPUT_THREAD = "INPUT_THREAD";
		public final static String INPUT_THREADTEXT = "INPUT_THREADTEXT";
		public final static String INPUT_POSTTEXT = "INPUT_POSTTEXT";
	}
}
