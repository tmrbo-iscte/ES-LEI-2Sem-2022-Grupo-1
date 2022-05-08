package org.jfree.chart.annotations;

import org.jfree.chart.plot.PlotRenderingInfo;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

public class Painter {
    /**
     * This class exists in order to make it easier to paint shapes across all XY..Annotation
     */

    /** The shape. */
    private transient Shape shape;

    /** The stroke used to draw the shape's outline. */
    private transient Stroke stroke;

    /** The paint used to draw the shape's outline. */
    private transient Paint outlinePaint;

    /** The paint used to fill the shape. */
    private transient Paint fillPaint;

    /** The polygon. */
    private GeneralPath area;

    private Rectangle2D box;

    public Painter(Shape shape, Stroke stroke, Paint outlinePaint, Paint fillPaint) {
        this.shape = shape;
        this.stroke = stroke;
        this.outlinePaint = outlinePaint;
        this.fillPaint = fillPaint;
    }

    public Painter(GeneralPath area, Stroke stroke, Paint outlinePaint, Paint fillPaint) {
        this.area = area;
        this.stroke = stroke;
        this.outlinePaint = outlinePaint;
        this.fillPaint = fillPaint;
    }

    public Painter(Rectangle2D box, Stroke stroke, Paint outlinePaint, Paint fillPaint) {
        this.box = box;
        this.stroke = stroke;
        this.outlinePaint = outlinePaint;
        this.fillPaint = fillPaint;
    }



    public void paintShape(Graphics2D g2) {
        if (this.fillPaint != null) {
            g2.setPaint(this.fillPaint);
            g2.fill(this.shape);
        }

        if (this.stroke != null && this.outlinePaint != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.stroke);
            g2.draw(this.shape);
        }
    }

    public void paintArea(Graphics2D g2){
        if (this.fillPaint != null) {
            g2.setPaint(this.fillPaint);
            g2.fill(this.area);
        }

        if (this.stroke != null && this.outlinePaint != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.stroke);
            g2.draw(this.area);
        }
    }

    public void paintBox(Graphics2D g2){

        if (this.fillPaint != null) {
            g2.setPaint(this.fillPaint);
            g2.fill(this.box);
        }

        if (this.stroke != null && this.outlinePaint != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.stroke);
            g2.draw(this.box);
        }
    }
}
