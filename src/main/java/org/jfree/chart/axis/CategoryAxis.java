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
 * -----------------
 * CategoryAxis.java
 * -----------------
 * (C) Copyright 2000-2022, by David Gilbert and Contributors.
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   Pady Srinivasan (patch 1217634);
 *                   Peter Kolb (patches 2497611 and 2603321);
 *
 */

package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.internal.ShapeUtils;
import org.jfree.data.category.CategoryDataset;

/**
 * An axis that displays categories.
 */
public class CategoryAxis extends Axis implements Cloneable, Serializable {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 5886554608114265863L;

    /**
     * The default margin for the axis (used for both lower and upper margins).
     */
    public static final double DEFAULT_AXIS_MARGIN = 0.05;

    /**
     * The default margin between categories (a percentage of the overall axis
     * length).
     */
    public static final double DEFAULT_CATEGORY_MARGIN = 0.20;

    /**
     * The amount of space reserved at the start of the axis.
     */
    private double lowerMargin;

    /**
     * The amount of space reserved at the end of the axis.
     */
    private double upperMargin;

    /**
     * The amount of space reserved between categories.
     */
    private double categoryMargin;

    /**
     * The maximum number of lines for category labels.
     */
    private int maximumCategoryLabelLines;

    private CategoryAxisHelper helper;

    /**
     * Storage for tick label font overrides (if any).
     */
    private Map<Comparable, Font> tickLabelFontMap;

    /**
     * Storage for tick label paint overrides (if any).
     */
    private transient Map<Comparable, Paint> tickLabelPaintMap;

    /**
     * Storage for the category label tooltips (if any).
     */
    private Map<Comparable, String> categoryLabelToolTips;

    /**
     * Storage for the category label URLs (if any).
     */
    private Map<Comparable, String> categoryLabelURLs;

    /**
     * Creates a new category axis with no label.
     */
    public CategoryAxis() {
        this(null);
    }

    /**
     * Constructs a category axis, using default values where necessary.
     *
     * @param label the axis label ({@code null} permitted).
     */
    public CategoryAxis(String label) {
        super(label);

        this.lowerMargin = DEFAULT_AXIS_MARGIN;
        this.upperMargin = DEFAULT_AXIS_MARGIN;
        this.categoryMargin = DEFAULT_CATEGORY_MARGIN;
        this.maximumCategoryLabelLines = 1;
        this.helper = new CategoryAxisHelper(this);
        this.tickLabelFontMap = new HashMap<>();
        this.tickLabelPaintMap = new HashMap<>();
        this.categoryLabelToolTips = new HashMap<>();
        this.categoryLabelURLs = new HashMap<>();
    }

    public CategoryAxisHelper getHelper() {
        return helper;
    }

    /**
     * Returns the lower margin for the axis.
     *
     * @return The margin.
     * @see #getUpperMargin()
     * @see #setLowerMargin(double)
     */
    public double getLowerMargin() {
        return this.lowerMargin;
    }

    /**
     * Sets the lower margin for the axis and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param margin the margin as a percentage of the axis length (for
     *               example, 0.05 is five percent).
     * @see #getLowerMargin()
     */
    public void setLowerMargin(double margin) {
        this.lowerMargin = margin;
        fireChangeEvent();
    }

    /**
     * Returns the upper margin for the axis.
     *
     * @return The margin.
     * @see #getLowerMargin()
     * @see #setUpperMargin(double)
     */
    public double getUpperMargin() {
        return this.upperMargin;
    }

    /**
     * Sets the upper margin for the axis and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @param margin the margin as a percentage of the axis length (for
     *               example, 0.05 is five percent).
     * @see #getUpperMargin()
     */
    public void setUpperMargin(double margin) {
        this.upperMargin = margin;
        fireChangeEvent();
    }

    /**
     * Returns the category margin.
     *
     * @return The margin.
     * @see #setCategoryMargin(double)
     */
    public double getCategoryMargin() {
        return this.categoryMargin;
    }

    /**
     * Sets the category margin and sends an {@link AxisChangeEvent} to all
     * registered listeners.  The overall category margin is distributed over
     * N-1 gaps, where N is the number of categories on the axis.
     *
     * @param margin the margin as a percentage of the axis length (for
     *               example, 0.05 is five percent).
     * @see #getCategoryMargin()
     */
    public void setCategoryMargin(double margin) {
        this.categoryMargin = margin;
        fireChangeEvent();
    }

    /**
     * Returns the maximum number of lines to use for each category label.
     *
     * @return The maximum number of lines.
     * @see #setMaximumCategoryLabelLines(int)
     */
    public int getMaximumCategoryLabelLines() {
        return this.maximumCategoryLabelLines;
    }

    /**
     * Sets the maximum number of lines to use for each category label and
     * sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param lines the maximum number of lines.
     * @see #getMaximumCategoryLabelLines()
     */
    public void setMaximumCategoryLabelLines(int lines) {
        this.maximumCategoryLabelLines = lines;
        fireChangeEvent();
    }

