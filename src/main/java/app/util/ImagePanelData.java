package app.util;

public class ImagePanelData {

	private String name;
	private int episode, panel;
	private int weight = 0;

	public ImagePanelData(String name, int episode, int panel) {
		this.name = name;
		this.episode = episode;
		this.panel = panel;
	}

	public ImagePanelData(String name, String episode, String panel) {
		this.name = name;
		this.episode = Integer.valueOf(episode);
		this.panel = Integer.valueOf(panel);
	}

	public String getKey() {
		return "" + name + ":" + episode + ":" + panel;
	}

	public String getName() {
		return name;
	}

	public int getEpisode() {
		return episode;
	}

	public int getPanel() {
		return panel;
	}

	public void incrementWeight() {
		weight++;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int newWeight) {
		weight = newWeight;
	}
}
