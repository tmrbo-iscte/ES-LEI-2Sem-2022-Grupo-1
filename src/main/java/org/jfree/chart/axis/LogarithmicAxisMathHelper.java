package org.jfree.chart.axis;

import org.jfree.data.Range;

public class LogarithmicAxisMathHelper {

    /** Useful constant for log(10). */
    public static final double LOG10 = Math.log(10.0);

    /** Smallest arbitrarily-close-to-zero value allowed. */
    public static final double SMALL_LOG_VALUE = 1e-100;

    private boolean allowNegatives = false;

    /** Helper flag for log axis processing. */
    private boolean smallLogFlag = false;

    public void setAllowNegativesFlag(boolean flgVal) {
        this.allowNegatives = flgVal;
    }

    /**
     * Returns the 'allowNegativesFlag' flag; true to allow negative values
     * in data, false to be able to plot positive values arbitrarily close
     * to zero.
     *
     * @return The flag.
     */
    public boolean allowsNegatives() {
        return this.allowNegatives;
    }

    public void setSmallLogFlag(boolean flag) {
        this.smallLogFlag = flag;
    }

    public boolean getSmallLogFlag() {
        return this.smallLogFlag;
    }

    public void setupSmallLogFlag(Range range) {
        // set flag true if negative values not allowed and the
        // lower bound is between 0 and 10:
        double lowerVal = range.getLowerBound();
        smallLogFlag = (!allowNegatives && lowerVal < 10.0 && lowerVal > 0.0);
    }

    protected double switchedLog10(double val) {
        return smallLogFlag ? Math.log10(val) : adjustedLog10(val);
    }

    public double switchedPow10(double val) {
        return smallLogFlag ? Math.pow(10.0, val) : adjustedPow10(val);
    }

    public double adjustedLog10(double val) {
        boolean isNegative = (val < 0.0);
        if (isNegative) val = -val; // if negative then set flag and make positive
        if (val < 10.0) val += (10.0 - val) / 10.0;  // if < 10 then increase so 0 translates to 0

        //return value; negate if original value was negative:
        double res = Math.log10(val);
        return isNegative ? (-res) : res;
    }

    public double adjustedPow10(double val) {
        boolean isNegative = (val < 0.0);
        if (isNegative) val = -val; // if negative then set flag and make positive

        double res;
        if (val < 1.0) res = (Math.pow(10, val + 1.0) - 10.0) / 9.0; // invert adjustLog10
        else res = Math.pow(10, val);

        return isNegative ? (-res) : res;
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
    public double logCeil(double upper) {
        double logCeil;
        double pow = Math.pow(10, Math.ceil(Math.log10(upper)));
        if (allowNegatives) { // negative values are allowed
            if (upper > 10.0) logCeil = pow;
            else if (upper < -10.0) logCeil = -Math.pow(10, -Math.ceil(-Math.log10(-upper)));
            else logCeil = Math.ceil(upper); // use as-is
        }
        else { // negative values not allowed
            if (upper > 0.0) logCeil = pow;
            else logCeil = Math.ceil(upper); // use as-is
        }
        return logCeil;
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
    public double logFloor(double lower) {
        double logFloor;
        double pow = Math.pow(10, Math.floor(Math.log10(lower)));
        if (allowNegatives) { // negative values are allowed
            if (lower > 10.0) logFloor = pow;
            else if (lower < -10.0) logFloor = -Math.pow(10, -Math.floor(-Math.log10(-lower)));
            else logFloor = Math.floor(lower); // use as-is
        }
        else { // negative values not allowed
            if (lower > 0.0) logFloor = pow;
            else logFloor = Math.floor(lower);   // use as-is
        }
        return logFloor;
    }
}