    /**
     * Returns the font for the tick label for the given category.
     *
     * @param category the category ({@code null} not permitted).
     * @return The font (never {@code null}).
     * @see #setTickLabelFont(Comparable, Font)
     */
    public Font getTickLabelFont(Comparable category) {
        Args.nullNotPermitted(category, "category");
        Font result = this.tickLabelFontMap.get(category);
        // if there is no specific font, use the general one...
        if (result == null) {
            result = tickLabel.getTickLabelFont();
        }
        return result;
    }

    /**
     * Sets the font for the tick label for the specified category and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param category the category ({@code null} not permitted).
     * @param font     the font ({@code null} permitted).
     * @see #getTickLabelFont(Comparable)
     */
    public void setTickLabelFont(Comparable category, Font font) {
        Args.nullNotPermitted(category, "category");
        if (font == null) {
            this.tickLabelFontMap.remove(category);
        } else {
            this.tickLabelFontMap.put(category, font);
        }
        fireChangeEvent();
    }

    /**
     * Returns the paint for the tick label for the given category.
     *
     * @param category the category ({@code null} not permitted).
     * @return The paint (never {@code null}).
     */
    public Paint getTickLabelPaint(Comparable category) {
        Args.nullNotPermitted(category, "category");
        Paint result = this.tickLabelPaintMap.get(category);
        // if there is no specific paint, use the general one...
        if (result == null) {
            result = tickLabel.getTickLabelPaint();
        }
        return result;
    }

    /**
     * Sets the paint for the tick label for the specified category and sends
     * an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param category the category ({@code null} not permitted).
     * @param paint    the paint ({@code null} permitted).
     * @see #getTickLabelPaint(Comparable)
     */
    public void setTickLabelPaint(Comparable category, Paint paint) {
        Args.nullNotPermitted(category, "category");
        if (paint == null) {
            this.tickLabelPaintMap.remove(category);
        } else {
            this.tickLabelPaintMap.put(category, paint);
        }
        fireChangeEvent();
    }

    /**
     * Adds a tooltip to the specified category and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param category the category ({@code null} not permitted).
     * @param tooltip  the tooltip text ({@code null} permitted).
     * @see #removeCategoryLabelToolTip(Comparable)
     */
    public void addCategoryLabelToolTip(Comparable category, String tooltip) {
        Args.nullNotPermitted(category, "category");
        this.categoryLabelToolTips.put(category, tooltip);
        fireChangeEvent();
    }

    /**
     * Returns the tool tip text for the label belonging to the specified
     * category.
     *
     * @param category the category ({@code null} not permitted).
     * @return The tool tip text (possibly {@code null}).
     * @see #addCategoryLabelToolTip(Comparable, String)
     * @see #removeCategoryLabelToolTip(Comparable)
     */
    public String getCategoryLabelToolTip(Comparable category) {
        Args.nullNotPermitted(category, "category");
        return this.categoryLabelToolTips.get(category);
    }

    /**
     * Removes the tooltip for the specified category and, if there was a value
     * associated with that category, sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param category the category ({@code null} not permitted).
     * @see #addCategoryLabelToolTip(Comparable, String)
     * @see #clearCategoryLabelToolTips()
     */
    public void removeCategoryLabelToolTip(Comparable category) {
        Args.nullNotPermitted(category, "category");
        if (this.categoryLabelToolTips.remove(category) != null) {
            fireChangeEvent();
        }
    }

    /**
     * Clears the category label tooltips and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @see #addCategoryLabelToolTip(Comparable, String)
     * @see #removeCategoryLabelToolTip(Comparable)
     */
    public void clearCategoryLabelToolTips() {
        this.categoryLabelToolTips.clear();
        fireChangeEvent();
    }

    /**
     * Adds a URL (to be used in image maps) to the specified category and
     * sends an {@link AxisChangeEvent} to all registered listeners.
     *
     * @param category the category ({@code null} not permitted).
     * @param url      the URL text ({@code null} permitted).
     * @see #removeCategoryLabelURL(Comparable)
     */
    public void addCategoryLabelURL(Comparable category, String url) {
        Args.nullNotPermitted(category, "category");
        this.categoryLabelURLs.put(category, url);
        fireChangeEvent();
    }

    /**
     * Returns the URL for the label belonging to the specified category.
     *
     * @param category the category ({@code null} not permitted).
     * @return The URL text (possibly {@code null}).
     * @see #addCategoryLabelURL(Comparable, String)
     * @see #removeCategoryLabelURL(Comparable)
     */
    public String getCategoryLabelURL(Comparable category) {
        Args.nullNotPermitted(category, "category");
        return this.categoryLabelURLs.get(category);
    }

    /**
     * Removes the URL for the specified category and, if there was a URL
     * associated with that category, sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param category the category ({@code null} not permitted).
     * @see #addCategoryLabelURL(Comparable, String)
     * @see #clearCategoryLabelURLs()
     */
    public void removeCategoryLabelURL(Comparable category) {
        Args.nullNotPermitted(category, "category");
        if (this.categoryLabelURLs.remove(category) != null) {
            fireChangeEvent();
        }
    }

