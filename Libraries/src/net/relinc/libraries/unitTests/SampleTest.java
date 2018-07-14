package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import net.relinc.libraries.sample.CompressionSample;
import net.relinc.libraries.staticClasses.SPOperations;

public class SampleTest extends BaseTest{
	//TODO: Testing (for customers?) (ha)

	@Test
	public void checkSampleCreatedTest() {
		
		CompressionSample compressionSample = new CompressionSample();
		compressionSample.setName("jUnit Test");
		compressionSample.setDensity(1);
		compressionSample.setDiameter(2);
		compressionSample.setYoungsModulus(3);
		compressionSample.setHeatCapacity(4);
		File samplePath = new File(TestingSettings.testingOutputLocation.getPath() + "/WhereIsThis.samcomp");
		compressionSample.writeSampleToFile(samplePath.getPath());
		
		File file = new File(TestingSettings.testingOutputLocation.getPath() + "/WhereIsThis.samcomp");
		assertTrue("Create Zip File Success", file.exists());
		
		CompressionSample c = null;
		try {
			c = (CompressionSample)SPOperations.loadSample(samplePath.getPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(c.getName(), "jUnit Test");
		assertTrue(c.getDensity() == 1.0);
		assertTrue(c.getDiameter() == 2.0);
		assertTrue(c.getYoungsModulus() == 3.0);
		assertTrue(c.getHeatCapacity() == 4.0);
		
		
		if(file.exists())
			file.delete();
	}
	

	
}
