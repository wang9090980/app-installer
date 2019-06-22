package me.panpf.app.install;

import androidx.annotation.IntDef;
import android.text.TextUtils;
import android.util.Log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

public class AILog {

    @SuppressWarnings("WeakerAccess")
    public static final int VERBOSE = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int DEBUG = 2;
    @SuppressWarnings("WeakerAccess")
    public static final int INFO = 4;
    @SuppressWarnings("WeakerAccess")
    public static final int WARNING = 8;
    @SuppressWarnings("WeakerAccess")
    public static final int ERROR = 16;
    @SuppressWarnings("WeakerAccess")
    public static final int NONE = 32;

    public static final String NAME_VERBOSE = "VERBOSE";
    public static final String NAME_DEBUG = "DEBUG";
    public static final String NAME_INFO = "INFO";
    public static final String NAME_WARNING = "WARNING";
    public static final String NAME_ERROR = "ERROR";
    public static final String NAME_NONE = "NONE";

    private static final String TAG = "AppInstaller";
    private static final String FORMAT_MSG = "%s. %s";
    private static int level = INFO;
    private static Proxy proxy = new ProxyImpl();

    /**
     * 设置日志代理，你可以借此自定义日志的输出方式
     *
     * @param proxy null: 恢复为默认的日志代理
     */
    public static void setProxy(Proxy proxy) {
        if (AILog.proxy != proxy) {
            AILog.proxy.onReplaced();
            AILog.proxy = proxy != null ? proxy : new ProxyImpl();
        }
    }

    /**
     * 获取日志级别
     */
    @Level
    public static int getLevel() {
        return level;
    }

    /**
     * 设置日志级别，用于过滤日志
     */
    public static void setLevel(@Level int level) {
        if (AILog.level != level) {
            String oldLevelName = getLevelName();
            AILog.level = level;
            Log.w(TAG, "AILog. " + String.format("setLevel. %s -> %s", oldLevelName, getLevelName()));
        }
    }

    /**
     * 获取级别名称
     */
    @SuppressWarnings("WeakerAccess")
    public static String getLevelName() {
        switch (level) {
            case VERBOSE:
                return NAME_VERBOSE;
            case DEBUG:
                return NAME_DEBUG;
            case INFO:
                return NAME_INFO;
            case WARNING:
                return NAME_WARNING;
            case ERROR:
                return NAME_ERROR;
            case NONE:
                return NAME_NONE;
            default:
                return "UNKNOWN(" + level + ")";
        }
    }

