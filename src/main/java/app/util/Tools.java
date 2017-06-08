package app.util;

import static app.Application.*;

public class Tools {
	
	public static void print(String text){
		if (devmode){
			System.out.println(text);
		}
	}
}
