package org.jfree.chart.axis;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;

import java.awt.*;
import java.io.*;

public class Arrow implements Serializable {
    /**
     * A flag that controls whether an arrow is drawn at the positive end of
     * the axis line.
     */
    boolean positiveArrowVisible;
    /**
     * A flag that controls whether an arrow is drawn at the negative end of
     * the axis line.
     */
    boolean negativeArrowVisible;
    /**
     * The shape used for an up arrow.
     */
    transient Shape upArrow;
    /**
     * The shape used for a down arrow.
     */
    transient Shape downArrow;
    /**
     * The shape used for a left arrow.
     */
    transient Shape leftArrow;
    /**
     * The shape used for a right arrow.
     */
    transient Shape rightArrow;

    private final ValueAxis vaxis;

    public Arrow(ValueAxis vaxis) {
        this.vaxis = vaxis;
    }

    /**
     * Returns a flag that controls whether or not the axis line has an arrow
     * drawn that points in the positive direction for the axis.
     *
     * @return A boolean.
     * @see #setPositiveArrowVisible(boolean)
     */
    public boolean isPositiveArrowVisible() {
        return this.positiveArrowVisible;
    }

    /**
     * Sets a flag that controls whether or not the axis lines has an arrow
     * drawn that points in the positive direction for the axis, and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param visible the flag.
     * @see #isPositiveArrowVisible()
     */
    public void setPositiveArrowVisible(boolean visible) {
        this.positiveArrowVisible = visible;
        vaxis.fireChangeEvent();
    }

    /**
     * Returns a flag that controls whether or not the axis line has an arrow
     * drawn that points in the negative direction for the axis.
     *
     * @return A boolean.
     * @see #setNegativeArrowVisible(boolean)
     */
    public boolean isNegativeArrowVisible() {
        return this.negativeArrowVisible;
    }

    /**
     * Sets a flag that controls whether or not the axis lines has an arrow
     * drawn that points in the negative direction for the axis, and sends an
     * {@link AxisChangeEvent} to all registered listeners.
     *
     * @param visible the flag.
     * @see #setNegativeArrowVisible(boolean)
     */
    public void setNegativeArrowVisible(boolean visible) {
        this.negativeArrowVisible = visible;
        vaxis.fireChangeEvent();
    }

    /**
     * Returns a shape that can be displayed as an arrow pointing upwards at
     * the end of an axis line.
     *
     * @return A shape (never {@code null}).
     * @see #setUpArrow(Shape)
     */
    public Shape getUpArrow() {
        return this.upArrow;
    }

    /**
     * Sets the shape that can be displayed as an arrow pointing upwards at
     * the end of an axis line and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param arrow the arrow shape ({@code null} not permitted).
     * @see #getUpArrow()
     */
    public void setUpArrow(Shape arrow) {
        Args.nullNotPermitted(arrow, "arrow");
        this.upArrow = arrow;
        vaxis.fireChangeEvent();
    }

    /**
     * Returns a shape that can be displayed as an arrow pointing downwards at
     * the end of an axis line.
     *
     * @return A shape (never {@code null}).
     * @see #setDownArrow(Shape)
     */
    public Shape getDownArrow() {
        return this.downArrow;
    }

    /**
     * Sets the shape that can be displayed as an arrow pointing downwards at
     * the end of an axis line and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param arrow the arrow shape ({@code null} not permitted).
     * @see #getDownArrow()
     */
    public void setDownArrow(Shape arrow) {
        Args.nullNotPermitted(arrow, "arrow");
        this.downArrow = arrow;
        vaxis.fireChangeEvent();
    }

    /**
     * Returns a shape that can be displayed as an arrow pointing left at the
     * end of an axis line.
     *
     * @return A shape (never {@code null}).
     * @see #setLeftArrow(Shape)
     */
    public Shape getLeftArrow() {
        return this.leftArrow;
    }

    /**
     * Sets the shape that can be displayed as an arrow pointing left at the
     * end of an axis line and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param arrow the arrow shape ({@code null} not permitted).
     * @see #getLeftArrow()
     */
    public void setLeftArrow(Shape arrow) {
        Args.nullNotPermitted(arrow, "arrow");
        this.leftArrow = arrow;
        vaxis.fireChangeEvent();
    }

    /**
     * Returns a shape that can be displayed as an arrow pointing right at the
     * end of an axis line.
     *
     * @return A shape (never {@code null}).
     * @see #setRightArrow(Shape)
     */
    public Shape getRightArrow() {
        return this.rightArrow;
    }

    /**
     * Sets the shape that can be displayed as an arrow pointing rightwards at
     * the end of an axis line and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     *
     * @param arrow the arrow shape ({@code null} not permitted).
     * @see #getRightArrow()
     */
    public void setRightArrow(Shape arrow) {
        Args.nullNotPermitted(arrow, "arrow");
        this.rightArrow = arrow;
        vaxis.fireChangeEvent();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Arrow that)) {
            return false;
        }
        if (isPositiveArrowVisible() != that.isPositiveArrowVisible()) {
            return false;
        }
        if (isNegativeArrowVisible() != that.isNegativeArrowVisible()) {
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
    @Serial
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeShape(getUpArrow(), stream);
        SerialUtils.writeShape(getDownArrow(), stream);
        SerialUtils.writeShape(getLeftArrow(), stream);
        SerialUtils.writeShape(getRightArrow(), stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    @Serial
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        setUpArrow(SerialUtils.readShape(stream));
        setDownArrow(SerialUtils.readShape(stream));
        setLeftArrow(SerialUtils.readShape(stream));
        setRightArrow(SerialUtils.readShape(stream));
    }
}