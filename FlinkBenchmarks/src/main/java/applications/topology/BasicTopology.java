package applications.topology;

import constants.BaseConstants;
import applications.sink.BaseSink;
import applications.spout.AbstractSpout;
import helper.parser.Parser;
import util.ClassLoaderUtils;
import org.apache.storm.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTopology extends AbstractTopology {
    private static final Logger LOG = LoggerFactory.getLogger(BasicTopology.class);
    protected AbstractSpout spout;
    protected Parser parser;
    //    protected applications.spout.myKafkaSpout kspout;
//    protected applications.spout.verbose.myKafkaSpout kspout_verbose;
    protected BaseSink sink;
    protected int spoutThreads;
    protected int sinkThreads;

    public BasicTopology(String topologyName, Config config) {
        super(topologyName, config);

        spoutThreads = (int) config.get(BaseConstants.BaseConf.SPOUT_THREADS);//now read from parameters.
//        forwardThreads = config.getInt("SPOUT_THREADS", 1);//now read from parameters.
        sinkThreads = (int) config.get(BaseConstants.BaseConf.SINK_THREADS);

    }

    //    @Override
//    public void initialize() {
//
////        if (config.getString("spout_class").equals("applications.spout.KafkaSpout")) {
////            if (config.getBoolean("verbose")) {
////                final SpoutConfig config = applications.spout.verbose.myKafkaSpout.create_config(this.config, getConfigPrefix());
////                kspout_verbose = new applications.spout.verbose.myKafkaSpout(config);
////            } else {
////                final SpoutConfig config = applications.spout.myKafkaSpout.create_config(this.config, getConfigPrefix());
////                kspout = new applications.spout.myKafkaSpout(config);
////            }
////        } else {
//        spout = loadSpout();
////        }
//
//        sink = loadSink();
//    }
    @Override
    public void initialize() {
        config.setConfigPrefix(getConfigPrefix());
        spout = loadSpout();
        initilize_parser();
    }

    protected void initilize_parser() {
        String parserClass = config.getString(getConfigKey(BaseConstants.BaseConf.SPOUT_PARSER), null);
        if (parserClass != null) {
            parser = (Parser) ClassLoaderUtils.newInstance(parserClass, "parser", LOG);
            parser.initialize(config);
        } else LOG.info("No parser is initialized");

    }

}
