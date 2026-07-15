package com.notfallkompass.risikoanalyse;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RiskRepository {
    private static final String PREF_NAME = "risikoanalyse_native_prefs";
    private static final String KEY_RISKS = "risks_v1";

    public static final String[] IMPACT_LABELS = new String[]{
            "Gefährdung\nfür das\nGesamtsystem",
            "Gefahr\nfür das\nTeilsystem",
            "Signifikante\nWirkung\nim Teilsystem",
            "Schwache bzw.\nindirekte Wirkung\nüber andere Risiken"
    };

    public static final String[] IMPACT_ICONS = new String[]{"🌍", "🧩", "🛡️", "〰️"};

    public static final String[] PROBABILITY_LABELS = new String[]{
            "erkannt", "möglich", "wahrscheinlich", "sehr wahrscheinlich", "aktuell"
    };

    public static final String[] PROBABILITY_ICONS = new String[]{"👁️", "?", "🎯", "!", "⏱️"};

    public static final String[] STATUS_OPTIONS = new String[]{"offen", "in Arbeit", "erledigt"};

    public static final String[][] ICON_OPTIONS = new String[][]{
            {"🌪️", "Natur / Krise"},
            {"⚡", "Blackout"},
            {"🛡️", "Reserve / Schutz"},
            {"☁️", "Digitale Infrastruktur"},
            {"🔐", "Cyber / Daten"},
            {"⚖️", "Vorschriften"},
            {"📦", "Lieferkette"},
            {"🧑‍💼", "Schlüsselkräfte"},
            {"👥", "Team / Fachkräfte"},
            {"🧾", "Forderungsausfall"},
            {"⛑️", "Tödlicher Arbeitsunfall"},
            {"🩹", "Arbeitsunfall"},
            {"✋", "Übergriff / Gewalt"},
            {"🧠", "Psychosoziale Krise"},
            {"📈", "Marktveränderung"},
            {"⚙️", "Anlage / Betrieb"},
            {"📉", "Marktrückgang"},
            {"🚑", "Medizinischer Notfall"},
            {"🤝", "Verlust / Krise"},
            {"🔋", "Motivation"},
            {"⚠️", "Allgemeines Risiko"}
    };

    private final SharedPreferences preferences;
    private final ArrayList<Risk> risks = new ArrayList<>();

    public RiskRepository(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        load();
    }

    public List<Risk> getRisks() {
        return Collections.unmodifiableList(risks);
    }

    public Risk findById(String id) {
        for (Risk risk : risks) {
            if (risk.id.equals(id)) return risk;
        }
        return null;
    }

    public Risk createBlankRisk() {
        Risk risk = new Risk(
                "custom-" + UUID.randomUUID(),
                "Neues Risiko",
                3,
                1,
                "⚠️",
                false,
                "",
                false
        );
        risk.status = "offen";
        return risk;
    }

    public void add(Risk risk) {
        risks.add(risk);
        save();
    }

    public void delete(Risk risk) {
        if (risk == null || risk.predefined) return;
        risks.remove(risk);
        save();
    }

    public void save() {
        JSONArray array = new JSONArray();
        try {
            for (Risk risk : risks) {
                array.put(risk.toJson());
            }
            preferences.edit().putString(KEY_RISKS, array.toString()).apply();
        } catch (JSONException ex) {
            // In der Praxis Logging ergänzen. Daten bleiben im Arbeitsspeicher erhalten.
        }
    }

    public void resetToDefaults() {
        risks.clear();
        risks.addAll(defaultRisks());
        save();
    }

    private void load() {
        String json = preferences.getString(KEY_RISKS, null);
        if (json == null || json.trim().isEmpty()) {
            resetToDefaults();
            return;
        }
        risks.clear();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                risks.add(Risk.fromJson(array.getJSONObject(i)));
            }
            if (risks.isEmpty()) resetToDefaults();
        } catch (JSONException ex) {
            resetToDefaults();
        }
    }

    private static ArrayList<Risk> defaultRisks() {
        ArrayList<Risk> list = new ArrayList<>();
        list.add(risk("r01", "Naturkatastrophen / Krieg", 0, 0, "🌪️", false, "Externe Lage mit möglicher Wirkung auf Standort, Lieferfähigkeit und Geschäftsbetrieb."));
        list.add(risk("r02", "Blackout / Strommangellage", 0, 1, "⚡", false, "Stromausfall oder Energiemangel mit Auswirkung auf kritische Prozesse."));
        list.add(risk("r03", "Reserve / strategische Ausfälle", 0, 2, "🛡️", false, "Fehlende Redundanz, Ersatzressourcen oder strategische Alternativen."));
        list.add(risk("r04", "Kritische Störung digitaler Infrastruktur", 0, 3, "☁️", false, "Ausfall zentraler Systeme, Netzwerke, Cloud- oder Kommunikationsdienste."));
        list.add(risk("r05", "Cyberangriff / Datenverlust", 0, 4, "🔐", false, "Angriff, Verschlüsselung, Datenabfluss oder Verlust geschäftskritischer Daten."));
        list.add(risk("r06", "Verstoß gegen Vorschriften", 1, 0, "⚖️", true, "Rechtliche oder organisatorische Nichteinhaltung, inklusive Arbeitnehmerschutz-Vorgaben."));
        list.add(risk("r07", "Lieferkettenunterbrechung", 1, 1, "📦", false, "Ausfall von Lieferanten, Material, Logistik oder externen Partnern."));
        list.add(risk("r08", "Personalverlust Schlüsselkräfte", 1, 2, "🧑‍💼", true, "Ausfall, Kündigung oder langfristige Abwesenheit kritischer Mitarbeitender."));
        list.add(risk("r09", "Fachkräftemangel", 1, 3, "👥", false, "Schwierigkeit, qualifiziertes Personal zu gewinnen oder zu halten."));
        list.add(risk("r10", "Forderungsausfall", 1, 4, "🧾", false, "Ausfall offener Forderungen oder Zahlungsschwierigkeiten wichtiger Kunden."));
        list.add(risk("r11", "Tödlicher Arbeitsunfall", 2, 0, "⛑️", true, "Schwerstes Ereignis im Bereich Arbeitssicherheit und Organisation."));
        list.add(risk("r12", "Arbeitsunfall mit Personenschaden", 2, 1, "🩹", true, "Unfall mit Verletzung, Ausfallzeit oder medizinischer Behandlung."));
        list.add(risk("r13", "Gewalt & sexuelle Übergriffe", 2, 2, "✋", true, "Grenzverletzungen, Gewalt, Belästigung oder Übergriffe am Arbeitsplatz."));
        list.add(risk("r14", "Psychosoziale Akutkrisen", 2, 3, "🧠", true, "Akute psychische Belastung, Konflikt- oder Krisenlage im Arbeitsumfeld."));
        list.add(risk("r15", "Marktveränderung", 2, 4, "📈", false, "Veränderte Nachfrage, Wettbewerb, Preise oder regulatorische Rahmenbedingungen."));
        list.add(risk("r16", "Anlagenausfall", 3, 0, "⚙️", false, "Ausfall einer Anlage, Maschine oder technischen Ressource."));
        list.add(risk("r17", "Marktrückgang", 3, 1, "📉", false, "Rückgang von Umsatz, Nachfrage oder Marktanteilen."));
        list.add(risk("r18", "Medizinischer Notfall", 3, 2, "🚑", true, "Akuter Notfall bei Mitarbeitenden, Gästen oder Dritten am Standort."));
        list.add(risk("r19", "Verlust- bzw. Lebensveränderungskrisen", 3, 3, "🤝", true, "Krisen durch Trauer, Trennung, Pflegebelastung oder einschneidende Lebensereignisse."));
        list.add(risk("r20", "Motivationsprobleme Mitarbeiter", 3, 4, "🔋", true, "Sinkende Motivation, Bindung, Arbeitszufriedenheit oder Leistungsbereitschaft."));
        return list;
    }

    private static Risk risk(String id, String title, int impactIndex, int probabilityIndex, String icon, boolean workerSafety, String description) {
        return new Risk(id, title, impactIndex, probabilityIndex, icon, workerSafety, description, true);
    }
}
