package com.cliffordlab.amoss.network.json;

/**
 * Created by michael on 3/2/16.
 */
public class MoodSwipeResponse {
	private int value;

	public MoodSwipeResponse(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setMood(int value) {
		this.value = value;
	}
}
