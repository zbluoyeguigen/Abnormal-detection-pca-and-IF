package com.pca_transform;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


import Jama.Matrix;

public class DataReader {
	public static InputStream getResource(String filename){
		return DataReader.class.getClassLoader().getResourceAsStream(filename);	}
	public static Matrix read(InputStream inStream, boolean ignoreLastColumn,boolean ignoreFirstRow)
	        throws IOException {
	        DataInputStream in = new DataInputStream(inStream);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        return read(br, ignoreLastColumn,ignoreFirstRow);
	    }
	
	public static Matrix read(BufferedReader br, boolean ignoreLastColumn,boolean ignoreFirstRow)
	        throws IOException {

	        String line;
	        int lineNo = 0;
	        ArrayList<double[]> vectors = new ArrayList<double[]>();
	        while ((line = br.readLine()) != null) {
	            lineNo++;
	            if(ignoreFirstRow){
	            	if (lineNo == 1) {
	                continue;
	            	}
	            }
	            int commentIndex = line.indexOf('#');
	            if (commentIndex != -1) {
	                line.substring(0, commentIndex);
	            }
	            line = line.trim();
	           
	            if (line.length() == 0) {
	                continue;
	            }
	            String[] elems = line.split(",");
	            int elemsNo = elems.length;
	            if (ignoreLastColumn) {
	                elemsNo = elems.length - 1;
	            }
	            double[] vector = new double[elemsNo];
	            for (int i = 0; i < elemsNo; i++) {
	                vector[i] = Double.parseDouble(elems[i]);
	            }
	            vectors.add(vector);
	        }

	        double[][] vectorsArray = new double[vectors.size()][];
	        for (int r = 0; r < vectors.size(); r++) {
	            vectorsArray[r] = vectors.get(r);
	        }
	        Matrix m = new Matrix(vectorsArray);
	        return m;
	    }
}
