/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2022, by David Gilbert and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------------
 * PaintScaleLegend.java
 * ---------------------
 * (C) Copyright 2007-2022, by David Gilbert.
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   Peter Kolb - see patch 2686872;
 *
 */

package org.jfree.chart.legend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;

/**
 * A legend that shows a range of values and their associated colors, driven
 * by an underlying {@link PaintScale} implementation.
 */
public class PaintScaleLegend extends Title implements AxisChangeListener, PublicCloneable {
    /** For serialization. */
    static final long serialVersionUID = -1365146490993227503L;

    /** The paint scale (never {@code null}). */
    private PaintScale scale;

    /** The value axis (never {@code null}). */
    private ValueAxis axis;

    /**
     * The axis location (handles both orientations, never
     * {@code null}).
     */
    private AxisLocation axisLocation;

    /** The offset between the axis and the paint strip (in Java2D units). */
    private double axisOffset;

    /** The background paint (never {@code null}). */
    private transient Paint backgroundPaint;

    private transient PaintScaleLegendStrip strip = new PaintScaleLegendStrip(this, 15.0, true, Color.GRAY, new BasicStroke(0.5f));;

    /**
     * The number of subdivisions for the scale when rendering.
     */
    private int subdivisions;

    /**
     * Creates a new instance.
     *
     * @param scale  the scale ({@code null} not permitted).
     * @param axis  the axis ({@code null} not permitted).
     */
    public PaintScaleLegend(PaintScale scale, ValueAxis axis) {
        Args.nullNotPermitted(axis, "axis");
        this.scale = scale;
        this.axis = axis;
        this.axis.addChangeListener(this);
        this.axisLocation = AxisLocation.BOTTOM_OR_LEFT;
        this.axisOffset = 0.0;
        this.axis.setRange(scale.getLowerBound(), scale.getUpperBound());
        this.backgroundPaint = Color.WHITE;
        this.subdivisions = 100;
    }

    public PaintScaleLegendStrip getStrip() {
        return strip;
    }

    public void notifyListeners(TitleChangeEvent event) {
        super.notifyListeners(event);
    }

    /**
     * Returns the scale used to convert values to colors.
     *
     * @return The scale (never {@code null}).
     *
     * @see #setScale(PaintScale)
     */
    public PaintScale getScale() {
        return this.scale;
    }

