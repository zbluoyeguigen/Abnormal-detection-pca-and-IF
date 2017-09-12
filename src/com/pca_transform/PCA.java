package com.pca_transform;

import java.util.function.Function;


import com.pca_transform.covmatrix.CovarianceMatrixEVDCalculate;
import com.pca_transform.covmatrix.EVDBased;
import com.pca_transform.covmatrix.EVDResult;
import com.pca_transform.covmatrix.SVDBased;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public final class PCA {
	public enum TransformationType{ROTATION ,WHITENING};
	private boolean centerMatrix;
	private final int inputDim;
	private final Matrix whiteningTransformation;
	private final Matrix pcaRotationTransformation;
	private final Matrix v;
	private final Matrix zerosRotationTransformation;
	private final Matrix d;
	private final double[] means;
	private final double[] std;
	private final double T2;
	private final double Q;
	
	public PCA(Matrix data){
		this(data,new EVDBased(),true);
	}
	
	public PCA(Matrix data, boolean center){
		this(data, new EVDBased(), center);
	}
	
	public PCA(Matrix data,CovarianceMatrixEVDCalculate  evdCalc){
		this(data, evdCalc, true);
	}
	
	public PCA(Matrix data,CovarianceMatrixEVDCalculate EVDCalc,boolean center){
		this.centerMatrix=center;
		this.inputDim = data.getColumnDimension();
		this.means = getColumnsMeans(data);
		this.std=getColumnsStd(data, means);
		Matrix centeredData = new Matrix(data.getRowDimension(),data.getColumnDimension());
		if(centerMatrix){
			centeredData = ZScoreColumns(shiftColumns(data, means), std);
		}
		
		
		EVDResult evd = EVDCalc.run(centeredData);
			
		EVDWithThreshold evdT = new EVDWithThreshold(evd);
		
		this.d = evdT.getDAboveThreshold();
		this.v = evdT.getVAboveThreshold();
		StatisticsThreshold ST=new StatisticsThreshold(evd,data.getRowDimension(),d.getColumnDimension());
		T2=ST.getT2threshold();
		Q=ST.getQthreshold();
		
		this.zerosRotationTransformation = evdT.getVBelowThreshold();
		
		
		Matrix sqrtD = sqrtDiagonalMatrix(d);
		Matrix scaling = inverseDiagonalMatrix(sqrtD);
		
		this.pcaRotationTransformation = v;
		this.whiteningTransformation = this.pcaRotationTransformation.times(scaling);
	}
	
	//获取特征向量
	public Matrix getEigenvectorsMatrix(){
		return v;
	}
	//获取协方差矩阵的特征值
	public double getEigenvalue(int dimNo){
		return d.get(dimNo, dimNo);
	}
	//输入数据的特征维数
	public int getInputDimsNo(){
		return inputDim;
	}
	//输出数据的特征维数
	public int getOutputDimsNo(){
		return v.getColumnDimension();
	}
	
	public double getT2(){
		return T2;
	}
	public double getQ(){
		return Q;
	}
	//PCA变换或者Whitening变换后的矩阵
	public Matrix transform(Matrix data, TransformationType type){
		Matrix centeredData = data;
		if(centerMatrix){
			centeredData = ZScoreColumns(shiftColumns(centeredData, means), std);
		}
		Matrix transformation = getTransformation(type); 
		return centeredData.times(transformation);
	}
	//获得转换矩阵
	private Matrix getTransformation(TransformationType type){
		switch(type){
		case ROTATION: return pcaRotationTransformation;//PCA变换
		case WHITENING: return  whiteningTransformation;//Whitening变换
		default: throw new RuntimeException("Unknown enum type: "+type);
		}
	}
	//取得每列平均值
	private static double[] getColumnsMeans(Matrix m){
		double[] means = new double[m.getColumnDimension()];
		for(int c = 0; c < m.getColumnDimension(); c++){
			double sum = 0;
			for(int r = 0; r < m.getRowDimension(); r++)
				sum += m.get(r, c);
			means[c] = sum/m.getRowDimension();
		}
		return means;
	}
	//将矩阵每列的平均值为0
	private static Matrix shiftColumns(Matrix data, double[] shifts){
		Assume.assume(shifts.length==data.getColumnDimension());
		Matrix m = new Matrix(
				data.getRowDimension(), data.getColumnDimension());
		for(int c = 0; c < data.getColumnDimension(); c++)
			for(int r = 0; r < data.getRowDimension(); r++)
				m.set(r, c, data.get(r, c) - shifts[c]);
		return m;		
	}
	//取得每列的标准差
	private static double[] getColumnsStd(Matrix m,double[] shifts){
		Assume.assume(shifts.length==m.getColumnDimension());
		double[] std=new double[m.getColumnDimension()];
		for(int c=0;c<m.getColumnDimension();c++){
			double sum=0;
			for(int r=0;r<m.getRowDimension();r++){
				sum=sum+Math.pow(m.get(r, c)-shifts[c], 2.0);
			}
			std[c]=Math.sqrt(sum/(m.getRowDimension()-1));
		}
		return std;
	}
	//标准化数据
	private static Matrix ZScoreColumns(Matrix data,double[] std){
		Assume.assume(std.length==data.getColumnDimension());
		Matrix s=new Matrix(data.getRowDimension(),data.getColumnDimension());
		for(int c=0;c<data.getColumnDimension();c++)
			for(int r=0;r<data.getRowDimension();r++){
				if(std[c]==0) s.set(r, c, 0);
				else{
				s.set(r, c, data.get(r, c)/std[c]);
				}
			}
		return s;
	}
	//对角线矩阵
	private static Matrix sqrtDiagonalMatrix(Matrix m){
		assert m.getRowDimension()==m.getColumnDimension();
		Matrix newM = new Matrix(m.getRowDimension(), m.getRowDimension());
		for(int i = 0; i < m.getRowDimension(); i++)
			newM.set(i, i, Math.sqrt(m.get(i, i)));
		return newM;
	}
	//逆对角矩阵
	private static Matrix inverseDiagonalMatrix(Matrix m){
		assert m.getRowDimension()==m.getColumnDimension();
		Matrix newM = new Matrix(m.getRowDimension(), m.getRowDimension());
		for(int i = 0; i < m.getRowDimension(); i++)
			newM.set(i, i, 1/m.get(i, i));
		return newM;
	}
	//计算测试数据每个样本的T2和Q
	public StatisticResult CalculateStatistics(Matrix data,boolean NewCenterMatix){
		int m=data.getRowDimension();
		int n=data.getColumnDimension();
		Matrix stddata=new Matrix(m, n);
		Matrix T2=new Matrix(1,m);
		Matrix Q=new Matrix(1,m);
		
		
		if(!NewCenterMatix){
			stddata = ZScoreColumns(shiftColumns(data, means), std);
		}
		else{
			double[] means_testdata=getColumnsMeans(data);
			double[] stds_testdata=getColumnsStd(data, means_testdata);
			stddata = ZScoreColumns(shiftColumns(data, means_testdata), stds_testdata);
		}
		for(int i=0;i<data.getRowDimension();i++){
			T2.setMatrix(0, 0, i, i,stddata.getMatrix(i, i, 0, n-1).times(v).times(inverseDiagonalMatrix(d)).times(v.transpose()).times(stddata.getMatrix(i, i, 0, n-1).transpose()));
			Q.setMatrix(0, 0, i, i, stddata.getMatrix(i, i, 0, n-1).times(Matrix.identity(n, n).minus(v.times(v.transpose()))).times(Matrix.identity(n, n).minus(v.times(v.transpose()))).times(stddata.getMatrix(i, i, 0, n-1).transpose()));
		}
		StatisticResult SR=new StatisticResult(T2, Q);
		return SR;
	}
	
}
class EVDWithThreshold {
	
