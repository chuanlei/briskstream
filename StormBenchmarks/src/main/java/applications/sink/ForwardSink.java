package applications.sink;


import helper.stable_sink_helper;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mayconbordin
 */
public class ForwardSink extends BaseSink {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardSink.class);
    stable_sink_helper helper;

    public ForwardSink() {
        super();
    }

    @Override
    public void initialize() {

        helper = new stable_sink_helper(LOG
                , config.getInt("runtimeInSeconds")
                , config.getString("metrics.output"), config.getDouble("predict", 0), 0, context.getThisTaskId());
    }

    @Override
    public void execute(Tuple input) {

        collector.emit(new Values(input));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
