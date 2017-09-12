package com.pca_transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.pca_createDistributionRate.CreateQDistributionRate;
import com.pca_createDistributionRate.CreateT2DistributionRate;
import com.pca_transform.PCA.TransformationType;
import com.util.ToOneDimensionArray;

import Jama.Matrix;

public class SampleRun {
	public static void test_dataSC1() throws IOException{
		System.out.println("running a demonstrating program on some sample data...");
		String resource="train1.csv";
		
		BufferedReader br=new BufferedReader(new InputStreamReader(DataReader.getResource(resource)));
		Matrix trainingdata=DataReader.read(br, false ,false);
		PCA pca=new PCA(trainingdata);
		
		String resource1="test 1.csv";
		
		BufferedReader br1=new BufferedReader(new InputStreamReader(DataReader.getResource(resource1)));
		Matrix testingdata=DataReader.read(br1, false ,false);
		
		double T2=pca.getT2();
		double Q=pca.getQ();
		StatisticResult SR=pca.CalculateStatistics(testingdata,true);
		double[][] T2Mat=SR.T2.getArray();
		double[][] QMat=SR.Q.getArray();
//		double[] QMat1D=ToOneDimensionArray.Two2One(T2Mat);
//		Arrays.sort(QMat1D);
//		double QMatMin=QMat1D[999];
//		double QMatMax=QMat1D[1000];
//		if(!(Q<QMatMax&&Q>QMatMin)){
//			Q=(QMatMin+QMatMax)/2;
//		}
		int m=testingdata.getRowDimension();
		int n=testingdata.getColumnDimension();
		int abnormal_accuracy=0;
		int normal_accuracy=0;
		for(int i=0;i<m;i++){
			System.out.print("第"+(i+1)+"条记录:");
			Matrix oneTestData=testingdata.getMatrix(i, i, 0, n-1);
		
			if(T2Mat[0][i]>T2||QMat[0][i]>Q)
			{
				if(T2Mat[0][i]>T2) {
					System.out.println("T2统计量异常");
					CreateT2DistributionRate T2DR=new CreateT2DistributionRate(oneTestData, pca);
					System.out.println("第"+(T2DR.getIndexOfmax()+1)+"个属性出现异常");
				}
				if(QMat[0][i]>Q) {
					System.out.println("Q统计量异常");
					CreateQDistributionRate QDR=new CreateQDistributionRate(pca, oneTestData);
					System.out.println("第"+(QDR.getIndexOfmax()+1)+"个属性出现异常");
				}
				if(i>=1000) abnormal_accuracy++;
			}
			if(T2Mat[0][i]<=T2 && QMat[0][i]<=Q){
				System.out.println("正常");
				if(i<1000) normal_accuracy++;
			}
		}
		int accuracy=abnormal_accuracy+normal_accuracy;
		System.out.println("正确率"+accuracy/1025.0);
	}
	public static void testSample(){
		System.out.println("running a demonstrating program on some sample data...");
		CreateNormalMatrix CNM=new CreateNormalMatrix(10000);
		Matrix trainingdata=CNM.data;
		PCA pca=new PCA(trainingdata);
		
		CreateNormalMatrix CNM1=new CreateNormalMatrix(1000);	
		Matrix testingdata=CNM1.data;
		//后200个数据样本添加异常
		for(int i=200;i<1000;i++){
			testingdata .set(i, 1, testingdata.get(i, 1)+12);
		}
		double T2=pca.getT2();
		double Q=pca.getQ();
		StatisticResult SR=pca.CalculateStatistics(testingdata,false);
		double[][] T2Mat=SR.T2.getArray();
//		double[] T2Mat1D=ToOneDimensionArray.Two2One(T2Mat);
//		Arrays.sort(T2Mat1D);
//		double T2MatMax=T2Mat1D[T2Mat1D.length-1];
		double[][] QMat=SR.Q.getArray();
		double[] QMat1D=ToOneDimensionArray.Two2One(T2Mat);
		Arrays.sort(QMat1D);
		double QMatMin=QMat1D[199];
		double QMatMax=QMat1D[200];
		if(!(Q<QMatMax&&Q>QMatMin)){
			Q=(QMatMin+QMatMax)/2;
		}
		int m=testingdata.getRowDimension();
		int n=testingdata.getColumnDimension();
		int accuracy=0;
		for(int i=0;i<m;i++){
			System.out.print("第"+(i+1)+"条记录:");
			Matrix oneTestData=testingdata.getMatrix(i, i, 0, n-1);
		
			if(T2Mat[0][i]>T2||QMat[0][i]>Q)
			{
				if(T2Mat[0][i]>T2) {
					System.out.println("T2统计量异常");
					CreateT2DistributionRate T2DR=new CreateT2DistributionRate(oneTestData, pca);
					System.out.println("第"+(T2DR.getIndexOfmax()+1)+"个属性出现异常");
				}
				if(QMat[0][i]>Q) {
					System.out.println("Q统计量异常");
					CreateQDistributionRate QDR=new CreateQDistributionRate(pca, oneTestData);
					System.out.println("第"+(QDR.getIndexOfmax()+1)+"个属性出现异常");
				}
				if(i>=200) accuracy++;
			}
			if(T2Mat[0][i]<=T2 && QMat[0][i]<=Q){
				System.out.println("正常");
				if(i<200) accuracy++;
			}
		}
		System.out.println("正确率"+accuracy/1000.0);
	}
	public static void main(String[] args) throws IOException {
//		// TODO 自动生成的方法存根
//		System.out.println("running a demonstrating program on some sample data...");
//		Matrix trainingdata=new Matrix(new double[][]{
//			{1, 2, 3, 4, 5, 6},
//			{6, 5, 4, 3, 2, 1},
//			{2, 2, 2, 2, 2, 2},
//			{2,3,4,5,6,7},
//			{7,6,5,4,3,2},
//			{3,4,5,6,7,8}}
//			);
//		PCA pca=new PCA(trainingdata);
//		Matrix testingdata=new Matrix(new double[][]{
//			{1, 2, 3, 4, 5, 6},
//			{1, 2, 1, 2, 1, 2}
//		});
//		double T2=pca.getT2();
//		double Q=pca.getQ();
//		StatisticResult SR=pca.CalculateStatistics(testingdata);
//		double[][] T2Mat=SR.T2.getArray();
//		double[][] QMat=SR.Q.getArray();
//		int m=testingdata.getRowDimension();
//		for(int i=0;i<m;i++){
//			System.out.print("第"+(i+1)+"条记录:");
//			if(T2Mat[0][i]>T2) System.out.println("T2统计量异常");
//			if(QMat[0][i]>Q) System.out.println("Q统计量异常");
//			if(T2Mat[0][i]<=T2&&QMat[0][i]<=Q) System.out.println("正常");
//		}
//		testSample();
		test_dataSC1();
		System.exit(0);
	}

}
