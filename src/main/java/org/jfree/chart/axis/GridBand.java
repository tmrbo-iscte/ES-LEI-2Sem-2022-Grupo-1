package org.jfree.chart.axis;

import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.SerialUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;

public class GridBand implements Serializable, Cloneable {
    /**
     * The default grid band paint.
     */
    public static final Paint DEFAULT_GRID_BAND_PAINT
            = new Color(232, 234, 232, 128);
    /**
     * The default paint for alternate grid bands.
     */
    public static final Paint DEFAULT_GRID_BAND_ALTERNATE_PAINT
            = new Color(0, 0, 0, 0);  // transparent/** Flag that indicates whether or not grid bands are visible. */
    public boolean gridBandsVisible;
    /**
     * The paint used to color the grid bands (if the bands are visible).
     */
    public transient Paint gridBandPaint;
    /**
     * The paint used to fill the alternate grid bands.
     */
    public transient Paint gridBandAlternatePaint;

    SymbolAxis saxis;

    public GridBand(SymbolAxis saxis) {
        this.saxis = saxis;
    }

    /**
     * Returns the flag that controls whether or not grid bands are drawn for
     * the axis.  The default value is {@code true}.
     *
     * @return A boolean.
     * @see #setGridBandsVisible(boolean)
     */
    public boolean isGridBandsVisible() {
        return this.gridBandsVisible;
    }

    /**
     * Sets the flag that controls whether or not grid bands are drawn for this
     * axis and notifies registered listeners that the axis has been modified.
     * Each band is the area between two adjacent gridlines
     * running perpendicular to the axis.  When the bands are drawn they are
     * filled with the colors {@link #getGridBandPaint()} and
     * {@link #getGridBandAlternatePaint()} in an alternating sequence.
     *
     * @param flag the new setting.
     * @see #isGridBandsVisible()
     */
    public void setGridBandsVisible(boolean flag) {
        this.gridBandsVisible = flag;
        saxis.fireChangeEvent();
    }

    /**
     * Returns the paint used to color grid bands (two colors are used
     * alternately, the other is returned by
     * {@link #getGridBandAlternatePaint()}).  The default value is
     * {@link #DEFAULT_GRID_BAND_PAINT}.
     *
     * @return The paint (never {@code null}).
     * @see #setGridBandPaint(Paint)
     * @see #isGridBandsVisible()
     */
    public Paint getGridBandPaint() {
        return this.gridBandPaint;
    }

    /**
     * Sets the grid band paint and notifies registered listeners that the
     * axis has been changed.  See the {@link #setGridBandsVisible(boolean)}
     * method for more information about grid bands.
     *
     * @param paint the paint ({@code null} not permitted).
     * @see #getGridBandPaint()
     */
    public void setGridBandPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.gridBandPaint = paint;
        saxis.fireChangeEvent();
    }

    /**
     * Returns the second paint used to color grid bands (two colors are used
     * alternately, the other is returned by {@link #getGridBandPaint()}).
     * The default value is {@link #DEFAULT_GRID_BAND_ALTERNATE_PAINT}
     * (transparent).
     *
     * @return The paint (never {@code null}).
     * @see #setGridBandAlternatePaint(Paint)
     */
    public Paint getGridBandAlternatePaint() {
        return this.gridBandAlternatePaint;
    }

    /**
     * Sets the grid band paint and notifies registered listeners that the
     * axis has been changed.  See the {@link #setGridBandsVisible(boolean)}
     * method for more information about grid bands.
     *
     * @param paint the paint ({@code null} not permitted).
     * @see #getGridBandAlternatePaint()
     * @see #setGridBandPaint(Paint)
     */
    public void setGridBandAlternatePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.gridBandAlternatePaint = paint;
        saxis.fireChangeEvent();
    }

    /**
     * Draws the grid bands (alternate bands are colored using
     * {@link #getGridBandPaint()} and {@link #getGridBandAlternatePaint()}.
     *
     * @param g2       the graphics target ({@code null} not permitted).
     * @param dataArea the data area to which the axes are aligned
     *                 ({@code null} not permitted).
     * @param edge     the edge to which the axis is aligned ({@code null} not
     *                 permitted).
     * @param ticks    the ticks ({@code null} not permitted).
     */
    public void drawGridBands(Graphics2D g2,
                              Rectangle2D dataArea, RectangleEdge edge, List ticks) {
        Shape savedClip = g2.getClip();
        g2.clip(dataArea);
        if (RectangleEdge.isTopOrBottom(edge)) {
            saxis.drawGridBandsHorizontal(g2, dataArea, true, ticks, this);
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            saxis.drawGridBandsVertical(g2, dataArea, true, ticks, this);
        }
        g2.setClip(savedClip);
    }

    @Override
    public GridBand clone() {
        try {
            return (GridBand) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
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
        SerialUtils.writePaint(getGridBandPaint(), stream);
        SerialUtils.writePaint(getGridBandAlternatePaint(), stream);
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
        setGridBandPaint(SerialUtils.readPaint(stream));
        setGridBandAlternatePaint(SerialUtils.readPaint(stream));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GridBand)) {
            return false;
        }
        GridBand that = (GridBand) obj;
        if (isGridBandsVisible() != that.isGridBandsVisible()) {
            return false;
        }
        if (!PaintUtils.equal(getGridBandPaint(), that.getGridBandPaint())) {
            return false;
        }
        if (!PaintUtils.equal(getGridBandAlternatePaint(),
                that.getGridBandAlternatePaint())) {
            return false;
        }
        return true;
    }
}
