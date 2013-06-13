package org.elitefactory.jamming.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

public class TrafficStateRecord {

	private String state;
	private float stateAsFloat;
	private String time;
	private String instant;
	private String date;
	private int dayOfWeek = -1;

	private final static SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	private final static SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
	private final static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

	public TrafficStateRecord(final TrafficState state) {
		this.state = StringUtils.leftPad(String.valueOf((int) (state.getStateAsFloat() * 100)), 3, "0");
		stateAsFloat = state.getStateAsFloat();
		time = iso8601Format.format(state.getTime());

		final Calendar c = Calendar.getInstance();
		c.setTime(state.getTime());
		dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

		instant = hourFormat.format(state.getTime());
		date = dayFormat.format(state.getTime());
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(final int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getState() {
		return state;
	}

	public String getTime() {
		return time;
	}

	public String getInstant() {
		return instant;
	}

	public void setState(final String state) {
		this.state = state;
	}

	public void setTime(final String time) {
		this.time = time;
	}

	public void setInstant(final String instant) {
		this.instant = instant;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public float getStateAsFloat() {
		return stateAsFloat;
	}

	public void setStateAsFloat(final float stateAsFloat) {
		this.stateAsFloat = stateAsFloat;
	}
}