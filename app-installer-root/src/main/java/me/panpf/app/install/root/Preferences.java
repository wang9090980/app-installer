package me.panpf.app.install;

public interface Preferences {
    boolean isEnabledRootInstall();

    void setEnabledRootInstall(boolean enabled);

    int getRemindEnableRootInstallCount();

    void setRemindEnableRootInstallCount(int count);

    long getNextRemindEnableRootInstallTime();

    void setNextRemindEnableRootInstallTime(long time);

    int getRootInstallFailedCount();

    void setRootInstallFailedCount(int count);


    boolean isOpenedAutoInstallService();

    void setOpenedAutoInstallService(boolean opened);

    boolean isNoLongerRemindAutoInstall();

    void setNoLongerRemindAutoInstall(boolean noLonger);

    int getRemindAutoInstallCount();

    void setRemindAutoInstallCount(int count);

    long getNextRemindAutoInstallTime();

    void setNextRemindAutoInstallTime(long time);

    boolean isAutoInstallServiceRunning();

    void setAutoInstallServiceRunning(boolean running);

    int getAutoInstallAppCount();

    void setAutoInstallAppCount(int count);

    int getClosedUnexpectedlyRemindCount();

    void setClosedUnexpectedlyRemindCount(int count);
}
