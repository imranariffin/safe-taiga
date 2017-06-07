package app.util;

import lombok.*;

public class Path {

	// The @Getter methods are needed in order to access
	// the variables from Velocity Templates
	public static class Web {
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

	public static class StaticStrings {
		
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
