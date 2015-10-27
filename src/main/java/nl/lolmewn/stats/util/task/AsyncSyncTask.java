package nl.lolmewn.stats.util.task;

import nl.lolmewn.stats.Main;

/**
 * @author Sybren
 * @param <T> Type of the
 */
public abstract class AsyncSyncTask<T> {

    public abstract T executeGetTask();

    public abstract void executeUseTask(T val);

    public void execute(Main supplier) {
        supplier.scheduleTaskAsync(() -> {
            T result = executeGetTask();
            supplier.scheduleTask(() -> {
                executeUseTask(result);
            }, 0);
        }, 0);
    }

}
