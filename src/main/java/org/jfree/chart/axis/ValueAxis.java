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
 * --------------
 * ValueAxis.java
 * --------------
 * (C) Copyright 2000-2022, by David Gilbert and Contributors.
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   Jonathan Nash;
 *                   Nicolas Brodu (for Astrium and EADS Corporate Research
 *                   Center);
 *                   Peter Kolb (patch 1934255);
 *                   Andrew Mickish (patch 1870189);
 *
 */

package org.jfree.chart.axis;

import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.data.Range;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;

/**
 * The base class for axes that display value data, where values are measured
 * using the {@code double} primitive.  The two key subclasses are
 * {@link DateAxis} and {@link NumberAxis}.
 */
public abstract class ValueAxis extends Axis
        implements Cloneable, PublicCloneable {

    /**
     * The default axis range.
     */
    public static final Range DEFAULT_RANGE = new Range(0.0, 1.0);

    /**
     * The default auto-range value.
     */
    public static final boolean DEFAULT_AUTO_RANGE = true;

    /**
     * The default inverted flag setting.
     */
    public static final boolean DEFAULT_INVERTED = false;

    /**
     * The default minimum auto range.
     */
    public static final double DEFAULT_AUTO_RANGE_MINIMUM_SIZE = 0.00000001;

    /**
     * The default value for the lower margin (0.05 = 5%).
     */
    public static final double DEFAULT_LOWER_MARGIN = 0.05;

    /**
     * The default value for the upper margin (0.05 = 5%).
     */
    public static final double DEFAULT_UPPER_MARGIN = 0.05;

    /**
     * The default auto-tick-unit-selection value.
     */
    public static final boolean DEFAULT_AUTO_TICK_UNIT_SELECTION = true;

    /**
     * The maximum tick count.
     */
    public static final int MAXIMUM_TICK_COUNT = 500;

    protected Arrow arrow;

    /**
     * The standard tick units for the axis.
     */
    private TickUnitSource standardTickUnits;

    /**
     * A flag that affects the orientation of the values on the axis.
     */
    private boolean inverted;

    /**
     * The axis range.
     */
    private Range range;

    /**
     * Flag that indicates whether the axis automatically scales to fit the
     * chart data.
     */
    private boolean autoRange;

    /**
     * The minimum size for the 'auto' axis range (excluding margins).
     */
    private double autoRangeMinimumSize;

    /**
     * The default range is used when the dataset is empty and the axis needs
     * to determine the auto range.
     */
    private Range defaultAutoRange;

    /**
     * The upper margin percentage.  This indicates the amount by which the
     * maximum axis value exceeds the maximum data value (as a percentage of
     * the range on the axis) when the axis range is determined automatically.
     */
    private double upperMargin;

    /**
     * The lower margin.  This is a percentage that indicates the amount by
     * which the minimum axis value is "less than" the minimum data value when
     * the axis range is determined automatically.
     */
    private double lowerMargin;

    /**
     * If this value is positive, the amount is subtracted from the maximum
     * data value to determine the lower axis range.  This can be used to
     * provide a fixed "window" on dynamic data.
     */
    private double fixedAutoRange;

    /**
     * Flag that indicates whether or not the tick unit is selected
     * automatically.
     */
    private boolean autoTickUnitSelection;

    /**
     * The number of minor ticks per major tick unit.  This is an override
     * field, if the value is &gt; 0 it is used, otherwise the axis refers to the
     * minorTickCount in the current tickUnit.
     */
    private int minorTickCount;

    /**
     * A flag indicating whether or not tick labels are rotated to vertical.
     */
    private boolean verticalTickLabels;

    /**
     * Constructs a value axis.
     *
     * @param label             the axis label ({@code null} permitted).
     * @param standardTickUnits the source for standard tick units
     *                          ({@code null} permitted).
     */
    protected ValueAxis(String label, TickUnitSource standardTickUnits) {

        super(label);
        arrow = new Arrow(this);

        this.range = DEFAULT_RANGE;
        this.autoRange = DEFAULT_AUTO_RANGE;
        this.defaultAutoRange = DEFAULT_RANGE;

        this.inverted = DEFAULT_INVERTED;
        this.autoRangeMinimumSize = DEFAULT_AUTO_RANGE_MINIMUM_SIZE;

        this.lowerMargin = DEFAULT_LOWER_MARGIN;
        this.upperMargin = DEFAULT_UPPER_MARGIN;

        this.fixedAutoRange = 0.0;

        this.autoTickUnitSelection = DEFAULT_AUTO_TICK_UNIT_SELECTION;
        this.standardTickUnits = standardTickUnits;

        Polygon p1 = new Polygon();
        p1.addPoint(0, 0);
        p1.addPoint(-2, 2);
        p1.addPoint(2, 2);

        this.arrow.setUpArrow(p1);

        Polygon p2 = new Polygon();
        p2.addPoint(0, 0);
        p2.addPoint(-2, -2);
        p2.addPoint(2, -2);

        this.arrow.setDownArrow(p2);

        Polygon p3 = new Polygon();
        p3.addPoint(0, 0);
        p3.addPoint(-2, -2);
        p3.addPoint(-2, 2);

        this.arrow.setRightArrow(p3);

        Polygon p4 = new Polygon();
        p4.addPoint(0, 0);
        p4.addPoint(2, -2);
        p4.addPoint(2, 2);

        this.arrow.setLeftArrow(p4);

        this.verticalTickLabels = false;
        this.minorTickCount = 0;

    }

    /**
     * Returns the source for obtaining standard tick units for the axis.
     *
     * @return The source (possibly {@code null}).
     * @see #setStandardTickUnits(TickUnitSource)
     */
    public TickUnitSource getStandardTickUnits() {
        return standardTickUnits;
    }

    /**
     * Sets the source for obtaining standard tick units for the axis and sends
     * an {@link AxisChangeEvent} to all registered listeners.  The axis will
     * try to select the smallest tick unit from the source that does not cause
     * the tick labels to overlap (see also the
     *
     * @param source the source for standard tick units ({@code null}
     *               permitted).
     * @see #getStandardTickUnits()
     */
    public void setStandardTickUnits(TickUnitSource source) {
        this.standardTickUnits = source;
        fireChangeEvent();
    }

    /**
     * Returns {@code true} if the tick labels should be rotated (to
     * vertical), and {@code false} otherwise.
     *
     * @return {@code true} or {@code false}.
     * @see #setVerticalTickLabels(boolean)
     */
    public boolean isVerticalTickLabels() {
        return this.verticalTickLabels;
    }

    /**
     * Sets the flag that controls whether the tick labels are displayed
     * vertically (that is, rotated 90 degrees from horizontal).  If the flag
     * is changed, an {@link AxisChangeEvent} is sent to all registered
     * listeners.
     *
     * @param flag the flag.
     * @see #isVerticalTickLabels()
     */
    public void setVerticalTickLabels(boolean flag) {
        if (this.verticalTickLabels != flag) {
            this.verticalTickLabels = flag;
            fireChangeEvent();
        }
    }

    /**
     * Draws an axis line at the current cursor position and edge.
     *
     * @param g2       the graphics device ({@code null} not permitted).
     * @param cursor   the cursor position.
     * @param dataArea the data area.
     * @param edge     the edge.
     */
    @Override
    protected void drawAxisLine(Graphics2D g2, double cursor,
                                Rectangle2D dataArea, RectangleEdge edge) {
        Line2D axisLine = null;
        if (edge == RectangleEdge.TOP) {
            axisLine = new Line2D.Double(dataArea.getX(), cursor, dataArea.getMaxX(),
                    cursor);
        } else if (edge == RectangleEdge.BOTTOM) {
            axisLine = new Line2D.Double(dataArea.getX(), cursor, dataArea.getMaxX(),
                    cursor);
        } else if (edge == RectangleEdge.LEFT) {
            axisLine = new Line2D.Double(cursor, dataArea.getY(), cursor,
                    dataArea.getMaxY());
        } else if (edge == RectangleEdge.RIGHT) {
            axisLine = new Line2D.Double(cursor, dataArea.getY(), cursor,
                    dataArea.getMaxY());
        }
        return axisLine;
    }

    private void paintAndRender(Graphics2D g2, Line2D axisLine){
        g2.setPaint(getAxisLinePaint());
        g2.setStroke(getAxisLineStroke());
        Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.draw(axisLine);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
    }

    private void drawArrow(Graphics2D g2, double x, double y, Shape arrow){
        AffineTransform transformer = new AffineTransform();
        transformer.setToTranslation(x, y);
        Shape shape = transformer.createTransformedShape(arrow);
        g2.fill(shape);
        g2.draw(shape);
    }

    private void drawDown(RectangleEdge edge, Rectangle2D dataArea, double cursor, Graphics2D g2){
        double x = 0.0;
        double y = 0.0;
        Shape arrow = null;
        if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
            x = dataArea.getMinX();
            y = cursor;
            arrow = this.leftArrow;
        } else if (edge == RectangleEdge.LEFT
                || edge == RectangleEdge.RIGHT) {
            x = cursor;
            y = dataArea.getMaxY();
            arrow = this.downArrow;
        }

        // draw the arrow...
        drawArrow(g2, x, y, arrow);
    }

    private void drawUp(RectangleEdge edge, Rectangle2D dataArea, double cursor, Graphics2D g2){
        double x = 0.0;
        double y = 0.0;
        Shape arrow = null;
        if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
            x = dataArea.getMaxX();
            y = cursor;
            arrow = this.rightArrow;
        } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
            x = cursor;
            y = dataArea.getMinY();
            arrow = this.upArrow;
        }

        // draw the arrow...
        drawArrow(g2, x, y, arrow);
    }

    /**
     * Draws an axis line at the current cursor position and edge.
     *
     * @param g2  the graphics device ({@code null} not permitted).
     * @param cursor  the cursor position.
     * @param dataArea  the data area.
     * @param edge  the edge.
     */
    @Override
    protected void drawAxisLine(Graphics2D g2, double cursor,
            Rectangle2D dataArea, RectangleEdge edge) {
        Line2D axisLine = setAxisLine(edge, dataArea, cursor);
        paintAndRender(g2, axisLine);

        boolean drawUpOrRight = false;
        boolean drawDownOrLeft = false;
        if (this.arrow.isPositiveArrowVisible()) {
            if (this.inverted) {
                drawDownOrLeft = true;
            } else {
                drawUpOrRight = true;
            }
        }
        if (this.arrow.isNegativeArrowVisible()) {
            if (this.inverted) {
                drawUpOrRight = true;
            } else {
                drawDownOrLeft = true;
            }
        }
        if (drawUpOrRight) {
            drawUp(edge, dataArea, cursor, g2);
        }

        if (drawDownOrLeft) {
            drawDown(edge, dataArea, cursor, g2);
        }

    }

    /**
     * Calculates the anchor point for a tick label.
     *
     * @param tick     the tick.
     * @param cursor   the cursor.
     * @param dataArea the data area.
     * @param edge     the edge on which the axis is drawn.
     * @return The x and y coordinates of the anchor point.
     */
    protected float[] calculateAnchorPoint(ValueTick tick, double cursor,
                                           Rectangle2D dataArea, RectangleEdge edge) {

        RectangleInsets insets = tickLabel.getTickLabelInsets();
        float[] result = new float[2];
        if (edge == RectangleEdge.TOP) {
            result[0] = (float) valueToJava2D(tick.getValue(), dataArea, edge);
            result[1] = (float) (cursor - insets.getBottom() - 2.0);
        } else if (edge == RectangleEdge.BOTTOM) {
            result[0] = (float) valueToJava2D(tick.getValue(), dataArea, edge);
            result[1] = (float) (cursor + insets.getTop() + 2.0);
        } else if (edge == RectangleEdge.LEFT) {
            result[0] = (float) (cursor - insets.getLeft() - 2.0);
            result[1] = (float) valueToJava2D(tick.getValue(), dataArea, edge);
        } else if (edge == RectangleEdge.RIGHT) {
            result[0] = (float) (cursor + insets.getRight() + 2.0);
            result[1] = (float) valueToJava2D(tick.getValue(), dataArea, edge);
        }
        return result;
    }

    private float[] setAnchorPoint(Graphics2D g2, ValueTick tick, double cursor, Rectangle2D dataArea, RectangleEdge edge){
        float[] anchorPoint = new float[0];
        if (isTickLabelsVisible()) {
            g2.setPaint(getTickLabelPaint());
            anchorPoint = calculateAnchorPoint(tick, cursor,
                    dataArea, edge);
        }
        return anchorPoint;
    }

    private double getOL(ValueTick tick){
        return (tick.getTickType().equals(TickType.MINOR))
                ? getMinorTickMarkOutsideLength()
                : getTickMarkOutsideLength();
    }

    private double getIL(ValueTick tick){
        return (tick.getTickType().equals(TickType.MINOR))
                ? getMinorTickMarkInsideLength()
                : getTickMarkInsideLength();
    }

    private void drawMark(Graphics2D g2, RectangleEdge edge, double cursor, double ol, double xx, double il){
        Line2D mark = null;
        g2.setStroke(getTickMarkStroke());
        g2.setPaint(getTickMarkPaint());
        if (edge == RectangleEdge.LEFT) {
            mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
        }
        else if (edge == RectangleEdge.RIGHT) {
            mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
        }
        else if (edge == RectangleEdge.TOP) {
            mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
        }
        else if (edge == RectangleEdge.BOTTOM) {
            mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
        }
        g2.draw(mark);
    }

    private void drawTickMarksHelper(ValueTick tick, RectangleEdge edge, Rectangle2D dataArea, Graphics2D g2, double cursor){
        if ((isTickMarksVisible() && tick.getTickType().equals(
                TickType.MAJOR)) || (isMinorTickMarksVisible()
                && tick.getTickType().equals(TickType.MINOR))) {

            double ol = getOL(tick);

            double il = getIL(tick);

            float xx = (float) valueToJava2D(tick.getValue(), dataArea,
                    edge);
            drawMark(g2, edge, cursor, ol, xx, il);
        }
    }

    private void setCursor(RectangleEdge edge, double used, List ticks, Graphics2D g2, Rectangle2D plotArea,
                           AxisState state){
        if (edge == RectangleEdge.LEFT) {
            used += findMaximumTickLabelWidth(ticks, g2, plotArea,
                    isVerticalTickLabels());
            state.cursorLeft(used);
        } else if (edge == RectangleEdge.RIGHT) {
            used = findMaximumTickLabelWidth(ticks, g2, plotArea,
                    isVerticalTickLabels());
            state.cursorRight(used);
        } else if (edge == RectangleEdge.TOP) {
            used = findMaximumTickLabelHeight(ticks, g2, plotArea,
                    isVerticalTickLabels());
            state.cursorUp(used);
        } else if (edge == RectangleEdge.BOTTOM) {
            used = findMaximumTickLabelHeight(ticks, g2, plotArea,
                    isVerticalTickLabels());
            state.cursorDown(used);
        }
    }


    /**
     * Draws the axis line, tick marks and tick mark labels.
     *
     * @param g2       the graphics device ({@code null} not permitted).
     * @param cursor   the cursor.
     * @param plotArea the plot area ({@code null} not permitted).
     * @param dataArea the data area ({@code null} not permitted).
     * @param edge     the edge that the axis is aligned with ({@code null}
     *                 not permitted).
     * @return The width or height used to draw the axis.
     */
    protected AxisState drawTickMarksAndLabels(Graphics2D g2,
                                               double cursor, Rectangle2D plotArea, Rectangle2D dataArea,
                                               RectangleEdge edge) {

        AxisState state = new AxisState(cursor);
        if (isAxisLineVisible()) {
            drawAxisLine(g2, cursor, dataArea, edge);
        }
        List ticks = refreshTicks(g2, state, dataArea, edge);
        state.setTicks(ticks);
        g2.setFont(tickLabel.getTickLabelFont());
        Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        for (Object o : ticks) {
            ValueTick tick = (ValueTick) o;
            float[] anchorPoint = setAnchorPoint(g2, tick, cursor, dataArea, edge);
            if (tick instanceof LogTick) {
                LogTick lt = (LogTick) tick;
                if (lt.getAttributedLabel() == null) {
                    continue;
                }
                AttrStringUtils.drawRotatedString(lt.getAttributedLabel(), g2, anchorPoint[0], anchorPoint[1],
                        tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor());
            } else {
                if (tick.getText() == null) {
                    continue;
                }
                TextUtils.drawRotatedString(tick.getText(), g2, anchorPoint[0], anchorPoint[1], tick.getTextAnchor(),
                        tick.getAngle(), tick.getRotationAnchor());
            }
            drawTickMarksHelper(tick, edge, dataArea, g2, cursor);
        }
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);

        // need to work out the space used by the tick labels...
        // so we can update the cursor...
        double used = 0.0;
        if (tickLabel.isTickLabelsVisible()) {
            setCursor(edge, used, ticks, g2, plotArea, state);
        }

        return state;
    }

    /**
     * Returns the space required to draw the axis.
     *
     * @param g2       the graphics device.
     * @param plot     the plot that the axis belongs to.
     * @param plotArea the area within which the plot should be drawn.
     * @param edge     the axis location.
     * @param space    the space already reserved (for other axes).
     * @return The space required to draw the axis (including pre-reserved
     * space).
     */
    @Override
    public AxisSpace reserveSpace(Graphics2D g2, Plot plot,
                                  Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {

        // create a new space object if one wasn't supplied...
        if (space == null) {
            space = new AxisSpace();
        }

        // if the axis is not visible, no additional space is required...
        if (!isVisible()) {
            return space;
        }

        // if the axis has a fixed dimension, return it...
        double dimension = getFixedDimension();
        if (dimension > 0.0) {
            space.add(dimension, edge);
            return space;
        }

        // calculate the max size of the tick labels (if visible)...
        double tickLabelHeight = 0.0;
        double tickLabelWidth = 0.0;
        if (tickLabel.isTickLabelsVisible()) {
            g2.setFont(tickLabel.getTickLabelFont());
            List ticks = refreshTicks(g2, new AxisState(), plotArea, edge);
            if (RectangleEdge.isTopOrBottom(edge)) {
                tickLabelHeight = findMaximumTickLabelHeight(ticks, g2,
                        isVerticalTickLabels());
            }
            else if (RectangleEdge.isLeftOrRight(edge)) {
                tickLabelWidth = findMaximumTickLabelWidth(ticks, g2,
                        isVerticalTickLabels());
            }
        }

        // get the axis label size and update the space object...
        Rectangle2D labelEnclosure = getLabelEnclosure(g2, edge);
        if (RectangleEdge.isTopOrBottom(edge)) {
            double labelHeight = labelEnclosure.getHeight();
            space.add(labelHeight + tickLabelHeight, edge);
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            double labelWidth = labelEnclosure.getWidth();
            space.add(labelWidth + tickLabelWidth, edge);
        }

        return space;

    }

    private Rectangle2D setLabelBounds(Graphics2D g2, FontMetrics fm, Tick o) {
        Rectangle2D labelBounds;
        if (o instanceof LogTick) {
            LogTick lt = (LogTick) o;
            if (lt.getAttributedLabel() != null) {
                labelBounds = AttrStringUtils.getTextBounds(
                        lt.getAttributedLabel(), g2);
                return labelBounds;
            }
        } else if (o.getText() != null) {
            labelBounds = TextUtils.getTextBounds(
                    o.getText(), g2, fm);
            return labelBounds;
        }
        return null;
    }

    /**
     * A utility method for determining the height of the tallest tick label.
     *
     * @param ticks  the ticks.
     * @param g2  the graphics device.
     * @param vertical  a flag that indicates whether or not the tick labels
     *                  are 'vertical'.
     *
     * @return The height of the tallest tick label.
     */
    protected double findMaximumTickLabelHeight(List ticks, Graphics2D g2,
                                                boolean vertical) {

        RectangleInsets insets = tickLabel.getTickLabelInsets();
        Font font = tickLabel.getTickLabelFont();
        g2.setFont(font);
        double maxHeight = 0.0;
        if (vertical) {
            FontMetrics fm = g2.getFontMetrics(font);
            for (Object o : ticks) {
                Rectangle2D labelBounds = setLabelBounds(g2, fm, (Tick) o);
                if (labelBounds != null && labelBounds.getWidth()
                        + insets.getTop() + insets.getBottom() > maxHeight) {
                    maxHeight = labelBounds.getWidth()
                            + insets.getTop() + insets.getBottom();
                }
            }
        } else {
            LineMetrics metrics = font.getLineMetrics("ABCxyz", g2.getFontRenderContext());
            maxHeight = metrics.getHeight() + insets.getTop() + insets.getBottom();
        }
        return maxHeight;

    }

    /**
     * A utility method for determining the width of the widest tick label.
     *
     * @param ticks  the ticks.
     * @param g2  the graphics device.
     * @param vertical  a flag that indicates whether or not the tick labels
     *                  are 'vertical'.
     *
     * @return The width of the tallest tick label.
     */
    protected double findMaximumTickLabelWidth(List ticks, Graphics2D g2,
                                               boolean vertical) {

        RectangleInsets insets = tickLabel.getTickLabelInsets();
        Font font = tickLabel.getTickLabelFont();
        double maxWidth = 0.0;
        if (!vertical) {
            FontMetrics fm = g2.getFontMetrics(font);
            for (Object o : ticks) {
                Rectangle2D labelBounds = setLabelBounds(g2, fm, (Tick) o);
                if (labelBounds != null
                        && labelBounds.getWidth() + insets.getLeft()
                        + insets.getRight() > maxWidth) {
                    maxWidth = labelBounds.getWidth()
                            + insets.getLeft() + insets.getRight();
                }
            }
        } else {
            LineMetrics metrics = font.getLineMetrics("ABCxyz", g2.getFontRenderContext());
            maxWidth = metrics.getHeight() + insets.getTop() + insets.getBottom();
        }
        return maxWidth;

    }

    /**
     * REFACTOR - usado para remover código duplicado entre findMaximumTickLabelWidth e findMaximumTickLabelHeight
     * @author Afonso Caniço
     */
    private Rectangle2D getLabelBounds(Graphics2D g2, FontMetrics fm, Tick tick) {
        if (tick instanceof LogTick) {
            LogTick lt = (LogTick) tick;
            if (lt.getAttributedLabel() != null) {
                return AttrStringUtils.getTextBounds(lt.getAttributedLabel(), g2);
            }
        } else if (tick.getText() != null) {
            return TextUtils.getTextBounds(tick.getText(), g2, fm);
        }
        return null;
    }

    /**
     * Returns a flag that controls the direction of values on the axis.
     * <p>
     * For a regular axis, values increase from left to right (for a horizontal
     * axis) and bottom to top (for a vertical axis).  When the axis is
     * 'inverted', the values increase in the opposite direction.
     *
     * @return The flag.
     * @see #setInverted(boolean)
     */
    public boolean isInverted() {
        return this.inverted;
    }

    /**
     * Sets a flag that controls the direction of values on the axis, and
     * notifies registered listeners that the axis has changed.
     *
     * @param flag the flag.
     * @see #isInverted()
     */
    public void setInverted(boolean flag) {
        if (this.inverted != flag) {
            this.inverted = flag;
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the axis range is
     * automatically adjusted to fit the data values.
     *
     * @return The flag.
     * @see #setAutoRange(boolean)
     */
    public boolean isAutoRange() {
        return this.autoRange;
    }

    /**
     * Sets a flag that determines whether or not the axis range is
     * automatically adjusted to fit the data, and notifies registered
     * listeners that the axis has been modified.
     *
     * @param auto the new value of the flag.
     * @see #isAutoRange()
     */
    public void setAutoRange(boolean auto) {
        setAutoRange(auto, true);
    }

    /**
     * Sets the auto range attribute.  If the {@code notify} flag is set,
     * an {@link AxisChangeEvent} is sent to registered listeners.
     *
     * @param auto   the flag.
     * @param notify notify listeners?
     * @see #isAutoRange()
     */
    protected void setAutoRange(boolean auto, boolean notify) {
        this.autoRange = auto;
        if (this.autoRange) {
            autoAdjustRange();
        }
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the minimum size allowed for the axis range when it is
     * automatically calculated.
     *
     * @return The minimum range.
     * @see #setAutoRangeMinimumSize(double)
     */
    public double getAutoRangeMinimumSize() {
        return this.autoRangeMinimumSize;
    }

    /**
     * Sets the auto range minimum size and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param size the size.
     * @see #getAutoRangeMinimumSize()
     */
    public void setAutoRangeMinimumSize(double size) {
        setAutoRangeMinimumSize(size, true);
    }

    /**
     * Sets the minimum size allowed for the axis range when it is
     * automatically calculated.
     * <p>
     * If requested, an {@link AxisChangeEvent} is forwarded to all registered
     * listeners.
     *
     * @param size   the new minimum.
     * @param notify notify listeners?
     */
    public void setAutoRangeMinimumSize(double size, boolean notify) {
        if (size <= 0.0) {
            throw new IllegalArgumentException(
                    "NumberAxis.setAutoRangeMinimumSize(double): must be > 0.0.");
        }
        if (this.autoRangeMinimumSize != size) {
            this.autoRangeMinimumSize = size;
            if (this.autoRange) {
                autoAdjustRange();
            }
            if (notify) {
                fireChangeEvent();
            }
        }

    }

    /**
     * Returns the default auto range.
     *
     * @return The default auto range (never {@code null}).
     * @see #setDefaultAutoRange(Range)
     */
    public Range getDefaultAutoRange() {
        return this.defaultAutoRange;
    }

    /**
     * Sets the default auto range and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param range the range ({@code null} not permitted).
     * @see #getDefaultAutoRange()
     */
    public void setDefaultAutoRange(Range range) {
        Args.nullNotPermitted(range, "range");
        this.defaultAutoRange = range;
        fireChangeEvent();
    }

    /**
     * Returns the lower margin for the axis, expressed as a percentage of the
     * axis range.  This controls the space added to the lower end of the axis
     * when the axis range is automatically calculated (it is ignored when the
     * axis range is set explicitly). The default value is 0.05 (five percent).
     *
     * @return The lower margin.
     * @see #setLowerMargin(double)
     */
    public double getLowerMargin() {
        return this.lowerMargin;
    }

    /**
     * Sets the lower margin for the axis (as a percentage of the axis range)
     * and sends an {@link AxisChangeEvent} to all registered listeners.  This
     * margin is added only when the axis range is auto-calculated - if you set
     * the axis range manually, the margin is ignored.
     *
     * @param margin the margin percentage (for example, 0.05 is five percent).
     * @see #getLowerMargin()
     * @see #setUpperMargin(double)
     */
    public void setLowerMargin(double margin) {
        this.lowerMargin = margin;
        if (isAutoRange()) {
            autoAdjustRange();
        }
        fireChangeEvent();
    }

    /**
     * Returns the upper margin for the axis, expressed as a percentage of the
     * axis range.  This controls the space added to the lower end of the axis
     * when the axis range is automatically calculated (it is ignored when the
     * axis range is set explicitly). The default value is 0.05 (five percent).
     *
     * @return The upper margin.
     * @see #setUpperMargin(double)
     */
    public double getUpperMargin() {
        return this.upperMargin;
    }

    /**
     * Sets the upper margin for the axis (as a percentage of the axis range)
     * and sends an {@link AxisChangeEvent} to all registered listeners.  This
     * margin is added only when the axis range is auto-calculated - if you set
     * the axis range manually, the margin is ignored.
     *
     * @param margin the margin percentage (for example, 0.05 is five percent).
     * @see #getLowerMargin()
     * @see #setLowerMargin(double)
     */
    public void setUpperMargin(double margin) {
        this.upperMargin = margin;
        if (isAutoRange()) {
            autoAdjustRange();
        }
        fireChangeEvent();
    }

    /**
     * Returns the fixed auto range.
     *
     * @return The length.
     * @see #setFixedAutoRange(double)
     */
    public double getFixedAutoRange() {
        return this.fixedAutoRange;
    }

    /**
     * Sets the fixed auto range for the axis.
     *
     * @param length the range length.
     * @see #getFixedAutoRange()
     */
    public void setFixedAutoRange(double length) {
        this.fixedAutoRange = length;
        if (isAutoRange()) {
            autoAdjustRange();
        }
        fireChangeEvent();
    }

    /**
     * Returns the lower bound of the axis range.
     *
     * @return The lower bound.
     * @see #setLowerBound(double)
     */
    public double getLowerBound() {
        return this.range.getLowerBound();
    }

    /**
     * Sets the lower bound for the axis range.  An {@link AxisChangeEvent} is
     * sent to all registered listeners.
     *
     * @param min the new minimum.
     * @see #getLowerBound()
     */
    public void setLowerBound(double min) {
        if (this.range.getUpperBound() > min) {
            setRange(new Range(min, this.range.getUpperBound()));
        } else {
            setRange(new Range(min, min + 1.0));
        }
    }

    /**
     * Returns the upper bound for the axis range.
     *
     * @return The upper bound.
     * @see #setUpperBound(double)
     */
    public double getUpperBound() {
        return this.range.getUpperBound();
    }

    /**
     * Sets the upper bound for the axis range, and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param max the new maximum.
     * @see #getUpperBound()
     */
    public void setUpperBound(double max) {
        if (this.range.getLowerBound() < max) {
            setRange(new Range(this.range.getLowerBound(), max));
        } else {
            setRange(max - 1.0, max);
        }
    }

    /**
     * Returns the range for the axis.
     *
     * @return The axis range (never {@code null}).
     * @see #setRange(Range)
     */
    public Range getRange() {
        return this.range;
    }

    /**
     * Sets the range for the axis and sends a change event to all registered
     * listeners.  As a side-effect, the auto-range flag is set to
     * {@code false}.
     *
     * @param range the range ({@code null} not permitted).
     * @see #getRange()
     */
    public void setRange(Range range) {
        // defer argument checking
        setRange(range, true, true);
    }

    /**
     * Sets the range for the axis and, if requested, sends a change event to
     * all registered listeners.  Furthermore, if {@code turnOffAutoRange}
     * is {@code true}, the auto-range flag is set to {@code false}
     * (normally when setting the axis range manually the caller expects that
     * range to remain in force).
     *
     * @param range            the range ({@code null} not permitted).
     * @param turnOffAutoRange a flag that controls whether or not the auto
     *                         range is turned off.
     * @param notify           a flag that controls whether or not listeners are
     *                         notified.
     * @see #getRange()
     */
    public void setRange(Range range, boolean turnOffAutoRange,
                         boolean notify) {
        Args.nullNotPermitted(range, "range");
        if (range.getLength() <= 0.0) {
            throw new IllegalArgumentException(
                    "A positive range length is required: " + range);
        }
        if (turnOffAutoRange) {
            this.autoRange = false;
        }
        this.range = range;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Sets the range for the axis and sends a change event to all registered
     * listeners.  As a side-effect, the auto-range flag is set to
     * {@code false}.
     *
     * @param lower the lower axis limit.
     * @param upper the upper axis limit.
     * @see #getRange()
     * @see #setRange(Range)
     */
    public void setRange(double lower, double upper) {
        setRange(new Range(lower, upper));
    }

    /**
     * Sets the range for the axis (after first adding the current margins to
     * the specified range) and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param range the range ({@code null} not permitted).
     */
    public void setRangeWithMargins(Range range) {
        setRangeWithMargins(range, true, true);
    }

    /**
     * Sets the range for the axis after first adding the current margins to
     * the range and, if requested, sends an {@link AxisChangeEvent} to all
     * registered listeners.  As a side-effect, the auto-range flag is set to
     * {@code false} (optional).
     *
     * @param range            the range (excluding margins, {@code null} not
     *                         permitted).
     * @param turnOffAutoRange a flag that controls whether or not the auto
     *                         range is turned off.
     * @param notify           a flag that controls whether or not listeners are
     *                         notified.
     */
    public void setRangeWithMargins(Range range, boolean turnOffAutoRange,
                                    boolean notify) {
        Args.nullNotPermitted(range, "range");
        setRange(Range.expand(range, getLowerMargin(), getUpperMargin()),
                turnOffAutoRange, notify);
    }

    /**
     * Sets the axis range (after first adding the current margins to the
     * range) and sends an {@link AxisChangeEvent} to all registered listeners.
     * As a side-effect, the auto-range flag is set to {@code false}.
     *
     * @param lower the lower axis limit.
     * @param upper the upper axis limit.
     */
    public void setRangeWithMargins(double lower, double upper) {
        setRangeWithMargins(new Range(lower, upper));
    }

    /**
     * Sets the axis range, where the new range is 'size' in length, and
     * centered on 'value'.
     *
     * @param value  the central value.
     * @param length the range length.
     */
    public void setRangeAboutValue(double value, double length) {
        setRange(new Range(value - length / 2, value + length / 2));
    }

    /**
     * Returns a flag indicating whether or not the tick unit is automatically
     * selected from a range of standard tick units.
     *
     * @return A flag indicating whether or not the tick unit is automatically
     * selected.
     * @see #setAutoTickUnitSelection(boolean)
     */
    public boolean isAutoTickUnitSelection() {
        return this.autoTickUnitSelection;
    }

    /**
     * Sets a flag indicating whether or not the tick unit is automatically
     * selected from a range of standard tick units.  If the flag is changed,
     * registered listeners are notified that the chart has changed.
     *
     * @param flag the new value of the flag.
     * @see #isAutoTickUnitSelection()
     */
    public void setAutoTickUnitSelection(boolean flag) {
        setAutoTickUnitSelection(flag, true);
    }

    /**
     * Sets a flag indicating whether or not the tick unit is automatically
     * selected from a range of standard tick units.
     *
     * @param flag   the new value of the flag.
     * @param notify notify listeners?
     * @see #isAutoTickUnitSelection()
     */
    public void setAutoTickUnitSelection(boolean flag, boolean notify) {

        if (this.autoTickUnitSelection != flag) {
            this.autoTickUnitSelection = flag;
            if (notify) {
                fireChangeEvent();
            }
        }
    }

    /**
     * Returns the number of minor tick marks to display.
     *
     * @return The number of minor tick marks to display.
     * @see #setMinorTickCount(int)
     */
    public int getMinorTickCount() {
        return this.minorTickCount;
    }

    /**
     * Sets the number of minor tick marks to display, and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param count the count.
     * @see #getMinorTickCount()
     */
    public void setMinorTickCount(int count) {
        this.minorTickCount = count;
        fireChangeEvent();
    }

    /**
     * Converts a data value to a coordinate in Java2D space, assuming that the
     * axis runs along one edge of the specified dataArea.
     * <p>
     * Note that it is possible for the coordinate to fall outside the area.
     *
     * @param value the data value.
     * @param area  the area for plotting the data.
     * @param edge  the edge along which the axis lies.
     * @return The Java2D coordinate.
     * @see #java2DToValue(double, Rectangle2D, RectangleEdge)
     */
    public abstract double valueToJava2D(double value, Rectangle2D area,
                                         RectangleEdge edge);

    /**
     * Converts a length in data coordinates into the corresponding length in
     * Java2D coordinates.
     *
     * @param length the length.
     * @param area   the plot area.
     * @param edge   the edge along which the axis lies.
     * @return The length in Java2D coordinates.
     */
    public double lengthToJava2D(double length, Rectangle2D area,
                                 RectangleEdge edge) {
        double zero = valueToJava2D(0.0, area, edge);
        double l = valueToJava2D(length, area, edge);
        return Math.abs(l - zero);
    }

    /**
     * Converts a coordinate in Java2D space to the corresponding data value,
     * assuming that the axis runs along one edge of the specified dataArea.
     *
     * @param java2DValue the coordinate in Java2D space.
     * @param area        the area in which the data is plotted.
     * @param edge        the edge along which the axis lies.
     * @return The data value.
     * @see #valueToJava2D(double, Rectangle2D, RectangleEdge)
     */
    public abstract double java2DToValue(double java2DValue, Rectangle2D area,
                                         RectangleEdge edge);

    /**
     * Automatically sets the axis range to fit the range of values in the
     * dataset.  Sometimes this can depend on the renderer used as well (for
     * example, the renderer may "stack" values, requiring an axis range
     * greater than otherwise necessary).
     */
    protected abstract void autoAdjustRange();

    /**
     * Centers the axis range about the specified value and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param value the center value.
     */
    public void centerRange(double value) {
        double central = this.range.getCentralValue();
        Range adjusted = new Range(this.range.getLowerBound() + value - central,
                this.range.getUpperBound() + value - central);
        setRange(adjusted);
    }

    /**
     * Increases or decreases the axis range by the specified percentage about
     * the central value and sends an {@link AxisChangeEvent} to all registered
     * listeners.
     * <p>
     * To double the length of the axis range, use 200% (2.0).
     * To halve the length of the axis range, use 50% (0.5).
     *
     * @param percent the resize factor.
     * @see #resizeRange(double, double)
     */
    public void resizeRange(double percent) {
        resizeRange(percent, this.range.getCentralValue());
    }

    /**
     * Increases or decreases the axis range by the specified percentage about
     * the specified anchor value and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     * <p>
     * To double the length of the axis range, use 200% (2.0).
     * To halve the length of the axis range, use 50% (0.5).
     *
     * @param percent     the resize factor.
     * @param anchorValue the new central value after the resize.
     * @see #resizeRange(double)
     */
    public void resizeRange(double percent, double anchorValue) {
        if (percent > 0.0) {
            double halfLength = this.range.getLength() * percent / 2;
            Range adjusted = new Range(anchorValue - halfLength,
                    anchorValue + halfLength);
            setRange(adjusted);
        } else {
            setAutoRange(true);
        }
    }

    /**
     * Increases or decreases the axis range by the specified percentage about
     * the specified anchor value and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     * <p>
     * To double the length of the axis range, use 200% (2.0).
     * To halve the length of the axis range, use 50% (0.5).
     *
     * @param percent     the resize factor.
     * @param anchorValue the new central value after the resize.
     * @see #resizeRange(double)
     */
    public void resizeRange2(double percent, double anchorValue) {
        if (percent > 0.0) {
            double left = anchorValue - getLowerBound();
            double right = getUpperBound() - anchorValue;
            Range adjusted = new Range(anchorValue - left * percent,
                    anchorValue + right * percent);
            setRange(adjusted);
        } else {
            setAutoRange(true);
        }
    }

    /**
     * Zooms in on the current range.
     *
     * @param lowerPercent the new lower bound.
     * @param upperPercent the new upper bound.
     */
    public void zoomRange(double lowerPercent, double upperPercent) {
        double start = this.range.getLowerBound();
        double length = this.range.getLength();
        double r0, r1;
        if (isInverted()) {
            r0 = start + (length * (1 - upperPercent));
            r1 = start + (length * (1 - lowerPercent));
        } else {
            r0 = start + length * lowerPercent;
            r1 = start + length * upperPercent;
        }
        if ((r1 > r0) && !Double.isInfinite(r1 - r0)) {
            setRange(new Range(r0, r1));
        }
    }

    /**
     * Slides the axis range by the specified percentage.
     *
     * @param percent the percentage.
     */
    public void pan(double percent) {
        Range r = getRange();
        double length = range.getLength();
        double adj = length * percent;
        double lower = r.getLowerBound() + adj;
        double upper = r.getUpperBound() + adj;
        setRange(lower, upper);
    }

    /**
     * Tests the axis for equality with an arbitrary object.
     *
     * @param obj the object ({@code null} permitted).
     * @return {@code true} or {@code false}.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ValueAxis)) {
            return false;
        }
        ValueAxis that = (ValueAxis) obj;
        if (this.inverted != that.inverted) {
            return false;
        }
        // if autoRange is true, then the current range is irrelevant
        if (!this.autoRange && !Objects.equals(this.range, that.range)) {
            return false;
        }
        if (this.autoRange != that.autoRange) {
            return false;
        }
        if (this.autoRangeMinimumSize != that.autoRangeMinimumSize) {
            return false;
        }
        if (!this.defaultAutoRange.equals(that.defaultAutoRange)) {
            return false;
        }
        if (this.upperMargin != that.upperMargin) {
            return false;
        }
        if (this.lowerMargin != that.lowerMargin) {
            return false;
        }
        if (this.fixedAutoRange != that.fixedAutoRange) {
            return false;
        }
        if (this.autoTickUnitSelection != that.autoTickUnitSelection) {
            return false;
        }
        if (!Objects.equals(this.standardTickUnits, that.standardTickUnits)) {
            return false;
        }
        if (this.verticalTickLabels != that.verticalTickLabels) {
            return false;
        }
        if (this.minorTickCount != that.minorTickCount) {
            return false;
        }
        if (!arrow.equals(that.arrow)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a clone of the object.
     *
     * @return A clone.
     * @throws CloneNotSupportedException if some component of the axis does
     *                                    not support cloning.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void apply(StandardChartTheme theme) {
        setLabelFont(theme.getLargeFont());
        setLabelPaint(theme.getAxisLabelPaint());
        tickLabel.setTickLabelFont(theme.getRegularFont());
        tickLabel.setTickLabelPaint(theme.getTickLabelPaint());
    }

}
