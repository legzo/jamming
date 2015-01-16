package org.elitefactory.jamming.db;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.elitefactory.jamming.WebConnector;
import org.elitefactory.jamming.model.TrafficState;
import org.elitefactory.jamming.model.TrafficStateRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ESConnector {

	private ObjectMapper mapper;

	private static final Logger logger = LoggerFactory
			.getLogger(ESConnector.class);

	public ESConnector() {
		mapper = new ObjectMapper();
	}

	public void save(TrafficState state) {
		final TrafficStateRecord stateRecord = new TrafficStateRecord(state);
		final String recordId = stateRecord.getRecordId();

		logger.info("Indexing {}", recordId);

		HttpPut indexQuery = new HttpPut(
				"http://data-jte.kermit.rd.francetelecom.fr/trafic/state-partial/"
						+ recordId);
		try {
			indexQuery.setEntity(new StringEntity(mapper
					.writeValueAsString(stateRecord)));
			HttpResponse response = WebConnector.getHTTP(true).execute(
					indexQuery);
			logger.info("Response : {}", response);
		} catch (IOException e) {
			logger.error("ERR ! ", e);
		}
	}

}
