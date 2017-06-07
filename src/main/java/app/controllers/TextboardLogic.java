package app.controllers;

import static app.Application.DATA_SOURCE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TextboardLogic {

	public static boolean checkIfBoardIsAvailable(String boardlink) {
		System.out.println("FROM:TextboardLogic:START:checkIfBoardIsAvailable");
		
		final String SCRIPT_SELECT_GIVEN_BOARDLINK = "SELECT * FROM boards WHERE boardlink = '" + boardlink + "';";
		System.out.println("SCRIPT_SELECT_GIVEN_BOARDLINK:" + SCRIPT_SELECT_GIVEN_BOARDLINK);
		
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(SCRIPT_SELECT_GIVEN_BOARDLINK);
			// this is how you get a column given the colum name in string
			// rs.getString(columnLabel)
			if (rs.next()) {
				// means rs is non-empty
				System.out.println("END:checkIfBoardIsAvailable:0");
				return false;
			} else {
				// means rs is empty
				System.out.println("END:checkIfBoardIsAvailable:1");
				return true;
			}
		} catch (Exception e) {
			System.out.println("ERROR:" + e.getMessage());
		}
		
		//connection to database likely experienced an error
		System.out.println("END:checkIfBoardIsAvailable:2");
		return false;
	}
}