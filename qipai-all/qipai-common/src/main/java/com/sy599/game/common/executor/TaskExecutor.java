package com.sy599.game.common.executor;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * 任务执行器
 * @author taohuiliang
 * @date 2012-10-5
 * @version v1.0
 */
public class TaskExecutor {
	public static final int PROCESSORS_COUNT = Runtime.getRuntime().availableProcessors();

	public static final ScheduledExecutorService delayExecutor = Executors.newScheduledThreadPool(1,createThreadFactory("delay_thread_pool"));
	public static final ScheduledExecutorService scheExecutor = Executors.newScheduledThreadPool(2,createThreadFactory("scheduleExecutor_thread_pool"));
	public static final ScheduledExecutorService coreExecutor = Executors.newScheduledThreadPool(2,createThreadFactory("coreExecutor_thread_pool"));
    private static final TaskExecutor _inst = new TaskExecutor();
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(createThreadFactory("service:CachedThreadPool"));
//    public static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(2,PROCESSORS_COUNT*2,
//                                      60L,TimeUnit.SECONDS,
//                                      	new LinkedBlockingQueue<Runnable>(),
//										createThreadFactory("service:ThreadPoolExecutor"));

    private static final Timer TIMER = new Timer("Scheduled-Timer");

	/**
	 * 统计类数据
	 */
	public static final ExecutorService EXECUTOR_SERVICE_STATISTICS = Executors.newFixedThreadPool(Math.max(2,PROCESSORS_COUNT/2),createThreadFactory("statistics:FixedThreadPool"));

	/**
	 * 俱乐部自动创房
	 */
	public static final ExecutorService SINGLE_EXECUTOR_SERVICE_GROUP = Executors.newFixedThreadPool(Math.max(2,PROCESSORS_COUNT/2),createThreadFactory("statistics:FixedThreadPool"));
    /**
     * 保存用户数据
     */
	public static final ExecutorService SINGLE_EXECUTOR_SERVICE_USER = Executors.newSingleThreadExecutor(createThreadFactory("save user data:SingleThreadPool"));
    /**
     * 保存房间数据
     */
	public static final ExecutorService SINGLE_EXECUTOR_SERVICE_TABLE = Executors.newSingleThreadExecutor(createThreadFactory("save table data:SingleThreadPool"));

    private TaskExecutor(){
    }

	public final static ThreadFactory createThreadFactory(final String threadName) {
		return new ThreadFactory() {
		    final ThreadFactory defaultFactory =
		        Executors.defaultThreadFactory();
		    public Thread newThread(final Runnable r) {
		        Thread thread = 
		            defaultFactory.newThread(r);
		        thread.setName("qipai: "
		            +threadName);
		        return thread;
		    }
		};
	}
    
    public static TaskExecutor getInstance(){
    	return _inst;
    }
    
    /**提交任务，立马执行*/
    public void submitTask(Runnable task){
    	if(task!=null){
			EXECUTOR_SERVICE.execute(task);
    	}
    }
    
    /**
     * 固定周期地执行某个任务
     * @param task      具体任务
     * @param initDelay 初次执行的延迟时间，以ms为单位
     * @param period    多长周期执行一次任务，以ms为单位 
     */
    public void submitSchTask(Runnable task,long initDelay,long period){
    	scheExecutor.scheduleAtFixedRate(task, initDelay, period, TimeUnit.MILLISECONDS);
    }

	/**
	 * 延时执行某个任务
	 * @param task 具体任务
	 * @param initDelay 初次执行的延迟时间，以ms为单位
	 * @param delay 执行的延迟时间，以ms为单位
	 */
	public void scheduleWithFixedDelay(Runnable task,long initDelay,long delay){
		scheExecutor.scheduleWithFixedDelay(task, initDelay, delay, TimeUnit.MILLISECONDS);
	}

	public void scheduleWithFixedDelay(Runnable task,long initDelay,long delay,boolean core){
		if (core){
			coreExecutor.scheduleWithFixedDelay(task, initDelay, delay, TimeUnit.MILLISECONDS);
		}else{
			scheExecutor.scheduleWithFixedDelay(task, initDelay, delay, TimeUnit.MILLISECONDS);
		}
	}

	public void scheduleWithFixedRate(TimerTask timerTask,Date firstDate,long period){
		TIMER.scheduleAtFixedRate(timerTask,firstDate,period);
	}
    
    /**等待所有执行器将未完成任务执行完再关闭*/
    public void shutDown(){
    	if (!EXECUTOR_SERVICE.isShutdown())
			EXECUTOR_SERVICE.shutdown();
		if (!SINGLE_EXECUTOR_SERVICE_GROUP.isShutdown())
            SINGLE_EXECUTOR_SERVICE_GROUP.shutdown();
        if (!SINGLE_EXECUTOR_SERVICE_USER.isShutdown())
            SINGLE_EXECUTOR_SERVICE_USER.shutdown();
        if (!SINGLE_EXECUTOR_SERVICE_TABLE.isShutdown())
            SINGLE_EXECUTOR_SERVICE_TABLE.shutdown();
		if (!EXECUTOR_SERVICE_STATISTICS.isShutdown())
			EXECUTOR_SERVICE_STATISTICS.shutdown();

    	if (coreExecutor!=null){
			if (!coreExecutor.isShutdown())
				coreExecutor.shutdown();
		}
		if (scheExecutor!=null){
			if (!scheExecutor.isShutdown())
				scheExecutor.shutdown();
		}
		if (delayExecutor!=null){
			if (!delayExecutor.isShutdown())
				delayExecutor.shutdown();
		}

		TIMER.cancel();
    }
    
//    private void shutdownAndAwaitTermination(ExecutorService service){
//    	service.shutdown();
//    	try {
//			service.awaitTermination(SharedConstants.DB_SAVE_INTERVAL, TimeUnit.MINUTES);
//		} catch (InterruptedException e) {
//
//			e.printStackTrace();
//			LogUtil.e("shutDownException,"+service.toString(),e);
//		}
//    }

}
