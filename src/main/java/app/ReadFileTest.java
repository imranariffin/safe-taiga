package app;

import java.io.IOException;

import app.util.FileManager;
import app.util.ScriptCreator;
import app.util.Tools;

public class ReadFileTest {

	public static void main(String[] args) {

		String animeName = "idolmaster";
		String fileType = ".txt";
		String insertScript = "";
		int[][][] partitionArrayRGB = null;
		for (int episode = 1; episode < 5; episode++) {
			for (int panel = 0; panel < 482; panel++) {
				try {
					partitionArrayRGB = FileManager.parsePartitionTextOutput(
							"dev_output/text/" + animeName + "_" + episode + "_" + panel + fileType);

					insertScript = ScriptCreator.INSERT_INTO_imagedb_anime_rgb(animeName, episode, panel,
							partitionArrayRGB);

					// Statement stmt = connection.createStatement();
					Tools.println("Executing script:" + insertScript);
					// stmt.executeUpdate(insertScript);

				} catch (IOException e) {
					Tools.println("id:" + panel);
					Tools.println(e.getMessage());
				}
			}
		}
	}
}
