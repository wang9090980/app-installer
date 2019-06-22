package me.panpf.app.install.auto;

import com.google.gson.annotations.SerializedName;

public class Identify {
    @SerializedName("type")
    private Type type;

    @SerializedName("values")
    private IdentifyValues values;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public IdentifyValues getValues() {
        return values;
    }

    public void setValues(IdentifyValues values) {
        this.values = values;
    }

    public enum Type{
        @SerializedName("systemProperty")
        SYSTEM_PROPERTY,

        @SerializedName("default")
        DEFAULT
    }
}
