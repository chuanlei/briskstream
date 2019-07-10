package applications.general.topology.benchmarks;

import applications.general.bolts.comm.ParserBolt;
import applications.general.bolts.wc.SplitSentenceBolt;
import applications.general.bolts.wc.WordCountBolt;
import applications.constants.WordCountConstants;
import applications.constants.WordCountConstants.Component;
import applications.constants.WordCountConstants.Field;
import applications.general.topology.BasicTopology;
import org.apache.flink.storm.api.FlinkTopology;
import org.apache.storm.Config;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static applications.constants.WordCountConstants.PREFIX;

public class WordCount extends BasicTopology {
	private static final Logger LOG = LoggerFactory.getLogger(WordCount.class);

	public WordCount(String topologyName, Config config) {
		super(topologyName, config);
	}

	public void initialize() {
		super.initialize();
		sink = loadSink();
		//  initilize_parser();
	}

	@Override
	public FlinkTopology buildTopology() {

		spout.setFields(
//				new Fields(Field.TEXT, MSG_ID, SYSTEMTIMESTAMP)
				new Fields(Field.TEXT)
		);

		builder.setSpout(Component.SPOUT, spout, spoutThreads);

		builder.setBolt(Component.PARSER, new ParserBolt(parser,
//						 new Fields(Field.WORD, MSG_ID, SYSTEMTIMESTAMP))
						new Fields(Field.WORD))
				, config.getInt(WordCountConstants.Conf.PARSER_THREADS, 1))
				.shuffleGrouping(Component.SPOUT);


		builder.setBolt(Component.SPLITTER, new SplitSentenceBolt()
				, config.getInt(WordCountConstants.Conf.SPLITTER_THREADS, 1))
				.shuffleGrouping(Component.PARSER);

		builder.setBolt(Component.COUNTER, new WordCountBolt()
				, config.getInt(WordCountConstants.Conf.COUNTER_THREADS, 1))
				.fieldsGrouping(Component.SPLITTER, new Fields(Field.WORD));

		builder.setBolt(Component.SINK, sink, sinkThreads)
				.shuffleGrouping(Component.COUNTER);
//                .shuffleGrouping(Component.SPOUT)
//                .globalGrouping(Component.SPOUT, Marker_STREAM_ID);

		return FlinkTopology.createTopology(builder, config);
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}

	@Override
	public String getConfigPrefix() {
		return PREFIX;
	}

}
