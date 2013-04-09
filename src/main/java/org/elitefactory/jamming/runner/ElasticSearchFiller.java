package org.elitefactory.jamming.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;
import org.elitefactory.jamming.model.TrafficHistory;
import org.elitefactory.jamming.model.TrafficState;
import org.elitefactory.jamming.model.TrafficStateRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

public class ElasticSearchFiller {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(Runner.class);
	static Client client;

	public static void main(final String[] args) throws Exception {

		logger.debug("starting ES");
		final Node node = NodeBuilder.nodeBuilder().client(true).node();
		client = node.client();
		logger.debug("ES started");

		// inject();

		query();

		// on shutdown
		node.close();
	}

	protected static void query() throws ParseException {
		//
		// getHitsForDay("0");
		// getHitsForDay("1");
		// getHitsForDay("2");
		// getHitsForDay("3");
		// getHitsForDay("4");
		// getHitsForDay("5");
		// getHitsForDay("6");
		//
		final StringBuilder sb = new StringBuilder();

		for (int i = 17; i < 20; i++) {
			for (int j = 0; j < 60; j++) {
				final String instantAsString = String.format("%02d:%02d", i, j);
				final double mean = getMeanForInstant("2", instantAsString);
				final String summary = getStateSummaryAsString(new Float(mean));
				logger.debug("{} {}", instantAsString, summary);
				sb.append(summary);
			}
		}
		logger.debug("{}", sb.toString());
	}

	protected static String getStateSummaryAsString(final float stateAsFloat) {
		String result = "@";

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

	//@formatter:off
	protected static void getHitsForDay(final String dayIndex) {
		final SearchResponse response = client.prepareSearch("traffic")
	        .setTypes("states")
	        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	        .setFilter(FilterBuilders.inFilter("dayOfWeek", dayIndex))
	        .execute()
	        .actionGet();
		 
		logger.debug("{} hits for dayIndex {}", response.getHits().getTotalHits(), dayIndex);
	}
	protected static double getMeanForInstant(final String dayIndex, final String instant) {
		final SearchRequestBuilder query = client.prepareSearch("traffic")
				.setTypes("states")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery()
						.must(QueryBuilders.queryString("\""+instant+"\"").field("instant"))
						.must(QueryBuilders.termQuery("dayOfWeek",dayIndex)))
				.addFacet(FacetBuilders.statisticalFacet("stat").field("state"));
		final SearchResponse response = query
				.execute()
				.actionGet();
		final double mean = response.getFacets().facet(StatisticalFacet.class, "stat").getMean();
		
		return mean;
	}
	//@formatter:on

	protected static void inject() throws FileNotFoundException {
		final File bisonFilesDirectory = ResourceUtils.getFile("file:C:\\Users\\JEAT8351\\Desktop\\bison");

		int states = 0;
		int nbOfFiles = 0;
		final Date start = new Date();
		for (final File bisonFile : bisonFilesDirectory.listFiles()) {
			try {
				final TrafficHistory history = mapper.readValue(new FileReader(bisonFile), TrafficHistory.class);
				states += history.getNumberOfSamples();
				nbOfFiles += 1;

				for (final TrafficState state : history.getStates().values()) {
					final TrafficStateRecord stateRecord = new TrafficStateRecord(state);
					final String recordId = String.valueOf(stateRecord.getTime().getTime());

					client.prepareIndex("traffic", "states", recordId)
							.setSource(mapper.writeValueAsString(stateRecord)).execute().actionGet();
				}
			} catch (final IOException e) {
				logger.warn("Could not load file {}", bisonFile.getName());
			}
		}

		logger.debug("{} files parsed", nbOfFiles);
		logger.debug("Loaded {} states in {}ms", states, new Date().getTime() - start.getTime());
	}
}
