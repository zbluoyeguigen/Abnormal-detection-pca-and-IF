package com.util;

public class ToOneDimensionArray {
	public static double[] Two2One(double[][] array2){
		int length=0;
		for(double[] m:array2){
			length+=m.length;
		}
		double[] array1=new double[length];
		int i=0;
		for(double[] m:array2)
			for(double n:m){
				array1[i++]=n;
			}
		return array1;
	}
}