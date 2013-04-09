package org.elitefactory.jamming.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TrafficStateRecord {

	private float state;
	private Date time;
	private String instant;
	private String date;
	private int dayOfWeek = -1;

	private final static SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
	private final static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-mm-dd");

	public TrafficStateRecord(final TrafficState state) {
		this.state = state.getStateAsFloat();
		time = state.getTime();

		final Calendar c = Calendar.getInstance();
		c.setTime(state.getTime());
		dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

		instant = hourFormat.format(time);
		date = dayFormat.format(time);
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(final int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public float getState() {
		return state;
	}

	public Date getTime() {
		return time;
	}

	public String getInstant() {
		return instant;
	}

	public void setState(final float state) {
		this.state = state;
	}

	public void setTime(final Date time) {
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
}