package org.elitefactory.jamming.db;

import java.io.IOException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.codehaus.jackson.map.ObjectMapper;
import org.elitefactory.jamming.model.TrafficState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class State {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final Logger logger = LoggerFactory.getLogger(State.class);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date time;

	private Float summary;

	@Column(length = 2000)
	private String stateAsJSON;

	public State() {
	}

	public State(TrafficState trafficState) {
		this.time = trafficState.getTime();
		this.summary = trafficState.getStateAsFloat();

		try {
			this.stateAsJSON = mapper.writeValueAsString(trafficState);
		} catch (IOException e) {
			logger.error("Problem marshalling state", e);
		}
	}

	public TrafficState unmarshall() {
		try {
			return mapper.readValue(stateAsJSON, TrafficState.class);
		} catch (IOException e) {
			logger.error("Problem unmarshalling state", e);
		}
		return null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getStateAsJSON() {
		return stateAsJSON;
	}

	public void setStateAsJSON(String stateAsJSON) {
		this.stateAsJSON = stateAsJSON;
	}

	public float getSummary() {
		return summary;
	}

	public void setSummary(float summary) {
		this.summary = summary;
	}

}
