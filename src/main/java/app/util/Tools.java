package app.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

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
			try {
				int[] tmpPanels = new int[animeArray[animeNumber].getNumberOfEpisodes()];
				for (int a = 1; a <= animeArray[animeNumber].getNumberOfEpisodes(); a++) {
					tmpPanels[a - 1] = Integer.valueOf(FileManager.readFile(
							"dev_output/description/" + animeArray[animeNumber].getName() + "_" + a + ".txt"));
				}

				try {
					animeArray[animeNumber].setPanels(tmpPanels);
				} catch (Exception e) {
					Tools.println("panels mistmatch");
					Tools.println(e.getMessage());
				}
			} catch (IOException e) {
				Tools.println("FAIL READING DESCRIPTION TEXT");
				Tools.println(e.getMessage());
			}
			for (int episodeNumber = 1; episodeNumber <= animeArray[animeNumber - 1]
					.getNumberOfEpisodes(); episodeNumber++) {
				for (int panelNumber = 0; panelNumber < animeArray[animeNumber - 1].getPanels()[episodeNumber
						- 1]; panelNumber++) {
					try (Connection connection = DATA_SOURCE.getConnection()) {
						partitionArrayRGB = FileManager.parsePartitionTextOutput("dev_output/text/"
								+ animeArray[animeNumber - 1].getName() + "/" + animeArray[animeNumber - 1].getName()
								+ "_" + episodeNumber + "_" + panelNumber + ".txt");

						insertScript = ScriptCreator.INSERT_INTO_imagedb_anime_rgb(
								animeArray[animeNumber - 1].getName(), episodeNumber, panelNumber, partitionArrayRGB);

						Statement stmt = connection.createStatement();
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
}
