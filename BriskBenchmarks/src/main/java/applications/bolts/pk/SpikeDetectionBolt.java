package applications.bolts.pk;

import applications.Constants;
import applications.constants.SpikeDetectionConstants;
import brisk.components.operators.base.filterBolt;
import brisk.execution.ExecutionGraph;
import brisk.execution.runtime.tuple.JumboTuple;
import brisk.execution.runtime.tuple.impl.Fields;
import brisk.execution.runtime.tuple.impl.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class SpikeDetectionBolt extends filterBolt {
    private static final Logger LOG = LoggerFactory.getLogger(SpikeDetectionBolt.class);
    private static final long serialVersionUID = -5919724558309333175L;
    double cnt = 0;
    double cnt1 = 0;
    int loop = 1;
    private double spikeThreshold;

    public SpikeDetectionBolt() {
        super(LOG, new HashMap<>());
        this.output_selectivity.put(Constants.DEFAULT_STREAM_ID, 1.0);
        this.setStateful();
        this.read_selectivity = 2.0;
    }

    @Override
    public void initialize(int thread_Id, int thisTaskId, ExecutionGraph graph) {
        super.initialize(thread_Id, thisTaskId, graph);
        spikeThreshold = config.getDouble(SpikeDetectionConstants.Conf.SPIKE_DETECTOR_THRESHOLD, 0.03d);
    }

    @Override
    public void execute(Tuple in) throws InterruptedException {
        // not in use
    }

    @Override
    public void execute(JumboTuple in) throws InterruptedException {
        int bounds = in.length;
//		final long bid = in.getBID();
        for (int i = 0; i < bounds; i++) {
//			int deviceID = in.getInt(0, i);
//			double movingAverageInstant = in.getDouble(1, i);
//			double nextDouble = in.getDouble(2, i);

//			if (Math.abs(nextDouble - movingAverageInstant) > spikeThreshold * movingAverageInstant) {
            //0, deviceID, movingAverageInstant, nextDouble, "spike detected"
            double movingAverageInstant = in.getDouble(1, i);
            double nextDouble = in.getDouble(2, i);
            collector.emit(0, (Math.abs(nextDouble - movingAverageInstant) > spikeThreshold * movingAverageInstant));//a workaround.
//			cnt1++;
//			}
        }

    }

    public void display() {
        LOG.info(this.getContext().getThisTaskId() + " cnt:" + cnt + "\tcnt1:" + cnt1 + "\toutput selectivity:" + ((cnt1) / cnt));
    }

    @Override
    public Fields getDefaultFields() {
        return new Fields(SpikeDetectionConstants.Field.DEVICE_ID, SpikeDetectionConstants.Field.MOVING_AVG, SpikeDetectionConstants.Field.VALUE, SpikeDetectionConstants.Field.MESSAGE);
    }
}