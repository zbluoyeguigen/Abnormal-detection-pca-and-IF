package com.pca_transform.covmatrix;


import Jama.Matrix;

public interface CovarianceMatrixEVDCalculate {
	public EVDResult run(Matrix centeredData);
}