    /**
     * Sets the scale and sends a {@link TitleChangeEvent} to all registered
     * listeners.
     *
     * @param scale  the scale ({@code null} not permitted).
     *
     * @see #getScale()
     */
    public void setScale(PaintScale scale) {
        Args.nullNotPermitted(scale, "scale");
        this.scale = scale;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the axis for the paint scale.
     *
     * @return The axis (never {@code null}).
     *
     * @see #setAxis(ValueAxis)
     */
    public ValueAxis getAxis() {
        return this.axis;
    }

    /**
     * Sets the axis for the paint scale and sends a {@link TitleChangeEvent}
     * to all registered listeners.
     *
     * @param axis  the axis ({@code null} not permitted).
     *
     * @see #getAxis()
     */
    public void setAxis(ValueAxis axis) {
        Args.nullNotPermitted(axis, "axis");
        this.axis.removeChangeListener(this);
        this.axis = axis;
        this.axis.addChangeListener(this);
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the axis location.
     *
     * @return The axis location (never {@code null}).
     *
     * @see #setAxisLocation(AxisLocation)
     */
    public AxisLocation getAxisLocation() {
        return this.axisLocation;
    }

    /**
     * Sets the axis location and sends a {@link TitleChangeEvent} to all
     * registered listeners.
     *
     * @param location  the location ({@code null} not permitted).
     *
     * @see #getAxisLocation()
     */
    public void setAxisLocation(AxisLocation location) {
        Args.nullNotPermitted(location, "location");
        this.axisLocation = location;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the offset between the axis and the paint strip.
     *
     * @return The offset between the axis and the paint strip.
     *
     * @see #setAxisOffset(double)
     */
    public double getAxisOffset() {
        return this.axisOffset;
    }

    /**
     * Sets the offset between the axis and the paint strip and sends a
     * {@link TitleChangeEvent} to all registered listeners.
     *
     * @param offset  the offset.
     */
    public void setAxisOffset(double offset) {
        this.axisOffset = offset;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the background paint.
     *
     * @return The background paint.
     */
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    /**
     * Sets the background paint and sends a {@link TitleChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint ({@code null} permitted).
     */
    public void setBackgroundPaint(Paint paint) {
        this.backgroundPaint = paint;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Returns the number of subdivisions used to draw the scale.
     *
     * @return The subdivision count.
     */
    public int getSubdivisionCount() {
        return this.subdivisions;
    }

    /**
     * Sets the subdivision count and sends a {@link TitleChangeEvent} to
     * all registered listeners.
     *
     * @param count  the count.
     */
    public void setSubdivisionCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Requires 'count' > 0.");
        }
        this.subdivisions = count;
        notifyListeners(new TitleChangeEvent(this));
    }

    /**
     * Receives notification of an axis change event and responds by firing
     * a title change event.
     *
     * @param event  the event.
     */
    @Override
    public void axisChanged(AxisChangeEvent event) {
        if (this.axis == event.getAxis()) {
            notifyListeners(new TitleChangeEvent(this));
        }
    }

    /**
     * Arranges the contents of the block, within the given constraints, and
     * returns the block size.
     *
     * @param g2  the graphics device.
     * @param constraint  the constraint ({@code null} not permitted).
     *
     * @return The block size (in Java2D units, never {@code null}).
     */
    @Override
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        RectangleConstraint cc = toContentConstraint(constraint);
        LengthConstraintType w = cc.getWidthConstraintType();
        LengthConstraintType h = cc.getHeightConstraintType();
        Size2D contentSize = null;
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = new Size2D(getWidth(), getHeight());
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                contentSize = arrangeRR(g2, cc.getWidthRange(),
                        cc.getHeightRange());
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        assert contentSize != null; // suppress compiler warning
        return new Size2D(calculateTotalWidth(contentSize.getWidth()),
                calculateTotalHeight(contentSize.getHeight()));
    }

    /**
     * Returns the content size for the title.  This will reflect the fact that
     * a text title positioned on the left or right of a chart will be rotated
     * 90 degrees.
     *
     * @param g2  the graphics device.
     * @param widthRange  the width range.
     * @param heightRange  the height range.
     *
     * @return The content size.
     */
    protected Size2D arrangeRR(Graphics2D g2, Range widthRange,
            Range heightRange) {

        RectangleEdge position = getPosition();
        if (position == RectangleEdge.TOP || position == RectangleEdge.BOTTOM) {


            float maxWidth = (float) widthRange.getUpperBound();

            // determine the space required for the axis
            AxisSpace space = this.axis.reserveSpace(g2, null,
                    new Rectangle2D.Double(0, 0, maxWidth, 100),
                    RectangleEdge.BOTTOM, null);

            return new Size2D(maxWidth, strip.getStripWidth() + this.axisOffset
                    + space.getTop() + space.getBottom());
        }
        else if (position == RectangleEdge.LEFT || position
                == RectangleEdge.RIGHT) {
            float maxHeight = (float) heightRange.getUpperBound();
            AxisSpace space = this.axis.reserveSpace(g2, null,
                    new Rectangle2D.Double(0, 0, 100, maxHeight),
                    RectangleEdge.RIGHT, null);
            return new Size2D(strip.getStripWidth() + this.axisOffset
                    + space.getLeft() + space.getRight(), maxHeight);
        }
        else {
            throw new RuntimeException("Unrecognised position.");
        }
    }

    /**
     * Draws the legend within the specified area.
     *
     * @param g2  the graphics target ({@code null} not permitted).
     * @param area  the drawing area ({@code null} not permitted).
     */
    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        draw(g2, area, null);
    }

    /**
     * Draws the legend within the specified area.
     *
     * @param g2  the graphics target ({@code null} not permitted).
     * @param area  the drawing area ({@code null} not permitted).
     * @param params  drawing parameters (ignored here).
     *
     * @return {@code null}.
     */
    @Override
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        Rectangle2D target = (Rectangle2D) area.clone();
        target = trimMargin(target);
        if (this.backgroundPaint != null) {
            g2.setPaint(this.backgroundPaint);
            g2.fill(target);
        }
        getFrame().draw(g2, target);
        getFrame().getInsets().trim(target);
        target = trimPadding(target);
        double base = this.axis.getLowerBound();
        double increment = this.axis.getRange().getLength() / this.subdivisions;
        Rectangle2D r = new Rectangle2D.Double();

        if (RectangleEdge.isTopOrBottom(getPosition())) {
            RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(
                    this.axisLocation, PlotOrientation.HORIZONTAL);
            if (axisEdge == RectangleEdge.TOP) {
                for (int i = 0; i < this.subdivisions; i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = this.axis.valueToJava2D(v, target,
                            RectangleEdge.TOP);
                    double vv1 = this.axis.valueToJava2D(v + increment, target,
                            RectangleEdge.TOP);
                    double ww = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(Math.min(vv0, vv1), target.getMaxY()
                            - strip.getStripWidth(), ww, strip.getStripWidth());
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (strip.isStripOutlineVisible()) {
                    g2.setPaint(strip.getStripOutlinePaint());
                    g2.setStroke(strip.getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMaxY() - strip.getStripWidth(),
                            target.getWidth(), strip.getStripWidth()));
                }
                this.axis.draw(g2, target.getMaxY() - strip.getStripWidth()
                        - this.axisOffset, target, target, RectangleEdge.TOP,
                        null);
            }
            else if (axisEdge == RectangleEdge.BOTTOM) {
                for (int i = 0; i < this.subdivisions; i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = this.axis.valueToJava2D(v, target,
                            RectangleEdge.BOTTOM);
                    double vv1 = this.axis.valueToJava2D(v + increment, target,
                            RectangleEdge.BOTTOM);
                    double ww = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(Math.min(vv0, vv1), target.getMinY(), ww,
                            strip.getStripWidth());
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (strip.isStripOutlineVisible()) {
                    g2.setPaint(strip.getStripOutlinePaint());
                    g2.setStroke(strip.getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMinY(), target.getWidth(),
                            strip.getStripWidth()));
                }
                this.axis.draw(g2, target.getMinY() + strip.getStripWidth()
                        + this.axisOffset, target, target,
                        RectangleEdge.BOTTOM, null);
            }
        }
        else {
            RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(
                    this.axisLocation, PlotOrientation.VERTICAL);
            if (axisEdge == RectangleEdge.LEFT) {
                for (int i = 0; i < this.subdivisions; i++) {
                    Paint p = setRectangleAndGetPaint(base, i, increment, target, r, false);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (strip.isStripOutlineVisible()) {
                    g2.setPaint(strip.getStripOutlinePaint());
                    g2.setStroke(strip.getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMaxX()
                            - strip.getStripWidth(), target.getMinY(),
                            strip.getStripWidth(), target.getHeight()));
                }
                this.axis.draw(g2, target.getMaxX() - strip.getStripWidth()
                        - this.axisOffset, target, target, RectangleEdge.LEFT,
                        null);
            }
            else if (axisEdge == RectangleEdge.RIGHT) {
                for (int i = 0; i < this.subdivisions; i++) {
                    Paint p = setRectangleAndGetPaint(base, i, increment, target, r, true);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (strip.isStripOutlineVisible()) {
                    g2.setPaint(strip.getStripOutlinePaint());
                    g2.setStroke(strip.getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMinY(), strip.getStripWidth(),
                            target.getHeight()));
                }
                this.axis.draw(g2, target.getMinX() + strip.getStripWidth()
                        + this.axisOffset, target, target, RectangleEdge.RIGHT,
                        null);
            }
        }
        return null;
    }

