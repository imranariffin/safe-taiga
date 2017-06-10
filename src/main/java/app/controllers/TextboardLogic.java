package app.controllers;

import static app.Application.DATA_SOURCE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import app.util.Tools;

public class TextboardLogic {

	public static boolean checkIfBoardIsAvailable(String boardlink) {
		Tools.print("FROM:TextboardLogic:START:checkIfBoardIsAvailable");

		final String SCRIPT_SELECT_GIVEN_BOARDLINK = "SELECT * FROM boards WHERE boardlink = '" + boardlink + "';";
		Tools.print("SCRIPT_SELECT_GIVEN_BOARDLINK:" + SCRIPT_SELECT_GIVEN_BOARDLINK);

		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_GIVEN_BOARDLINK);
			// this is how you get a column given the colum name in string
			// rs.getString(columnLabel)
			if (rs.next()) {
				// means rs is non-empty
				Tools.print("END:checkIfBoardIsAvailable:0");
				return false;
			} else {
				// means rs is empty
				Tools.print("END:checkIfBoardIsAvailable:1");
				return true;
			}
		} catch (Exception e) {
			Tools.print("ERROR:" + e.getMessage());
		}

		// connection to database likely experienced an error
		Tools.print("END:checkIfBoardIsAvailable:2");
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