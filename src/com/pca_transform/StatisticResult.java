package com.pca_transform;

import Jama.Matrix;

public class StatisticResult {
	public Matrix T2;
	public Matrix Q;
	public StatisticResult(Matrix T2,Matrix Q){
		this.T2=T2;
		this.Q=Q;
	}

}
