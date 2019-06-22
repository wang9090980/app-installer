package me.panpf.app.install;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesImpl implements Preferences {
    private SharedPreferences preferences;

    PreferencesImpl(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public boolean isEnabledRootInstall() {
        return preferences.getBoolean("enabled_root_install", false);
    }

    @Override
    public void setEnabledRootInstall(boolean enabled) {
        preferences.edit().putBoolean("enabled_root_install", enabled).apply();
    }

    @Override
    public int getRemindEnableRootInstallCount() {
        return preferences.getInt("remind_enable_root_install_count", 0);
    }

    @Override
    public void setRemindEnableRootInstallCount(int count) {
        preferences.edit().putInt("remind_enable_root_install_count", count).apply();
    }

    @Override
    public long getNextRemindEnableRootInstallTime() {
        return preferences.getLong("next_remind_enable_root_install_time", 0);
    }

    @Override
    public void setNextRemindEnableRootInstallTime(long time) {
        preferences.edit().putLong("next_remind_enable_root_install_time", time).apply();
    }

    @Override
    public int getRootInstallFailedCount() {
        return preferences.getInt("root_install_failed_count", 0);
    }

    @Override
    public void setRootInstallFailedCount(int count) {
        preferences.edit().putInt("root_install_failed_count", count).apply();
    }

    @Override
    public boolean isOpenedAutoInstallService() {
        return preferences.getBoolean("opened_auto_install_service", false);
    }

    @Override
    public void setOpenedAutoInstallService(boolean opened) {
        preferences.edit().putBoolean("opened_auto_install_service", opened).apply();
    }

    @Override
    public boolean isNoLongerRemindAutoInstall() {
        return preferences.getBoolean("no_longer_remind_auto_install", false);
    }

    @Override
    public void setNoLongerRemindAutoInstall(boolean noLonger) {
        preferences.edit().putBoolean("no_longer_remind_auto_install", noLonger).apply();
    }

    @Override
    public int getRemindAutoInstallCount() {
        return preferences.getInt("remind_auto_install_count", 0);
    }

    @Override
    public void setRemindAutoInstallCount(int count) {
        preferences.edit().putInt("remind_auto_install_count", count).apply();
    }

    @Override
    public long getNextRemindAutoInstallTime() {
        return preferences.getLong("next_remind_auto_install_time", 0);
    }

    @Override
    public void setNextRemindAutoInstallTime(long time) {
        preferences.edit().putLong("next_remind_auto_install_time", time).apply();
    }

    @Override
    public boolean isAutoInstallServiceRunning() {
        return preferences.getBoolean("auto_install_service_running", false);
    }

    @Override
    public void setAutoInstallServiceRunning(boolean running) {
        preferences.edit().putBoolean("auto_install_service_running", running).apply();
    }

    @Override
    public int getAutoInstallAppCount() {
        return preferences.getInt("auto_install_app_count", 0);
    }

    @Override
    public void setAutoInstallAppCount(int count) {
        preferences.edit().putInt("auto_install_app_count", count).apply();
    }

    @Override
    public int getClosedUnexpectedlyRemindCount() {
        return preferences.getInt("closed_unexpectedly_remind_count", 0);
    }

    @Override
    public void setClosedUnexpectedlyRemindCount(int count) {
        preferences.edit().putInt("closed_unexpectedly_remind_count", count).apply();
    }
}
