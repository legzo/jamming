package org.elitefactory.jamming.model;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class TrafficState {

	private final static DecimalFormat twoDecimalFormatter = new DecimalFormat("0.000");
	private Date time;

	private Map<RocadePoint, TrafficStatus> state = new TreeMap<RocadePoint, TrafficStatus>();

	/**
	 * for json
	 */
	public TrafficState() {
	}

	public TrafficState(Date time) {
		super();
		this.time = time;
	}

	public void setStatusForPoint(RocadePoint point, TrafficStatus status) {
		state.put(point, status);
	}

	public Date getTime() {
		return time;
	}

	public String getFormattedTime() {
		return String.format("%1$tm-%1$td-%1$tk_%1$tM", time);
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Map<RocadePoint, TrafficStatus> getState() {
		return state;
	}

	public void setState(Map<RocadePoint, TrafficStatus> state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "TraffsicState [time=" + time + ", summary=" + getStateAsFormattedFloat() + ", state="
				+ getStateAsString() + "]";
	}

	public String getStateAsString() {
		StringBuffer sb = new StringBuffer();
		for (TrafficStatus aState : state.values()) {
			sb.append(aState.getDigit());
		}

		return sb.toString();
	}

	public String getStateSummaryAsString() {
		String result = "@";

		float stateAsFloat = getStateAsFloat();

		if (stateAsFloat < 0.6f) {
			result = "O";
		}
		if (stateAsFloat < 0.4f) {
			result = "o";
		}
		if (stateAsFloat < 0.2f) {
			result = "=";
		}
		if (stateAsFloat < 0.1f) {
			result = "-";
		}

		return result;
	}

	public float getStateAsFloat() {
		float total = 0f;
		int numberOfSamples = 0;

		for (TrafficStatus aState : state.values()) {
			if (aState.getValue() >= 0) {
				total += aState.getValue();
				numberOfSamples++;
			}
		}

		return total / numberOfSamples;
	}

	public String getStateAsFormattedFloat() {
		float total = 0f;
		int numberOfSamples = 0;

		for (TrafficStatus aState : state.values()) {
			if (aState.getValue() >= 0) {
				total += aState.getValue();
				numberOfSamples++;
			}
		}

		return twoDecimalFormatter.format(total / numberOfSamples);
	}

	// for JSON

	public void setStateAsFloat(float f) {

	}

	public void setStateAsFormattedFloat(String s) {

	}

	public void setStateAsString(String s) {

	}

	public void setStateSummaryAsString(String s) {

	}

	public void setFormattedTime(String time) {

	}
}
