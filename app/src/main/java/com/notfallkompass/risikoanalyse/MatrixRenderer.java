package com.notfallkompass.risikoanalyse;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class MatrixRenderer {
    public static final float DESIGN_W = 1280f;
    public static final float DESIGN_H = 820f;

    private static final float HEADER_H = 82f;
    private static final float LEFT_W = 232f;
    private static final float GRID_X = LEFT_W;
    private static final float GRID_Y = 174f;
    private static final float GRID_W = 1010f;
    private static final float GRID_H = 520f;
    private static final float COL_W = GRID_W / 5f;
    private static final float ROW_H = GRID_H / 4f;

    private static final int COLOR_BG = Color.rgb(250, 250, 250);
    private static final int COLOR_PANEL = Color.WHITE;
    private static final int COLOR_TEXT = Color.rgb(17, 24, 39);
    private static final int COLOR_MUTED = Color.rgb(75, 85, 99);
    private static final int COLOR_LINE = Color.rgb(209, 213, 219);
    private static final int COLOR_ORANGE = Color.rgb(249, 115, 22);
    private static final int COLOR_ORANGE_SOFT = Color.rgb(255, 237, 213);
    private static final int COLOR_GREY = Color.rgb(229, 231, 235);
    private static final int COLOR_GREY_SOFT = Color.rgb(243, 244, 246);

    public static class CardHit {
        public final Risk risk;
        public final RectF rectDesign;

        CardHit(Risk risk, RectF rectDesign) {
            this.risk = risk;
            this.rectDesign = rectDesign;
        }
    }

    public static class RenderResult {
        public float scale;
        public float offsetX;
        public float offsetY;
        public final ArrayList<CardHit> cards = new ArrayList<>();
    }

    public static class CellPosition {
        public final int row;
        public final int col;

        CellPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public static RenderResult draw(Canvas canvas, float width, float height, List<Risk> risks, Risk draggedRisk, float dragXDesign, float dragYDesign) {
        RenderResult result = new RenderResult();
        result.scale = Math.min(width / DESIGN_W, height / DESIGN_H);
        result.offsetX = (width - DESIGN_W * result.scale) / 2f;
        result.offsetY = (height - DESIGN_H * result.scale) / 2f;

        canvas.drawColor(COLOR_BG);
        canvas.save();
        canvas.translate(result.offsetX, result.offsetY);
        canvas.scale(result.scale, result.scale);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TextPaint text = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(COLOR_TEXT);

        drawHeader(canvas, paint, text, risks);
        drawAxisLabels(canvas, paint, text);
        drawGrid(canvas, paint, text, risks, draggedRisk, result.cards);
        drawLegend(canvas, paint, text, risks);

        if (draggedRisk != null) {
            drawRiskCard(canvas, paint, text, draggedRisk, new RectF(dragXDesign - 90f, dragYDesign - 42f, dragXDesign + 90f, dragYDesign + 42f), true);
        }

        canvas.restore();
        return result;
    }

    public static CellPosition cellForDesignPoint(float x, float y) {
        if (x < GRID_X || x > GRID_X + GRID_W || y < GRID_Y || y > GRID_Y + GRID_H) return null;
        int col = Math.max(0, Math.min(4, (int) ((x - GRID_X) / COL_W)));
        int row = Math.max(0, Math.min(3, (int) ((y - GRID_Y) / ROW_H)));
        return new CellPosition(row, col);
    }

    private static void drawHeader(Canvas canvas, Paint paint, TextPaint text, List<Risk> risks) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(new RectF(18, 16, DESIGN_W - 18, HEADER_H), 22, 22, paint);

        text.setFakeBoldText(true);
        text.setTextSize(30);
        text.setColor(COLOR_TEXT);
        canvas.drawText("Risikoanalyse für Unternehmen", 42, 50, text);

        text.setFakeBoldText(false);
        text.setTextSize(16);
        text.setColor(COLOR_MUTED);
        canvas.drawText("Native Android-App: vorgegebene Risikofelder, Orange für Arbeitnehmerschutz & Verstoß gegen Vorschriften", 42, 73, text);

        int total = risks == null ? 0 : risks.size();
        int worker = 0;
        if (risks != null) {
            for (Risk risk : risks) if (risk.workerSafety) worker++;
        }
        drawTopBadge(canvas, paint, text, "Risiken " + total, DESIGN_W - 315, 35, COLOR_GREY_SOFT, COLOR_TEXT);
        drawTopBadge(canvas, paint, text, "Orange " + worker, DESIGN_W - 190, 35, COLOR_ORANGE_SOFT, COLOR_ORANGE);
    }

    private static void drawTopBadge(Canvas canvas, Paint paint, TextPaint text, String label, float x, float y, int bg, int fg) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bg);
        RectF r = new RectF(x, y - 22, x + 112, y + 14);
        canvas.drawRoundRect(r, 18, 18, paint);
        text.setFakeBoldText(true);
        text.setTextSize(15);
        text.setColor(fg);
        drawTextCentered(canvas, label, r, text);
        text.setFakeBoldText(false);
    }

    private static void drawAxisLabels(Canvas canvas, Paint paint, TextPaint text) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(new RectF(GRID_X, 92, GRID_X + GRID_W, 166), 18, 18, paint);

        text.setTextSize(16);
        text.setFakeBoldText(true);
        text.setColor(COLOR_TEXT);
        canvas.drawText("Eintrittswahrscheinlichkeit / Aktualität", GRID_X + 18, 112, text);

        for (int col = 0; col < 5; col++) {
            float x = GRID_X + col * COL_W + 8;
            RectF r = new RectF(x, 120, x + COL_W - 16, 162);
            drawProbabilityBadge(canvas, paint, text, r, RiskRepository.PROBABILITY_ICONS[col], RiskRepository.PROBABILITY_LABELS[col]);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(new RectF(18, GRID_Y, LEFT_W - 14, GRID_Y + GRID_H), 18, 18, paint);
        text.setColor(COLOR_TEXT);
        text.setFakeBoldText(true);
        text.setTextSize(18);
        canvas.drawText("Auswirkung", 38, GRID_Y - 18, text);
        text.setFakeBoldText(false);
        text.setTextSize(13);
        text.setColor(COLOR_MUTED);
        canvas.drawText("mit Symbolen", 135, GRID_Y - 18, text);

        for (int row = 0; row < 4; row++) {
            float y = GRID_Y + row * ROW_H + 8;
            RectF r = new RectF(32, y, LEFT_W - 28, y + ROW_H - 16);
            drawImpactLabel(canvas, paint, text, r, RiskRepository.IMPACT_ICONS[row], RiskRepository.IMPACT_LABELS[row]);
        }
    }

    private static void drawProbabilityBadge(Canvas canvas, Paint paint, TextPaint text, RectF r, String icon, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_GREY_SOFT);
        canvas.drawRoundRect(r, 16, 16, paint);
        text.setTextSize(22);
        text.setColor(COLOR_TEXT);
        text.setFakeBoldText(false);
        canvas.drawText(icon, r.left + 15, r.centerY() + 8, text);
        text.setTextSize(14);
        text.setFakeBoldText(true);
        text.setColor(COLOR_TEXT);
        canvas.drawText(label, r.left + 46, r.centerY() + 5, text);
        text.setFakeBoldText(false);
    }

    private static void drawImpactLabel(Canvas canvas, Paint paint, TextPaint text, RectF r, String icon, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_GREY_SOFT);
        canvas.drawRoundRect(r, 16, 16, paint);

        text.setTextSize(25);
        text.setColor(COLOR_TEXT);
        canvas.drawText(icon, r.left + 14, r.top + 36, text);

        text.setTextSize(14);
        text.setColor(COLOR_TEXT);
        text.setFakeBoldText(true);
        drawWrappedText(canvas, label.replace("\n", " "), r.left + 52, r.top + 22, r.width() - 62, 18, 4, text);
        text.setFakeBoldText(false);
    }

    private static void drawGrid(Canvas canvas, Paint paint, TextPaint text, List<Risk> risks, Risk draggedRisk, ArrayList<CardHit> hits) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(new RectF(GRID_X - 2, GRID_Y - 2, GRID_X + GRID_W + 2, GRID_Y + GRID_H + 2), 18, 18, paint);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                float x = GRID_X + col * COL_W;
                float y = GRID_Y + row * ROW_H;
                RectF cell = new RectF(x + 6, y + 6, x + COL_W - 6, y + ROW_H - 6);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                canvas.drawRoundRect(cell, 14, 14, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                paint.setColor(COLOR_LINE);
                canvas.drawRoundRect(cell, 14, 14, paint);
            }
        }

        if (risks == null) return;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                ArrayList<Risk> cellRisks = new ArrayList<>();
                for (Risk risk : risks) {
                    if (risk == draggedRisk) continue;
                    if (risk.impactIndex == row && risk.probabilityIndex == col) cellRisks.add(risk);
                }
                drawRisksInCell(canvas, paint, text, row, col, cellRisks, hits);
            }
        }
    }

    private static void drawRisksInCell(Canvas canvas, Paint paint, TextPaint text, int row, int col, ArrayList<Risk> cellRisks, ArrayList<CardHit> hits) {
        if (cellRisks.isEmpty()) {
            text.setTextSize(14);
            text.setColor(Color.rgb(156, 163, 175));
            text.setFakeBoldText(false);
            RectF r = new RectF(GRID_X + col * COL_W + 12, GRID_Y + row * ROW_H + 10, GRID_X + (col + 1) * COL_W - 12, GRID_Y + (row + 1) * ROW_H - 10);
            drawTextCentered(canvas, "+", r, text);
            return;
        }

        float x = GRID_X + col * COL_W + 14;
        float top = GRID_Y + row * ROW_H + 12;
        float width = COL_W - 28;
        int count = cellRisks.size();
        float cardH = count == 1 ? 98f : Math.max(48f, Math.min(76f, (ROW_H - 22f) / count - 4f));
        for (int i = 0; i < count; i++) {
            float y = top + i * (cardH + 5f);
            if (y + cardH > GRID_Y + (row + 1) * ROW_H - 8) break;
            RectF card = new RectF(x, y, x + width, y + cardH);
            drawRiskCard(canvas, paint, text, cellRisks.get(i), card, false);
            hits.add(new CardHit(cellRisks.get(i), new RectF(card)));
        }
    }

    private static void drawRiskCard(Canvas canvas, Paint paint, TextPaint text, Risk risk, RectF card, boolean dragging) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(risk.workerSafety ? COLOR_ORANGE_SOFT : COLOR_GREY);
        if (dragging) paint.setAlpha(220);
        canvas.drawRoundRect(card, 14, 14, paint);
        paint.setAlpha(255);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(risk.workerSafety ? 3f : 2f);
        paint.setColor(risk.workerSafety ? COLOR_ORANGE : Color.rgb(156, 163, 175));
        canvas.drawRoundRect(card, 14, 14, paint);

        RectF iconCircle = new RectF(card.left + 10, card.top + 10, card.left + 44, card.top + 44);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawOval(iconCircle, paint);
        text.setColor(COLOR_TEXT);
        text.setTextSize(21);
        text.setFakeBoldText(false);
        drawTextCentered(canvas, risk.icon == null || risk.icon.isEmpty() ? "⚠️" : risk.icon, iconCircle, text);

        text.setColor(COLOR_TEXT);
        text.setTextSize(card.height() < 60 ? 12f : 13.5f);
        text.setFakeBoldText(true);
        drawWrappedText(canvas, risk.title, card.left + 52, card.top + 19, card.width() - 60, 16, card.height() < 60 ? 2 : 3, text);
        text.setFakeBoldText(false);

        if (card.height() > 74) {
            text.setTextSize(10.5f);
            text.setColor(COLOR_MUTED);
            String meta = risk.workerSafety ? "Arbeitnehmerschutz" : "Unternehmen";
            if (risk.status != null && !risk.status.isEmpty()) meta += " · " + risk.status;
            canvas.drawText(meta, card.left + 13, card.bottom - 12, text);
        }
    }

    private static void drawLegend(Canvas canvas, Paint paint, TextPaint text, List<Risk> risks) {
        RectF legend = new RectF(18, 718, DESIGN_W - 18, 800);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(legend, 18, 18, paint);

        text.setTextSize(16);
        text.setFakeBoldText(true);
        text.setColor(COLOR_TEXT);
        canvas.drawText("Legende", 42, 747, text);
        text.setFakeBoldText(false);

        drawLegendItem(canvas, paint, text, 135, 738, COLOR_ORANGE_SOFT, COLOR_ORANGE, "Orange: Arbeitnehmerschutz / Verstoß gegen Vorschriften");
        drawLegendItem(canvas, paint, text, 520, 738, COLOR_GREY, Color.rgb(156, 163, 175), "Grau: andere Unternehmensrisiken");

        int critical = 0;
        int open = 0;
        if (risks != null) {
            for (Risk risk : risks) {
                if (risk.impactIndex <= 1 && risk.probabilityIndex >= 2) critical++;
                if (!"erledigt".equalsIgnoreCase(risk.status)) open++;
            }
        }
        text.setColor(COLOR_MUTED);
        text.setTextSize(13);
        canvas.drawText("Kritische Felder: " + critical + " · offene Maßnahmen: " + open + " · Export: A4-Querformat PDF", 42, 780, text);
    }

    private static void drawLegendItem(Canvas canvas, Paint paint, TextPaint text, float x, float y, int fill, int stroke, String label) {
        RectF box = new RectF(x, y, x + 34, y + 24);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawRoundRect(box, 8, 8, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(stroke);
        canvas.drawRoundRect(box, 8, 8, paint);
        text.setColor(COLOR_TEXT);
        text.setTextSize(13.5f);
        canvas.drawText(label, x + 44, y + 17, text);
    }

    private static void drawTextCentered(Canvas canvas, String value, RectF rect, TextPaint paint) {
        Paint.Align old = paint.getTextAlign();
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float y = rect.centerY() - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(value == null ? "" : value, rect.centerX(), y, paint);
        paint.setTextAlign(old);
    }

    private static void drawWrappedText(Canvas canvas, String value, float x, float y, float maxWidth, float lineHeight, int maxLines, TextPaint paint) {
        if (value == null) value = "";
        ArrayList<String> lines = wrap(value, maxWidth, paint, maxLines);
        for (int i = 0; i < lines.size(); i++) {
            canvas.drawText(lines.get(i), x, y + i * lineHeight, paint);
        }
    }

    private static ArrayList<String> wrap(String value, float maxWidth, TextPaint paint, int maxLines) {
        ArrayList<String> lines = new ArrayList<>();
        String cleaned = value.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) return lines;
        String[] words = cleaned.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String attempt = current.length() == 0 ? word : current + " " + word;
            if (paint.measureText(attempt) <= maxWidth || current.length() == 0) {
                current = new StringBuilder(attempt);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
                if (lines.size() == maxLines - 1) break;
            }
        }
        if (current.length() > 0 && lines.size() < maxLines) lines.add(current.toString());
        if (lines.size() == maxLines) {
            String last = lines.get(lines.size() - 1);
            while (paint.measureText(last + "…") > maxWidth && last.length() > 1) {
                last = last.substring(0, last.length() - 1);
            }
            if (!TextUtils.equals(last, lines.get(lines.size() - 1))) lines.set(lines.size() - 1, last + "…");
        }
        return lines;
    }
}
