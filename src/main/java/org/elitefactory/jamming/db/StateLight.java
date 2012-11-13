package org.elitefactory.jamming.db;

import java.util.Date;

public class StateLight {

	private Date time;

	private int dayIndex;

	private Float summary;

	public StateLight(State state) {
		super();
		this.time = state.getTime();
		this.dayIndex = state.getDayIndex();
		this.summary = state.getSummary();
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getDayIndex() {
		return dayIndex;
	}

	public void setDayIndex(int dayIndex) {
		this.dayIndex = dayIndex;
	}

	public Float getSummary() {
		return summary;
	}

	public void setSummary(Float summary) {
		this.summary = summary;
	}

}
