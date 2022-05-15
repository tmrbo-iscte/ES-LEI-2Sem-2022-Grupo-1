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
 * --------------------
 * SubCategoryAxis.java
 * --------------------
 * (C) Copyright 2004-2022, by David Gilbert.
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   Adriaan Joubert;
 *
 */

package org.jfree.chart.axis;

import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.text.TextUtils;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A specialised category axis that can display sub-categories.
 */
public class SubCategoryAxis extends CategoryAxis implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -1279463299793228344L;

    /** Storage for the sub-categories (these need to be set manually). */
    private SubCategoryAxisHelper helper;

    /**
     * Creates a new axis.
     *
     * @param label  the axis label.
     */
    public SubCategoryAxis(String label) {
        super(label);
        helper = new SubCategoryAxisHelper(this, new java.util.ArrayList());
    }

    public SubCategoryAxisHelper getSubHelper() {
        return helper;
    }

    /**
     * Estimates the space required for the axis, given a specific drawing area.
     *
     * @param g2  the graphics device (used to obtain font information).
     * @param plot  the plot that the axis belongs to.
     * @param plotArea  the area within which the axis should be drawn.
     * @param edge  the axis location (top or bottom).
     * @param space  the space already reserved.
     *
     * @return The space required to draw the axis.
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

        space = super.reserveSpace(g2, plot, plotArea, edge, space);
        double maxdim = getMaxDim(g2, edge);
        if (RectangleEdge.isTopOrBottom(edge)) {
            space.add(maxdim, edge);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            space.add(maxdim, edge);
        }
        return space;
    }

    /**
     * Returns the maximum of the relevant dimension (height or width) of the
     * subcategory labels.
     *
     * @param g2  the graphics device.
     * @param edge  the edge.
     *
     * @return The maximum dimension.
     */
    private double getMaxDim(Graphics2D g2, RectangleEdge edge) {
        double result = 0.0;
        g2.setFont(helper.getSubLabelFont());
        FontMetrics fm = g2.getFontMetrics();
        for (Object subCategory : helper.getSubCategories()) {
            Comparable subcategory = (Comparable) subCategory;
            String label = subcategory.toString();
            Rectangle2D bounds = TextUtils.getTextBounds(label, g2, fm);
            double dim;
            if (RectangleEdge.isLeftOrRight(edge)) {
                dim = bounds.getWidth();
            }
            else {  // must be top or bottom
                dim = bounds.getHeight();
            }
            result = Math.max(result, dim);
        }
        return result;
    }

    /**
     * Draws the axis on a Java 2D graphics device (such as the screen or a
     * printer).
     *
     * @param g2  the graphics device ({@code null} not permitted).
     * @param cursor  the cursor location.
     * @param plotArea  the area within which the axis should be drawn
     *                  ({@code null} not permitted).
     * @param dataArea  the area within which the plot is being drawn
     *                  ({@code null} not permitted).
     * @param edge  the location of the axis ({@code null} not permitted).
     * @param plotState  collects information about the plot
     *                   ({@code null} permitted).
     *
     * @return The axis state (never {@code null}).
     */
    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge, 
            PlotRenderingInfo plotState) {

        // if the axis is not visible, don't draw it...
        if (!isVisible()) {
            return new AxisState(cursor);
        }

        if (isAxisLineVisible()) {
            drawAxisLine(g2, cursor, dataArea, edge);
        }

        // draw the category labels and axis label
        AxisState state = new AxisState(cursor);
        state = drawSubCategoryLabels(g2, dataArea, edge, state);
        state = drawCategoryLabels(g2, plotArea, dataArea, edge, state,
                plotState);
        if (getAttributedLabel() != null) {
            state = drawAttributedLabel(getAttributedLabel(), g2,
                    dataArea, edge, state);
        } else {
            state = drawLabel(getLabel(), g2, dataArea, edge, state);
        } 
        return state;

    }

    /**
     * Draws the category labels and returns the updated axis state.
     *
     * @param g2  the graphics device ({@code null} not permitted).
     * @param dataArea  the area inside the axes ({@code null} not
     *                  permitted).
     * @param edge  the axis location ({@code null} not permitted).
     * @param state  the axis state ({@code null} not permitted).
     * @return The updated axis state (never {@code null}).
     */
    protected AxisState drawSubCategoryLabels(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge, AxisState state) {
        Args.nullNotPermitted(state, "state");

        g2.setFont(helper.getSubLabelFont());
        g2.setPaint(helper.getSubLabelPaint());
        CategoryPlot plot = (CategoryPlot) getPlot();
        int categoryCount = 0;
        CategoryDataset dataset = plot.getDataset();
        if (dataset != null) categoryCount = dataset.getColumnCount();

        double maxdim = getMaxDim(g2, edge);
        for (int categoryIndex = 0; categoryIndex < categoryCount; categoryIndex++) {
            double x0 = 0.0;
            double x1 = 0.0;
            double y0 = 0.0;
            double y1 = 0.0;
            if (edge == RectangleEdge.TOP) {
                x0 = getCategoryStart(categoryIndex, categoryCount, dataArea, edge);
                x1 = getCategoryEnd(categoryIndex, categoryCount, dataArea, edge);
                y1 = state.getCursor();
                y0 = y1 - maxdim;
            }
            else if (edge == RectangleEdge.BOTTOM) {
                x0 = getCategoryStart(categoryIndex, categoryCount, dataArea, edge);
                x1 = getCategoryEnd(categoryIndex, categoryCount, dataArea, edge);
                y0 = state.getCursor();
                y1 = y0 + maxdim;
            }
            else if (edge == RectangleEdge.LEFT) {
                y0 = getCategoryStart(categoryIndex, categoryCount, dataArea, edge);
                y1 = getCategoryEnd(categoryIndex, categoryCount, dataArea, edge);
                x1 = state.getCursor();
                x0 = x1 - maxdim;
            }
            else if (edge == RectangleEdge.RIGHT) {
                y0 = getCategoryStart(categoryIndex, categoryCount, dataArea, edge);
                y1 = getCategoryEnd(categoryIndex, categoryCount, dataArea, edge);
                x0 = state.getCursor();
                x1 = x0 + maxdim;
            }
            Rectangle2D area = new Rectangle2D.Double(x0, y0, (x1 - x0), (y1 - y0));
            int subCategoryCount = helper.getSubCategories().size();
            float width = (float) ((x1 - x0) / subCategoryCount);
            float height = (float) ((y1 - y0) / subCategoryCount);
            float xx, yy;
            for (int i = 0; i < subCategoryCount; i++) {
                if (RectangleEdge.isTopOrBottom(edge)) {
                    xx = (float) (x0 + (i + 0.5) * width);
                    yy = (float) area.getCenterY();
                }
                else {
                    xx = (float) area.getCenterX();
                    yy = (float) (y0 + (i + 0.5) * height);
                }
                String label = helper.getSubCategory(i).toString();
                TextUtils.drawRotatedString(label, g2, xx, yy, TextAnchor.CENTER, 0.0, TextAnchor.CENTER);
            }
        }

        if (edge.equals(RectangleEdge.TOP)) {
            double h = maxdim;
            state.cursorUp(h);
        }
        else if (edge.equals(RectangleEdge.BOTTOM)) {
            double h = maxdim;
            state.cursorDown(h);
        }
        else if (edge == RectangleEdge.LEFT) {
            double w = maxdim;
            state.cursorLeft(w);
        }
        else if (edge == RectangleEdge.RIGHT) {
            double w = maxdim;
            state.cursorRight(w);
        }
        return state;
    }

    /**
     * Tests the axis for equality with an arbitrary object.
     *
     * @param obj  the object ({@code null} permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof SubCategoryAxis && super.equals(obj)) {
            SubCategoryAxis other = (SubCategoryAxis) obj;
            return helper.equals(other.helper);
        }
        return false;
    }

    /**
     * Returns a hashcode for this instance.
     * 
     * @return A hashcode for this instance. 
     */
    @Override
    public int hashCode() {
        return super.hashCode();
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
        SerialUtils.writePaint(helper.getSubLabelPaint(), stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        helper.setSubLabelPaint(SerialUtils.readPaint(stream));
    }

}
