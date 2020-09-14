/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.usercenter.mgr;

import java.util.Timer;
import java.util.TimerTask;

import com.utils.Log;
import com.utils.RandomUtil;
import com.utils.TimeUtil;

/**
 * <pre>
 * 定时器
 * </pre>
 * 
 * @author reison
 */
public final class TimeTaskMgr {

	/** 定时扫描线程 */
	private static Timer scanTimer;
	/** 重置数据线程 */
	private static Timer resetTimer;
	/** 保存用户数据线程 */
	private static Timer saveUserDataTimer;
	/** 保存日志数据线程 */
	private static Timer saveLogDataTimer;

	/** 一分钟毫秒数 */
	public final static long MINUTE = javax.management.timer.Timer.ONE_MINUTE;

	public final static boolean init() {
		int intMin = (int) MINUTE;
		saveUserDataTimer = new Timer("SaveUserDataTimer");
		saveUserDataTimer.schedule(new SaveUserDataTask(), MINUTE * 2,
				MINUTE * 5 + RandomUtil.rand(-2 * intMin, 1 * intMin));

		// =======重置类逻辑start================///
		try {
			// // 每天10:30定时任务
			// SimpleDateFormat sdf1030 = new SimpleDateFormat("yyyy-MM-dd '10:30:00'");
			// Date time1030 = new SimpleDateFormat("yyyy-MM-dd
			// HH:mm:ss").parse(sdf1030.format(now));
			// if (now.after(time1030)) {
			// time1030 = TimeUtil.addTimeInterval(time1030, Calendar.DATE, 1);
			// }
			// resetTimer.scheduleAtFixedRate(new Time1030ExecTask(), time1030, MINUTE * 60
			// * 24);
			//
			// // 每天22:30定时任务
			// SimpleDateFormat sdf2230 = new SimpleDateFormat("yyyy-MM-dd '22:30:00'");
			// Date time2230 = new SimpleDateFormat("yyyy-MM-dd
			// HH:mm:ss").parse(sdf2230.format(now));
			// if (now.after(time2230)) {
			// time2230 = TimeUtil.addTimeInterval(time2230, Calendar.DATE, 1);
			// }
			// scanTimer.scheduleAtFixedRate(new Time2230ExecTask(), time2230, MINUTE * 60 *
			// 24);
			//
			// // 整点的任务
			// scanTimer.scheduleAtFixedRate(new IntegerHourExecTask(),
			// TimeUtil.getTheRecentNextHour(), MINUTE * 60);
			Log.info("定时器初始化成功~");
		} catch (Exception e) {
			Log.error("", e);
			return false;
		}
		// =======重置类逻辑end================///

		return true;
	}

	public static void main(String[] args) {
		Log.init(TimeTaskMgr.class);
		init();
	}

}

abstract class Task extends TimerTask {
	private String name;

	public Task() {
		this.name = "定时器任务-" + this.getClass().getSimpleName();
	}

	@Override
	public void run() {
		try {
			long st = System.currentTimeMillis();
			exec();
			long ct = System.currentTimeMillis() - st;
			if (ct > 200) {
				Log.error(name + "耗时过长:" + ct + "ms");
			}
		} catch (Throwable e) {
			Log.error(name + "异常", e);
		}
	}

	public abstract void exec();

}

class SaveUserDataTask extends Task {

	public SaveUserDataTask() {
	}

	/**
	 */
	@Override
	public void exec() {
		// 错开0点重置的高峰期
		if (TimeUtil.during0ResetTime()) {
			return;
		}
		UserCenterMgr.save();
	}

}