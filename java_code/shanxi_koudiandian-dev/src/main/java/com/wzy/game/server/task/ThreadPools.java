package com.wzy.game.server.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
 * 线程池，负责处理机器人的业务逻辑
 */
public class ThreadPools {
    private static final ThreadPools pools = new ThreadPools();
    private ScheduledExecutorService scheduleService;

    private ThreadPools() {
        int nThreads = Runtime.getRuntime().availableProcessors();
        scheduleService = Executors.newScheduledThreadPool(nThreads*2);
    }

    public static final ThreadPools getInstance() {
        return pools;
    }


    public void destory() {
        scheduleService.shutdown();
    }

    public ScheduledFuture addSchedule(Runnable runnable, long delaySeconds) {
        return scheduleService.schedule(runnable, delaySeconds, TimeUnit.SECONDS);
    }

    public ScheduledFuture addScheduleMilliseconds(Runnable runnable, long delayMilliseconds) {
        return scheduleService.schedule(runnable, delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture addScheduleFixedRateMilli(Runnable runnable, long delaySeconds, long periodSeconds) {
        return scheduleService.scheduleAtFixedRate(runnable, delaySeconds, periodSeconds, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture addScheduleFixedRate(Runnable runnable, long delaySeconds, long periodSeconds) {
        return scheduleService.scheduleAtFixedRate(runnable, delaySeconds, periodSeconds, TimeUnit.SECONDS);
    }
}