	public static final double precision = 0.8;

	private final EVDResult evd;
	private final double threshold;
	
	
	public EVDWithThreshold(EVDResult evd){
		this(evd, precision);
	}
	
	public EVDWithThreshold(EVDResult evd, double InputPrecision){
		this.evd = evd;
		this.threshold = InputPrecision;
	}
	
	
	public double getThreshold(){
		return threshold;
	}
	//取得大于阈值的特征值对应的对角向量
	public Matrix getDAboveThreshold(){
		int aboveThresholdElemsNo = getElementsNoAboveThreshold();
		Matrix newD = evd.d.getMatrix(0, aboveThresholdElemsNo-1, 
				0, aboveThresholdElemsNo-1);
		return newD;
	}
	//取得大于阈值的特征值对应的特征向量
	public Matrix getVAboveThreshold(){
		return evd.v.getMatrix(0, evd.v.getRowDimension()-1, 
				0, getElementsNoAboveThreshold()-1);
	}
	//取得小于阈值的特征值对应的特征向量
	public Matrix getVBelowThreshold(){
		return evd.v.getMatrix(0, evd.v.getRowDimension()-1,
				getElementsNoAboveThreshold(), evd.v.getColumnDimension()-1);
	}
	//返回第一个特征值小于阈值的序号
	private int getElementsNoAboveThreshold(){
		double sum=0.0;double t=0.0;
		for(int i = 0; i < evd.d.getColumnDimension(); i++){
			sum+=evd.d.get(i, i);
			
		}
		for(int i = 0; i < evd.d.getColumnDimension(); i++){
			t+=evd.d.get(i, i);
			if (t/sum>precision) 
				return i+1;
		}
		return evd.d.getColumnDimension();
	}
}
class StatisticsThreshold{
	private final EVDResult evd;
	private double T2threshold;
	private double Qthreshold;
	private final int m;
	private final int n;
//	private final double p=0.99;
	
