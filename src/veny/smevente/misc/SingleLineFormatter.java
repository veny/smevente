package veny.smevente.misc;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * To eliminate the most annoying thing about
 * <code>java.util.logging.SimpleFormatter</code>: writing out the log message
 * on two lines.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 03.03.2011
 * {@link http://www.letor.ca/post/single-line-java-logging}
 */
public class SingleLineFormatter extends Formatter {

    /** Date formatter. */
    private static SimpleDateFormat dateFormatter;

    /** Static constructor. */
    static {
        // check if the date format was specified
        dateFormatter = new SimpleDateFormat(System.getProperty("java.util.logging.dateFormat", "yyMMdd HH:mm:ss"));
    }

    /** {@inheritDoc} */
    @Override
    public String format(final LogRecord record) {
        // use the buffer for optimal string construction
        final StringBuffer sb = new StringBuffer();

        // level
        sb.append(record.getLevel().toString());
        sb.append(":\t");

        // format time
        sb.append(dateFormatter.format(record.getMillis())).append(" ");

        // thread
        sb.append("[").append(Thread.currentThread().getName()).append("] ");

        // package/class name, logging name
        final String name = record.getLoggerName();

        sb.append(name);
        sb.append(" | ");
        sb.append(record.getMessage());

        // if there was an exception thrown, log it as well
        if (null != record.getThrown()) {
            sb.append("\n").append(printThrown(record.getThrown()));
        }

        sb.append("\n");

        return sb.toString();
    }

    /**
     * Prints a stack trace.
     * @param thrown an exception
     * @return formated stack trace
     */
    private String printThrown(final Throwable thrown) {
        final StringBuffer sb = new StringBuffer();

        sb.append(thrown.getClass().getName());
        sb.append(" - ").append(thrown.getMessage());
        sb.append("\n");

        for (StackTraceElement trace : thrown.getStackTrace()) {
            sb.append("\tat ").append(trace).append("\n");
        }

        final Throwable cause = thrown.getCause();
        if (null != cause) {
            sb.append("\n").append(printThrown(cause));
        }

        return sb.toString();
    }

}