    private Paint setRectangleAndGetPaint(double base, double i, double increment, Rectangle2D target, Rectangle2D r, boolean min) {
        double v = base + (i * increment);
        Paint p = this.scale.getPaint(v);
        double vv0 = this.axis.valueToJava2D(v, target, RectangleEdge.LEFT);
        double vv1 = this.axis.valueToJava2D(v + increment, target, RectangleEdge.LEFT);
        double hh = Math.abs(vv1 - vv0) + 1.0;
        if (min) r.setRect(target.getMinX(), Math.min(vv0, vv1), strip.getStripWidth(), hh);
        else r.setRect(target.getMaxX() - strip.getStripWidth(), Math.min(vv0, vv1), strip.getStripWidth(), hh);

        return p;
    }

    /**
     * Tests this legend for equality with an arbitrary object.
     *
     * @param obj  the object ({@code null} permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PaintScaleLegend)) {
            return false;
        }
        PaintScaleLegend that = (PaintScaleLegend) obj;
        if (!this.scale.equals(that.scale)) {
            return false;
        }
        if (!this.axis.equals(that.axis)) {
            return false;
        }
        if (!this.axisLocation.equals(that.axisLocation)) {
            return false;
        }
        if (this.axisOffset != that.axisOffset) {
            return false;
        }
        if (!this.strip.equals(that.strip)) {
            return false;
        }
        if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
        }
        if (this.subdivisions != that.subdivisions) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.backgroundPaint, stream);
        SerialUtils.writePaint(strip.getStripOutlinePaint(), stream);
        SerialUtils.writeStroke(strip.getStripOutlineStroke(), stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.backgroundPaint = SerialUtils.readPaint(stream);
        this.strip = new PaintScaleLegendStrip(this, 15.0, true, Color.GRAY, new BasicStroke(0.5f));
        strip.setStripOutlinePaint(SerialUtils.readPaint(stream));
        strip.setStripOutlineStroke(SerialUtils.readStroke(stream));
    }

}
