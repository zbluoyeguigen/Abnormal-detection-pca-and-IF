package com.pca_transform.covmatrix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class EVD implements Serializable{

	private static final long serialVersionUID = 4463222698415876303L;
	public final Matrix v;
	public final Matrix d;
	
	public EVD(Matrix m){
		EigenvalueDecomposition evd = m.eig();

		double[] diagonal = getDiagonal(evd.getD());
		PermutationResult result= calculateNondecreasingPermutation(diagonal);
		int[] permutation = result.permutation;
		double[] newDiagonal = result.values;
		this.v = permutateColumns(evd.getV(), permutation);
		this.d = createDiagonalMatrix(newDiagonal);
		assert eigenvaluesAreNonIncreasing(this.d);
	}
	//�������ֵ
	private static double[] getDiagonal(Matrix m){
		assert m.getRowDimension()==m.getColumnDimension();
		double[] diag = new double[m.getRowDimension()];
		for(int i = 0; i < m.getRowDimension(); i++)
			diag[i] = m.get(i, i);
		return diag;
	}
	//����ֵ��С�Ӵ�С���У����ұ���ԭ�ȶ�Ӧ���
	private static PermutationResult calculateNondecreasingPermutation(
			double[] vals){
		ArrayList<ValuePlace> list = new ArrayList<ValuePlace>();
		for(int i = 0; i < vals.length; i++) 
			list.add(new ValuePlace(i,vals[i]));
		Collections.sort(list);
		double[] newVals = new double[vals.length];
		int[] permutation = new int[vals.length];
		for(int i = 0; i < vals.length; i++){
			newVals[i] = list.get(i).value;
			permutation[i] = list.get(i).place;
		}
		return new PermutationResult(permutation, newVals);
	}
	//���к������ֵ��Ӧ����������
	private static Matrix permutateColumns(Matrix m, int[] permutation){
		assert m.getColumnDimension()==permutation.length;
		
		Matrix newM = new Matrix(m.getRowDimension(), m.getColumnDimension());
		for(int c = 0; c < newM.getColumnDimension(); c++){
			int copyFrom = permutation[c];
			for(int r = 0; r < newM.getRowDimension(); r++){
				newM.set(r, c, m.get(r, copyFrom));
			}
		}
		return newM;
	}
	//�����ԽǾ���
	private static Matrix createDiagonalMatrix(double[] diagonal){
		Matrix m = new Matrix(diagonal.length, diagonal.length);
		for(int i = 0; i < diagonal.length; i++) m.set(i, i, diagonal[i]);
		return m;
	}
	//�ж�����ֵ���ǵ�����
	private static boolean eigenvaluesAreNonIncreasing(Matrix d){
		for(int i = 0; i < d.getRowDimension()-1; i++)
			if(d.get(i, i) < d.get(i+1, i+1)) return false;
		return true;
	}
}

class PermutationResult {
	public int[] permutation;
	public double[] values;
	
	public PermutationResult(int[] permutation, double[] values){
		this.permutation = permutation;
		this.values = values;
	}
}
class ValuePlace implements Comparable<ValuePlace>{
	
	public double value;
	public int place;
	public ValuePlace(int place,double value) {
		// TODO �Զ����ɵĹ��캯�����
		this.value=value;
		this.place=place;
	}
	@Override
	public int compareTo(ValuePlace o) {
		// TODO �Զ����ɵķ������
		if(value<o.value) return 1;
		else if (value==o.value) {
			return 0;
		}
		else return -1;
	}
}
