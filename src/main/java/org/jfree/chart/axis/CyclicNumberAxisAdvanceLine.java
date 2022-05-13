package org.jfree.chart.axis;

import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.PaintUtils;

import java.awt.*;
import java.util.Objects;

public class CyclicNumberAxisAdvanceLine {

    /** The default axis line stroke. */
    public static Stroke DEFAULT_ADVANCE_LINE_STROKE = new BasicStroke(1.0f);

    /** The default axis line paint. */
    public static final Paint DEFAULT_ADVANCE_LINE_PAINT = Color.GRAY;

    /** A flag that controls whether or not the advance line is visible. */
    private boolean visible;

    /** The advance line stroke. */
    private transient Stroke advanceLineStroke = DEFAULT_ADVANCE_LINE_STROKE;

    /** The advance line paint. */
    private transient Paint advanceLinePaint = DEFAULT_ADVANCE_LINE_PAINT;

    public CyclicNumberAxisAdvanceLine(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Paint getPaint() {
        return this.advanceLinePaint;
    }

    public void setPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.advanceLinePaint = paint;
    }

    public Stroke getStroke() {
        return this.advanceLineStroke;
    }

    public void setStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.advanceLineStroke = stroke;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CyclicNumberAxisAdvanceLine that = (CyclicNumberAxisAdvanceLine) o;
        return visible == that.visible
                && Objects.equals(advanceLineStroke, that.advanceLineStroke)
                && PaintUtils.equal(advanceLinePaint, that.advanceLinePaint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(visible, advanceLineStroke, advanceLinePaint);
    }
}
