package com.ctrip.zeus.service.query.sort;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Random;

/**
 * Created by zhoumy on 2016/11/3.
 */
public class SortEngineTest {
    @Test
    public void sortSortableProperties() throws Exception {
        SortEngine sortEngine = new SortEngine();
        PropertySortObject[] input = new PropertySortObject[50];
        for (int i = 0; i < 50; i++) {
            input[i] = new PropertySortObject();
        }

        PropertySortObject[] copy = new PropertySortObject[50];
        for (int i = 0; i < 50; i++) {
            copy[i] = input[i];
        }
        sortEngine.sort("null", input, true);
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals(copy[i], input[i]);
        }

        String[] properties = new String[]{"aLong", "aLongWrapper", "aBoolean", "aString", "aDate"};
        for (String p : properties) {
            sortEngine.sort(p, input, true);
            for (int i = 0; i < 49; i++) {
                Assert.assertTrue(input[i].getValue(p).getClass().equals(input[i + 1].getValue(p).getClass()));
                Assert.assertTrue("property:" + p + ", " + input[i].getValue(p) + " <= " + input[i + 1].getValue(p), input[i].getValue(p).compareTo(input[i + 1].getValue(p)) <= 0);
            }
        }
    }

    @Test
    public void sortRegisteredProperties() throws Exception {
        SortEngine sortEngine = new SortEngine();
        String[] properties = new String[]{"aLong", "aLongWrapper", "aBoolean", "aString", "aDate"};
        for (String p : properties) {
            sortEngine.register(p, PropertySortObject1.class);
        }
        PropertySortObject1[] input = new PropertySortObject1[50];
        for (int i = 0; i < 50; i++) {
            input[i] = new PropertySortObject1();
        }

        PropertySortObject1[] copy = new PropertySortObject1[50];
        for (int i = 0; i < 50; i++) {
            copy[i] = input[i];
        }
        sortEngine.sort("null", input, true);
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals(copy[i], input[i]);
        }

        for (String p : properties) {
            sortEngine.sort(p, input, true);
            Method m = PropertySortObject1.class.getMethod("get" + p.substring(0, 1).toUpperCase() + p.substring(1));
            for (int i = 0; i < 49; i++) {
                Comparable v1 = (Comparable) m.invoke(input[i]);
                Comparable v2 = (Comparable) m.invoke(input[i + 1]);
                Assert.assertTrue("property:" + p + ", " + v1 + " <= " + v2, v1.compareTo(v2) <= 0);
            }
        }
    }

    class PropertySortObject implements PropertySortable {
        private long aLong;
        private Long aLongWrapper;
        private boolean aBoolean;
        private String aString;
        private Date aDate;

        public PropertySortObject() {
            Random rand = new Random();
            aLong = rand.nextLong();
            aLongWrapper = rand.nextLong();
            aBoolean = rand.nextBoolean();

            byte[] randByte = new byte[20];
            rand.nextBytes(randByte);
            aString = new String(randByte);
            aDate = new Date();
        }

        @Override
        public Comparable getValue(String property) {
            switch (property) {
                case "aLong":
                    return aLong;
                case "aLongWrapper":
                    return aLongWrapper;
                case "aBoolean":
                    return aBoolean;
                case "aString":
                    return aString;
                case "aDate":
                    return aDate;
                default:
                    return null;
            }
        }
    }

    class PropertySortObject1 {
        private long aLong;
        private Long aLongWrapper;
        private boolean aBoolean;
        private String aString;
        private Date aDate;

        public PropertySortObject1() {
            Random rand = new Random();
            aLong = rand.nextLong();
            aLongWrapper = rand.nextLong();
            aBoolean = rand.nextBoolean();

            byte[] randByte = new byte[20];
            rand.nextBytes(randByte);
            aString = new String(randByte);
            aDate = new Date();
        }

        public long getALong() {
            return aLong;
        }

        public void setALong(long aLong) {
            this.aLong = aLong;
        }

        public Long getALongWrapper() {
            return aLongWrapper;
        }

        public void setALongWrapper(Long aLongWrapper) {
            this.aLongWrapper = aLongWrapper;
        }

        public boolean getABoolean() {
            return aBoolean;
        }

        public void setABoolean(boolean aBoolean) {
            this.aBoolean = aBoolean;
        }

        public String getAString() {
            return aString;
        }

        public void setAString(String aString) {
            this.aString = aString;
        }

        public Date getADate() {
            return aDate;
        }

        public void setADate(Date aDate) {
            this.aDate = aDate;
        }
    }
}