package com.asher_stern.crf.utilities;

import static com.asher_stern.crf.utilities.DoubleUtilities.*;

import java.math.BigDecimal;

/**
 * A collection of linear algebra functions over vectors.
 * 
 * @author Asher Stern
 * Date: Nov 7, 2014
 *
 */
public class VectorUtilities
{
	/**
	 * Returns the inner product of the two given vectors.
	 * @param rowVector
	 * @param columnVector
	 * @return
	 */
	public static BigDecimal product(BigDecimal[] rowVector, BigDecimal[] columnVector)
	{
		if (rowVector.length!=columnVector.length) throw new CrfException("Cannot multiply vector of different sizes.");
		BigDecimal ret = BigDecimal.ZERO;
		for (int i=0;i<rowVector.length;++i)
		{
			ret = safeAdd(ret, safeMultiply(rowVector[i], columnVector[i]));
		}
		return ret;
	}
	
	/**
	 * Returns a new vector which is the multiplication of the given vector by a scalar.
	 * @param scalar
	 * @param vector
	 * @return
	 */
	public static BigDecimal[] multiplyByScalar(BigDecimal scalar, BigDecimal[] vector)
	{
		BigDecimal[] ret = new BigDecimal[vector.length];
		for (int i=0;i<vector.length;++i)
		{
			ret[i] = safeMultiply(scalar, vector[i]);
		}
		return ret;
	}
	
	/**
	 * Returns the sum of the two vectors, as a new vector. For example [1,2]+[3,4] = [4,6].
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static BigDecimal[] addVectors(BigDecimal[] vector1, BigDecimal[] vector2)
	{
		if (vector1.length!=vector2.length) throw new CrfException("Cannot add two vectors of different sizes.");
		BigDecimal[] ret = new BigDecimal[vector1.length];
		for (int i=0;i<vector1.length;++i)
		{
			ret[i] = safeAdd(vector1[i], vector2[i]);
		}
		return ret;
	}
	
	/**
	 * Returns the substraction of the given two vectors. For example [10,20]-[5,6] = [5,14].
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static BigDecimal[] subtractVectors(BigDecimal[] vector1, BigDecimal[] vector2)
	{
		if (vector1.length!=vector2.length) throw new CrfException("Cannot substract vectors of difference sizes.");
		BigDecimal[] ret = new BigDecimal[vector1.length];
		for (int i=0;i<vector1.length;++i)
		{
			ret[i] = safeSubtract(vector1[i], vector2[i]);
		}
		return ret;
	}
	
	public static BigDecimal euclideanNormSquare(BigDecimal[] vector)
	{
		return product(vector, vector);
	}
	
//	public static double euclideanNorm(double[] vector)
//	{
//		return Math.sqrt(euclideanNormSquare(vector));
//	}
	
	
	/**
	 * Changes every infinity value in the array to Double.MAX_VALUE (or -Double.MAX_VALUE for negative infinity).
	 * @param array a given array. 
	 */
	@Deprecated
	public static final double[] changeInfinityToDoubleMax(final double[] array)
	{
		double[] ret = new double[array.length];
		for (int i=0; i<array.length; ++i)
		{
			if (Double.POSITIVE_INFINITY==array[i])
			{
				ret[i] = Double.MAX_VALUE;
			}
			else if (Double.NEGATIVE_INFINITY==array[i])
			{
				ret[i] = -Double.MAX_VALUE;
			}
			else
			{
				ret[i] = array[i];
			}
		}
		return ret;
	}

}
