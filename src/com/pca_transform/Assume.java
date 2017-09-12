package com.pca_transform;
//如果没有达到给定条件，报出异常
public class Assume {
	public static void assume(boolean expression){
		assume(expression, "");
	}
	public static void assume(boolean expression,String comment){
		if(!expression){
			throw new RuntimeException(comment);
		}
	}
}
