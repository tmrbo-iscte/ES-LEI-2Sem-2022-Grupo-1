package org.jfree.chart.axis;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.internal.Args;
import java.io.Serializable;
import java.util.Objects;

/**
 * Extract Class - refactor God Class CategoryAxis.
 * @author Afonso Caniço (adapted from JDeodorant suggestion)
 */
public class CategoryAxisHelper implements Serializable, Cloneable {
    private final CategoryAxis axis;
    private float maximumCategoryLabelWidthRatio = 0.0f;
    private int categoryLabelPositionOffset = 4;
    private CategoryLabelPositions categoryLabelPositions = CategoryLabelPositions.STANDARD;

    // Default constructor.
    public CategoryAxisHelper(CategoryAxis axis) {
        Args.nullNotPermitted(axis, "axis");
        this.axis = axis;
    }

    public CategoryAxisHelper(CategoryAxis axis, float ratio, int offset, CategoryLabelPositions positions) {
        Args.nullNotPermitted(axis, "axis");
        Args.nullNotPermitted(positions, "positions");
        this.axis = axis;
        this.maximumCategoryLabelWidthRatio = ratio;
        this.categoryLabelPositionOffset = offset;
        this.categoryLabelPositions = positions;
    }

    public float getMaximumCategoryLabelWidthRatio() {
        return maximumCategoryLabelWidthRatio;
    }

    /**
     * Sets the maximum category label width ratio and sends an {@link AxisChangeEvent}  to all registered listeners.
     * @param ratio   the ratio.
     * @see #getMaximumCategoryLabelWidthRatio()
     */
    public void setMaximumCategoryLabelWidthRatio(float ratio) {
        this.maximumCategoryLabelWidthRatio = ratio;
        axis.fireChangeEvent();
    }

    public int getCategoryLabelPositionOffset() {
        return categoryLabelPositionOffset;
    }

    /**
     * Sets the offset between the axis and the category labels (before label positioning is taken into account) and sends a change event to all  registered listeners.
     * @param offset   the offset (in Java2D units).
     * @see #getCategoryLabelPositionOffset()
     */
    public void setCategoryLabelPositionOffset(int offset) {
        this.categoryLabelPositionOffset = offset;
        axis.fireChangeEvent();
    }

    public CategoryLabelPositions getCategoryLabelPositions() {
        return categoryLabelPositions;
    }

    /**
     * Sets the category label position specification for the axis and sends an {@link AxisChangeEvent}  to all registered listeners.
     * @param positions   the positions ( {@code  null}  not permitted).
     * @see #getCategoryLabelPositions()
     */
    public void setCategoryLabelPositions(CategoryLabelPositions positions) {
        Args.nullNotPermitted(positions, "positions");
        this.categoryLabelPositions = positions;
        axis.fireChangeEvent();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryAxisHelper that = (CategoryAxisHelper) o;
        return Float.compare(that.maximumCategoryLabelWidthRatio, maximumCategoryLabelWidthRatio) == 0 && categoryLabelPositionOffset == that.categoryLabelPositionOffset && Objects.equals(categoryLabelPositions, that.categoryLabelPositions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maximumCategoryLabelWidthRatio, categoryLabelPositionOffset, categoryLabelPositions);
    }
}
