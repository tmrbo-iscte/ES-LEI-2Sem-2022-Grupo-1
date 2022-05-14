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
 * ---------------
 * SymbolAxis.java
 * ---------------
 * (C) Copyright 2002-2021, by Anthony Boulestreau and Contributors.
 *
 * Original Author:  Anthony Boulestreau;
 * Contributor(s):   David Gilbert;
 *
 */

package org.jfree.chart.axis;

import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.text.TextUtils;
import org.jfree.data.Range;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A standard linear value axis that replaces integer values with symbols.
 */
public class SymbolAxis extends NumberAxis implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7216330468770619716L;

    public GridBand getGridBand() {
        return gridBand;
    }

    protected final GridBand gridBand = new GridBand(this);

    /** The list of symbols to display instead of the numeric values. */
    private final List symbols;

    /**
     * Constructs a symbol axis, using default attribute values where
     * necessary.
     *
     * @param label  the axis label ({@code null} permitted).
     * @param sv  the list of symbols to display instead of the numeric
     *            values.
     */
    public SymbolAxis(String label, String[] sv) {
        super(label);
        this.symbols = Arrays.asList(sv);
        this.gridBand.setGridBandsVisible(true);
        this.gridBand.setGridBandPaint(GridBand.DEFAULT_GRID_BAND_PAINT);
        this.gridBand.setGridBandAlternatePaint(GridBand.DEFAULT_GRID_BAND_ALTERNATE_PAINT);
        setAutoTickUnitSelection(false, false);
        setAutoRangeStickyZero(false);
    }

    /**
     * This operation is not supported by this axis.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot and axes should be drawn.
     * @param edge  the edge along which the axis is drawn.
     */
    @Override
    protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {
        throw new UnsupportedOperationException();
    }

    /**
     * Draws the axis on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device ({@code null} not permitted).
     * @param cursor  the cursor location.
     * @param plotArea  the area within which the plot and axes should be drawn
     *                  ({@code null} not permitted).
     * @param dataArea  the area within which the data should be drawn
     *                  ({@code null} not permitted).
     * @param edge  the axis location ({@code null} not permitted).
     * @param plotState  collects information about the plot
     *                   ({@code null} permitted).
     *
     * @return The axis state (never {@code null}).
     */
    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge, 
            PlotRenderingInfo plotState) {

        AxisState info = new AxisState(cursor);
        if (isVisible()) {
            info = super.draw(g2, cursor, plotArea, dataArea, edge, plotState);
        }
        if (this.gridBand.isGridBandsVisible()) {
            gridBand.drawGridBands(g2, dataArea, edge, info.getTicks());
        }
        return info;

    }

    /**
     * Rescales the axis to ensure that all data is visible.
     */
    @Override
    protected void autoAdjustRange() {
        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data
        }

        if (plot instanceof ValueAxisPlot) {

            // ensure that all the symbols are displayed
            double upper = this.symbols.size() - 1;
            double lower = 0;
            double range = upper - lower;

            // ensure the autorange is at least <minRange> in size...
            double minRange = getAutoRangeMinimumSize();
            if (range < minRange) {
                upper = (upper + lower + minRange) / 2;
                lower = (upper + lower - minRange) / 2;
            }

            // this ensure that the grid bands will be displayed correctly.
            double upperMargin = 0.5;
            double lowerMargin = 0.5;

            if (getAutoRangeIncludesZero()) {
                if (getAutoRangeStickyZero()) {
                    if (upper <= 0.0) {
                        upper = 0.0;
                    } else {
                        upper = upper + upperMargin;
                    }
                    if (lower >= 0.0) {
                        lower = 0.0;
                    } else {
                        lower = lower - lowerMargin;
                    }
                } else {
                    upper = Math.max(0.0, upper + upperMargin);
                    lower = Math.min(0.0, lower - lowerMargin);
                }
            } else {
                if (getAutoRangeStickyZero()) {
                    if (upper <= 0.0) {
                        upper = Math.min(0.0, upper + upperMargin);
                    } else {
                        upper = upper + upperMargin * range;
                    }
                    if (lower >= 0.0) {
                        lower = Math.max(0.0, lower - lowerMargin);
                    } else {
                        lower = lower - lowerMargin;
                    }
                } else {
                    upper = upper + upperMargin;
                    lower = lower - lowerMargin;
                }
            }
            setRange(new Range(lower, upper), false, false);
        }
    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param state  the axis state.
     * @param dataArea  the area in which the data should be drawn.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    @Override
    public List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
        List ticks = null;
        if (RectangleEdge.isTopOrBottom(edge)) {
            ticks = refreshTicksHorizontal(g2, dataArea, edge);
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            ticks = refreshTicksVertical(g2, dataArea, edge);
        }
        return ticks;
    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the data should be drawn.
     * @param edge  the location of the axis.
     *
     * @return The ticks.
     */
    @Override
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        List ticks = new java.util.ArrayList();

        Font tickLabelFont = tickLabel.getTickLabelFont();
        g2.setFont(tickLabelFont);

        double size = getTickUnit().getSize();
        int count = calculateVisibleTickCount();
        double lowestTickValue = calculateLowestVisibleTickValue();

        double previousDrawnTickLabelPos = 0.0;
        double previousDrawnTickLabelLength = 0.0;

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                double xx = valueToJava2D(currentTickValue, dataArea, edge);
                String tickLabel;
                NumberFormat formatter = getNumberFormatOverride();
                if (formatter != null) {
                    tickLabel = formatter.format(currentTickValue);
                }
                else {
                    tickLabel = valueToString(currentTickValue);
                }

                // avoid to draw overlapping tick labels
                Rectangle2D bounds = TextUtils.getTextBounds(tickLabel, g2,
                        g2.getFontMetrics());
                double tickLabelLength = isVerticalTickLabels()
                        ? bounds.getHeight() : bounds.getWidth();
                boolean tickLabelsOverlapping = false;
                if (i > 0) {
                    double avgTickLabelLength = (previousDrawnTickLabelLength
                            + tickLabelLength) / 2.0;
                    if (Math.abs(xx - previousDrawnTickLabelPos)
                            < avgTickLabelLength) {
                        tickLabelsOverlapping = true;
                    }
                }
                if (tickLabelsOverlapping) {
                    tickLabel = ""; // don't draw this tick label
                }
                else {
                    // remember these values for next comparison
                    previousDrawnTickLabelPos = xx;
                    previousDrawnTickLabelLength = tickLabelLength;
                }

                TextAnchor anchor;
                TextAnchor rotationAnchor;
                double angle = 0.0;
                if (isVerticalTickLabels()) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;
                    if (edge == RectangleEdge.TOP) {
                        angle = Math.PI / 2.0;
                    }
                    else {
                        angle = -Math.PI / 2.0;
                    }
                }
                else {
                    if (edge == RectangleEdge.TOP) {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    }
                    else {
                        anchor = TextAnchor.TOP_CENTER;
                        rotationAnchor = TextAnchor.TOP_CENTER;
                    }
                }
                Tick tick = new NumberTick(currentTickValue, tickLabel, anchor, 
                        rotationAnchor, angle);
                ticks.add(tick);
            }
        }
        return ticks;

    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot should be drawn.
     * @param edge  the location of the axis.
     *
     * @return The ticks.
     */
    @Override
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        List ticks = new java.util.ArrayList();

        Font tickLabelFont = tickLabel.getTickLabelFont();
        g2.setFont(tickLabelFont);

        double size = getTickUnit().getSize();
        int count = calculateVisibleTickCount();
        double lowestTickValue = calculateLowestVisibleTickValue();

        double previousDrawnTickLabelPos = 0.0;
        double previousDrawnTickLabelLength = 0.0;

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                double yy = valueToJava2D(currentTickValue, dataArea, edge);
                String tickLabel;
                NumberFormat formatter = getNumberFormatOverride();
                if (formatter != null) {
                    tickLabel = formatter.format(currentTickValue);
                }
                else {
                    tickLabel = valueToString(currentTickValue);
                }

                // avoid to draw overlapping tick labels
                Rectangle2D bounds = TextUtils.getTextBounds(tickLabel, g2,
                        g2.getFontMetrics());
                double tickLabelLength = isVerticalTickLabels()
                    ? bounds.getWidth() : bounds.getHeight();
                boolean tickLabelsOverlapping = false;
                if (i > 0) {
                    double avgTickLabelLength = (previousDrawnTickLabelLength
                            + tickLabelLength) / 2.0;
                    if (Math.abs(yy - previousDrawnTickLabelPos)
                            < avgTickLabelLength) {
                        tickLabelsOverlapping = true;
                    }
                }
                if (tickLabelsOverlapping) {
                    tickLabel = ""; // don't draw this tick label
                }
                else {
                    // remember these values for next comparison
                    previousDrawnTickLabelPos = yy;
                    previousDrawnTickLabelLength = tickLabelLength;
                }

                TextAnchor anchor;
                TextAnchor rotationAnchor;
                double angle = 0.0;
                if (isVerticalTickLabels()) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    if (edge == RectangleEdge.LEFT) {
                        angle = -Math.PI / 2.0;
                    }
                    else {
                        angle = Math.PI / 2.0;
                    }
                }
                else {
                    if (edge == RectangleEdge.LEFT) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                    }
                    else {
                        anchor = TextAnchor.CENTER_LEFT;
                        rotationAnchor = TextAnchor.CENTER_LEFT;
                    }
                }
                Tick tick = new NumberTick(currentTickValue, tickLabel, anchor, 
                        rotationAnchor, angle);
                ticks.add(tick);
            }
        }
        return ticks;

    }

    /**
     * Converts a value to a string, using the list of symbols.
     *
     * @param value  value to convert.
     *
     * @return The symbol.
     */
    public String valueToString(double value) {
        String strToReturn;
        try {
            strToReturn = (String) this.symbols.get((int) value);
        }
        catch (IndexOutOfBoundsException  ex) {
            strToReturn = "";
        }
        return strToReturn;
    }

    /**
     * Tests this axis for equality with an arbitrary object.
     *
     * @param obj  the object ({@code null} permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SymbolAxis)) {
            return false;
        }
        SymbolAxis that = (SymbolAxis) obj;
        if (!this.symbols.equals(that.symbols)) {
            return false;
        }
        if (!gridBand.equals(that.gridBand)) {
            return false;
        }
        return super.equals(obj);
    }



    /**
     * Draws the grid bands for the axis when it is at the top or bottom of
     * the plot.
     *
     * @param g2                  the graphics target ({@code null} not permitted).
     * @param dataArea            the area for the data (to which the axes are aligned,
     *                            {@code null} not permitted).
     * @param firstGridBandIsDark True: the first grid band takes the
     *                            color of {@code gridBandPaint}.
     *                            False: the second grid band takes the
     *                            color of {@code gridBandPaint}.
     * @param ticks               a list of ticks ({@code null} not permitted).
     */
    public void drawGridBandsHorizontal(Graphics2D g2,
                                        Rectangle2D dataArea,
                                        boolean firstGridBandIsDark, List ticks, GridBand gridBand) {

        boolean currentGridBandIsDark = firstGridBandIsDark;
        double yy = dataArea.getY();
        double xx1, xx2;

        //gets the outline stroke width of the plot
        double outlineStrokeWidth = 1.0;
        Stroke outlineStroke = getPlot().getOutlineStroke();
        if (outlineStroke != null && outlineStroke instanceof BasicStroke) {
            outlineStrokeWidth = ((BasicStroke) outlineStroke).getLineWidth();
        }

        Iterator iterator = ticks.iterator();
        ValueTick tick;
        Rectangle2D band;
        while (iterator.hasNext()) {
            tick = (ValueTick) iterator.next();
            xx1 = valueToJava2D(tick.getValue() - 0.5d, dataArea,
                    RectangleEdge.BOTTOM);
            xx2 = valueToJava2D(tick.getValue() + 0.5d, dataArea,
                    RectangleEdge.BOTTOM);
            if (currentGridBandIsDark) {
                g2.setPaint(gridBand.gridBandPaint);
            } else {
                g2.setPaint(gridBand.gridBandAlternatePaint);
            }
            band = new Rectangle2D.Double(Math.min(xx1, xx2),
                    yy + outlineStrokeWidth, Math.abs(xx2 - xx1),
                    dataArea.getMaxY() - yy - outlineStrokeWidth);
            g2.fill(band);
            currentGridBandIsDark = !currentGridBandIsDark;
        }
    }

    /**
     * Draws the grid bands for an axis that is aligned to the left or
     * right of the data area (that is, a vertical axis).
     *
     * @param g2                  the graphics target ({@code null} not permitted).
     * @param dataArea            the area for the data (to which the axes are aligned,
     *                            {@code null} not permitted).
     * @param firstGridBandIsDark True: the first grid band takes the
     *                            color of {@code gridBandPaint}.
     *                            False: the second grid band takes the
     *                            color of {@code gridBandPaint}.
     * @param ticks               a list of ticks ({@code null} not permitted).
     */
    public void drawGridBandsVertical(Graphics2D g2,
                                      Rectangle2D dataArea, boolean firstGridBandIsDark,
                                      List ticks, GridBand gridBand) {

        boolean currentGridBandIsDark = firstGridBandIsDark;
        double xx = dataArea.getX();
        double yy1, yy2;

        //gets the outline stroke width of the plot
        double outlineStrokeWidth = 1.0;
        Stroke outlineStroke = getPlot().getOutlineStroke();
        if (outlineStroke != null && outlineStroke instanceof BasicStroke) {
            outlineStrokeWidth = ((BasicStroke) outlineStroke).getLineWidth();
        }

        Iterator iterator = ticks.iterator();
        ValueTick tick;
        Rectangle2D band;
        while (iterator.hasNext()) {
            tick = (ValueTick) iterator.next();
            yy1 = valueToJava2D(tick.getValue() + 0.5d, dataArea,
                    RectangleEdge.LEFT);
            yy2 = valueToJava2D(tick.getValue() - 0.5d, dataArea,
                    RectangleEdge.LEFT);
            if (currentGridBandIsDark) {
                g2.setPaint(gridBand.gridBandPaint);
            } else {
                g2.setPaint(gridBand.gridBandAlternatePaint);
            }
            band = new Rectangle2D.Double(xx + outlineStrokeWidth,
                    Math.min(yy1, yy2), dataArea.getMaxX() - xx
                    - outlineStrokeWidth, Math.abs(yy2 - yy1));
            g2.fill(band);
            currentGridBandIsDark = !currentGridBandIsDark;
        }
    }
}
