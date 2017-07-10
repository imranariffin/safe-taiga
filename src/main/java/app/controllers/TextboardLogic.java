package app.controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import app.util.Tools;

public class TextboardLogic {

	public static boolean checkIfBoardIsAvailable(String boardlink) {
		final String SCRIPT_SELECT_GIVEN_BOARDLINK = "SELECT * FROM boards WHERE boardlink = '" + boardlink + "';";
		Tools.println("SCRIPT_SELECT_GIVEN_BOARDLINK:" + SCRIPT_SELECT_GIVEN_BOARDLINK);

		try (Connection connection = app.Application.getConnection()) {

			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_GIVEN_BOARDLINK);
			// this is how you get a column given the colum name in string
			// rs.getString(columnLabel)
			if (rs.next()) {
				// means rs is non-empty
				Tools.println("END:checkIfBoardIsAvailable:0");
				return false;
			} else {
				// means rs is empty
				Tools.println("END:checkIfBoardIsAvailable:1");
				return true;
			}
		} catch (Exception e) {
			Tools.println("ERROR:" + e.getMessage());
		}

		// connection to database likely experienced an error
		return false;
	}

	public static boolean checkIfTextIsAcceptable(String givenText) {
		if (givenText.length() == 0) {
			return false;
		} else {
			return true;
		}
	}
}