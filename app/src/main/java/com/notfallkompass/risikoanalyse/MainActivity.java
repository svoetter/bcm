package com.notfallkompass.risikoanalyse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements MatrixView.Listener {
    private static final int REQUEST_CREATE_PDF = 501;

    private RiskRepository repository;
    private MatrixView matrixView;
    private TextView summaryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new RiskRepository(this);
        buildUi();
        refresh();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(this);
        title.setText("Risikoanalyse für Unternehmen");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF111827);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Native Android-App mit 20 vorgegebenen Feldern. Orange = Arbeitnehmerschutz / Verstoß gegen Vorschriften, Grau = andere Unternehmensrisiken.");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF4B5563);
        subtitle.setPadding(0, dp(4), 0, dp(12));
        root.addView(subtitle);

        HorizontalScrollView buttonScroll = new HorizontalScrollView(this);
        buttonScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonScroll.addView(buttonRow);
        root.addView(buttonScroll);

        Button addButton = makeButton("+ Risiko");
        addButton.setOnClickListener(v -> openRiskDialog(repository.createBlankRisk(), true));
        buttonRow.addView(addButton);

        Button pdfButton = makeButton("A4-PDF exportieren");
        pdfButton.setOnClickListener(v -> startPdfExport());
        buttonRow.addView(pdfButton);

        Button resetButton = makeButton("Vorlage wiederherstellen");
        resetButton.setOnClickListener(v -> confirmReset());
        buttonRow.addView(resetButton);

        summaryView = new TextView(this);
        summaryView.setTextSize(14);
        summaryView.setTextColor(0xFF111827);
        summaryView.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        summaryParams.setMargins(0, dp(14), 0, dp(10));
        summaryView.setBackgroundColor(0xFFF3F4F6);
        root.addView(summaryView, summaryParams);

        TextView hint = new TextView(this);
        hint.setText("Tipp: Feld antippen zum Bearbeiten. Feld gedrückt halten/ziehen zum Verschieben. Auf kleinen Displays horizontal scrollen.");
        hint.setTextSize(13);
        hint.setTextColor(0xFF6B7280);
        hint.setPadding(0, 0, 0, dp(8));
        root.addView(hint);

        HorizontalScrollView matrixScroll = new HorizontalScrollView(this);
        matrixScroll.setFillViewport(false);
        matrixScroll.setHorizontalScrollBarEnabled(true);
        matrixView = new MatrixView(this);
        matrixView.setListener(this);
        matrixScroll.addView(matrixView, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        root.addView(matrixScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        setContentView(scrollView);
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, dp(8), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void refresh() {
        List<Risk> risks = repository.getRisks();
        matrixView.setRisks(risks);
        updateSummary(risks);
    }

    private void updateSummary(List<Risk> risks) {
        int worker = 0;
        int critical = 0;
        int open = 0;
        for (Risk risk : risks) {
            if (risk.workerSafety) worker++;
            if (risk.impactIndex <= 1 && risk.probabilityIndex >= 2) critical++;
            if (!"erledigt".equalsIgnoreCase(risk.status)) open++;
        }
        summaryView.setText("Risiken: " + risks.size()
                + "   |   Orange: " + worker
                + "   |   Kritische Felder: " + critical
                + "   |   Offene Maßnahmen: " + open);
    }

    @Override
    public void onRiskSelected(Risk risk) {
        openRiskDialog(risk, false);
    }

    @Override
    public void onRiskMoved(Risk risk, int newImpactIndex, int newProbabilityIndex) {
        risk.impactIndex = newImpactIndex;
        risk.probabilityIndex = newProbabilityIndex;
        repository.save();
        refresh();
        Toast.makeText(this, "Risiko verschoben: " + risk.title, Toast.LENGTH_SHORT).show();
    }

    private void openRiskDialog(Risk risk, boolean isNew) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(12), dp(20), dp(4));

        EditText titleInput = makeEditText(risk.title, false, 1);
        addLabel(form, "Titel");
        form.addView(titleInput);

        Spinner impactSpinner = makeSpinner(asSingleLineLabels(RiskRepository.IMPACT_LABELS));
        impactSpinner.setSelection(clamp(risk.impactIndex, 0, RiskRepository.IMPACT_LABELS.length - 1));
        addLabel(form, "Auswirkung");
        form.addView(impactSpinner);

        Spinner probabilitySpinner = makeSpinner(RiskRepository.PROBABILITY_LABELS);
        probabilitySpinner.setSelection(clamp(risk.probabilityIndex, 0, RiskRepository.PROBABILITY_LABELS.length - 1));
        addLabel(form, "Eintrittswahrscheinlichkeit / Aktualität");
        form.addView(probabilitySpinner);

        Spinner iconSpinner = makeSpinner(iconLabels());
        iconSpinner.setSelection(iconIndexFor(risk.icon));
        addLabel(form, "Symbol");
        form.addView(iconSpinner);

        CheckBox workerBox = new CheckBox(this);
        workerBox.setText("Arbeitnehmerschutz / Vorschriften: Orange hinterlegen");
        workerBox.setChecked(risk.workerSafety);
        workerBox.setPadding(0, dp(8), 0, dp(8));
        form.addView(workerBox);

        EditText descriptionInput = makeEditText(risk.description, true, 3);
        addLabel(form, "Beschreibung");
        form.addView(descriptionInput);

        EditText ownerInput = makeEditText(risk.owner, false, 1);
        addLabel(form, "Verantwortlich");
        form.addView(ownerInput);

        EditText dueInput = makeEditText(risk.due, false, 1);
        dueInput.setHint("z. B. 31.12.2026");
        addLabel(form, "Termin / Frist");
        form.addView(dueInput);

        EditText measuresInput = makeEditText(risk.measures, true, 4);
        addLabel(form, "Maßnahmen");
        form.addView(measuresInput);

        Spinner statusSpinner = makeSpinner(RiskRepository.STATUS_OPTIONS);
        statusSpinner.setSelection(indexOf(RiskRepository.STATUS_OPTIONS, risk.status));
        addLabel(form, "Status");
        form.addView(statusSpinner);

        ScrollView dialogScroll = new ScrollView(this);
        dialogScroll.addView(form);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(isNew ? "Risiko hinzufügen" : "Risiko bearbeiten")
                .setView(dialogScroll)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Speichern", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) title = "Unbenanntes Risiko";

                    risk.title = title;
                    risk.impactIndex = impactSpinner.getSelectedItemPosition();
                    risk.probabilityIndex = probabilitySpinner.getSelectedItemPosition();
                    risk.icon = RiskRepository.ICON_OPTIONS[iconSpinner.getSelectedItemPosition()][0];
                    risk.description = descriptionInput.getText().toString().trim();
                    risk.owner = ownerInput.getText().toString().trim();
                    risk.due = dueInput.getText().toString().trim();
                    risk.measures = measuresInput.getText().toString().trim();
                    risk.status = RiskRepository.STATUS_OPTIONS[statusSpinner.getSelectedItemPosition()];

                    boolean titleSuggestsRegulation = title.toLowerCase(Locale.ROOT).contains("vorschrift")
                            || title.toLowerCase(Locale.ROOT).contains("arbeitnehmer")
                            || title.toLowerCase(Locale.ROOT).contains("arbeitsschutz");
                    risk.workerSafety = workerBox.isChecked() || titleSuggestsRegulation;

                    if (isNew) repository.add(risk); else repository.save();
                    refresh();
                    Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show();
                });

        if (!isNew && !risk.predefined) {
            builder.setNeutralButton("Löschen", (dialog, which) -> confirmDelete(risk));
        }

        builder.show();
    }

    private void confirmDelete(Risk risk) {
        new AlertDialog.Builder(this)
                .setTitle("Risiko löschen?")
                .setMessage("Dieses zusätzliche Risiko wird entfernt. Vorgegebene Standardfelder können nicht gelöscht werden.")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Löschen", (dialog, which) -> {
                    repository.delete(risk);
                    refresh();
                    Toast.makeText(this, "Gelöscht", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Vorlage wiederherstellen?")
                .setMessage("Alle Änderungen werden auf die 20 Standardfelder zurückgesetzt. Zusätzliche Risiken werden entfernt.")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Wiederherstellen", (dialog, which) -> {
                    repository.resetToDefaults();
                    refresh();
                    Toast.makeText(this, "Vorlage wiederhergestellt", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void startPdfExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "risikoanalyse-a4.pdf");
        startActivityForResult(intent, REQUEST_CREATE_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_PDF && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            try {
                PdfExporter.writeA4LandscapePdf(this, uri, repository.getRisks());
                Toast.makeText(this, "PDF exportiert", Toast.LENGTH_LONG).show();
            } catch (IOException ex) {
                Toast.makeText(this, "PDF-Export fehlgeschlagen: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addLabel(LinearLayout parent, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(13);
        label.setTextColor(0xFF374151);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setPadding(0, dp(10), 0, dp(4));
        parent.addView(label);
    }

    private EditText makeEditText(String value, boolean multiLine, int minLines) {
        EditText input = new EditText(this);
        input.setText(value == null ? "" : value);
        input.setTextSize(15);
        input.setSingleLine(!multiLine);
        input.setMinLines(minLines);
        input.setGravity(multiLine ? Gravity.TOP | Gravity.START : Gravity.CENTER_VERTICAL | Gravity.START);
        input.setSelectAllOnFocus(false);
        return input;
    }

    private Spinner makeSpinner(String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private String[] asSingleLineLabels(String[] labels) {
        String[] result = new String[labels.length];
        for (int i = 0; i < labels.length; i++) result[i] = labels[i].replace('\n', ' ');
        return result;
    }

    private String[] iconLabels() {
        String[] result = new String[RiskRepository.ICON_OPTIONS.length];
        for (int i = 0; i < RiskRepository.ICON_OPTIONS.length; i++) {
            result[i] = RiskRepository.ICON_OPTIONS[i][0] + "  " + RiskRepository.ICON_OPTIONS[i][1];
        }
        return result;
    }

    private int iconIndexFor(String icon) {
        for (int i = 0; i < RiskRepository.ICON_OPTIONS.length; i++) {
            if (RiskRepository.ICON_OPTIONS[i][0].equals(icon)) return i;
        }
        return RiskRepository.ICON_OPTIONS.length - 1;
    }

    private int indexOf(String[] values, String value) {
        if (value == null) return 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(value)) return i;
        }
        return 0;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
