package com.pca_createDistributionRate;



import com.pca_transform.PCA;

import Jama.Matrix;

public class CreateT2DistributionRate {
	
	private double[] T2DistributionOf1D;
	private double[] T2DistributionRateOf1D;
	private double sumT2;
	private double maxT2DistributionRateOf1D;
	private int indexOfmaxT2DistributionRateOf1D;
	
	public CreateT2DistributionRate(Matrix test_data,PCA pca){
		Matrix v=pca.getEigenvectorsMatrix();
		int n=v.getRowDimension();
		assert test_data.getRowDimension()==1 && test_data.getColumnDimension()==n;
		
		int k=v.getColumnDimension();
		T2DistributionOf1D=new double[n];
		T2DistributionRateOf1D=new double[n];
		Matrix L=Matrix.identity(k, k);
		for(int i=0;i<k;i++){
			L.set(i, i, 1/pca.getEigenvalue(i));
		}
		Matrix E=Matrix.identity(n, n);
		for(int i=0;i<n; i++){
			Matrix E1=E.getMatrix(0, n-1, i, i);
			T2DistributionOf1D[i]=(test_data.times(v).times(L).times(v.transpose()).times(E1).times(E1.transpose()).times(test_data.transpose())).get(0, 0);
		}
		for(double a: T2DistributionOf1D){
			sumT2+=a;
		}
		for(int i=0;i<n;i++){
			T2DistributionRateOf1D[i]=T2DistributionOf1D[i]/sumT2;
		}
		TuplemaxT2DistributionRateOf1D tuple=new TuplemaxT2DistributionRateOf1D(T2DistributionRateOf1D);
		indexOfmaxT2DistributionRateOf1D=tuple.getIndex();
		maxT2DistributionRateOf1D=tuple.getmaxT2DistributionRateOf1D();
	}
	public double[] getT2DistributionRate(){
		return T2DistributionRateOf1D;
	}
	public int getIndexOfmax(){
		return indexOfmaxT2DistributionRateOf1D;
	}
	public double getmaxT2DistributionRateOf1D(){
		return maxT2DistributionRateOf1D;
	}
}
	class TuplemaxT2DistributionRateOf1D{
		private int index;
		private double maxT2DistributionRateOf1D=0.0;
		public TuplemaxT2DistributionRateOf1D(double[] array){
			for(int i=0;i<array.length;i++){
				if(maxT2DistributionRateOf1D<array[i]) {
					maxT2DistributionRateOf1D=array[i];
					index=i;
				}
			}		
		}
		public int getIndex(){
			return index;
		}
		public double getmaxT2DistributionRateOf1D(){
			return maxT2DistributionRateOf1D;
		}
	}
