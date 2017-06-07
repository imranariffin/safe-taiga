package app.util;

import lombok.*;

public class Path {

	// The @Getter methods are needed in order to access
	// the variables from Velocity Templates
	public static class Web {
		@Getter
		public static final String INDEX = "/index/";
		@Getter
		public static final String LOGIN = "/login/";
		@Getter
		public static final String LOGOUT = "/logout/";
		@Getter
		public static final String BOOKS = "/books/";
		@Getter
		public static final String ONE_BOOK = "/books/:isbn/";
		@Getter
		public final static String DATABASE = "/database/";
		@Getter
		public final static String HOME = "/home/";

		/**
		 * My GET paths
		 */
		@Getter
		public static final String ROOT = "/";
		@Getter
		public final static String TEXTBOARD = "/textboard/";
		@Getter
		public final static String TEXTBOARD_BOARD = "/textboard/:boardlink/";
		@Getter
		public final static String TEXTBOARD_BOARD_THREAD = "/textboard/:boardlink/:threadid/";

		/**
		 * My POST paths
		 */
		public final static String CREATE_BOARD = "/textboard/";
	}

	public static class Template {
		public final static String INDEX = "/velocity/index/index.vm";
		public final static String LOGIN = "/velocity/login/login.vm";
		public final static String DATABASE = "/velocity/database/database.vm";
		public final static String BOOKS_ALL = "/velocity/book/all.vm";
		public static final String BOOKS_ONE = "/velocity/book/one.vm";
		public static final String NOT_FOUND = "/velocity/notFound.vm";
		public static final String HOME = "/velocity/home/home.vm";

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
		public final static String ERROR = "/velocity/error.vm";
	}

	public static class StaticStrings {

		/**
		 * ERROR HANDLER VOCABULARIES
		 */
		public final static String RETURNLINK = "returnLink";
		public final static String ERROR = "ERROR";

		/**
		 * DATABASE TEXTBOARD VOCABULARIES
		 */
		public final static String BOARDNAME = "boardname";
		public final static String BOARDLINK = "boardlink";
		public final static String THREADID = "threadid";
		public final static String POSTID = "postid";
		public final static String POSTTEXT = "posttext";

		/**
		 * DATABASE TEXTBOARD SCRIPTS
		 */
		public final static String BOARDDESCRIPTION = "boarddescription";
		public final static String SCRIPT_CREATE_BOARDS = "CREATE TABLE IF NOT EXISTS boards ( boardlink VARCHAR(5), boardname VARCHAR(25), boarddescription VARCHAR(100), PRIMARY KEY(boardlink));";
		public final static String SCRIPT_CREATE_THREADS = "CREATE TABLE IF NOT EXISTS threads (threadid SERIAL, boardlink VARCHAR(5), PRIMARY KEY (threadid), FOREIGN KEY (threadid) REFERENCES boards(boardlink));";
		public final static String SCRIPT_CREATE_POSTS = "CREATE TABLE IF NOT EXISTS posts (postid SERIAL, threadid INTEGER, posttext TEXT, PRIMARY KEY (postid), FOREIGN KEY (threadid) REFERENCES threads(threadid));";

		/**
		 * PATHS CONSTANTS
		 */
		public final static String ROOTLINK = "/";
		public final static String TEXTBOARDLINK = "/textboard/";

		public static String getPREVIOUSBOARDLINK(String previousBoardLink) {
			return TEXTBOARDLINK + previousBoardLink;
		}
	}
}
