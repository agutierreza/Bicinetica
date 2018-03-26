package bicinetica.com.bicinetica.diagnostics;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Trace {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static Writer writer;

    public static void init() {

        File log = new File(Environment.getExternalStorageDirectory(), "bicinetica.txt");
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            writer = new FileWriter(log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void critical(String message) {
        trace(TraceEventType.Critical, message);
    }
    public static void critical(String message, Object... args) {
        trace(TraceEventType.Critical, message, args);
    }
    public static void critical(Throwable ex) {
        exception(TraceEventType.Critical, ex);
    }

    public static void error(String message) {
        trace(TraceEventType.Error, message);
    }
    public static void error(String message, Object... args) {
        trace(TraceEventType.Error, message, args);
    }
    public static void error(Throwable ex) {
        exception(TraceEventType.Error, ex);
    }

    public static void warning(String message) {
        trace(TraceEventType.Warning, message);
    }
    public static void warning(String message, Object... args) {
        trace(TraceEventType.Warning, message, args);
    }
    public static void warning(Throwable ex) {
        exception(TraceEventType.Warning, ex);
    }

    public static void info(String message) {
        trace(TraceEventType.Info, message);
    }
    public static void info(String message, Object... args) {
        trace(TraceEventType.Info, message, args);
    }

    public static void debug(String message) {
        trace(TraceEventType.Debug, message);
    }
    public static void debug(String message, Object... args) {
        trace(TraceEventType.Debug, message, args);
    }

    public static void verbose(String message) {
        trace(TraceEventType.Verbose, message);
    }
    public static void verbose(String message, Object... args) {
        trace(TraceEventType.Verbose, message, args);
    }

    public static void start(String message) {
        trace(TraceEventType.Start, message);
    }
    public static void start(String message, Object... args) {
        trace(TraceEventType.Start, message, args);
    }

    public static void stop(String message) {
        trace(TraceEventType.Stop, message);
    }
    public static void stop(String message, Object... args) {
        trace(TraceEventType.Stop, message, args);
    }

    private static void trace(TraceEventType eventType, String format, Object... args) {
        trace(eventType, String.format(format, args));
    }
    private static void trace(TraceEventType eventType, String message) {
        try {
            writer.append(String.format("[%s] %s:\t%s\n\r", DATE_FORMAT.format(Calendar.getInstance().getTime()), eventType, message));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exception(TraceEventType eventType, Throwable ex) {
        trace(eventType, Log.getStackTraceString(ex));
    }
}
