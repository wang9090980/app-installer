package me.panpf.app.install.auto;

import com.google.gson.annotations.SerializedName;

public class IdentifyValues {
    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    @SerializedName("rule")
    private Rule rule;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public enum Rule{
        @SerializedName("exist")
        EXIST,

        @SerializedName("matchValue")
        MATCH_VALUE,

        @SerializedName("startsWithValue")
        STARTS_WITH_VALUE,
    }
}
