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
 * ------------
 * LogTick.java
 * ------------
 * (C) Copyright 2014-2022, by David Gilbert and Contributors.
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 18-Mar-2014 : Version 1 (DG);
 *
 */

package org.jfree.chart.axis;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.util.AttrStringUtils;

/**
 * A tick from a {@link LogAxis}.
 */
public class LogTick extends ValueTick {

    /**
     * The attributed string for the tick label.
     */
    AttributedString attributedLabel;

    /**
     * Creates a new instance.
     *
     * @param type       the type (major or minor tick, {@code null} not
     *                   permitted).
     * @param value      the value.
     * @param label      the label ({@code null} permitted).
     * @param textAnchor the text anchor.
     */
    public LogTick(TickType type, double value, AttributedString label,
                   TextAnchor textAnchor) {
        super(type, value, null, textAnchor, textAnchor, 0.0);
        this.attributedLabel = label;
    }

    /**
     * Returns the attributed string for the tick label, or {@code null}
     * if there is no label.
     *
     * @return The attributed string or {@code null}.
     */
    public AttributedString getAttributedLabel() {
        return this.attributedLabel;
    }
    
    /**
     * Draws this tick's label, if present, at the specified coordinates
     *
     * @param g2 Graphics2D context
     * @param x  x position
     * @param y  y position
     */
    @Override
    public void drawLabel(Graphics2D g2, float x, float y) {
        if (getAttributedLabel() == null) return;
        AttrStringUtils.drawRotatedString(getAttributedLabel(),
                g2, x, y,
                getTextAnchor(), getAngle(),
                getRotationAnchor());
    }

    /**
     * Returns the label bounds
     *
     * @param g2 Graphics2D context
     * @param fm FontMetrics object
     * @return The label bounds when present, or null if this tick has no label
     */
    @Override
    public Rectangle2D getLabelBounds(Graphics2D g2, FontMetrics fm) {
        if (getAttributedLabel() == null) return null;
        return AttrStringUtils.getTextBounds(getAttributedLabel(), g2);
    }
}
