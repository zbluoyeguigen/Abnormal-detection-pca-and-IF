package com.pca_transform;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import org.junit.Test;

import Jama.Matrix;
import junit.framework.TestCase;

public class DataReaderTest extends TestCase{

	@Test
	public void test() throws IOException{
		String resource="test 1.csv";
		
		BufferedReader br=new BufferedReader(new InputStreamReader(DataReader.getResource(resource)));
		Matrix read=DataReader.read(br, false ,false);
		
		assertNotNull(read);
		assertEquals(1025, read.getRowDimension());
	}

}