	//finv(p,i,m-i):i is the ith dimension, m is the number of trainingdata
	private enum finv{
//		finv1(6.637430388954019),finv2(4.607292020974902),finv3(3.783579481695577),finv4(3.321048872788440),finv5(3.019079387702405),finv6(2.803778924173294),finv7(2.641109692014191),finv8(2.513050612841722);
		finv1(6.647584581345885),finv2(4.615800925464531),finv3(3.791432893717339),finv4(3.328567732636136),finv5(3.026409601785578),finv6(2.810999238372253),finv7(2.648267317399349),finv8(2.520175939686569),
		finv9(2.416213789586881),finv10(2.329808828389694),finv11(2.256624030390007),finv12(2.193672202438761),finv13(2.138823095375115),finv14(2.090513160162702),finv15(2.047566583729964),finv16(2.009080600886392),
		finv17(1.974349545959002),finv18(1.942813116847724),finv19(1.914020254531433);
		private double value;
		private finv(double value){
			this.value=value;
		}
		public double getValue(){return this.value;}
		}
	public StatisticsThreshold(EVDResult evd,int m,int n){
		this.evd=evd;
		this.m=m;//样本数
		this.n=n;//主成分数
		T2Threshold();
		QThreshold();
	}
	
	public double getT2threshold(){
		return T2threshold;
	}
	
