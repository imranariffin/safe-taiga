package app.util;

public class DefaultValues {

	public static class ImagePartitioning {

		private static int divisorSize = 10;
		private static int RGB = 3;

		public static int getDivisorSize() {
			return divisorSize;
		}

		public static int getRGB() {
			return RGB;
		}
	}

}
