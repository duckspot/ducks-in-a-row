/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.duckspot.diar.model;

import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Dobson
 */
public class UtilTest {
    
    public UtilTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }    

    @Test
    public void testParseTime() {
        System.out.println("parseTime");
        assertEquals(0, Util.parseTime("12:00a"));
        assertEquals(60, Util.parseTime("1:00"));
        assertEquals(12*60, Util.parseTime("12:00"));
        assertEquals(12*60+30, Util.parseTime("12:30p"));
        assertEquals(-1, Util.parseTime("1230p"));
    }    
}