    /**
     * Clears the category label URLs and sends an {@link AxisChangeEvent}
     * to all registered listeners.
     *
     * @see #addCategoryLabelURL(Comparable, String)
     * @see #removeCategoryLabelURL(Comparable)
     */
    public void clearCategoryLabelURLs() {
        this.categoryLabelURLs.clear();
        fireChangeEvent();
    }

    /**
     * Returns the Java 2D coordinate for a category.
     *
     * @param anchor        the anchor point ({@code null} not permitted).
     * @param category      the category index.
     * @param categoryCount the category count.
     * @param area          the data area.
     * @param edge          the location of the axis.
     * @return The coordinate.
     */
    public double getCategoryJava2DCoordinate(CategoryAnchor anchor,
                                              int category, int categoryCount, Rectangle2D area,
                                              RectangleEdge edge) {
        Args.nullNotPermitted(anchor, "anchor");
        double result = 0.0;
        switch (anchor) {
            case START:
                result = getCategoryStart(category, categoryCount, area, edge);
                break;
            case MIDDLE:
                result = getCategoryMiddle(category, categoryCount, area, edge);
                break;
            case END:
                result = getCategoryEnd(category, categoryCount, area, edge);
                break;
            default:
                throw new IllegalStateException("Unexpected anchor value.");
        }
        return result;

    }

    /**
     * Returns the starting coordinate for the specified category.
     *
     * @param category      the category.
     * @param categoryCount the number of categories.
     * @param area          the data area.
     * @param edge          the axis location.
     * @return The coordinate.
     * @see #getCategoryMiddle(int, int, Rectangle2D, RectangleEdge)
     * @see #getCategoryEnd(int, int, Rectangle2D, RectangleEdge)
     */
    public double getCategoryStart(int category, int categoryCount,
                                   Rectangle2D area, RectangleEdge edge) {

        double result = 0.0;
        if ((edge == RectangleEdge.TOP) || (edge == RectangleEdge.BOTTOM)) {
            result = area.getX() + area.getWidth() * getLowerMargin();
        } else if ((edge == RectangleEdge.LEFT)
                || (edge == RectangleEdge.RIGHT)) {
            result = area.getMinY() + area.getHeight() * getLowerMargin();
        }

        double categorySize = calculateCategorySize(categoryCount, area, edge);
        double categoryGapWidth = calculateCategoryGapSize(categoryCount, area,
                edge);

        result = result + category * (categorySize + categoryGapWidth);
        return result;
    }

    /**
     * Returns the middle coordinate for the specified category.
     *
     * @param category      the category.
     * @param categoryCount the number of categories.
     * @param area          the data area.
     * @param edge          the axis location.
     * @return The coordinate.
     * @see #getCategoryStart(int, int, Rectangle2D, RectangleEdge)
     * @see #getCategoryEnd(int, int, Rectangle2D, RectangleEdge)
     */
    public double getCategoryMiddle(int category, int categoryCount,
                                    Rectangle2D area, RectangleEdge edge) {

        if (category < 0 || category >= categoryCount) {
            throw new IllegalArgumentException("Invalid category index: "
                    + category);
        }
        return getCategoryStart(category, categoryCount, area, edge)
                + calculateCategorySize(categoryCount, area, edge) / 2;

    }

    /**
     * Returns the end coordinate for the specified category.
     *
     * @param category      the category.
     * @param categoryCount the number of categories.
     * @param area          the data area.
     * @param edge          the axis location.
     * @return The coordinate.
     * @see #getCategoryStart(int, int, Rectangle2D, RectangleEdge)
     * @see #getCategoryMiddle(int, int, Rectangle2D, RectangleEdge)
     */
    public double getCategoryEnd(int category, int categoryCount,
                                 Rectangle2D area, RectangleEdge edge) {
        return getCategoryStart(category, categoryCount, area, edge)
                + calculateCategorySize(categoryCount, area, edge);
    }

    /**
     * A convenience method that returns the axis coordinate for the centre of
     * a category.
     *
     * @param category   the category key ({@code null} not permitted).
     * @param categories the categories ({@code null} not permitted).
     * @param area       the data area ({@code null} not permitted).
     * @param edge       the edge along which the axis lies ({@code null} not
     *                   permitted).
     * @return The centre coordinate.
     * @see #getCategorySeriesMiddle(Comparable, Comparable, CategoryDataset,
     * double, Rectangle2D, RectangleEdge)
     */
    public double getCategoryMiddle(Comparable category, List categories, Rectangle2D area, RectangleEdge edge) {
        Args.nullNotPermitted(categories, "categories");
        int categoryIndex = categories.indexOf(category);
        int categoryCount = categories.size();
        return getCategoryMiddle(categoryIndex, categoryCount, area, edge);
    }

