package org.jfree.chart.entity;

import org.jfree.chart.TestUtils;
import org.jfree.chart.internal.CloneUtils;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.jupiter.api.Assertions.*;

public class XYAnnotationEntityTest {
    /**
     * Confirm that the equals method can distinguish all the required fields.
     */
    @Test
    public void testEquals() {
        XYAnnotationEntity e1 = createEntity();
        XYAnnotationEntity e2 = createEntity();
        assertEquals(e1, e2);

        e1.setToolTipText("New ToolTip");
        assertNotEquals(e1, e2);
        e2.setToolTipText("New ToolTip");
        assertEquals(e1, e2);

        e1.setURLText("New URL");
        assertNotEquals(e1, e2);
        e2.setURLText("New URL");
        assertEquals(e1, e2);

        e1.setRendererIndex(2);
        assertNotEquals(e1, e2);
        e2.setRendererIndex(2);
        assertEquals(e1, e2);

        e1.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertNotEquals(e1, e2);
        e2.setArea(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertEquals(e1, e2);
    }

    /**
     * Confirm that cloning works.
     */
    @Test
    public void testCloning() throws CloneNotSupportedException {
        XYAnnotationEntity e1 = createEntity();
        XYAnnotationEntity e2 = CloneUtils.clone(e1);
        assertNotSame(e1, e2);
        assertSame(e1.getClass(), e2.getClass());
        assertEquals(e1, e2);
    }

    /**
     * Serialize an instance, restore it, and check for equality.
     */
    @Test
    public void testSerialization() {
        XYAnnotationEntity e1 = createEntity();
        XYAnnotationEntity e2 = TestUtils.serialised(e1);
        assertEquals(e1, e2);
    }

    private static XYAnnotationEntity createEntity() {
        return new XYAnnotationEntity(
                new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0),
                0, "ToolTip", "URL");
    }
}