package app.controllers;

import java.sql.SQLException;
import java.sql.Statement;

import app.util.ScriptManager;
import app.util.Tools;

public class ImageProcessingManager {

	public static void insertImageDataToDatabase(Statement stmt, String ipAddress, int[][][] tripleArray) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingDatabase:START:insertImageDataToDatabase");
		Tools.println("userIp:" + ipAddress);

		String insertIntoImageDbUserImageRequest = ScriptManager.insertIntoImageDbUserImageRequest(ipAddress,
				tripleArray);
		try {
			stmt.executeUpdate(insertIntoImageDbUserImageRequest);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Tools.println("END:insertImageDataToDatabase" + System.lineSeparator());
	}
}