    /**
     * Returns the middle coordinate (in Java2D space) for a series within a
     * category.
     *
     * @param category   the category ({@code null} not permitted).
     * @param seriesKey  the series key ({@code null} not permitted).
     * @param dataset    the dataset ({@code null} not permitted).
     * @param itemMargin the item margin (0.0 &lt;= itemMargin &lt; 1.0);
     * @param area       the area ({@code null} not permitted).
     * @param edge       the edge ({@code null} not permitted).
     * @return The coordinate in Java2D space.
     */
    public double getCategorySeriesMiddle(Comparable category, Comparable seriesKey, CategoryDataset dataset, double itemMargin, Rectangle2D area, RectangleEdge edge) {
        int categoryIndex = dataset.getColumnIndex(category);
        int categoryCount = dataset.getColumnCount();
        int seriesIndex = dataset.getRowIndex(seriesKey);
        int seriesCount = dataset.getRowCount();
        return getCategorySeriesMiddle(categoryIndex, categoryCount, seriesIndex, seriesCount, itemMargin, area, edge);
    }

    /**
     * Returns the middle coordinate (in Java2D space) for a series within a
     * category.
     *
     * @param categoryIndex the category index.
     * @param categoryCount the category count.
     * @param seriesIndex   the series index.
     * @param seriesCount   the series count.
     * @param itemMargin    the item margin (0.0 &lt;= itemMargin &lt; 1.0);
     * @param area          the area ({@code null} not permitted).
     * @param edge          the edge ({@code null} not permitted).
     * @return The coordinate in Java2D space.
     */
    public double getCategorySeriesMiddle(int categoryIndex, int categoryCount, int seriesIndex, int seriesCount, double itemMargin, Rectangle2D area, RectangleEdge edge) {
        double start = getCategoryStart(categoryIndex, categoryCount, area, edge);
        double end = getCategoryEnd(categoryIndex, categoryCount, area, edge);
        double width = end - start;
        if (seriesCount == 1) {
            return start + width / 2.0;
        } else {
            double gap = (width * itemMargin) / (seriesCount - 1);
            double ww = (width * (1 - itemMargin)) / seriesCount;
            return start + (seriesIndex * (ww + gap)) + ww / 2.0;
        }
    }

    /**
     * Calculates the size (width or height, depending on the location of the
     * axis) of a category.
     *
     * @param categoryCount the number of categories.
     * @param area          the area within which the categories will be drawn.
     * @param edge          the axis location.
     * @return The category size.
     */
    protected double calculateCategorySize(int categoryCount, Rectangle2D area, RectangleEdge edge) {
        double result;
        double available = 0.0;

        if ((edge == RectangleEdge.TOP) || (edge == RectangleEdge.BOTTOM)) available = area.getWidth();
        else if ((edge == RectangleEdge.LEFT) || (edge == RectangleEdge.RIGHT)) available = area.getHeight();

        if (categoryCount > 1) {
            result = available * (1 - getLowerMargin() - getUpperMargin() - getCategoryMargin()) / categoryCount;
        } else {
            result = available * (1 - getLowerMargin() - getUpperMargin());
        }
        return result;
    }

    /**
     * Calculates the size (width or height, depending on the location of the
     * axis) of a category gap.
     *
     * @param categoryCount the number of categories.
     * @param area          the area within which the categories will be drawn.
     * @param edge          the axis location.
     * @return The category gap width.
     */
    protected double calculateCategoryGapSize(int categoryCount, Rectangle2D area, RectangleEdge edge) {
        double result = 0.0;
        double available = 0.0;

        if ((edge == RectangleEdge.TOP) || (edge == RectangleEdge.BOTTOM)) available = area.getWidth();
        else if ((edge == RectangleEdge.LEFT) || (edge == RectangleEdge.RIGHT)) available = area.getHeight();

        if (categoryCount > 1) result = available * getCategoryMargin() / (categoryCount - 1);

        return result;
    }

    /**
     * Estimates the space required for the axis, given a specific drawing area.
     *
     * @param g2       the graphics device (used to obtain font information).
     * @param plot     the plot that the axis belongs to.
     * @param plotArea the area within which the axis should be drawn.
     * @param edge     the axis location ({@code null} not permitted).
     * @param space    the space already reserved.
     * @return The space required to draw the axis.
     */
    @Override
    public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {
        // create a new space object if one wasn't supplied...
        if (space == null) {
            space = new AxisSpace();
        }

        // if the axis is not visible, no additional space is required...
        if (!isVisible()) return space;

        // calculate the max size of the tick labels (if visible)...
        double tickLabelHeight = 0.0;
        double tickLabelWidth = 0.0;
        if (tickLabel.isTickLabelsVisible()) {
            g2.setFont(tickLabel.getTickLabelFont());
            AxisState state = new AxisState();
            // we call refresh ticks just to get the maximum width or height
            refreshTicks(g2, state, plotArea, edge);
            switch (edge) {
                case TOP: case BOTTOM:
                    tickLabelHeight = state.getMax();
                    break;
                case LEFT: case RIGHT:
                    tickLabelWidth = state.getMax();
                    break;
                default:
                    throw new IllegalStateException("Unexpected RectangleEdge value.");
            }
        }

        // get the axis label size and update the space object...
        Rectangle2D labelEnclosure = getLabelEnclosure(g2, edge);
        double labelHeight, labelWidth;
        if (RectangleEdge.isTopOrBottom(edge)) {
            labelHeight = labelEnclosure.getHeight();
            space.add(labelHeight + tickLabelHeight + helper.getCategoryLabelPositionOffset(), edge);
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            labelWidth = labelEnclosure.getWidth();
            space.add(labelWidth + tickLabelWidth + helper.getCategoryLabelPositionOffset(), edge);
        }
        return space;
    }

