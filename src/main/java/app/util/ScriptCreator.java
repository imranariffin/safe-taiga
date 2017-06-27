package app.util;

public class ScriptCreator {

	public final static String CREATE_BOARDS = "CREATE TABLE IF NOT EXISTS boards ( boardlink VARCHAR(5), boardname VARCHAR(25), boarddescription VARCHAR(100), PRIMARY KEY(boardlink));";
	public final static String CREATE_THREADS = "CREATE TABLE IF NOT EXISTS threads (threadid SERIAL, boardlink VARCHAR(5), threadtext TEXT, PRIMARY KEY (threadid), FOREIGN KEY (boardlink) REFERENCES boards(boardlink));";
	public final static String CREATE_POSTS = "CREATE TABLE IF NOT EXISTS posts (postid SERIAL, threadid INTEGER, posttext TEXT, PRIMARY KEY (postid), FOREIGN KEY (threadid) REFERENCES threads(threadid));";
	public final static String CREATE_IMAGEDB = "CREATE TABLE IF NOT EXISTS imagedb_anime_rgb (name TEXT, episode INT, panel INT, pixel_rgb INT[10][10][3], PRIMARY KEY(name, episode, panel));";
	
	public final static String SELECT_ALL_FROM_BOARDS = "SELECT * FROM boards;";
	public final static String SELECT_ALL_FROM_THREADS = "SELECT * FROM threads;";
	public final static String SELCT_ALL_FROM_POSTS = "SELECT * FROM posts;";
	public final static String SELECT_ALL_FROM_IMAGEDB = "SELECT * FROM imagedb_anime_rgb;";
	
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

	public static String INSERT_INTO_imagedb_anime_rgb(String name, int episode, int panel,
			int[][][] partitionArrayRGB) {
		Tools.println("FROM:ScriptCreator:START:INSERT_INTO_imagedb_anime_rgb");
		String script = "INSERT INTO imagedb_anime_rgb (name, episode, panel, pixel_rgb) VALUES ('" + name + "','"
				+ Integer.toString(episode) + "', '" + Integer.toString(panel) + "', ";

		/**
		 * create string for the RGBs
		 */
		String[] RGBArray = new String[] { "{", "{", "{" };
		for (int a = 0; a < partitionArrayRGB.length; a++) { // y-axis
			for (int e = 0; e < 3; e++) {
				RGBArray[e] += "{";
			}
			for (int b = 0; b < partitionArrayRGB[a].length; b++) { // x-axis
				for (int c = 0; c < partitionArrayRGB[a][b].length; c++) {
					RGBArray[c] += partitionArrayRGB[a][b][c];
					if (b < (partitionArrayRGB[a].length - 1)) {
						RGBArray[c] += ",";
					} else {
						if (a < (partitionArrayRGB.length - 1)) {
							RGBArray[c] += "},";
						} else {
							RGBArray[c] += "}";
						}
					}
				}
			}
		}
		for (int e = 0; e < 3; e++) {
			RGBArray[e] += "}";
		}

		script += "'{";
		for (int d = 0; d < partitionArrayRGB[1][1].length; d++) {
			if (d < (partitionArrayRGB[1][1].length - 1)) {
				script += RGBArray[d] + ", ";
			} else {
				script += RGBArray[d];
			}
		}
		script += "}');";
		Tools.println("RED:" + RGBArray[0] + "\nGREEN:" + RGBArray[1] + "\nBLUE:" + RGBArray[2], false);
		Tools.println(script);
		Tools.println("END:INSERT_INTO_imagedb_partition_rgb");
		return script;
	}

	public static String INSERT_INTO_imagedb_anime_rgb2(String name, int episode, int panel,
			int[][][] partitionArrayRGB) {
		Tools.println("FROM:ScriptCreator:START:INSERT_INTO_imagedb_anime_rgb");
		String script = "INSERT INTO imagedb_anime_rgb (name, episode, panel, pixel_rgb) VALUES ('" + name + "','"
				+ Integer.toString(episode) + "', '" + Integer.toString(panel) + "', ";

		/**
		 * create string for the RGBs
		 */
		String RGBArray = "'{"; // start
		for (int a = 0; a < partitionArrayRGB.length; a++) { // y-axis
			RGBArray += "{";
			for (int b = 0; b < partitionArrayRGB[a].length; b++) { // x-axis
				RGBArray += "{";
				for (int c = 0; c < partitionArrayRGB[a][b].length; c++) {
					if (c < partitionArrayRGB[a][b].length - 1) {
						RGBArray += Integer.toString(partitionArrayRGB[a][b][c]) + ", ";
					} else {
						RGBArray += Integer.toString(partitionArrayRGB[a][b][c]);
					}
				}
				if (b < partitionArrayRGB[a].length - 1) {
					RGBArray += "}, ";
				} else {
					RGBArray += "}";
				}
			}
			if (a < partitionArrayRGB.length - 1) {
				RGBArray += "}, ";
			} else {
				RGBArray += "}";
			}
		}
		RGBArray += "}'"; // end

		script += RGBArray + ");";
		return script;
	}

}
