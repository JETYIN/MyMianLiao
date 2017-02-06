package com.tjut.mianliao.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.AsyncTask;

/**
 * Default AsyncTask behaves differently depends on API level. And starting from
 * API 11, the tasks are executed one by one in one thread, but we need: A)
 * Parallel tasks execution by default. B) A thread pool only for long time
 * operations such like network tasks. This will only improves performance
 * starting from API 11.
 */
public abstract class AdvAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "QuickAsyncTask #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> POOL_WORK_QUEUE = new LinkedBlockingQueue<Runnable>(128);
    /**
     * 创建阻塞队列
     **/
    private static final BlockingQueue<Runnable> POOL_WORK_QUEUE2 = new LinkedBlockingQueue<Runnable>(128);
    /**
     * 创建线程池:基本大小,4，最大大小128，线程活动保持时间10，线程活动保存时间单位s，任务阻塞队列,
     **/
    public static final Executor SINGLE_EXECUTOR = new ThreadPoolExecutor(4, 128, 10, TimeUnit.SECONDS,
            POOL_WORK_QUEUE2, THREAD_FACTORY);

    public static final Executor SINGLE_EXECUTOR_LONG = new ThreadPoolExecutor(4, 128, 10, TimeUnit.SECONDS,
            POOL_WORK_QUEUE, THREAD_FACTORY, new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * Should only do quick tasks here. Starting from API 11, the tasks are
     * executed in a single thread by default, and it should be enough for quick
     * tasks. Network related tasks should be done with executeLong.
     */
    public AsyncTask<Params, Progress, Result> executeQuick(Params... params) {
        return executeOnExecutor(SINGLE_EXECUTOR, params);
    }

    /**
     * Tasks might take a long time should be done here (like network tasks).
     */
    public AsyncTask<Params, Progress, Result> executeLong(Params... params) {
        return executeOnExecutor(SINGLE_EXECUTOR_LONG, params);
    }
}
