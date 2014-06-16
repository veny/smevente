package veny.smevente.client.utils;

import java.util.Date;

/**
 * A collection of date and time oriented utilities.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.6.2010
 */
public final class DateUtils {

    /** Seconds in day. */
    public static final long SEC_IN_DAY = 3600L * 24L;
    /** Milliseconds in day. */
    public static final long MILIS_IN_DAY = 1000L * SEC_IN_DAY;
    /** Calendar column height. */
    public static final int CALENDAR_COLUMN_HEIGHT = 2016; /* CALENDAR ROWS */

    /** Suppresses default constructor, ensuring non-instantiability. */
    private DateUtils() { }


    /**
     * Gets time created in browser recalculated into UTC.
     * Object of <code>Date</code> instantiated in browser is created including the browser time zone offset.
     *
     * @param withBrowserTz date if time zone offset
     * @return date with UTC time
     */
    @SuppressWarnings("deprecation")
    public static Date toUTC(final Date withBrowserTz) {
        withBrowserTz.setTime(withBrowserTz.getTime() + (withBrowserTz.getTimezoneOffset() * 60 * 1000));
        return withBrowserTz;
    }

    /**
     * Gets current time in UTC.
     * @return current time recalculated into UTC
     */
    public static Date nowUTC() {
        return toUTC(new Date());
    }

    /**
     * Gets <code>Date</code> of a given day in current week.
     *
     * @param dayIdx index of the day (Mon=1, Sun=7)
     * @return the date
     */
    public static Date getDateInCurrentWeek(final int dayIdx) {
        final Date today = nowUTC();
        int todayIdx = getWeekIndex(today);

        return new Date(today.getTime() - ((todayIdx - dayIdx) * MILIS_IN_DAY));
    }

    /**
     * Updates date to time 0:0:0.
     * @param d date to change
     * @return given date with time 0:0:0
     */
    @SuppressWarnings("deprecation")
    public static Date getStartOfDay(final Date d) {
        final Date rslt = (Date) d.clone();
        rslt.setHours(0);
        rslt.setMinutes(0);
        rslt.setSeconds(0);
        return rslt;
    }

    /**
     * Updates date to time 23:59:59.
     * @param d date to change
     * @return given date with time 23:59:59
     */
    @SuppressWarnings("deprecation")
    public static Date getEndOfDay(final Date d) {
        final Date rslt = (Date) d.clone();
        rslt.setHours(23);
        rslt.setMinutes(59);
        rslt.setSeconds(59);
        return rslt;
    }

    /**
     * Calculates date and time from click position in calendar.
     * @param dayIdx index of day column
     * @param y relative Y coordinate
     * @return corresponding date and time
     */
    @SuppressWarnings("deprecation")
    public static Date calculateDateFromClick(final int dayIdx, final int y) {
        final Date rslt = getStartOfDay(getDateInCurrentWeek(dayIdx));

        // 150 == 2,5 min (rounded to 5 min)
        final int secInDay = (int) ((float) y / (float) CALENDAR_COLUMN_HEIGHT * (float) SEC_IN_DAY) + 150;

        rslt.setHours(secInDay / 3600);
        rslt.setMinutes((secInDay % 3600) / 60 / 5 * 5);
        rslt.setSeconds(0);

        return rslt;
    }

    /**
     * Calculates Y coordinate in calendar according to start time of medical help.
     * @param date start time
     * @return Y coordinate in day column
     */
    @SuppressWarnings("deprecation")
    public static int calculateYFromDate(final Date date) {
        final int secInDay = (date.getHours() * 3600) + (date.getMinutes() * 60) + date.getSeconds();
        return (int) ((float) secInDay / (float) SEC_IN_DAY * (float) CALENDAR_COLUMN_HEIGHT);
    }

    /**
     * Calculates Y coordinate in calendar according to start time of medical help.
     * @param length time in minutes
     * @return height of widget
     */
    public static int calculateWidgetHeight(final int length) {
        final int sec = length * 60;
        return (int) ((float) sec / (float) SEC_IN_DAY * (float) CALENDAR_COLUMN_HEIGHT);
    }

    /**
     * Gets date of Monday 0:0:0 in week around given.
     *
     * @param date date to calculate Monday in its week
     * @return date of Monday 0:0:0 in current week
     */
    public static Date getWeekFrom(final Date date) {
        final int dayIdx = getWeekIndex(date);
        final Date from = new Date(date.getTime() + ((long) (1 - dayIdx) * MILIS_IN_DAY));
        return getStartOfDay(from);
    }

    /**
     * Gets date of Sunday 23:59:59 in week around given date.
     *
     * @param date date to calculate Sunday in its week
     * @return date of Sunday 23:59:59 in current week
     */
    public static Date getWeekTo(final Date date) {
        final int dayIdx = getWeekIndex(date);
        final Date to = new Date(date.getTime() + ((long) (7 - dayIdx) * MILIS_IN_DAY));
        return getEndOfDay(to);
    }

    /**
     * Gets index of day in week according to given date (Mon=1, Sun=7).
     * @param date date to calculate the index
     * @return index of day in current week
     */
    public static int getWeekIndex(final Date date) {
        @SuppressWarnings("deprecation")
        int dayIdx = date.getDay();
        if (0 == dayIdx) { dayIdx = 7; } // Mon=1, Sun=7
        return dayIdx;
    }

    /**
     * Gets a new date moved for a given weeks in time.
     * @param weekDate time to be moved
     * @param offset number of weeks (negative - history, positive - future)
     * @return moved date
     */
    @SuppressWarnings("deprecation")
    public static Date getOtherWeek(final Date weekDate, final int offset) {
        if (null == weekDate) { throw new NullPointerException("date cannot be null"); }
        final Date newWeekDate = (Date) weekDate.clone();
        newWeekDate.setDate(newWeekDate.getDate() + (7 * offset));
        return newWeekDate;
    }

    /**
     * For testing purposes.
     * @param args CLI arguments
     * @exception Exception if something goes wrong
     */
    @SuppressWarnings("deprecation")
    public static void main(final String[] args) throws Exception {
//        Date d = new Date(1277676000931L);
//        Date d = new Date(1280699999931L);

        Date d = new Date();
        d.setDate(1);
        d.setMonth(8);
        d = getStartOfDay(d);
        System.out.println(d + " | " + d.getTime()); //CSOFF
        d.setDate(30);
        d.setMonth(8);
        d = getEndOfDay(d);
        System.out.println(d + " | " + d.getTime()); //CSOFF

//        Charset charset = Charset.forName("ASCII");
//        CharsetDecoder decoder = charset.newDecoder();
//        CharsetEncoder encoder = charset.newEncoder();
//
//        ByteBuffer bbuf = encoder.encode(CharBuffer.wrap("Sýkora Chládková"));
//        CharBuffer cbuf = decoder.decode(bbuf);
//        System.out.println(cbuf.toString()); //CSOFF
    }

}
