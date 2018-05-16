package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.Gson;

import net.relinc.libraries.application.StrikerBar;

public class StrikerBarTests extends BaseTest{
	@Test
	public void testSaveAndLoadStrikerBar(){
		StrikerBar sb = new StrikerBar();
		sb.setDensity(1);
		sb.setDiameter(2);
		sb.setLength(3);
		sb.setSpeed(4);
		String file = sb.getStringForFile();
		
		StrikerBar loadBar = new Gson().fromJson(file, StrikerBar.class);
		assertTrue(loadBar.getDensity() == 1.0);
		assertTrue(loadBar.getDiameter() == 2.0);
		assertTrue(loadBar.getLength() == 3.0);
		assertTrue(loadBar.getSpeed() == 4.0);
		assertTrue(loadBar.isValid());
		assertTrue(loadBar.isFullySpecified());
		
		StrikerBar sb2 = new StrikerBar();
		sb2.setDensity(1);
		assertTrue(sb2.isValid());
		assertTrue(!sb2.isFullySpecified());
		
		StrikerBar sb3 = new StrikerBar();
		
		assertTrue(!sb3.isValid());
		assertTrue(!sb3.isFullySpecified());
	}
	
}
