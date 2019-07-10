package applications.general.bolts.comm;


import applications.general.bolts.AbstractBolt;
import applications.general.spout.helper.parser.Parser;
import applications.util.datatypes.StreamValues;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by tony on 5/5/2017.
 */
public class ParserBolt extends AbstractBolt {
    private static final Logger LOG = LoggerFactory.getLogger(ParserBolt.class);
    Fields fields;
    int loop;
    int cnt;
    private Parser parser;

    public ParserBolt(Parser parser, Fields fields) {
        this.parser = parser;
        this.fields = fields;
        cnt = 0;
        loop = 1;
    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        super.prepare(conf, context, collector);
        cnt = 0;
    }

    @Override
    public Fields getDefaultFields() {
        return fields;
    }

    @Override
    public void execute(Tuple input) {
        String value = input.getString(0);
        StreamValues tuples = parser.parse(value);
        if (tuples != null) {
            collector.emit(tuples);
        }
    }
}
