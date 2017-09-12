package com.pca_transform.covmatrix;


import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class SVDBased implements CovarianceMatrixEVDCalculate {

	@Override
	public EVDResult run(Matrix centeredData) {
		int m = centeredData.getRowDimension();
		int n = centeredData.getColumnDimension();
		SingularValueDecomposition svd = centeredData.svd();
		double[] singularValues = svd.getSingularValues();
		Matrix d = Matrix.identity(n, n);
		for(int i = 0; i < n; i++){
			
			double val;
			if(i < m) val = singularValues[i];
			else val = 0;
			
			d.set(i, i, 1.0/(m-1) * Math.pow(val, 2));
		}
		Matrix v = svd.getV();
		return new EVDResult(d, v);//d为A协方差矩阵的特征值矩阵,v是A'A的特征向量
	}

}
