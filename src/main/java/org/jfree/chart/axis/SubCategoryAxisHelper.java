package org.jfree.chart.axis;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.internal.Args;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Extract Class - refactor God Class SubCategoryAxis.
 * @author Afonso Caniço (adapted from JDeodorant suggestion)
 */
public class SubCategoryAxisHelper implements Serializable, Cloneable {
    private final SubCategoryAxis axis;
    private final List subCategories;
    private Font subLabelFont = new Font("SansSerif", Font.PLAIN, 10);
    private transient Paint subLabelPaint = Color.BLACK; /** The paint for the sub-category labels. **/

    public SubCategoryAxisHelper(SubCategoryAxis axis, List subCategories) {
        Args.nullNotPermitted(axis, "axis");
        Args.nullNotPermitted(subCategories, "subCategories");
        this.axis = axis;
        this.subCategories = subCategories;
    }

    public List getSubCategories() {
        return subCategories;
    }

    public Object getSubCategory(int index) {
        return subCategories.get(index);
    }

    public Font getSubLabelFont() {
        return subLabelFont;
    }

    public Paint getSubLabelPaint() {
        return subLabelPaint;
    }

    public void setSubLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.subLabelPaint = paint;
        axis.notifyListeners(new AxisChangeEvent(axis));
    }

    /**
     * Adds a sub-category to the axis and sends an  {@link AxisChangeEvent}  to all registered listeners.
     * @param subCategory   the sub-category ( {@code  null}  not permitted).
     */
    public void addSubCategory(Comparable subCategory) {
        Args.nullNotPermitted(subCategory, "subCategory");
        this.subCategories.add(subCategory);
        axis.notifyListeners(new AxisChangeEvent(axis));
    }

    /**
     * Sets the font used to display the sub-category labels and sends an {@link AxisChangeEvent}  to all registered listeners.
     * @param font   the font ( {@code  null}  not permitted).
     * @see #getSubLabelFont()
     */
    public void setSubLabelFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.subLabelFont = font;
        axis.notifyListeners(new AxisChangeEvent(axis));
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubCategoryAxisHelper that = (SubCategoryAxisHelper) o;
        return Objects.equals(subCategories, that.subCategories) && Objects.equals(subLabelFont, that.subLabelFont) && Objects.equals(subLabelPaint, that.subLabelPaint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subCategories, subLabelFont, subLabelPaint);
    }
}
