package org.elitefactory.jamming.model;

public enum TrafficStatus {

	normal("·", 0f), slow("o", 0.5f), stopped("@", 1f), unknown(" ", -1f);

	private String digit;
	private float value;

	private TrafficStatus(String digit, float value) {
		this.digit = digit;
		this.value = value;
	}

	public String getDigit() {
		return digit;
	}

	public float getValue() {
		return value;
	}

	@Override
	public String toString() {
		return digit;
	}
}
