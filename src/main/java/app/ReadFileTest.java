package app;

import java.io.IOException;

import app.util.FileManager;
import app.util.ScriptCreator;
import app.util.Tools;

public class ReadFileTest {

	public static void main2(String[] args) {

		String animeName = "idolmaster";
		String fileType = ".txt";
		String insertScript = "";
		int[][][] partitionArrayRGB = null;
		for (int episode = 1; episode < 5; episode++) {
			for (int panel = 0; panel < 482; panel++) {
				try {
					partitionArrayRGB = FileManager.parsePartitionTextOutput(
							"dev_output/text/" + animeName + "_" + episode + "_" + panel + fileType);

					insertScript = ScriptCreator.insertIntoImagedbAnimeRgb(animeName, episode, panel,
							partitionArrayRGB);

					// Statement stmt = connection.createStatement();
					Tools.println("Executing script:" + insertScript);
					// stmt.executeUpdate(insertScript);

				} catch (IOException e) {
					e.printStackTrace();
					Tools.println("id:" + panel);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		
		int[][][] tripleArray = new int[10][10][3];
		for (int a = 0; a < 10; a++){
			for (int b = 0; b < 10; b++){
				for (int c = 0; c < 3; c++){
					tripleArray[a][b][c] = c;
				}
			}
		}
		FileManager.writeTripleArrayToString(tripleArray, "text.txt");
	}
}
