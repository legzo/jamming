package org.elitefactory.jamming.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TrafficHistory {

	private Map<Date, TrafficState> states = new HashMap<Date, TrafficState>();

	public void putState(TrafficState state) {
		if (state != null) {
			states.put(state.getTime(), state);
		}
	}

	public int getNumberOfSamples() {
		return states.values().size();
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

}
