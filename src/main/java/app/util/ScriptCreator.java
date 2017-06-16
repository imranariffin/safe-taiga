package app.util;

public class ScriptCreator {

	public static String INSERT_INTO_imagedb_partition_rgb(String imagename, int[][][] partitionArrayRGB) {

		String script = "INSERT INTO image_db_partition_rgb VALUES ('" + imagename + "',";

		/**
		 * include values of the RGB
		 */
		String[] RGBArray = new String[] { "'{", "'{", "'{" };
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
			RGBArray[e] += "}'";
		}

		for (int d = 0; d < partitionArrayRGB[1][1].length; d++) {
			if (d < (partitionArrayRGB[1][1].length - 1)) {
				script += RGBArray[d] + ", ";
			} else {
				script += RGBArray[d];
			}
		}
		script += ");";
		Tools.println("RED:" + RGBArray[0] + "\nGREEN:" + RGBArray[1] + "\nBLUE:" + RGBArray[2]);
		Tools.println(script);
		return script;
	}
}
