package me.panpf.app.install.auto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TargetPage {
    @SerializedName("className")
    public String className;

    @SerializedName("startPage")
    public boolean startPage;

    @SerializedName("donePage")
    public boolean donePage;

    @SerializedName("buttons")
    public List<TargetButton> targetButtonList;

    public String getInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("className").append(":").append(className).append("; ");
        builder.append("startPage").append(":").append(startPage).append("; ");
        builder.append("donePage").append(":").append(donePage).append("; ");
        builder.append("targetButtonList").append(":").append("[");
            if(targetButtonList != null && !targetButtonList.isEmpty()){
                for(TargetButton button : targetButtonList){
                    builder.append(button.getInfo()).append(", ");
                }
            }
        builder.append("]");
        return builder.toString();
    }
}