    /**
     * 指定的 log 级别是否可用
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean isLoggable(@Level int level) {
        return level >= AILog.level;
    }

    private static String joinMessage(String scope, String msg) {
        return TextUtils.isEmpty(scope) ? msg : String.format(Locale.US, FORMAT_MSG, scope, msg);
    }

    @SuppressWarnings("unused")
    public static int v(String msg) {
        if (!isLoggable(VERBOSE)) {
            return 0;
        }

        return proxy.v(TAG, msg);
    }

    @SuppressWarnings("unused")
    public static int v(String scope, String msg) {
        if (!isLoggable(VERBOSE)) {
            return 0;
        }

        return proxy.v(TAG, joinMessage(scope, msg));
    }

    @SuppressWarnings("unused")
    public static int v(String scope, String msg, Throwable tr) {
        if (!isLoggable(VERBOSE)) {
            return 0;
        }
        return proxy.v(TAG, joinMessage(scope, msg), tr);
    }

    public static int d(String msg) {
        if (!isLoggable(DEBUG)) {
            return 0;
        }
        return proxy.d(TAG, msg);
    }

    public static int d(String scope, String msg) {
        if (!isLoggable(DEBUG)) {
            return 0;
        }
        return proxy.d(TAG, joinMessage(scope, msg));
    }

    @SuppressWarnings("unused")
    public static int d(String scope, String msg, Throwable tr) {
        if (!isLoggable(DEBUG)) {
            return 0;
        }
        return proxy.d(TAG, joinMessage(scope, msg), tr);
    }

    public static int i(String msg) {
        if (!isLoggable(INFO)) {
            return 0;
        }
        return proxy.i(TAG, msg);
    }

    public static int i(String scope, String msg) {
        if (!isLoggable(INFO)) {
            return 0;
        }
        return proxy.i(TAG, joinMessage(scope, msg));
    }

    @SuppressWarnings("unused")
    public static int i(String scope, String msg, Throwable tr) {
        if (!isLoggable(INFO)) {
            return 0;
        }
        return proxy.i(TAG, joinMessage(scope, msg), tr);
    }

    public static int w(String msg) {
        if (!isLoggable(WARNING)) {
            return 0;
        }
        return proxy.w(TAG, msg);
    }

    public static int w(String scope, String msg) {
        if (!isLoggable(WARNING)) {
            return 0;
        }
        return proxy.w(TAG, joinMessage(scope, msg));
    }

    @SuppressWarnings("unused")
    public static int w(String scope, String msg, Throwable tr) {
        if (!isLoggable(WARNING)) {
            return 0;
        }
        return proxy.w(TAG, joinMessage(scope, msg), tr);
    }

    @SuppressWarnings("unused")
    public static int w(String scope, Throwable tr) {
        if (!isLoggable(WARNING)) {
            return 0;
        }
        return proxy.w(TAG, joinMessage(scope, ""), tr);
    }

    public static int e(String msg) {
        if (!isLoggable(ERROR)) {
            return 0;
        }
        return proxy.e(TAG, msg);
    }

    public static int e(String scope, String msg) {
        if (!isLoggable(ERROR)) {
            return 0;
        }
        return proxy.e(TAG, joinMessage(scope, msg));
    }

    @SuppressWarnings("unused")
    public static int e(String scope, String msg, Throwable tr) {
        if (!isLoggable(ERROR)) {
            return 0;
        }
        return proxy.e(TAG, joinMessage(scope, msg), tr);
    }

    @SuppressWarnings("WeakerAccess")
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE})
    @IntDef({
            VERBOSE,
            DEBUG,
            INFO,
            WARNING,
            ERROR,
            NONE,
    })
    public @interface Level {

    }

    public interface Proxy {
        int v(String tag, String msg);

        int v(String tag, String msg, Throwable tr);

        int d(String tag, String msg);

        int d(String tag, String msg, Throwable tr);

        int i(String tag, String msg);

        int i(String tag, String msg, Throwable tr);

        int w(String tag, String msg);

        int w(String tag, String msg, Throwable tr);

        int w(String tag, Throwable tr);

        int e(String tag, String msg);

        int e(String tag, String msg, Throwable tr);

        void onReplaced();
    }

    private static class ProxyImpl implements Proxy {

        @Override
        public int v(String tag, String msg) {
            return Log.v(tag, msg);
        }

        @Override
        public int v(String tag, String msg, Throwable tr) {
            return Log.v(tag, msg, tr);
        }

        @Override
        public int d(String tag, String msg) {
            return Log.d(tag, msg);
        }

        @Override
        public int d(String tag, String msg, Throwable tr) {
            return Log.d(tag, msg, tr);
        }

        @Override
        public int i(String tag, String msg) {
            return Log.i(tag, msg);
        }

        @Override
        public int i(String tag, String msg, Throwable tr) {
            return Log.i(tag, msg, tr);
        }

        @Override
        public int w(String tag, String msg) {
            return Log.w(tag, msg);
        }

        @Override
        public int w(String tag, String msg, Throwable tr) {
            return Log.w(tag, msg, tr);
        }

        @Override
        public int w(String tag, Throwable tr) {
            return Log.w(tag, tr);
        }

        @Override
        public int e(String tag, String msg) {
            return Log.e(tag, msg);
        }

        @Override
        public int e(String tag, String msg, Throwable tr) {
            return Log.e(tag, msg, tr);
        }

        @Override
        public void onReplaced() {

        }
    }
}
