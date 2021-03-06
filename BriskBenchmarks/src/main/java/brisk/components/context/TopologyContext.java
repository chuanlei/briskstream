package brisk.components.context;

import brisk.components.TopologyComponent;
import brisk.components.grouping.Grouping;
import brisk.controller.input.InputStreamController;
import brisk.execution.ExecutionGraph;
import brisk.execution.ExecutionNode;
import brisk.execution.runtime.executorThread;
import brisk.execution.runtime.tuple.impl.Fields;
import brisk.optimization.ExecutionPlan;
import ch.usi.overseer.OverHpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A TopologyContext is created for each executor.
 * It is given to bolts and spouts in their "Loading" and "open"
 * methods, respectively. This object provides information about the component's
 * place within the Brisk.topology, such as Task ids, inputs and outputs, etc.
 * <profiling/>
 */

public class TopologyContext {
    public static ExecutionPlan plan;
    public static OverHpc HPCMonotor;
    private static ExecutionGraph graph;
    private static HashMap<Integer, executorThread> threadMap;
    private final int _taskId;

    /**
     * Instead of Store Brisk.topology, we Store Brisk.execution graph directly!
     * This is a global access memory structure,
     */


    public TopologyContext(ExecutionGraph g, ExecutionPlan plan, ExecutionNode executor, HashMap<Integer, executorThread> threadMap, OverHpc HPCMonotor) {
        TopologyContext.plan = plan;
        TopologyContext.graph = g;
        TopologyContext.threadMap = threadMap;
        TopologyContext.HPCMonotor = HPCMonotor;

        this._taskId = executor.getExecutorID();

    }

    public HashMap<String, Map<TopologyComponent, Grouping>> getThisSources() {
        return this.getComponent(this.getThisComponentId()).getParents();
    }

    public Map<TopologyComponent, Grouping> getSources(String componentId, String StreamId) {
        return this.getComponent(componentId).getParentsOfStream(StreamId);
    }

    public String getThisComponentId() {
        return this.getComponentId(this._taskId);
    }

    public TopologyComponent getThisComponent() {
        return this.getComponent(this._taskId);
    }


    private String getComponentId(int taskId) {
        return getComponent(taskId).getId();
    }

    public ExecutionPlan getPlan() {
        return plan;
    }

    public ExecutionGraph getGraph() {
        return graph;
    }

    /**
     * Gets the component for the specified Task id. The component maps
     * to a component specified for a AbstractSpout or GeneralParserBolt in the Brisk.topology definition.
     *
     * @param taskId the Task id
     * @return the Operator (Brisk.topology component) for the input taskid
     */
    public TopologyComponent getComponent(int taskId) {

        return graph.getExecutionNodeArrayList().get(taskId).operator;
    }

    public ExecutionNode getExecutor(int taskId) {
        return graph.getExecutionNode(taskId);
    }

    public ArrayList<Integer> getComponentTasks(TopologyComponent component) {
        return component.getExecutorIDList();
    }

    private TopologyComponent getComponent(String component) {
        return graph.topology.getComponent(component);
    }


    /**
     * Gets the declared output fields for the specified component.
     * TODO: Add multiple stream support in future.
     *
     * @since 0.0.4 the multiple stream feature is supported.
     */
    public Fields getComponentOutputFields(String componentId, String sourceStreamId) {
        TopologyComponent op = graph.topology.getComponent(componentId);
        return op.get_output_fields(sourceStreamId);
    }

    /**
     * Gets the Task id of this Task.
     *
     * @return the Task id
     */
    public int getThisTaskId() {
        return _taskId;
    }

    public InputStreamController getScheduler() {
        return graph.getGlobal_tuple_scheduler();
    }

    public int getThisTaskIndex() {
        return getThisTaskId();
    }

    public void wait_for_all() throws InterruptedException {
        for (int id : threadMap.keySet()) {
            if (id != getThisTaskId()) {
                threadMap.get(id).join(10000);
            }
        }
    }

    public void stop_running() {
        threadMap.get(getThisTaskId()).running = false;
        threadMap.get(getThisTaskId()).interrupt();
    }

    public void force_exist() {
        threadMap.get(getThisTaskId()).running = false;
        threadMap.get(getThisTaskId()).interrupt();
    }

    public void stop_runningALL() {
        for (int id : threadMap.keySet()) {
            if (id != getThisTaskId()) {
                threadMap.get(id).running = false;
                threadMap.get(id).interrupt();
            }
        }
    }

    public void force_existALL() {
        for (int id : threadMap.keySet()) {
            if (id != getThisTaskId()) {
//                threadMap.get(id).running = false;
                while (threadMap.get(id).isAlive()) {
                    threadMap.get(id).stop();
                }
            }
        }
    }

    public void Sequential_stopAll() {
        for (int id : threadMap.keySet()) {//sequentially stop from spout.
            if (id != getThisTaskId()) {
                int cnt = 0;
                threadMap.get(id).running = false;
                while (threadMap.get(id).isAlive()) {
                    threadMap.get(id).interrupt();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cnt++;
                    if (cnt > 100) {
                        System.out.println("Failed to interrupt thread: " + threadMap.get(id).getName());
                        threadMap.get(id).stop();
                    }
                }
            }
        }

        //stop myself.
        threadMap.get(getThisTaskId()).running = false;
        threadMap.get(getThisTaskId()).interrupt();
    }

    public int getNUMTasks() {
        return this.getComponent(_taskId).getNumTasks();
    }

    public void join() throws InterruptedException {
        threadMap.get(this.getGraph().getSinkThread()).join();
    }
}
