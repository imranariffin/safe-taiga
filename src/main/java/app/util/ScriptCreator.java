package app.util;

public class ScriptCreator {

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
