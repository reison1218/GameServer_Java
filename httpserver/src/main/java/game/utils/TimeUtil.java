package game.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public final static int COST_TIME_MAX_LOG = 100;// cmd或Action执行超过该ms则记录日志

    /**
     * 使用org.apache.commons.lang包的格式化：多线程安全、可以使用静态实例，不用每次都New
     */
    public final static FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /**
     * 使用org.apache.commons.lang包的格式化：多线程安全、可以使用静态实例，不用每次都New
     */
    public final static FastDateFormat formatterNum = FastDateFormat.getInstance("yyyyMMddHHmmss");

    /**
     * 使用org.apache.commons.lang包的格式化：多线程安全、可以使用静态实例，不用每次都New
     */
    public final static FastDateFormat formatterDateNum = FastDateFormat.getInstance("yyyyMMdd");

    /**
     * 使用org.apache.commons.lang包的格式化：多线程安全、可以使用静态实例，不用每次都New
     */
    public final static FastDateFormat formatterDate = FastDateFormat.getInstance("yyyy-MM-dd");

    public final static String TIME_2000 = "2000-01-01 00:00:00";
    public final static String TIME_2999 = "2099-01-01 00:00:00";
    public final static String TIME_2038 = "2038-01-01 00:00:00";

    /****
     * 获取时间间隔
     *
     * @param beginDate
     * @param endDate
     * @param timeUnit 返回结果使用的时间单位
     * @return
     */
    public static long getTimeInterval(Date beginDate, Date endDate, TimeUnit timeUnit) {
        long intervaleMs = endDate.getTime() - beginDate.getTime();
        return timeUnit.convert(intervaleMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取当前小时
     */
    public static int getCurrentHour() {
        return getCalendar().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获 取当前分钟
     */
    public static int getCurrentMinute() {
        return getCalendar().get(Calendar.MINUTE);
    }

    /**
     * 获取当前天
     */
    public static int getCurrentDay() {
        return getCalendar().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 指定的毫秒long值转成Date类型
     */
    public static Date getMillisToDate(long value) {
        return new Date(value);
    }

    /**
     * 获取指定日期距1970年1月1日总秒
     */
    public static long getDateToSeconds(Date date) {
        if (date == null) {
            return getCalendar(TimeUtil.getSysteCurTime()).getTimeInMillis() / 1000;
        }
        return getCalendar(date).getTimeInMillis() / 1000;
    }

    /**
     * 获取指定日期距1970年1月1日总秒
     */
    public static long getDateToSecondsByString(String date) {
        if (date == null || date.length() == 0) {
            return getCalendar(TimeUtil.getSysteCurTime()).getTimeInMillis() / 1000;
        }
        return getDateToSeconds(format(date));
    }

    /**
     * 当前系统时间增加值
     */
    public static Date addSystemCurTime(int type, int value) {
        Calendar cal = getCalendar();
        switch (type) {
            case Calendar.DATE:// 增加天数
                cal.add(Calendar.DATE, value);
                break;
            case Calendar.HOUR:// 增加小时
                cal.add(Calendar.HOUR, value);
                break;
            case Calendar.MINUTE:// 增加分钟
                cal.add(Calendar.MINUTE, value);
                break;
            case Calendar.SECOND:// 增加秒
                cal.add(Calendar.SECOND, value);
                break;
            case Calendar.MILLISECOND:// 增加毫秒
                cal.add(Calendar.MILLISECOND, value);
                break;
            default:
                break;
        }
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 增加指定的时间间隔
     */
    public static Date addTimeInterval(Date date, int type, int value) {
        switch (type) {
            case Calendar.DATE:// 增加天
                return new Date(date.getTime() + (long) 24 * 60 * 60 * 1000 * value);
            case Calendar.HOUR:// 增加小时
                return new Date(date.getTime() + (long) 60 * 60 * 1000 * value);
            case Calendar.MINUTE:// 增加分钟
                return new Date(date.getTime() + (long) 60 * 1000 * value);
            case Calendar.SECOND:// 增加秒钟
                return new Date(date.getTime() + (long) 1000 * value);
            case Calendar.MILLISECOND:// 增加毫秒
                return new Date(date.getTime() + value);
            default:
                throw new IllegalArgumentException();
        }
    }

    public static long getAddSec(int type, int value) {
        Calendar cal = getCalendar();
        switch (type) {
            case Calendar.DATE:// 增加天数
                cal.add(Calendar.DATE, value);
                break;
            case Calendar.HOUR:// 增加小时
                cal.add(Calendar.HOUR, value);
                break;
            case Calendar.MINUTE:// 增加分钟
                cal.add(Calendar.MINUTE, value);
                break;
            case Calendar.SECOND:// 增加秒
                cal.add(Calendar.SECOND, value);
                break;
            case Calendar.MILLISECOND:// 增加毫秒
                cal.add(Calendar.MILLISECOND, value);
                break;
            default:
                break;
        }
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * <pre>
     * JSON的第二天时间
     * FastJson有Bug，0点整时间序列化异常，所以加1毫秒
     * </pre>
     */
    public static Date getNextDate() {
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * <pre>
     * 0点几分~0点几分的随机时间
     * FastJson有Bug，0点整时间序列化异常，所以加1毫秒
     * </pre>
     */
    public static Date getNextDateRandMins(int minSecond, int maxSecond) {
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, RandomUtil.rand(minSecond, maxSecond));
        cal.set(Calendar.SECOND, RandomUtil.rand(0, 60));
        cal.set(Calendar.MILLISECOND, RandomUtil.rand(1, 1000));
        return new Date(cal.getTimeInMillis());
    }

    /**
     * <pre>
     * 获取下一分钟起始时刻
     * </pre>
     */
    public static Date getNextMinute() {
        Calendar cal = getCalendar();
        cal.add(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    // 大富翁每日00:10重置
    public static Date getRichManNextDate() {
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 10);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MILLISECOND, 1);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 获取一个当天的00:01时间戳
     */
    public static Date getDayStart() {
        return getDayStart(new Date());
    }

    /**
     * 获取一个当天的00:01时间戳
     */
    public static Date getDayStart(Date start) {
        if (start == null) {
            start = new Date();
        }
        Calendar cal = getCalendar(start);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 获取一个当天的23:59时间戳
     */
    public static Date getDayEnd(Date end) {
        if (end == null) {
            end = new Date();
        }
        Calendar cal = getCalendar(end);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 1);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 获取一个当天的23:55时间戳
     */
    public static Date getDay55End(Date end) {
        if (end == null) {
            end = new Date();
        }
        Calendar cal = getCalendar(end);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 55);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    public static Date getLastDayStart() {
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return new Date(cal.getTimeInMillis());
    }

    public static Date getNextDayStart() {
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 格式化日期
     */
    public static String getDateFormat() {
        return getDateFormat(new Date());
    }

    /**
     * 格式化日期
     */
    public static String getDateFormat(Date date) {
        String ctime = formatter.format(date);
        return ctime;
    }

    /**
     * 格式化日期
     *
     * @return yyyyMMddHHmmss
     */
    public static String getNumDateFormat() {
        return getNumDateFormat(new Date());
    }

    /**
     * 格式化日期
     *
     * @return yyyyMMddHHmmss
     */
    public static String getNumDateFormat(Date date) {
        String ctime = formatterNum.format(date);
        return ctime;
    }

    /**
     * 格式化日期
     *
     * @return yyyyMMdd
     */
    public static String getNumDateOnlyFormat() {
        return getNumDateFormat(new Date());
    }

    /**
     * 格式化日期
     *
     * @return yyyyMMdd
     */
    public static String getNumDateOnlyFormat(Date date) {
        String ctime = formatterDateNum.format(date);
        return ctime;
    }

    /**
     * <pre>
     * 获取日期字符串
     * ！只有年月日，如：2016-06-14
     * </pre>
     */
    public static String getDateOnlyFormat(Date date) {
        String ctime = formatterDate.format(date);
        return ctime;
    }

    /**
     * 获取默认日期2000-01-01
     *
     * @return 返回默认起始时间
     */
    public static Date getDefaultDate() {
        Date defaultDate = null;
        try {
            defaultDate = (Date) formatter.parse(TIME_2000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(defaultDate.getTime());
    }

    /**
     * <pre>
     * 获取默认上限日期2099年
     * !转化成秒数时，需要用long类型存储
     * </pre>
     *
     * @return 返回默认上限时间
     */
    public final static Date getDefaultMaxDate() {
        Date defaultDate = null;
        try {
            defaultDate = (Date) formatter.parse(TIME_2999);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(defaultDate.getTime());
    }

    /**
     * <pre>
     * 获取默认上限日期2099年的秒数
     * </pre>
     */
    public final static long getDefaultMaxSecs() {
        return getDefaultMaxDate().getTime() / 1000;
    }

    /**
     * <pre>
     * 获取整形最大值上限日期2038年
     * ！转化成秒数时，可以用int类型存储
     * </pre>
     *
     * @return 返回默认上限时间
     */
    public final static Date get2038Date() {
        Date defaultDate = null;
        try {
            defaultDate = (Date) formatter.parse(TIME_2038);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(defaultDate.getTime());
    }

    /**
     * <pre>
     * 获取默认上限日期2038年的秒数
     * </pre>
     */
    public final static int get2038DateSecs() {
        return (int) (get2038Date().getTime() / 1000);
    }

    /**
     * <pre>
     * 比较日期是否同一天(注意：分界线为晚上 12 点)
     * </pre>
     *
     * @return 同一天返回true
     */
    public static boolean dateCompare(Date date) {
        if (date == null) {
            return false;
        }
        Calendar now = getCalendar();
        Calendar other = getCalendar(date);
        return dateCompare(now, other) == 0 ? true : false;
    }

    /**
     * <pre>
     * 比较日期是否同一天(注意：分界线为晚上 12 点)
     * </pre>
     */
    public static boolean dateCompare(long date) {
        Calendar now = getCalendar();
        Calendar other = getCalendar(getMillisToDate(date));
        return dateCompare(now, other) == 0 ? true : false;
    }

    /**
     * <pre>
     * 比较是否为同一天(注意：分界线为凌晨 5 点)
     * </pre>
     */
    public static boolean dateCompare5(Date date) {
        if (date == null) {
            return false;
        }
        Calendar now = getCalendar();
        now.add(Calendar.HOUR_OF_DAY, -5);
        Calendar other = getCalendar(date);
        other.add(Calendar.HOUR_OF_DAY, -5);
        if (dateCompare(now, other) == 0) {
            return true;
        }
        return false;
    }

    /**
     * <pre>
     * 比较日期是否同一天(注意：分界线为晚上 12 点)
     * </pre>
     */
    public static boolean dataCompare(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar c1 = getCalendar(date1);
        Calendar c2 = getCalendar(date2);
        return dateCompare(c1, c2) == 0 ? true : false;
    }

    /**
     * <pre>
     * 返回两个日期天数差（注意：分界线为晚上 12 点）
     * </pre>
     */
    public static int dateCompareDays(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar now = getCalendar();
        Calendar other = getCalendar(date);
        return dateCompare(now, other);
    }

    /**
     * 返回两个日期相差天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     */
    public static int dateCompare(Calendar startDate, Calendar endDate) {
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        int day = (int) (endDate.getTimeInMillis() / 1000 / 60 / 60 / 24 - startDate.getTimeInMillis() / 1000 / 60 / 60 / 24);
        return day;
    }

    /**
     * 间隔时间以小时为单位
     */
    public static boolean isInterval(Date startDate, int interval) {
        return dateCompare5(startDate);
    }

    /**
     * 获取系统时间
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 获取指定的时间
     */
    public static Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * <pre>
     * 今天是一周的周几
     * ! 周一返回1...
     * ! 周天返回7
     * </pre>
     */
    public static int getDayOfWeekIndex() {
        Calendar calendar = Calendar.getInstance();
        int index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (index == 0) {
            index = 7;
        }
        return index;
    }

    /**
     * <pre>
     *  今天是一月的第几天
     *  从0开始
     * </pre>
     */
    public static int getDayOfMonthIndex() {
        Calendar calendar = Calendar.getInstance();
        int index = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        if (index == 0) {
            index = 7;
        }
        return index;
    }


    /**
     * <pre>
     * 最近过去的整点
     * </pre>
     */
    public static Date getTheRecentPastHour() {
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.MILLISECOND, 1);
        cale.set(Calendar.SECOND, 0);
        cale.set(Calendar.MINUTE, 0);
        return cale.getTime();
    }

    /**
     * <pre>
     * 获取当前与时区无关的秒数
     * </pre>
     */
    public static int getCurSecs() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * <pre>
     * 判断是否为同一周, 和年月无关
     * 注意: 以周一0点为分界线
     * </pre>
     */
    public final static boolean dateCampareWeekOfMonday(Date date) {
        if (date == null) {
            return true;
        }

        Calendar other = getCalendar();
        other.setTime(date);
        // 当前时间
        Calendar now = Calendar.getInstance();
        int subYear = now.get(Calendar.YEAR) - other.get(Calendar.YEAR);
        if (subYear != 0) {
            return false;
        }
        // 已周一为标准的话就要减掉一天时间, java判定是否为同一周是从周天到周六,也就是说周天和后面一天周一是判定为同一周的
        // 比如:
        // 1.上次重置是周天, 今天为周一, 两个都减少一天, 则是周六, 周天, 判定结果为不是同一周
        // 2.上次重置是周一, 今天为周二, 两个都减少一天, 则是周天, 周一, 判定结果为是同一周
        now.add(Calendar.DAY_OF_YEAR, -1);
        other.add(Calendar.DAY_OF_YEAR, -1);
        if (now.get(Calendar.WEEK_OF_YEAR) == other.get(Calendar.WEEK_OF_YEAR)) {
            return true;
        }
        return false;
    }

    /**
     * <pre>
     * 字符串转日期
     * </pre>
     */
    public static Date format(String str) {
        if (str == null) {
            return null;
        }
        Date date = null;
        try {
            date = formatter.parse(str);
        } catch (ParseException e) {
            Log.error("", e);
            date = new Date();
        }
        return date;
    }

    /**
     * <pre>
     * 验证并转换日期
     * </pre>
     */
    public static Date validDate(String str) {
        if (str == null) {
            return null;
        }
        Date date = null;
        try {
            date = formatter.parse(str);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    /**
     * <pre>
     * 是否在同一周
     * 否在同一周
     * 周第一天为周一
     * </pre>
     */
    public static boolean inSameWeek(Date date) {
        return inSameWeek(date, new Date());
    }

    /**
     * <pre>
     * 是否在同一周
     * 周第一天为周一
     * </pre>
     */
    public static boolean inSameWeek(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal1.setFirstDayOfWeek(Calendar.MONDAY);
        cal2.setFirstDayOfWeek(Calendar.MONDAY);
        return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    /**
     * <pre>
     * 是否在同一天
     * </pre>
     */
    public static boolean inSameDay(Date date) {
        if (date == null) {
            return false;
        }
        return inSameDay(date, new Date());
    }

    /**
     * <pre>
     * 是否在同一天
     * </pre>
     */
    public static boolean inSameDay(int secs) {
        return inSameDay(new Date((long) (secs * 1000)), new Date());
    }

    /**
     * <pre>
     * 是否在同一天
     * </pre>
     */
    public static boolean inSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    /**
     * <pre>
     * 当前系统时间的时间撮
     * </pre>
     */
    public static Date getSysteCurTime() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 获取系统距1970年1月1日总毫秒
     */
    public static long getSysCurTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * <pre>
     * 时间是否在0点左右
     * </pre>
     */
    public final static boolean during0ResetTime() {
        try {
            Calendar cal = Calendar.getInstance();
            // 时间段1：23:50:00-23:59:59
            boolean during1 = false;
            if (cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) >= 50) {
                during1 = true;
            }
            // 时间段2：00:00:00-00:10:59
            boolean during2 = false;
            if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) <= 10) {
                during2 = true;
            }
            return during1 || during2;
        } catch (Exception e) {
            Log.error("during0ResetTime方法异常", e);
        }
        return false;
    }

    /**
     * <pre>
     * 时间是否在0:00-0:02分
     * </pre>
     */
    public final static boolean during0ResetTime2() {
        try {
            Calendar cal = Calendar.getInstance();
            boolean during2 = false;
            if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) <= 1) {
                during2 = true;
            }
            return during2;
        } catch (Exception e) {
            Log.error("during0ResetTime2方法异常", e);
        }
        return false;
    }

    /**
     * <pre>
     * 时间是否在00:00-23:55分
     * </pre>
     */
    public final static boolean duringTime55() {
        try {
            Calendar cal = Calendar.getInstance();
            boolean during2 = true;
            if (cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) >= 55) {
                during2 = false;
            }
            return during2;
        } catch (Exception e) {
            Log.error("duringTime55方法异常", e);
        }
        return false;
    }

    /**
     * <pre>
     * 获取下一个小时的整点
     * </pre>
     */
    public final static Date getTheRecentNextHour() {
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.MILLISECOND, 1);
        cale.set(Calendar.SECOND, 0);
        cale.set(Calendar.MINUTE, 0);
        cale.add(Calendar.HOUR_OF_DAY, 1);
        return cale.getTime();
    }

    /**
     * <pre>
     * 将时间设置为当天0点整
     * </pre>
     */
    public final static Calendar setDayStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal;
    }

    public static void main(String[] args) {
        // testComFastFormat();
        // testJDK8Format();
        // testComFastFormat();
        // testJDK8Format();
        //System.out.println(getDateFormat(getDayEnd(new Date())));
    }

    /**
     * <pre>
     * Common.lang时间格式化
     * Common.FastDateFormat时间格式化1000000次，耗时：339ms,结果：2018-06-05 15:06:23
     * </pre>
     */
    public final static void testComFastFormat() {
        final int size = 1000000;
        String dateStr = "";
        long st = System.currentTimeMillis();
        Date date = new Date();
        for (int i = 0; i < size; i++) {
            dateStr = formatter.format(date);
        }
        long ct = System.currentTimeMillis() - st;
        System.out.println("Common.FastDateFormat时间格式化" + size + "次，耗时：" + ct + "ms,结果：" + dateStr);
    }

    /**
     * <pre>
     * JDK8时间格式化
     * JDK8.DateTimeFormatter时间格式化1000000次，耗时：280ms,结果：2018-06-05 15:06:24
     * </pre>
     */
    public final static void testJDK8Format() {
        final int size = 1000000;
        String dateStr = "";
        long st = System.currentTimeMillis();
        LocalDateTime date = LocalDateTime.now();
        final DateTimeFormatter jdk8Fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < size; i++) {
            dateStr = jdk8Fmt.format(date);
        }
        long ct = System.currentTimeMillis() - st;
        System.out.println("JDK8.DateTimeFormatter时间格式化" + size + "次，耗时：" + ct + "ms,结果：" + dateStr);
    }

}
