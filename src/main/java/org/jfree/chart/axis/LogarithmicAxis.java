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
 * LogarithmicAxis.java
 * --------------------
 * (C) Copyright 2000-2022, by David Gilbert and Contributors.
 *
 * Original Author:  Michael Duffy / Eric Thomas;
 * Contributor(s):   David Gilbert;
 *                   David M. O'Donnell;
 *                   Scott Sams;
 *                   Sergei Ivanov;
 *
 */

package org.jfree.chart.axis;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.text.TextAnchor;
import org.jfree.data.Range;

/**
 * A numerical axis that uses a logarithmic scale.
 */
public class LogarithmicAxis extends NumberAxis {

    /** For serialization. */
    private static final long serialVersionUID = 2502918599004103054L;

    /**
     * Flag set true make axis throw exception if any values are &lt;= 0 and 
     * 'math.allowNegativesFlag' is false.
     */
    protected boolean strictValuesFlag = true;

    /** Number formatter for generating numeric strings. */
    protected final NumberFormat numberFormatterObj
        = NumberFormat.getInstance();

    /** Flag set true for "1e#"-style tick labels. */
    protected boolean expTickLabelsFlag = false;

    /** Flag set true for "10^n"-style tick labels. */
    protected boolean log10TickLabelsFlag = false;

    /** True to make 'autoAdjustRange()' select "10^n" values. */
    protected boolean autoRangeNextLogFlag = false;

    private transient final LogarithmicAxisMathHelper math = new LogarithmicAxisMathHelper();

    /**
     * Creates a new axis.
     *
     * @param label  the axis label.
     */
    public LogarithmicAxis(String label) {
        super(label);
        setupNumberFmtObj();      //setup number formatter obj
    }

    public LogarithmicAxisMathHelper getMathHelper() {
        return math;
    }

    protected double switchedLog10(double val) {
        return math.switchedLog10(val);
    }

    /**
     * Sets the 'log10TickLabelsFlag' flag.  The default value is false.
     *
     * @param flag true for "10^n"-style tick labels, false for "1e#"-style
     * or regular numeric tick labels.
     */
    public void setLog10TickLabelsFlag(boolean flag) {
        this.log10TickLabelsFlag = flag;
    }

    /**
     * Overridden version that calls original and then sets up flag for
     * log axis processing.
     *
     * @param range  the new range.
     */
    @Override
    public void setRange(Range range) {
        super.setRange(range);      // call parent method
        setupSmallLogFlag();        // setup flag based on bounds values
    }

    /**
     * Sets up flag for log axis processing.  Set true if negative values
     * not allowed and the lower bound is between 0 and 10.
     */
    protected void setupSmallLogFlag() {
        math.setupSmallLogFlag(getRange());
    }

    /**
     * Sets up the number formatter object according to the
     * 'expTickLabelsFlag' flag.
     */
    protected void setupNumberFmtObj() {
        if (this.numberFormatterObj instanceof DecimalFormat) {
            //setup for "1e#"-style tick labels or regular
            // numeric tick labels, depending on flag:
            ((DecimalFormat) this.numberFormatterObj).applyPattern(this.expTickLabelsFlag ? "0E0" : "0.###");
        }
    }

    /**
     * Returns the largest (closest to positive infinity) double value that is
     * not greater than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param lower a double value below which a floor will be calcualted.
     *
     * @return 10<sup>N</sup> with N .. { 1 ... }
     */
    protected double computeLogFloor(double lower) {
        return math.logFloor(lower);
    }

    /**
     * Returns the smallest (closest to negative infinity) double value that is
     * not less than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param upper a double value above which a ceiling will be calcualted.
     *
     * @return 10<sup>N</sup> with N .. { 1 ... }
     */
    protected double computeLogCeil(double upper) {
        return math.logCeil(upper);
    }

    /**
     * Rescales the axis to ensure that all data is visible.
     */
    @Override
    public void autoAdjustRange() {

        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data.
        }

