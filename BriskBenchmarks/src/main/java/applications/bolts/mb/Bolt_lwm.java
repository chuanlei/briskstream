package applications.bolts.mb;


import brisk.execution.ExecutionGraph;
import brisk.faulttolerance.impl.ValueState;
import engine.transaction.dedicated.ordered.TxnManagerLWM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bolt_lwm extends Bolt_LA {
    private static final Logger LOG = LoggerFactory.getLogger(Bolt_lwm.class);
    private static final long serialVersionUID = -5968750340131744744L;



    public Bolt_lwm(int fid) {
        super(LOG, fid);
        state = new ValueState();
    }

    @Override
    public void initialize(int thread_Id, int thisTaskId, ExecutionGraph graph) {
        super.initialize(thread_Id, thisTaskId, graph);
        transactionManager = new TxnManagerLWM(db.getStorageManager(), this.context.getThisComponentId(), thread_Id, this.context.getThisComponent().getNumTasks());
    }


}
