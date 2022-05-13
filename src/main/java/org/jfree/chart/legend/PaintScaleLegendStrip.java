package org.jfree.chart.legend;

import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.PaintUtils;

import java.awt.*;
import java.util.Objects;

public class PaintScaleLegendStrip {
    private final PaintScaleLegend psl;

    /**
     * The thickness of the paint strip (in Java2D units).
     */
    private double stripWidth;

    /**
     * A flag that controls whether or not an outline is drawn around the
     * paint strip.
     */
    private boolean stripOutlineVisible;

    /**
     * The paint used to draw an outline around the paint strip.
     */
    private transient Paint stripOutlinePaint;

    /**
     * The stroke used to draw an outline around the paint strip.
     */
    private transient Stroke stripOutlineStroke;

    public PaintScaleLegendStrip(PaintScaleLegend psl, double stripWidth, boolean stripOutlineVisible, Paint stripOutlinePaint, Stroke stripOutlineStroke) {
        Args.nullNotPermitted(psl, "psl");
        Args.nullNotPermitted(stripOutlinePaint, "stripOutlinePaint");
        Args.nullNotPermitted(stripOutlineStroke, "stripOutlineStroke");
        this.psl = psl;
        this.stripWidth = stripWidth;
        this.stripOutlineVisible = stripOutlineVisible;
        this.stripOutlinePaint = stripOutlinePaint;
        this.stripOutlineStroke = stripOutlineStroke;
    }

    public double getStripWidth() {
        return this.stripWidth;
    }

    public void setStripWidth(double width) {
        this.stripWidth = width;
        psl.notifyListeners(new TitleChangeEvent(psl));
    }

    public boolean isStripOutlineVisible() {
        return this.stripOutlineVisible;
    }

    public void setStripOutlineVisible(boolean visible) {
        this.stripOutlineVisible = visible;
        psl.notifyListeners(new TitleChangeEvent(psl));
    }

    public Paint getStripOutlinePaint() {
        return this.stripOutlinePaint;
    }

    public void setStripOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.stripOutlinePaint = paint;
        psl.notifyListeners(new TitleChangeEvent(psl));
    }

    public Stroke getStripOutlineStroke() {
        return this.stripOutlineStroke;
    }

    public void setStripOutlineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.stripOutlineStroke = stroke;
        psl.notifyListeners(new TitleChangeEvent(psl));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaintScaleLegendStrip that = (PaintScaleLegendStrip) o;
        return Double.compare(that.stripWidth, stripWidth) == 0
                && stripOutlineVisible == that.stripOutlineVisible
                && PaintUtils.equal(stripOutlinePaint, that.stripOutlinePaint)
                && Objects.equals(stripOutlineStroke, that.stripOutlineStroke);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psl, stripWidth, stripOutlineVisible, stripOutlinePaint, stripOutlineStroke);
    }
}
