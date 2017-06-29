package app.util;

public class ScriptCreator {

	public final static String CREATE_BOARDS = "CREATE TABLE IF NOT EXISTS boards ( boardlink VARCHAR(5), boardname VARCHAR(25), boarddescription VARCHAR(100), PRIMARY KEY(boardlink));";
	public final static String CREATE_THREADS = "CREATE TABLE IF NOT EXISTS threads (threadid SERIAL, boardlink VARCHAR(5), threadtext TEXT, PRIMARY KEY (threadid), FOREIGN KEY (boardlink) REFERENCES boards(boardlink));";
	public final static String CREATE_POSTS = "CREATE TABLE IF NOT EXISTS posts (postid SERIAL, threadid INTEGER, posttext TEXT, PRIMARY KEY (postid), FOREIGN KEY (threadid) REFERENCES threads(threadid));";
	public final static String CREATE_IMAGEDB = "CREATE TABLE IF NOT EXISTS imagedb_anime_rgb (name TEXT, episode INT, panel INT, pixel_rgb INT[10][10][3], PRIMARY KEY(name, episode, panel));";
	public final static String CREATE_IMAGEDB_USER_IMAGE_REQUEST = "CREATE TABLE IF NOT EXISTS imagedb_user_image_request (image_id SERIAL, request_ip TEXT, pixel_rgb INT[10][10][3], PRIMARY KEY(image_id));";

	public final static String SELECT_ALL_FROM_BOARDS = "SELECT * FROM boards;";
	public final static String SELECT_ALL_FROM_THREADS = "SELECT * FROM threads;";
	public final static String SELCT_ALL_FROM_POSTS = "SELECT * FROM posts;";
	public final static String SELECT_ALL_FROM_IMAGEDB = "SELECT * FROM imagedb_anime_rgb;";
	public final static String SELECT_ALL_FROM_IMAGEDB_USER_IMAGE_REQUEST = "SELECT * FROM imagedb_user_image_request;";

	public static String insertIntoImageDbUserImageRequest(String ipAddress,
			int[][][] partitionArrayRGB) {
		String script = "INSERT INTO imagedb_user_image_request (request_ip, pixel_rgb) VALUES ('" + ipAddress + "', "
				+ Tools.convertTripleArrayToString(partitionArrayRGB) + ");";
		return script;
	}

	public static String selectThreadFromThreadsGivenThreadid(String threadid) {
		return "SELECT * FROM threads WHERE threadid = '" + threadid + "' LIMIT 1;";
	}

	public static String selectAllPostFromPostsGivenThreadId(int threadid) {
		return "SELECT * FROM posts AS post WHERE post.threadid ='" + threadid + "';";
	}

	public static String selectAllPostFromPostsGivenThreadId(String threadid) {
		return selectAllPostFromPostsGivenThreadId(Integer.valueOf(threadid));
	}

	public static String selectAllThreadFromThreadsGivenBoardLink(String boardlink) {
		return "SELECT * FROM threads AS thread WHERE thread.boardlink = '" + boardlink + "';";
	}

	public static String insertIntoImagedbAnimeRgb(String name, int episode, int panel,
			int[][][] partitionArrayRGB) {
		String script = "INSERT INTO imagedb_anime_rgb (name, episode, panel, pixel_rgb) VALUES ('" + name + "','"
				+ Integer.toString(episode) + "', '" + Integer.toString(panel) + "', ";
		String RGBArray = Tools.convertTripleArrayToString(partitionArrayRGB);
		script += RGBArray + ");";
		return script;
	}

}
