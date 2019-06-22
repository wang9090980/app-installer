package me.panpf.app.install.auto;

import android.content.Context;

import me.panpf.app.install.AILog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import me.panpf.javax.io.Streamx;

public class ConfigurationManager {
    public static final String NAME = "ConfigurationManager";
    private static ConfigurationManager instance;
    private Configuration configuration;

    private ConfigurationManager(Context context) {
        checkRomInfo(context);
    }

    public static ConfigurationManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager(context);
                }
            }
        }
        return instance;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void checkRomInfo(Context context) {
        List<Configuration> configurationList = readConfigurationList(context);
        if (configurationList == null || configurationList.size() == 0) {
            return;
        }

        Configuration finalConfiguration = null;
        for (Configuration configuration : configurationList) {
            Identify identify = configuration.getIdentify();
            if (identify == null) {
                AILog.w(NAME, "checkRomInfo. not found param " + configuration.getName() + ".identify");
                continue;
            }

            if (identify.getType() == null) {
                AILog.w(NAME, "checkRomInfo. not found param " + configuration.getName() + ".identify.type");
                continue;
            }

            if (identify.getType() == Identify.Type.SYSTEM_PROPERTY) {
                IdentifyValues identifyValues = identify.getValues();
                if (identifyValues == null) {
                    AILog.w(NAME, "checkRomInfo. not found param " + configuration.getName() + ".identifyValues");
                    continue;
                }

                if (identifyValues.getKey() == null) {
                    AILog.w(NAME, "checkRomInfo. not found param " + configuration.getName() + ".identify.key");
                    continue;
                }

                if (identifyValues.getRule() == null) {
                    AILog.w(NAME, "checkRomInfo. not found param " + configuration.getName() + ".identify.rule");
                    continue;
                }

                String systemFlag = AutoInstallUtils.getSystemProperty(identifyValues.getKey());
                if (identifyValues.getRule() == IdentifyValues.Rule.MATCH_VALUE) {
                    if (identifyValues.getValue() == null) {
                        AILog.w(NAME, "checkRomInfo. not found param " + configuration.getName() + ".identify.value");
                        continue;
                    }
                    if (systemFlag != null && systemFlag.equals(identifyValues.getValue())) {
                        finalConfiguration = configuration;
                        break;
                    } else {
                        AILog.d(NAME, "checkRomInfo. not " + configuration.getName());
                    }
                } else if (identifyValues.getRule() == IdentifyValues.Rule.STARTS_WITH_VALUE) {
                    if (systemFlag != null && systemFlag.startsWith(identifyValues.getValue())) {
                        finalConfiguration = configuration;
                        AILog.d(NAME, "checkRomInfo. is " + configuration.getName());
                        break;
                    } else {
                        AILog.d(NAME, "checkRomInfo. not " + configuration.getName());
                    }
                } else if (identifyValues.getRule() == IdentifyValues.Rule.EXIST) {
                    if (systemFlag != null && !"".equalsIgnoreCase(systemFlag)) {
                        finalConfiguration = configuration;
                        AILog.d(NAME, "checkRomInfo. is " + configuration.getName());
                        break;
                    } else {
                        AILog.d(NAME, "checkRomInfo. not " + configuration.getName());
                    }
                } else {
                    AILog.d(NAME, "checkRomInfo. not " + configuration.getName());
                }
            } else if (identify.getType() == Identify.Type.DEFAULT) {
                finalConfiguration = configuration;
                AILog.d(NAME, "checkRomInfo. is " + configuration.getName());
                break;
            } else {
                AILog.d(NAME, "checkRomInfo. not " + configuration.getName());
            }
        }

        this.configuration = finalConfiguration;
    }

    private List<Configuration> readConfigurationList(Context context) {
        String configurationJson = null;
        try {
            configurationJson = Streamx.readTextClose(Streamx.bufferedReader(context.getAssets().open("configuration.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (configurationJson == null) {
            return null;
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(configurationJson);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        List<Configuration> configurationList = new LinkedList<>();
        Gson gson = new Gson();
        for (int w = 0, size = jsonArray.length(); w < size; w++) {
            String jsonObject = jsonArray.optString(w);
            if (jsonObject == null) {
                return null;
            }

            Configuration configuration = gson.fromJson(jsonObject, Configuration.class);
            if (configuration == null) {
                return null;
            }
            configurationList.add(configuration);
        }

        return configurationList.size() > 0 ? configurationList : null;
    }

}