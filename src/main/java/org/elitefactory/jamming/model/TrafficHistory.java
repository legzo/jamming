package org.elitefactory.jamming.model;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class TrafficHistory {

	private TreeMap<Date, TrafficState> states = new TreeMap<Date, TrafficState>();

	public void putState(TrafficState state) {
		if (state != null) {
			states.put(state.getTime(), state);
		}
	}

	public int getNumberOfSamples() {
		return states.values().size();
	}

	public String getHistorySummaryAsString() {
		StringBuffer history = new StringBuffer();

		for (Date date : states.keySet()) {
			TrafficState trafficState = states.get(date);
			history.append(trafficState.getStateSummaryAsString());
		}

		return history.toString();
	}

	public Map<Date, String> getHistoryAsString() {
		Map<Date, String> history = new TreeMap<Date, String>();

		for (TrafficState trafficState : states.values()) {
			history.put(trafficState.getTime(),
					trafficState.getStateAsString() + " " + trafficState.getStateAsFormattedFloat());
		}

		return history;
	}

	public TrafficState getMax() {
		TrafficState max = states.values().iterator().next();

		for (TrafficState sample : states.values()) {
			if (sample.getStateAsFloat() > max.getStateAsFloat()) {
				max = sample;
			}
		}

		return max;
	}

	public Map<Date, TrafficState> getStates() {
		return states;
	}

	public void setStates(TreeMap<Date, TrafficState> states) {
		this.states = states;
	}

	public void setNumberOfSamples(int i) {
	}

	public void setMax(TrafficState max) {
	}

	public void setHistorySummaryAsString(String s) {
	}

	public void setHistoryAsString(Map<Date, String> s) {
	}

}