    /**
     * Configures the axis against the current plot.
     */
    @Override
    public void configure() {
        // nothing required
    }

    /**
     * Draws the axis on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2        the graphics device ({@code null} not permitted).
     * @param cursor    the cursor location.
     * @param plotArea  the area within which the axis should be drawn
     *                  ({@code null} not permitted).
     * @param dataArea  the area within which the plot is being drawn
     *                  ({@code null} not permitted).
     * @param edge      the location of the axis ({@code null} not permitted).
     * @param plotState collects information about the plot
     *                  ({@code null} permitted).
     * @return The axis state (never {@code null}).
     */
    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
        // if the axis is not visible, don't draw it...
        if (!isVisible()) return new AxisState(cursor);

        if (isAxisLineVisible()) drawAxisLine(g2, cursor, dataArea, edge);

        AxisState state = new AxisState(cursor);
        if (tickMarks.isTickMarksVisible()) {
            drawTickMarks(g2, cursor, dataArea, edge, state);
        }

        createAndAddEntity(cursor, state, dataArea, edge, plotState);

        // draw the category labels and axis label
        state = drawCategoryLabels(g2, plotArea, dataArea, edge, state, plotState);
        if (getAttributedLabel() != null) {
            state = drawAttributedLabel(getAttributedLabel(), g2,
                    dataArea, edge, state);
            
        } else state = drawLabel(getLabel(), g2, dataArea, edge, state);
        return state;

    }

    private double[] drawCategoryLabelsHelper(Object o, Graphics2D g2, RectangleEdge edge, int categoryIndex, List ticks, Rectangle2D dataArea, AxisState state,
                                              CategoryTick tick){

        g2.setFont(getTickLabelFont(tick.getCategory()));
        g2.setPaint(getTickLabelPaint(tick.getCategory()));
        double[] auxArray = new double[4];
        if (edge == RectangleEdge.TOP) {
            auxArray[0] = getCategoryStart(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[1] = getCategoryEnd(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[3] = state.getCursor() - this.categoryLabelPositionOffset;
            auxArray[2] = auxArray[3] - state.getMax();
            return auxArray;
        }
        else if (edge == RectangleEdge.BOTTOM) {
            auxArray[0] = getCategoryStart(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[1]= getCategoryEnd(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[2] = state.getCursor() + this.categoryLabelPositionOffset;
            auxArray[3] = auxArray[2] + state.getMax();
            return auxArray;
        }
        else if (edge == RectangleEdge.LEFT) {
            auxArray[2] = getCategoryStart(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[3] = getCategoryEnd(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[1] = state.getCursor() - this.categoryLabelPositionOffset;
            auxArray[0] = auxArray[1] - state.getMax();
            return auxArray;
        }
        else if (edge == RectangleEdge.RIGHT) {
            auxArray[2] = getCategoryStart(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[3] = getCategoryEnd(categoryIndex, ticks.size(), dataArea,
                    edge);
            auxArray[0] = state.getCursor() + this.categoryLabelPositionOffset;
            auxArray[1] = auxArray[0] - state.getMax();
            return auxArray;
        }

        return null;
    }

    private Shape drawShape(double x0, double x1, double y0, double y1, RectangleEdge edge, CategoryTick tick, Graphics2D g2){
        CategoryLabelPosition position
                = this.categoryLabelPositions.getLabelPosition(edge);
        Rectangle2D area = new Rectangle2D.Double(x0, y0, (x1 - x0),
                (y1 - y0));
        Point2D anchorPoint = position.getCategoryAnchor().getAnchorPoint(area);
        TextBlock block = tick.getLabel();
        block.draw(g2, (float) anchorPoint.getX(),
                (float) anchorPoint.getY(), position.getLabelAnchor(),
                (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                position.getAngle());
        Shape bounds = block.calculateBounds(g2,
                (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                position.getLabelAnchor(), (float) anchorPoint.getX(),
                (float) anchorPoint.getY(), position.getAngle());
        return bounds;
    }

    private void setState(RectangleEdge edge, AxisState state){
        if (edge.equals(RectangleEdge.TOP)) {
            double h = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorUp(h);
        }
        else if (edge.equals(RectangleEdge.BOTTOM)) {
            double h = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorDown(h);
        }
        else if (edge == RectangleEdge.LEFT) {
            double w = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorLeft(w);
        }
        else if (edge == RectangleEdge.RIGHT) {
            double w = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorRight(w);
        }
    }

    /**
     * REFACTOR - USADO PARA SIMPLIFICAR CategoryAxis.drawCategoryLabels.
     *
     * @author Afonso Caniço
     */
    private Rectangle2D getRectangle(int categoryIndex, AxisState state, List ticks, Rectangle2D dataArea, RectangleEdge edge) {
        double x0 = 0.0;
        double x1 = 0.0;
        double y0 = 0.0;
        double y1 = 0.0;
        switch (edge) {
            case TOP:
                x0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                y0 = y1 - state.getMax();
                y1 = state.getCursor() - helper.getCategoryLabelPositionOffset();
                break;
            case BOTTOM:
                x0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                y0 = state.getCursor() + helper.getCategoryLabelPositionOffset();
                y1 = y0 + state.getMax();
                break;
            case LEFT:
                x0 = x1 - state.getMax();
                x1 = state.getCursor() - helper.getCategoryLabelPositionOffset();
                y0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                break;
            case RIGHT:
                x0 = state.getCursor() + helper.getCategoryLabelPositionOffset();
                x1 = x0 - state.getMax();
                y0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                break;
        }
        return new Rectangle2D.Double(x0, y0, (x1 - x0), (y1 - y0));
    }

    /**
     * Draws the category labels and returns the updated axis state.
     *
     * @param g2        the graphics device ({@code null} not permitted).
     * @param plotArea  the plot area ({@code null} not permitted).
     * @param dataArea  the area inside the axes ({@code null} not
     *                  permitted).
     * @param edge      the axis location ({@code null} not permitted).
     * @param state     the axis state ({@code null} not permitted).
     * @param plotState collects information about the plot ({@code null}
     *                  permitted).
     * @return The updated axis state (never {@code null}).
     */
    protected AxisState drawCategoryLabels(Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, AxisState state, PlotRenderingInfo plotState) {
        Args.nullNotPermitted(state, "state");
        if (!tickLabel.isTickLabelsVisible()) {
            return state;
        }

        List ticks = refreshTicks(g2, state, plotArea, edge);
        state.setTicks(ticks);
        int categoryIndex = 0;
        for (Object o : ticks) {
            CategoryTick tick = (CategoryTick) o;

            CategoryLabelPosition position = helper.getCategoryLabelPositions().getLabelPosition(edge);
            Rectangle2D area = getRectangle(categoryIndex, state, ticks, dataArea, edge); // REFACTORED @ambco
            TextBlock block = tick.getLabel();
            block.draw(g2, position, area); // REFACTORED @ambco
            Shape bounds = block.calculateBounds(g2, position, area); // REFACTORED @ambco
            if (plotState != null && plotState.getOwner() != null) {
                EntityCollection entities = plotState.getOwner().getEntityCollection();
                if (entities != null) {
                    String tooltip = getCategoryLabelToolTip(tick.getCategory());
                    String url = getCategoryLabelURL(tick.getCategory());
                    entities.add(new CategoryLabelEntity(tick.getCategory(), bounds, tooltip, url));
                }
            }
            categoryIndex++;
        }
        state.moveCursor(state.getMax() + helper.getCategoryLabelPositionOffset(), edge); // REFACTORED @ambco
        return state;
    }

    /**
     * Creates a temporary list of ticks that can be used when drawing the axis.
     *
     * @param g2       the graphics device (used to get font measurements).
     * @param state    the axis state.
     * @param dataArea the area inside the axes.
     * @param edge     the location of the axis.
     * @return A list of ticks.
     */
    @Override
    public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {

        List ticks = new java.util.ArrayList(); // FIXME generics

        // sanity check for data area...
        if (dataArea.getHeight() <= 0.0 || dataArea.getWidth() < 0.0) {
            return ticks;
        }

        CategoryPlot plot = (CategoryPlot) getPlot();
        List categories = plot.getCategoriesForAxis(this);
        double max = 0.0;

        if (categories != null) {
            CategoryLabelPosition position = helper.getCategoryLabelPositions().getLabelPosition(edge);
            float r = helper.getMaximumCategoryLabelWidthRatio();
            if (r <= 0.0) {
                r = position.getWidthRatio();
            }

            float l;
            if (position.getWidthType() == CategoryLabelWidthType.CATEGORY) {
                l = (float) calculateCategorySize(categories.size(), dataArea,
                        edge);
            } else {
                if (RectangleEdge.isLeftOrRight(edge)) {
                    l = (float) dataArea.getWidth();
                } else {
                    l = (float) dataArea.getHeight();
                }
            }
            int categoryIndex = 0;
            for (Object o : categories) {
                Comparable category = (Comparable) o;
                g2.setFont(getTickLabelFont(category));
                TextBlock label = createLabel(category, l * r, edge, g2);
                if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
                    max = Math.max(max, calculateCategoryLabelHeight(label,
                            position, tickLabel.getTickLabelInsets(), g2));
                } else if (edge == RectangleEdge.LEFT
                        || edge == RectangleEdge.RIGHT) {
                    max = Math.max(max, calculateCategoryLabelWidth(label,
                            position, tickLabel.getTickLabelInsets(), g2));
                }
                Tick tick = new CategoryTick(category, label,
                        position.getLabelAnchor(),
                        position.getRotationAnchor(), position.getAngle());
                ticks.add(tick);
                categoryIndex = categoryIndex + 1;
            }
        }
        state.setMax(max);
        return ticks;

    }

    private void stateUp(List<Comparable> categories, Rectangle2D dataArea, RectangleEdge edge, Line2D line, double cursor, double il, double ol, Graphics2D g2,
                         AxisState state){
        for (Comparable category : categories) {
            double x = getCategoryMiddle(category, categories, dataArea, edge);
            line.setLine(x, cursor, x, cursor + il);
            g2.draw(line);
            line.setLine(x, cursor, x, cursor - ol);
            g2.draw(line);
        }
        state.cursorUp(ol);
    }

    private void stateDown(List<Comparable> categories, Rectangle2D dataArea, RectangleEdge edge, Line2D line, double cursor, double il, double ol, Graphics2D g2,
                           AxisState state){
        for (Comparable category : categories) {
            double x = getCategoryMiddle(category, categories, dataArea, edge);
            line.setLine(x, cursor, x, cursor - il);
            g2.draw(line);
            line.setLine(x, cursor, x, cursor + ol);
            g2.draw(line);
        }
        state.cursorDown(ol);
    }

    private void stateLeft(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge, AxisState state, double il, double ol, Line2D line, List<Comparable> categories) {
        for (Comparable category : categories) {
            double y = getCategoryMiddle(category, categories, dataArea, edge);
            line.setLine(cursor, y, cursor + il, y);
            g2.draw(line);
            line.setLine(cursor, y, cursor - ol, y);
            g2.draw(line);
        }
        state.cursorLeft(ol);
    }

    private void stateRight(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge, AxisState state, double il, double ol, Line2D line, List<Comparable> categories) {
        for (Comparable category : categories) {
            double y = getCategoryMiddle(category, categories, dataArea, edge);
            line.setLine(cursor, y, cursor - il, y);
            g2.draw(line);
            line.setLine(cursor, y, cursor + ol, y);
            g2.draw(line);
        }
        state.cursorRight(ol);
    }


    /**
     * Draws the tick marks.
     *
     * @param g2       the graphics target.
     * @param cursor   the cursor position (an offset when drawing multiple axes)
     * @param dataArea the area for plotting the data.
     * @param edge     the location of the axis.
     * @param state    the axis state.
     */
    public void drawTickMarks(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge, AxisState state) {
        Plot p = getPlot();
        if (p == null) return;

        CategoryPlot plot = (CategoryPlot) p;
        double insideLength = tickMarks.getTickMarkInsideLength();
        double outsideLength = tickMarks.getTickMarkOutsideLength();
        Line2D line = new Line2D.Double();
        List<Comparable> categories = plot.getCategoriesForAxis(this);
        g2.setPaint(tickMarks.getTickMarkPaint());
        g2.setStroke(tickMarks.getTickMarkStroke());
        Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_NORMALIZE);
        if (edge.equals(RectangleEdge.TOP)) {
            stateUp(categories, dataArea, edge, line, cursor, il, ol, g2, state);
        } else if (edge.equals(RectangleEdge.BOTTOM)) {
            stateDown(categories, dataArea, edge, line, cursor, il, ol, g2, state);
        } else if (edge.equals(RectangleEdge.LEFT)) {
            stateLeft(g2, cursor, dataArea, edge, state, il, ol, line, categories);
        } else if (edge.equals(RectangleEdge.RIGHT)) {
            stateRight(g2, cursor, dataArea, edge, state, il, ol, line, categories);
        }

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
    }


    /**
     * Creates a label.
     *
     * @param category the category.
     * @param width    the available width.
     * @param edge     the edge on which the axis appears.
     * @param g2       the graphics device.
     * @return A label.
     */
    protected TextBlock createLabel(Comparable category, float width,
                                    RectangleEdge edge, Graphics2D g2) {
        TextBlock label = TextUtils.createTextBlock(category.toString(),
                getTickLabelFont(category), getTickLabelPaint(category), width,
                this.maximumCategoryLabelLines, new G2TextMeasurer(g2));
        return label;
    }

    /**
     * Calculates the width of a category label when rendered.
     *
     * @param label    the text block ({@code null} not permitted).
     * @param position the position.
     * @param insets   the label insets.
     * @param g2       the graphics device.
     * @return The width.
     */
    protected double calculateCategoryLabelWidth(TextBlock label,
                                                 CategoryLabelPosition position, RectangleInsets insets, Graphics2D g2) {
        Size2D size = label.calculateDimensions(g2);
        Rectangle2D box = new Rectangle2D.Double(0.0, 0.0, size.getWidth(),
                size.getHeight());
        Shape rotatedBox = ShapeUtils.rotateShape(box, position.getAngle(),
                0.0f, 0.0f);
        double w = rotatedBox.getBounds2D().getWidth() + insets.getLeft()
                + insets.getRight();
        return w;
    }

    /**
     * Calculates the height of a category label when rendered.
     *
     * @param block    the text block ({@code null} not permitted).
     * @param position the label position ({@code null} not permitted).
     * @param insets   the label insets ({@code null} not permitted).
     * @param g2       the graphics device ({@code null} not permitted).
     * @return The height.
     */
    protected double calculateCategoryLabelHeight(TextBlock block,
                                                  CategoryLabelPosition position, RectangleInsets insets, Graphics2D g2) {
        Size2D size = block.calculateDimensions(g2);
        Rectangle2D box = new Rectangle2D.Double(0.0, 0.0, size.getWidth(),
                size.getHeight());
        Shape rotatedBox = ShapeUtils.rotateShape(box, position.getAngle(),
                0.0f, 0.0f);
        double h = rotatedBox.getBounds2D().getHeight()
                + insets.getTop() + insets.getBottom();
        return h;
    }

    /**
     * Creates a clone of the axis.
     *
     * @return A clone.
     * @throws CloneNotSupportedException if some component of the axis does
     *                                    not support cloning.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        CategoryAxis clone = (CategoryAxis) super.clone();
        clone.tickLabelFontMap = new HashMap<>(this.tickLabelFontMap);
        clone.tickLabelPaintMap = new HashMap<>(this.tickLabelPaintMap);
        clone.categoryLabelToolTips = new HashMap<>(this.categoryLabelToolTips);
        clone.categoryLabelURLs = new HashMap<>(this.categoryLabelToolTips);
        return clone;
    }

    /**
     * Tests this axis for equality with an arbitrary object.
     *
     * @param obj the object ({@code null} permitted).
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryAxis)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CategoryAxis that = (CategoryAxis) obj;
        if (that.lowerMargin != this.lowerMargin) {
            return false;
        }
        if (that.upperMargin != this.upperMargin) {
            return false;
        }
        if (that.categoryMargin != this.categoryMargin) {
            return false;
        }
        if (!that.helper.equals(helper)) { // REFACTOR @ambco
            return false;
        }
        if (!Objects.equals(that.categoryLabelToolTips, this.categoryLabelToolTips)) {
            return false;
        }
        if (!Objects.equals(this.categoryLabelURLs, that.categoryLabelURLs)) {
            return false;
        }
        if (!Objects.equals(this.tickLabelFontMap, that.tickLabelFontMap)) {
            return false;
        }
        return PaintUtils.equal(this.tickLabelPaintMap, that.tickLabelPaintMap);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Provides serialization support.
     *
     * @param stream the output stream.
     * @throws IOException if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        writePaintMap(this.tickLabelPaintMap, stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream the input stream.
     * @throws IOException            if there is an I/O error.
     * @throws ClassNotFoundException if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.tickLabelPaintMap = readPaintMap(stream);
    }

    /**
     * Reads a {@code Map} of ({@code Comparable}, {@code Paint})
     * elements from a stream.
     *
     * @param in the input stream.
     * @return The map.
     * @throws IOException
     * @throws ClassNotFoundException
     * @see #writePaintMap(Map, ObjectOutputStream)
     */
    private Map readPaintMap(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        boolean isNull = in.readBoolean();
        if (isNull) {
            return null;
        }
        Map result = new HashMap();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            Comparable category = (Comparable) in.readObject();
            Paint paint = SerialUtils.readPaint(in);
            result.put(category, paint);
        }
        return result;
    }

    /**
     * Writes a map of ({@code Comparable}, {@code Paint})
     * elements to a stream.
     *
     * @param map the map ({@code null} permitted).
     * @param out
     * @throws IOException
     * @see #readPaintMap(ObjectInputStream)
     */
    private void writePaintMap(Map map, ObjectOutputStream out)
            throws IOException {
        if (map == null) {
            out.writeBoolean(true);
        } else {
            out.writeBoolean(false);
            Set keys = map.keySet();
            int count = keys.size();
            out.writeInt(count);
            for (Object o : keys) {
                Comparable key = (Comparable) o;
                out.writeObject(key);
                SerialUtils.writePaint((Paint) map.get(key), out);
            }
        }
    }

    @Override
    public void apply(StandardChartTheme theme) {
        setLabelFont(theme.getLargeFont());
        setLabelPaint(theme.getAxisLabelPaint());
        tickLabel.setTickLabelFont(theme.getRegularFont());
        tickLabel.setTickLabelPaint(theme.getTickLabelPaint());
        if (this instanceof SubCategoryAxis) {
            SubCategoryAxis sca = (SubCategoryAxis) this;
            sca.getSubHelper().setSubLabelFont(theme.getRegularFont());
            sca.getSubHelper().setSubLabelPaint(theme.getTickLabelPaint());
        }
    }
}
