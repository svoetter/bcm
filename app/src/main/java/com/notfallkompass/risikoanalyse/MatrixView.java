package com.notfallkompass.risikoanalyse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MatrixView extends View {
    public interface Listener {
        void onRiskSelected(Risk risk);
        void onRiskMoved(Risk risk, int newImpactIndex, int newProbabilityIndex);
    }

    private List<Risk> risks = new ArrayList<>();
    private Listener listener;
    private MatrixRenderer.RenderResult lastResult;
    private int touchSlop;
    private Risk downRisk;
    private Risk draggedRisk;
    private float downX;
    private float downY;
    private float dragXDesign;
    private float dragYDesign;
    private boolean dragging;

    public MatrixView(Context context) {
        super(context);
        init(context);
    }

    public MatrixView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setFocusable(true);
        setClickable(true);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setRisks(List<Risk> risks) {
        this.risks = risks == null ? new ArrayList<Risk>() : risks;
        invalidate();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidth = dp(1180);
        int minHeight = dp(780);
        int measuredWidth = resolveSize(minWidth, widthMeasureSpec);
        int measuredHeight = resolveSize(minHeight, heightMeasureSpec);
        setMeasuredDimension(Math.max(minWidth, measuredWidth), Math.max(minHeight, measuredHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        lastResult = MatrixRenderer.draw(
                canvas,
                getWidth(),
                getHeight(),
                risks,
                draggedRisk,
                dragXDesign,
                dragYDesign
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (lastResult == null) return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                float[] designDown = toDesign(downX, downY);
                downRisk = findRiskAtDesign(designDown[0], designDown[1]);
                draggedRisk = null;
                dragging = false;
                if (downRisk != null && getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (downRisk != null) {
                    float dx = event.getX() - downX;
                    float dy = event.getY() - downY;
                    if (!dragging && Math.sqrt(dx * dx + dy * dy) > touchSlop * 1.4f) {
                        dragging = true;
                        draggedRisk = downRisk;
                    }
                    if (dragging) {
                        float[] designMove = toDesign(event.getX(), event.getY());
                        dragXDesign = designMove[0];
                        dragYDesign = designMove[1];
                        invalidate();
                    }
                    return true;
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging && draggedRisk != null) {
                    float[] designUp = toDesign(event.getX(), event.getY());
                    MatrixRenderer.CellPosition cell = MatrixRenderer.cellForDesignPoint(designUp[0], designUp[1]);
                    Risk moved = draggedRisk;
                    draggedRisk = null;
                    dragging = false;
                    downRisk = null;
                    invalidate();
                    if (cell != null && listener != null) {
                        listener.onRiskMoved(moved, cell.row, cell.col);
                    }
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
                Risk tapped = downRisk;
                downRisk = null;
                draggedRisk = null;
                dragging = false;
                if (tapped != null && listener != null && event.getActionMasked() == MotionEvent.ACTION_UP) {
                    listener.onRiskSelected(tapped);
                }
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return true;
    }

    private Risk findRiskAtDesign(float x, float y) {
        if (lastResult == null) return null;
        for (int i = lastResult.cards.size() - 1; i >= 0; i--) {
            MatrixRenderer.CardHit hit = lastResult.cards.get(i);
            RectF rect = hit.rectDesign;
            if (rect.contains(x, y)) return hit.risk;
        }
        return null;
    }

    private float[] toDesign(float screenX, float screenY) {
        if (lastResult == null || lastResult.scale <= 0) return new float[]{screenX, screenY};
        return new float[]{
                (screenX - lastResult.offsetX) / lastResult.scale,
                (screenY - lastResult.offsetY) / lastResult.scale
        };
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
