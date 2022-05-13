package org.jfree.chart.axis;


import java.text.Format;
import java.text.DecimalFormat;
import org.jfree.chart.internal.Args;
import java.io.Serializable;

public class LogAxisProduct implements Serializable, Cloneable {
    private String baseSymbol = null;
    private Format baseFormatter = new DecimalFormat("0");

    public String getBaseSymbol() {
        return baseSymbol;
    }

    public Format getBaseFormatter() {
        return baseFormatter;
    }

    /**
     * Sets the symbol used to represent the base value of the logarithmic  scale and sends a change event to all registered listeners.
     * @param symbol   the symbol ( {@code  null}  permitted).
     */
    public void setBaseSymbol(String symbol, LogAxis logAxis) {
        this.baseSymbol = symbol;
        logAxis.fireChangeEvent();
    }

    /**
     * Sets the formatter used to format the base value of the logarithmic  scale when it is displayed numerically and sends a change event to all registered listeners.
     * @param formatter   the formatter ( {@code  null}  not permitted).
     */
    public void setBaseFormatter(Format formatter, LogAxis logAxis) {
        Args.nullNotPermitted(formatter, "formatter");
        this.baseFormatter = formatter;
        logAxis.fireChangeEvent();
    }

    public Object clone() throws CloneNotSupportedException {
        return (LogAxisProduct) super.clone();
    }
}