package me.panpf.app.install.auto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Configuration {
    @SerializedName("name")
    private String name;

    @SerializedName("identify")
    private Identify identify;

    @SerializedName("pages")
    private List<TargetPage> targetPageList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Identify getIdentify() {
        return identify;
    }

    public void setIdentify(Identify identify) {
        this.identify = identify;
    }

    public List<TargetPage> getTargetPageList() {
        return targetPageList;
    }

    public void setTargetPageList(List<TargetPage> targetPageList) {
        this.targetPageList = targetPageList;
    }

    public AccessibilityHandler getAccessibilityHandler(AccessibilityHandlerListener accessibilityHandlerListener) {
        return new DefaultAccessibilityHandler(accessibilityHandlerListener, targetPageList);
    }
}
