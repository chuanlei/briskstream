package applications.bolts.mb;


import applications.param.mb.MicroEvent;
import brisk.execution.ExecutionGraph;
import brisk.execution.runtime.tuple.impl.Tuple;
import brisk.faulttolerance.impl.ValueState;
import engine.DatabaseException;
import engine.transaction.dedicated.TxnManagerLock;
import engine.transaction.impl.TxnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static applications.CONTROL.combo_bid_size;
import static applications.CONTROL.enable_latency_measurement;
import static engine.profiler.Metrics.MeasureTools.*;


/**
 * Combine Read-Write for nocc.
 */
public class Bolt_nocc extends GSBolt {
    private static final Logger LOG = LoggerFactory.getLogger(Bolt_nocc.class);
    private static final long serialVersionUID = -5968750340131744744L;

    public Bolt_nocc(int fid) {
        super(LOG, fid);
        state = new ValueState();
    }

    private void write_txn_process(MicroEvent event, long i, long _bid) throws DatabaseException, InterruptedException {
        BEGIN_TP_CORE_TIME_MEASURE(thread_Id);
        boolean success = write_request(event, txn_context[(int) (i - _bid)]);
        END_TP_CORE_TIME_MEASURE_ACC(thread_Id);

        if (success) {
            BEGIN_COMPUTE_TIME_MEASURE(thread_Id);
            write_core(event);
            END_COMPUTE_TIME_MEASURE(thread_Id);
            transactionManager.CommitTransaction(txn_context[(int) (i - _bid)]);//always success..
        } else {//being aborted.
            txn_context[(int) (i - _bid)].is_retry_ = true;
            BEGIN_ABORT_TIME_MEASURE(thread_Id);
            while (!write_request(event, txn_context[(int) (i - _bid)])) ;
            END_ABORT_TIME_MEASURE_ACC(thread_Id);

            BEGIN_COMPUTE_TIME_MEASURE(thread_Id);
            write_core(event);
            END_COMPUTE_TIME_MEASURE(thread_Id);

            transactionManager.CommitTransaction(txn_context[(int) (i - _bid)]);//always success..
        }
    }

    private void read_txn_process(MicroEvent event, long i, long _bid) throws DatabaseException, InterruptedException {
        BEGIN_TP_CORE_TIME_MEASURE(thread_Id);
        boolean success = read_request(event, txn_context[(int) (i - _bid)]);
        END_TP_CORE_TIME_MEASURE_ACC(thread_Id);

        if (success) {
            BEGIN_COMPUTE_TIME_MEASURE(thread_Id);
            read_core(event);
            END_COMPUTE_TIME_MEASURE(thread_Id);
            transactionManager.CommitTransaction(txn_context[(int) (i - _bid)]);//always success..
        } else {//being aborted.
            txn_context[(int) (i - _bid)].is_retry_ = true;
            BEGIN_ABORT_TIME_MEASURE(thread_Id);
            while (!read_request(event, txn_context[(int) (i - _bid)])) ;
            END_ABORT_TIME_MEASURE_ACC(thread_Id);

            BEGIN_COMPUTE_TIME_MEASURE(thread_Id);
            read_core(event);
            END_COMPUTE_TIME_MEASURE(thread_Id);

            transactionManager.CommitTransaction(txn_context[(int) (i - _bid)]);//always success..
        }
    }

    private void txn_process(long _bid) throws DatabaseException, InterruptedException {
        for (long i = _bid; i < _bid + combo_bid_size; i++) {
            txn_context[(int) (i - _bid)] = new TxnContext(thread_Id, this.fid, i);
            MicroEvent event = (MicroEvent) db.eventManager.get((int) i);
            boolean flag = event.READ_EVENT();

            if (flag) {
                read_txn_process(event, i, _bid);
            } else {
                write_txn_process(event, i, _bid);
            }
        }
    }


    @Override
    public void initialize(int thread_Id, int thisTaskId, ExecutionGraph graph) {
        super.initialize(thread_Id, thisTaskId, graph);
        transactionManager = new TxnManagerLock(db.getStorageManager(), this.context.getThisComponentId(), thread_Id, this.context.getThisComponent().getNumTasks());
    }

    @Override
    public void execute(Tuple in) throws InterruptedException, DatabaseException {

        //pre stream processing phase..
        BEGIN_PREPARE_TIME_MEASURE(thread_Id);
        Long timestamp;//in.getLong(1);
        if (enable_latency_measurement)
            timestamp = in.getLong(0);
        else
            timestamp = 0L;//

        long _bid = in.getBID();
        END_PREPARE_TIME_MEASURE(thread_Id);

        //begin transaction processing.
        BEGIN_TRANSACTION_TIME_MEASURE(thread_Id);//need to amortize.

        txn_process(_bid);

        //end transaction processing.
        END_TRANSACTION_TIME_MEASURE(thread_Id);

        post_process(_bid, timestamp);

        END_TOTAL_TIME_MEASURE_ACC(thread_Id);
    }


}
