package me.panpf.app.install;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 安装状态
 */
@IntDef({
        InstallStatus.INSTALL_QUEUEING,
        InstallStatus.INSTALLING,
        InstallStatus.INSTALL_DECOMPRESSING,
})
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface InstallStatus {

    /**
     * 安装队列中
     */
    int INSTALL_QUEUEING = 1211;

    /**
     * 安装中
     */
    int INSTALLING = 1221;

    /**
     * 安装解压中
     */
    int INSTALL_DECOMPRESSING = 1231;
}