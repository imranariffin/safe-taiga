package app.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static app.Application.DATA_SOURCE;

public class Tools {

	// private static Map<String, AnimeObject> animeMap = new HashMap<String,
	// AnimeObject>();
	private static AnimeObject[] animeArray = new AnimeObject[] { new AnimeObject("idolmaster", 25, 47) };
	public static boolean devmode = true;

	public static void println(String text) {
		if (devmode) {
			System.out.println(text);
		}
	}

	public static void println(String text, boolean bool) {
		if (bool) {
			System.out.println(text);
		}
	}

	public static void print(String text) {
		if (devmode) {
			System.out.print(text);
		}
	}

	public static void print(String text, boolean bool) {
		if (bool) {
			System.out.print(text);
		}
	}

	public static void InsertTextDumpToDatabase() {

		String insertScript = "";
		int[][][] partitionArrayRGB = null;

		Tools.println("beginning to insert " + animeArray.length + " anime into the database");

		for (int animeNumber = 0; animeNumber < animeArray.length; animeNumber++) {
			Tools.println("animeNumber:" + animeNumber);
			try {
				int[] tmpPanels = new int[animeArray[animeNumber].getNumberOfEpisodes()];
				for (int a = 1; a <= animeArray[animeNumber].getNumberOfEpisodes(); a++) {
					Tools.println("a:" + a);
					tmpPanels[a - 1] = Integer.valueOf(FileManager.readFile(
							"dev_output/description/" + animeArray[animeNumber].getName() + "_" + a + ".txt"));
				}

				try {
					animeArray[animeNumber].setPanels(tmpPanels);
				} catch (Exception e) {
					Tools.println("FAIL ASSIGNING PANEL VALUE");
					Tools.println(e.getMessage());
				}
			} catch (IOException e) {
				Tools.println("FAIL READING DESCRIPTION TEXT");
				Tools.println(e.getMessage());
			}
			for (int episodeNumber = 1; episodeNumber <= animeArray[animeNumber]
					.getNumberOfEpisodes(); episodeNumber++) {
				Tools.println("episodeNumber:" + episodeNumber);
				Tools.println("panelNumbers: ");
				for (int panelNumber = 0; panelNumber < animeArray[animeNumber].getPanels()[episodeNumber
						- 1]; panelNumber++) {
					Tools.print(panelNumber + " ");
					try (Connection connection = DATA_SOURCE.getConnection()) {

						Statement stmt = connection.createStatement();
						/**
						 * Create imagedb_anime_rgb table if not exist
						 */
						Tools.println("Execute script:" + ScriptCreator.CREATE_IMAGEDB);
						stmt.executeUpdate(ScriptCreator.CREATE_IMAGEDB);

						partitionArrayRGB = FileManager.parsePartitionTextOutput("dev_output/text/"
								+ animeArray[animeNumber].getName() + "/" + animeArray[animeNumber].getName() + "_"
								+ episodeNumber + "_" + panelNumber + ".txt");

						insertScript = ScriptCreator.INSERT_INTO_imagedb_anime_rgb2(animeArray[animeNumber].getName(),
								episodeNumber, panelNumber, partitionArrayRGB);

						Tools.println("Executing script:" + insertScript);
						stmt.executeUpdate(insertScript);

					} catch (IOException e) {
						Tools.println("id:" + panelNumber);
						Tools.println(e.getMessage());
					} catch (SQLException e) {
						Tools.println("id:" + panelNumber);
						Tools.println("query:" + insertScript);
						Tools.println(e.getMessage());
					}
				}
			}
		}
	}

	public static String convertTripleArrayToString(int[][][] tripleArray) {
		String result = "";

		for (int a = 0; a < tripleArray.length; a++) {
			for (int b = 0; b < tripleArray[a].length; b++) {
				for (int c = 0; c < tripleArray[a][b].length; c++) {
					result += tripleArray[a][b][c] + " ";
				}
			}
			if (a == (tripleArray.length - 1)) {
				System.out.println("new line");
				result += "/n";
			}
		}
		return result;
	}
}
