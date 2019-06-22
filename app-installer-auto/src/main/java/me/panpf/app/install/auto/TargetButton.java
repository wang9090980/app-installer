package me.panpf.app.install.auto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TargetButton {
    /**
     * 类名
     */
    @SerializedName("className")
    public String className;

    /**
     * 名称列表
     */
    @SerializedName("names")
    public List<String> nameList;

    /**
     * 是否是安装按钮
     */
    @SerializedName("installButton")
    public boolean installButton;

    /**
     * 是否是完成按钮
     */
    @SerializedName("doneButton")
    public boolean doneButton;

    public String getInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("className").append(":").append(className).append("; ");
        builder.append("targetButtonList").append(":").append("{");
        if(nameList != null && !nameList.isEmpty()){
            for(String name : nameList){
                builder.append(name).append(", ");
            }
        }
        builder.append("}").append("; ");
        builder.append("installButton").append(":").append(installButton).append("; ");
        builder.append("doneButton").append(":").append(doneButton);
        return builder.toString();
    }
}
