package app;

import java.io.IOException;

import app.util.FileManager;
import app.util.ScriptCreator;
import app.util.Tools;

public class ReadFileTest {

	public static void main(String[] args) {

		String baseFilename = "idolmaster_1-";
		String fileType = ".txt";
		String insertScript = "";
		int[][][] partitionArrayRGB = null;
		for (int id = 0; id < 482; id++) {
			try {
				partitionArrayRGB = FileManager
						.parsePartitionTextOutput("dev_output/text/" + baseFilename + id + fileType);

				insertScript = ScriptCreator.INSERT_INTO_imagedb_partition_rgb(baseFilename, partitionArrayRGB);

				// Statement stmt = connection.createStatement();
				Tools.println("Executing script:" + insertScript);
				// stmt.executeUpdate(insertScript);

			} catch (IOException e) {
				Tools.println("id:" + id);
				Tools.println(e.getMessage());
			}
		}
	}
}
