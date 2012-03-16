package org.elitefactory.jamming.model;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class TrafficState {

	private final static DecimalFormat twoDecimalFormatter = new DecimalFormat("0.000");
	private Date time;

	private Map<RocadePoint, TrafficStatus> state = new TreeMap<RocadePoint, TrafficStatus>();

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

	public void setTime(Date time) {
		this.time = time;
	}

	public Map<RocadePoint, TrafficStatus> getState() {
		return state;
	}

	@Override
	public String toString() {
		return "TrafficState [time=" + time + ", summary=" + getStateAsFormattedFloat() + ", state="
				+ getStateAsString() + "]";
	}

	private String getStateAsString() {
		StringBuffer sb = new StringBuffer();
		for (TrafficStatus aState : state.values()) {
			sb.append(aState.getDigit());
		}

		return sb.toString();
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
}
