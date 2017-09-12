package com.pca_transform;

import java.util.Random;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class CreateNormalMatrix {
	public Matrix data;
	public CreateNormalMatrix(int m){
		data=create_data(m);
	}
	
	private Matrix gaussian(int m,int n){
		
		Matrix A=new Matrix(m,n);
		for(int j=0;j<n;j++)
		{
			Random random=new Random();
			for(int i=0;i<m;i++){
				
				A.set(i, j,random.nextGaussian() );
			}
		}
		return A;
	} 
	private Matrix orth(Matrix A){
		SingularValueDecomposition svd=new SingularValueDecomposition(A);
		return svd.getV();
	}
	private Matrix create_data(int m){
		Matrix a=orth(gaussian(8, 8));
		Matrix A=a.getMatrix(0, 7, 0, 3);
		Matrix s=gaussian(4, m);
		Matrix E=gaussian(8, m);
		E=E.transpose().times(0.2);
		Matrix data=(A.times(s)).transpose().plus(E);
		return data;
	}
}
