package me.panpf.app.install;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 安装状态
 */
@IntDef(value = {
        InstallStatus.WAITING_QUEUE,
        InstallStatus.INSTALLING,
        InstallStatus.DECOMPRESSING,
}, open = true)
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface InstallStatus {

    /**
     * 排队中
     */
    int WAITING_QUEUE = 1211;

    /**
     * 安装中
     */
    int INSTALLING = 1221;

    /**
     * 解压中
     */
    int DECOMPRESSING = 1231;
}