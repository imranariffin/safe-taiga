package app.structure;

import app.util.Tools;

public class AnimePanel {

	private String name;
	private int episode, panel;
	private int weight = 0;

	public AnimePanel(String name, int episode, int panel) {
		this.name = name;
		this.episode = episode;
		this.panel = panel;
	}

	public AnimePanel(String name, String episode, String panel) {
		this.name = name;
		this.episode = Integer.valueOf(episode);
		this.panel = Integer.valueOf(panel);
	}

	public String getKey() {
		return "" + name + ":" + episode + ":" + panel;
	}

	public String getKey(int degree) {
		if (degree == 1) {
			return "" + name;
		} else if (degree == 2) {
			return "" + name + ":" + episode;
		} else if (degree == 3) {
			return "" + name + ":" + episode + ":" + panel;
		}
		Tools.println("AnimePanel:ILLEGAL ARGUMENT:getKey");
		return null;
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
