package com.ramimartin.multibluetooth.bluetooth.manager;



import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * Created by Rami on 16/06/2017.
 */
public class SerialExecutor implements Executor {
    private final Queue<Runnable> mTasks;
    private final Executor mExecutor;
    private Runnable mRunnableActive;

    public SerialExecutor(Executor executor) {
        mTasks = new ArrayDeque();
        mExecutor = executor;
    }

    public synchronized void execute(final Runnable runnable) {
        mTasks.offer(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(300);
                    runnable.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (mRunnableActive == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((mRunnableActive = mTasks.poll()) != null) {
            mExecutor.execute(mRunnableActive);
        }
    }
}
