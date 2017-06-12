package app.util;

import static app.Application.*;

public class Tools {

	public static void println(String text) {
		if (devmode) {
			System.out.println(text);
		}
	}

	public static void print(String text) {
		if (devmode) {
			System.out.print(text);
		}
	}
}
