package com.pca_createDistributionRate;

import com.pca_transform.PCA;

import Jama.Matrix;

public class CreateQDistributionRate {
	private double[] QDistributionOf1D;
	private double[] QDistributionRateOf1D;
	private double sumQ;
	private double maxQDistributionRateOf1D;
	private int indexOfmaxQDistributionRateOf1D;
	
	
	public CreateQDistributionRate(PCA pca,Matrix test_data) {
		// TODO 自动生成的构造函数存根
		Matrix v=pca.getEigenvectorsMatrix();
		int n=v.getRowDimension();
		assert test_data.getRowDimension()==1 && test_data.getColumnDimension()==n;
		
		QDistributionOf1D=new double[n];
		QDistributionRateOf1D=new double[n];
		Matrix I=Matrix.identity(n, n);
		Matrix e=test_data.times(I.minus(v.times(v.transpose())));
		Matrix e2=e.arrayTimesEquals(e);
		QDistributionOf1D=e2.getRowPackedCopy();
		
		for(double a: QDistributionOf1D){
			sumQ+=a;
		}
		for(int i=0;i<n;i++){
			QDistributionRateOf1D[i]=QDistributionOf1D[i]/sumQ;
		}
		TuplemaxQDistributionRateOf1D tuple=new TuplemaxQDistributionRateOf1D(QDistributionRateOf1D);
		indexOfmaxQDistributionRateOf1D=tuple.getIndex();
		maxQDistributionRateOf1D=tuple.getmaxQDistributionRateOf1D();
	}
	public double[] getQDistributionRate(){
		return QDistributionRateOf1D;
	}
	public int getIndexOfmax(){
		return indexOfmaxQDistributionRateOf1D;
	}
	public double getmaxQDistributionRateOf1D(){
		return maxQDistributionRateOf1D;
	}
}
class TuplemaxQDistributionRateOf1D{
	private int index;
	private double maxQDistributionRateOf1D=0.0;
	public TuplemaxQDistributionRateOf1D(double[] array){
		for(int i=0;i<array.length;i++){
			if(maxQDistributionRateOf1D<array[i]) {
				maxQDistributionRateOf1D=array[i];
				index=i;
			}
		}		
	}
	public int getIndex(){
		return index;
	}
	public double getmaxQDistributionRateOf1D(){
		return maxQDistributionRateOf1D;
	}
}