	public double getQthreshold(){
		return Qthreshold;
	}
//	//龙贝格求微积分
//	private double simpson(double a,double b,Function<Double,Double> df){
//		double c=(a+b)/2;
//		return (df.apply(a)+4*df.apply(c)+df.apply(b))*(a-b)/6;
//	}
//	private  double simpsonRule(double upper,double lower,double eps,Function<Double,Double> df,double hub){
//		double middle=(upper+lower)/2;
//		double L=simpson(upper, middle, df),R=simpson(middle, lower, df);
//		if(hub==-1.0){
//			hub=simpson(upper, lower, df);
//		}
//		if(Math.abs(hub-L-R)<=15*eps) return L+R+(hub-L-R)/15;
//		return simpsonRule(upper, middle, eps/2,df, L)+simpsonRule(middle, lower, eps/2, df, R);
//	}
	//计算不完全的贝塔函数
//	private double betainc(double x,double z,double w){
//		double B=simpsonRule(1.0, 0.0, 1e-8, t->{
//			if(t==0.0) return 0.0;
//			else if (t==1.0) {
//				return 1.0;
//			}
//			else return Math.pow(t, z-1)*Math.pow(1-t,w-1);},-1.0);
//		double I=1/B*simpsonRule(x, 0.0, 1e-15, t->{
//			if(t==0.0) return 0.0;
//			else if (t==1.0) {
//				return 1.0;
//			}
//		else return Math.pow(t, z-1)*Math.pow(1-t,w-1);},-1.0);
//		return I;
//	}
//	//计算不完全贝塔函数的反函数
//	private double betaincinv(double y,double z,double w){
//		assert y<=1.0 && y>=0.0;
//		double bottom=betainc(0.0, z, w);
//		double top=betainc(1.0, z, w);
//		double middle;
//		double i=1.0;
//		double bottom_x=0.0;
//		double top_x=1.0;
//		if (bottom-y==0){
//			return 0.0;
//		}
//		if(top-y==0.0){
//			return 1.0;
//		}
//		assert z<w;
//		do {
//			i*=0.5;
//			middle=betainc(bottom_x+i, z, w);
//			if(middle>y){
//				top=middle;
//				top_x=bottom_x+i;
//			}
//			else if (middle<y) {
//				bottom=middle;
//				bottom_x=bottom_x+i;
//			}
//			else{
//				return bottom_x;
//			}
//			
//		} while (Math.abs(middle-y)>=5e-16);
//		
//		return bottom_x+i;
//	}
//	//计算F累计分布函数
//	private double finv(double p,int v1,int v2){
//		assert v1>0 && v2>0;
//		double t=0.0,z=0.0,x=0.0;
//		if(p>1-v1/v2){
//			z=betaincinv(1-p, v2/2.0, v1/2.0);
//			t=(1-z)/z;
//		}
//		else {
//			z=betaincinv(p, v1/2.0, v2/2.0);
//			t=z/(1-z);
//		}
//		x= t * v2 / v1;
//		return x;
//	}
	//计算的T2统计
	private void T2Threshold(){
		switch (n) {
		case 1:
			this.T2threshold= finv.finv1.getValue()*n*(m-1)/(m-n);
			break;
		case 2:
			this.T2threshold= finv.finv2.getValue()*n*(m-1)/(m-n);
			break;
		case 3:
			this.T2threshold= finv.finv3.getValue()*n*(m-1)/(m-n);
			break;
		case 4:
			this.T2threshold= finv.finv4.getValue()*n*(m-1)/(m-n);
			break;
		case 5:
			this.T2threshold= finv.finv5.getValue()*n*(m-1)/(m-n);
			break;	
		case 6:
			this.T2threshold= finv.finv6.getValue()*n*(m-1)/(m-n);
			break;
		case 7:
			this.T2threshold= finv.finv7.getValue()*n*(m-1)/(m-n);
			break;
		case 8:
			this.T2threshold= finv.finv8.getValue()*n*(m-1)/(m-n);
			break;
		case 9:
			this.T2threshold= finv.finv9.getValue()*n*(m-1)/(m-n);
			break;
		case 10:
			this.T2threshold= finv.finv10.getValue()*n*(m-1)/(m-n);
			break;
		case 11:
			this.T2threshold= finv.finv11.getValue()*n*(m-1)/(m-n);
			break;
		case 12:
			this.T2threshold= finv.finv12.getValue()*n*(m-1)/(m-n);
			break;
		case 13:
			this.T2threshold= finv.finv13.getValue()*n*(m-1)/(m-n);
			break;
		case 14:
			this.T2threshold= finv.finv14.getValue()*n*(m-1)/(m-n);
			break;
		case 15:
			this.T2threshold= finv.finv15.getValue()*n*(m-1)/(m-n);
			break;
		case 16:
			this.T2threshold= finv.finv16.getValue()*n*(m-1)/(m-n);
			break;
		case 17:
			this.T2threshold= finv.finv17.getValue()*n*(m-1)/(m-n);
			break;
		case 18:
			this.T2threshold= finv.finv18.getValue()*n*(m-1)/(m-n);
			break;
		case 19:
			this.T2threshold= finv.finv19.getValue()*n*(m-1)/(m-n);
			break;
		default:
			break;
		}
		
	}
	
	//计算Q统计量
	private void QThreshold(){
		final double NormalInv=2.326347874040841;
		double delta_q;
		double[] theta=new double[3];
		for(int j=0;j<3;j++)
			for(int k=n+1;k<=evd.d.getColumnDimension();k++){
				theta[j]+=Math.pow(evd.d.get(k-1, k-1), j+1);
			}
		double h=1-2*theta[0]*theta[2]/3/Math.pow(theta[1], 2);
		if(h==Double.NaN){
			delta_q=Double.NaN;
		}
		else{
			delta_q=theta[0] * Math.pow(NormalInv * h*Math.pow(2 * theta[1], 0.5) / theta[0] + 1 + theta[1] * h * (h - 1) / Math.pow(theta[0], 2), 1/h);
		}
			
		
		
		this.Qthreshold=delta_q;
	}
}
