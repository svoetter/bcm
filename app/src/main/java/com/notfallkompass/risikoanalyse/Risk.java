package com.notfallkompass.risikoanalyse;

import org.json.JSONException;
import org.json.JSONObject;

public class Risk {
    public String id;
    public String title;
    public int impactIndex;
    public int probabilityIndex;
    public String icon;
    public boolean workerSafety;
    public String description;
    public boolean predefined;
    public String owner;
    public String due;
    public String measures;
    public String status;

    public Risk(
            String id,
            String title,
            int impactIndex,
            int probabilityIndex,
            String icon,
            boolean workerSafety,
            String description,
            boolean predefined
    ) {
        this.id = id;
        this.title = title;
        this.impactIndex = impactIndex;
        this.probabilityIndex = probabilityIndex;
        this.icon = icon;
        this.workerSafety = workerSafety;
        this.description = description == null ? "" : description;
        this.predefined = predefined;
        this.owner = "";
        this.due = "";
        this.measures = "";
        this.status = "offen";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("impactIndex", impactIndex);
        obj.put("probabilityIndex", probabilityIndex);
        obj.put("icon", icon);
        obj.put("workerSafety", workerSafety);
        obj.put("description", description);
        obj.put("predefined", predefined);
        obj.put("owner", owner);
        obj.put("due", due);
        obj.put("measures", measures);
        obj.put("status", status);
        return obj;
    }

    public static Risk fromJson(JSONObject obj) throws JSONException {
        Risk risk = new Risk(
                obj.optString("id"),
                obj.optString("title"),
                obj.optInt("impactIndex", 0),
                obj.optInt("probabilityIndex", 0),
                obj.optString("icon", "⚠️"),
                obj.optBoolean("workerSafety", false),
                obj.optString("description", ""),
                obj.optBoolean("predefined", false)
        );
        risk.owner = obj.optString("owner", "");
        risk.due = obj.optString("due", "");
        risk.measures = obj.optString("measures", "");
        risk.status = obj.optString("status", "offen");
        return risk;
    }
}
