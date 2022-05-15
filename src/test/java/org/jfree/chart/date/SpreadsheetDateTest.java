package org.jfree.chart.date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpreadsheetDateTest {

    @Test
    public void testIsBefore() {
        assertTrue(getDate1().isBefore(getDate2()));
        assertFalse(getDate2().isBefore(getDate1()));
    }

    @Test
    public void testIsAfter() {
        assertTrue(getDate2().isAfter(getDate1()));
        assertFalse(getDate1().isAfter(getDate2()));
    }

    @Test
    public void testIsInRange() {
        assertTrue(getDateBetween().isInRange(getDate1(), getDate2()));
        // Test inverted range
        assertTrue(getDateBetween().isInRange(getDate2(), getDate1()));
        // Test outside
        assertFalse(getDate1().isInRange(getDateBetween(), getDate2()));
        assertFalse(getDate2().isInRange(getDate1(), getDateBetween()));
        // Test self
        assertTrue(getDate1().isInRange(getDate1(), getDate1()));
    }

    @Test
    public void testGetPreviousDayOfWeek() {
        assertEquals(new SpreadsheetDate(11, 5, 2022), getDate1().getPreviousDayOfWeek(SerialDate.WEDNESDAY));
        assertEquals(new SpreadsheetDate(18, 5, 2022), getDate2().getPreviousDayOfWeek(SerialDate.WEDNESDAY));
    }

    @Test
    public void testGetFollowingDayOfWeek() {
        assertEquals(new SpreadsheetDate(18, 5, 2022), getDate1().getFollowingDayOfWeek(SerialDate.WEDNESDAY));
        assertEquals(new SpreadsheetDate(25, 5, 2022), getDate2().getFollowingDayOfWeek(SerialDate.WEDNESDAY));
    }

    @Test
    public void testGetDayOfWeek() {
        assertEquals(SpreadsheetDate.MONDAY, getDate1().getDayOfWeek());
        assertEquals(SpreadsheetDate.FRIDAY, getDate2().getDayOfWeek());
    }

    private static SpreadsheetDate getDate1() {
        //Monday
        return new SpreadsheetDate(16, 5, 2022);
    }

    private static SpreadsheetDate getDate2() {
        //Friday
        return new SpreadsheetDate(20, 5, 2022);
    }

    private static SpreadsheetDate getDateBetween() {
        return new SpreadsheetDate(18, 5, 2022);
    }
}