        if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot) plot;

            double lower;
            Range r = vap.getDataRange(this);
            if (r == null) {
                   //no real data present
                r = getDefaultAutoRange();
                lower = r.getLowerBound();    //get lower bound value
            }
            else {
                //actual data is present
                lower = r.getLowerBound();    //get lower bound value
                if (this.strictValuesFlag && !math.allowsNegatives() && lower <= 0.0) {
                    //strict flag set, allow-negatives not set and values <= 0
                    throw new RuntimeException("Values less than or equal to zero not allowed with logarithmic axis");
                }
            }

            //apply lower margin by decreasing lower bound:
            final double lowerMargin;
            if (lower > 0.0 && (lowerMargin = getLowerMargin()) > 0.0) {
                   //lower bound and margin OK; get log10 of lower bound
                final double logLower = (Math.log(lower) / LogarithmicAxisMathHelper.LOG10);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logLower)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //subtract out margin and get exponential value:
                lower = Math.pow(10, (logLower - (logAbs * lowerMargin)));
            }

            //if flag then change to log version of lowest value
            // to make range begin at a 10^n value:
            if (this.autoRangeNextLogFlag) {
                lower = computeLogFloor(lower);
            }

            if (!math.allowsNegatives() && lower >= 0.0
                    && lower < LogarithmicAxisMathHelper.SMALL_LOG_VALUE) {
                //negatives not allowed and lower range bound is zero
                lower = r.getLowerBound();    //use data range bound instead
            }

            double upper = r.getUpperBound();

             //apply upper margin by increasing upper bound:
            final double upperMargin;
            if (upper > 0.0 && (upperMargin = getUpperMargin()) > 0.0) {
                   //upper bound and margin OK; get log10 of upper bound
                final double logUpper = (Math.log(upper) / LogarithmicAxisMathHelper.LOG10);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logUpper)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //add in margin and get exponential value:
                upper = Math.pow(10, (logUpper + (logAbs * upperMargin)));
            }

            if (!math.allowsNegatives() && upper < 1.0 && upper > 0.0
                    && lower > 0.0) {
                //negatives not allowed and upper bound between 0 & 1
                //round up to nearest significant digit for bound:
                //get negative exponent:
                double expVal = Math.log(upper) / LogarithmicAxisMathHelper.LOG10;
                expVal = Math.ceil(-expVal + 0.001); //get positive exponent
                expVal = Math.pow(10, expVal);      //create multiplier value
                //multiply, round up, and divide for bound value:
                upper = (expVal > 0.0) ? Math.ceil(upper * expVal) / expVal
                    : Math.ceil(upper);
            }
            else {
                //negatives allowed or upper bound not between 0 & 1
                //if flag then change to log version of highest value to
                // make range begin at a 10^n value; else use nearest int
                upper = (this.autoRangeNextLogFlag) ? computeLogCeil(upper)
                    : Math.ceil(upper);
            }
            // ensure the autorange is at least <minRange> in size...
            double minRange = getAutoRangeMinimumSize();
            if (upper - lower < minRange) {
                upper = (upper + lower + minRange) / 2;
                lower = (upper + lower - minRange) / 2;
                //if autorange still below minimum then adjust by 1%
                // (can be needed when minRange is very small):
                if (upper - lower < minRange) {
                    double absUpper = Math.abs(upper);
                    //need to account for case where upper==0.0
                    double adjVal = (absUpper > LogarithmicAxisMathHelper.SMALL_LOG_VALUE) ? absUpper
                        / 100.0 : 0.01;
                    upper = (upper + lower + adjVal) / 2;
                    lower = (upper + lower - adjVal) / 2;
                }
            }

            setRange(new Range(lower, upper), false, false);
            setupSmallLogFlag();       //setup flag based on bounds values
        }
    }

    /**
     * Converts a data value to a coordinate in Java2D space, assuming that
     * the axis runs along one edge of the specified plotArea.
     * Note that it is possible for the coordinate to fall outside the
     * plotArea.
     *
     * @param value  the data value.
     * @param plotArea  the area for plotting the data.
     * @param edge  the axis location.
     *
     * @return The Java2D coordinate.
     */
    @Override
    public double valueToJava2D(double value, Rectangle2D plotArea, RectangleEdge edge) {
        Range range = getRange();
        double axisMin = math.switchedLog10(range.getLowerBound());
        double axisMax = math.switchedLog10(range.getUpperBound());

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = plotArea.getMinX();
            max = plotArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            min = plotArea.getMaxY();
            max = plotArea.getMinY();
        }

        value = math.switchedLog10(value);

        double v = ((value - axisMin) / (axisMax - axisMin)) * (max - min);
        if (isInverted()) {
            return max - v;
        }
        else {
            return min + v;
        }

    }

    /**
     * Converts a coordinate in Java2D space to the corresponding data
     * value, assuming that the axis runs along one edge of the specified
     * plotArea.
     *
     * @param java2DValue  the coordinate in Java2D space.
     * @param plotArea  the area in which the data is plotted.
     * @param edge  the axis location.
     *
     * @return The data value.
     */
    @Override
    public double java2DToValue(double java2DValue, Rectangle2D plotArea, RectangleEdge edge) {
        Range range = getRange();
        double axisMin = math.switchedLog10(range.getLowerBound());
        double axisMax = math.switchedLog10(range.getUpperBound());

        double plotMin = 0.0;
        double plotMax = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            plotMin = plotArea.getX();
            plotMax = plotArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            plotMin = plotArea.getMaxY();
            plotMax = plotArea.getMinY();
        }

        double v = ((java2DValue - plotMin) / (plotMax - plotMin)) * (axisMax - axisMin);
        if (isInverted()) return math.switchedPow10(axisMax - v);
        else return math.switchedPow10(axisMin + v);
    }

    /**
     * Zooms in on the current range.
     *
     * @param lowerPercent  the new lower bound.
     * @param upperPercent  the new upper bound.
     */
    @Override
    public void zoomRange(double lowerPercent, double upperPercent) {
        double startLog = math.switchedLog10(getRange().getLowerBound());
        double lengthLog = math.switchedLog10(getRange().getUpperBound()) - startLog;
        Range adjusted;

        if (isInverted()) {
            adjusted = new Range(
                    math.switchedPow10(startLog + (lengthLog * (1 - upperPercent))),
                    math.switchedPow10(startLog + (lengthLog * (1 - lowerPercent))));
        }
        else {
            adjusted = new Range(
                    math.switchedPow10(startLog + (lengthLog * lowerPercent)),
                    math.switchedPow10(startLog + (lengthLog * upperPercent)));
        }

        setRange(adjusted);
    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot should be drawn.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    @Override
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        List ticks = new java.util.ArrayList();
        Range range = getRange();

        //get lower bound value:
        double lowerBoundVal = range.getLowerBound();

        // if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (math.getSmallLogFlag() && lowerBoundVal < LogarithmicAxisMathHelper.SMALL_LOG_VALUE)
            lowerBoundVal = LogarithmicAxisMathHelper.SMALL_LOG_VALUE;

        // get upper bound value
        double upperBoundVal = range.getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(math.switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(math.switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0 && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount; // decrement to generate more ticks
        }

        double currentTickValue;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            // for each power of 10 value; create ten ticks
            for (int j = 0; j < 10; ++j) {
                // for each tick to be displayed
                if (math.getSmallLogFlag()) {
                    // small log values in use; create numeric value for tick
                    currentTickValue = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (this.expTickLabelsFlag || (i < 0 && currentTickValue > 0.0 && currentTickValue < 1.0)) {
                        // showing "1e#"-style ticks or negative exponent
                        // generating tick value between 0 & 1; show fewer
                        if (j == 0 || (i > -4 && j < 2) || currentTickValue >= upperBoundVal) {
                          // first tick of series, or not too small a value and
                          // one of first 3 ticks, or last tick to be displayed
                            // set exact number of fractional digits to be shown
                            // (no effect if showing "1e#"-style ticks):
                            this.numberFormatterObj.setMaximumFractionDigits(-i);
                            // create tick label (force use of fmt obj):
                            tickLabel = makeTickLabel(currentTickValue, true);
                        }
                        else tickLabel = "";
                    }
                    else {     //tick value not between 0 & 1
                               //show tick label if it's the first or last in
                               // the set, or if it's 1-5; beyond that show
                               // fewer as the values get larger:
                        tickLabel = (j < 1 || (i < 1 && j < 5) || (j < 4 - i)
                                         || currentTickValue >= upperBoundVal)
                                         ? makeTickLabel(currentTickValue) : "";
                    }
                } else { // not small log values in use; allow for values <= 0
                    if (zeroTickFlag) --j; // if did zero tick last iter then decrement to do 1.0 tick now

                    //calculate power-of-ten value for tick:
                    currentTickValue = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j) : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (!zeroTickFlag) {  // did not do zero tick last iteration
                        if (Math.abs(currentTickValue - 1.0) < 0.0001 && lowerBoundVal <= 0.0 && upperBoundVal >= 0.0) {
                            // tick value is 1.0 and 0.0 is within data range
                            currentTickValue = 0.0;     //set tick value to zero
                            zeroTickFlag = true;        //indicate zero tick
                        }
                    } else { // did zero tick last iteration
                        zeroTickFlag = false; //clear flag
                    }
                    // create tick label string:
                    // show tick label if "1e#"-style and it's one
                    // of the first two, if it's the first or last
                    // in the set, or if it's 1-5; beyond that
                    // show fewer as the values get larger:
                    tickLabel = ((this.expTickLabelsFlag && j < 2) || j < 1 || (i < 1 && j < 5) || (j < 4 - i) || currentTickValue >= upperBoundVal) ? makeTickLabel(currentTickValue) : "";
                }

                if (currentTickValue > upperBoundVal) return ticks; // if past highest data value then exit method

                if (currentTickValue >= lowerBoundVal - LogarithmicAxisMathHelper.SMALL_LOG_VALUE) //tick value not below lowest data value
                    ticks.add(getNumberTick(edge, currentTickValue, tickLabel));
            }
        }
        return ticks;
    }

    private NumberTick getNumberTick(RectangleEdge edge, double currentTickValue, String tickLabel) {
        TextAnchor anchor;
        TextAnchor rotationAnchor;
        double angle = 0.0;
        if (isVerticalTickLabels()) {
            anchor = TextAnchor.CENTER_RIGHT;
            rotationAnchor = TextAnchor.CENTER_RIGHT;
            if (edge == RectangleEdge.TOP) angle = Math.PI / 2.0;
            else angle = -Math.PI / 2.0;
        } else {
            if (edge == RectangleEdge.TOP) {
                anchor = TextAnchor.BOTTOM_CENTER;
                rotationAnchor = TextAnchor.BOTTOM_CENTER;
            } else {
                anchor = TextAnchor.TOP_CENTER;
                rotationAnchor = TextAnchor.TOP_CENTER;
            }
        }
        return new NumberTick(currentTickValue, tickLabel, anchor, rotationAnchor, angle);
    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot should be drawn.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    @Override
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {

        List ticks = new java.util.ArrayList();

        //get lower bound value:
        double lowerBoundVal = getRange().getLowerBound();
        //if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (math.getSmallLogFlag() && lowerBoundVal < LogarithmicAxisMathHelper.SMALL_LOG_VALUE) {
            lowerBoundVal = LogarithmicAxisMathHelper.SMALL_LOG_VALUE;
        }
        //get upper bound value
        double upperBoundVal = getRange().getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(math.switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(math.switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        double tickVal;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each tick with a label to be displayed
            int jEndCount = 10;
            if (i == iEndCount) {
                jEndCount = 1;
            }

            for (int j = 0; j < jEndCount; j++) {
                //for each tick to be displayed
                if (math.getSmallLogFlag()) {
                    //small log values in use
                    tickVal = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (j == 0) {
                        //first tick of group; create label text
                        if (this.log10TickLabelsFlag) {
                            //if flag then
                            tickLabel = "10^" + i;   //create "log10"-type label
                        }
                        else {    //not "log10"-type label
                            if (this.expTickLabelsFlag) {
                                //if flag then
                                tickLabel = "1e" + i;  //create "1e#"-type label
                            }
                            else {    //not "1e#"-type label
                                if (i >= 0) {   // if positive exponent then
                                                // make integer
                                    NumberFormat format
                                        = getNumberFormatOverride();
                                    if (format != null) {
                                        tickLabel = format.format(tickVal);
                                    }
                                    else {
                                        tickLabel = Long.toString((long)
                                                Math.rint(tickVal));
                                    }
                                }
                                else {
                                    //negative exponent; create fractional value
                                    //set exact number of fractional digits to
                                    // be shown:
                                    this.numberFormatterObj
                                        .setMaximumFractionDigits(-i);
                                    //create tick label:
                                    tickLabel = this.numberFormatterObj.format(
                                            tickVal);
                                }
                            }
                        }
                    }
                    else {   //not first tick to be displayed
                        tickLabel = "";     //no tick label
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {      //if did zero tick last iter then
                        --j;
                    }               //decrement to do 1.0 tick now
                    tickVal = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j)
                             : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (j == 0) {  //first tick of group
                        if (!zeroTickFlag) {     // did not do zero tick last
                                                 // iteration
                            if (i > iBegCount && i < iEndCount
                                    && Math.abs(tickVal - 1.0) < 0.0001) {
                                // not first or last tick on graph and value
                                // is 1.0
                                tickVal = 0.0;        //change value to 0.0
                                zeroTickFlag = true;  //indicate zero tick
                                tickLabel = "0";      //create label for tick
                            }
                            else {
                                //first or last tick on graph or value is 1.0
                                //create label for tick:
                                if (this.log10TickLabelsFlag) {
                                       //create "log10"-type label
                                    tickLabel = (((i < 0) ? "-" : "")
                                            + "10^" + Math.abs(i));
                                }
                                else {
                                    if (this.expTickLabelsFlag) {
                                           //create "1e#"-type label
                                        tickLabel = (((i < 0) ? "-" : "")
                                                + "1e" + Math.abs(i));
                                    }
                                    else {
                                        NumberFormat format
                                            = getNumberFormatOverride();
                                        if (format != null) {
                                            tickLabel = format.format(tickVal);
                                        }
                                        else {
                                            tickLabel =  Long.toString(
                                                    (long) Math.rint(tickVal));
                                        }
                                    }
                                }
                            }
                        }
                        else {     // did zero tick last iteration
                            tickLabel = "";         //no label
                            zeroTickFlag = false;   //clear flag
                        }
                    }
                    else {       // not first tick of group
                        tickLabel = "";           //no label
                        zeroTickFlag = false;     //make sure flag cleared
                    }
                }

                if (tickVal > upperBoundVal) {
                    return ticks;  //if past highest data value then exit method
                }

                if (tickVal >= lowerBoundVal - LogarithmicAxisMathHelper.SMALL_LOG_VALUE) // tick value not below lowest data value
                    ticks.add(getNumberTick(edge, tickVal, tickLabel)); // create tick object and add to list:
            }
        }
        return ticks;
    }

    /**
     * Converts the given value to a tick label string.
     *
     * @param val the value to convert.
     * @param forceFmtFlag true to force the number-formatter object
     * to be used.
     *
     * @return The tick label string.
     */
    protected String makeTickLabel(double val, boolean forceFmtFlag) {
        if (this.expTickLabelsFlag || forceFmtFlag) {
            //using exponents or force-formatter flag is set
            // (convert 'E' to lower-case 'e'):
            return this.numberFormatterObj.format(val).toLowerCase();
        }
        return getTickUnit().valueToString(val);
    }

    /**
     * Converts the given value to a tick label string.
     * @param val the value to convert.
     *
     * @return The tick label string.
     */
    protected String makeTickLabel(double val) {
        return makeTickLabel(val, false);
    }

}
