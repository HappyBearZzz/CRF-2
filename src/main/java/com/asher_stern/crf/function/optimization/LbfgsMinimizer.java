package com.asher_stern.crf.function.optimization;

import java.util.ArrayList;
import static com.asher_stern.crf.utilities.DoubleUtilities.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.asher_stern.crf.function.DerivableFunction;
import com.asher_stern.crf.utilities.CrfException;
import com.asher_stern.crf.utilities.DerivableFunctionWithLastCache;
import com.asher_stern.crf.utilities.VectorUtilities;

/**
 * Implementation of L-BFGS algorithm for minimizing a function.
 * <BR>
 * L-BFGS stands for "Limited memory BFGS", where "BFGS" is an acronym of
 * "Broyden Fletcher Goldfarb Shanno" who developed the BFGS algorithm.
 * <BR>
 * The BFGS algorithm approximates Newton method for optimization, by approximating
 * the inverse of the Hessian without calculating the exact Hessian.
 * The L-BFGS algorithm approximates the BFGS algorithm by approximating calculations
 * that are performed with the inverse of the Hessian, but stores neither the
 * inverse of the Hessian nor its approximation in the memory.
 * Thus the L-BFGS algorithm is much cheaper in space complexity notion.
 * <BR>
 * The L-BFGS algorithm is described in the book "Numerical Optimization" by Jorge Nocedal and Stephen J. Wright,
 * Chapter 9. The book can be downloaded from http://www.bioinfo.org.cn/~wangchao/maa/Numerical_Optimization.pdf 
 *  
 *  
 * 
 * @author Asher Stern
 * Date: Nov 7, 2014
 *
 */
public class LbfgsMinimizer extends Minimizer<DerivableFunction>
{
	public static final int DEFAULT_NUMBER_OF_PREVIOUS_ITERATIONS_TO_MEMORIZE = 20;
	public static final double DEFAULT_GRADIENT_CONVERGENCE = 0.01;

	public LbfgsMinimizer(DerivableFunction function)
	{
		this(function,DEFAULT_NUMBER_OF_PREVIOUS_ITERATIONS_TO_MEMORIZE, DEFAULT_GRADIENT_CONVERGENCE);
	}

	public LbfgsMinimizer(DerivableFunction function, int numberOfPreviousIterationsToMemorize, double convergence)
	{
//		super(function);
		super(new DerivableFunctionWithLastCache(function));
		this.numberOfPreviousIterationsToMemorize = numberOfPreviousIterationsToMemorize;
		this.convergence = convergence;
		this.convergenceSquare = this.convergence*this.convergence;
	}
	
	public void setDebugInfo(DebugInfo debugInfo)
	{
		this.debugInfo = debugInfo;
	}

	@Override
	public void find()
	{
		previousItrations = new LinkedList<PointAndGradientSubstractions>();
		LineSearch<DerivableFunction> lineSearch = new ArmijoLineSearch<DerivableFunction>();
		
		point = new double[function.size()];
		for (int i=0;i<point.length;++i) {point[i]=0.0;}
		value = function.value(point);
		if (logger.isInfoEnabled()) {logger.info("LBFGS: initial value = "+String.format("%-3.3f", value));}
		double[] gradient = function.gradient(point);
		double previousValue = value;
		int forLogger_iterationIndex=0;
		while (VectorUtilities.euclideanNormSquare(gradient)>convergenceSquare)
		{
			if (logger.isDebugEnabled()) {logger.debug(String.format("Gradient norm square = %-10.7f", VectorUtilities.euclideanNormSquare(gradient) ));}
			previousValue = value;
			double[] previousPoint = Arrays.copyOf(point, point.length);
			double[] previousGradient = Arrays.copyOf(gradient, gradient.length);

			// 1. Update point (which is the vector "x").
			
			double[] direction = VectorUtilities.multiplyByScalar(-1.0, twoLoopRecursion(point));
			double alpha_rate = lineSearch.findRate(function, point, direction);
			point = VectorUtilities.addVectors(point, VectorUtilities.multiplyByScalar(alpha_rate, direction));
			
			// 2. Prepare next iteration
			value = function.value(point);
			gradient = function.gradient(point);

			previousItrations.add(new PointAndGradientSubstractions(VectorUtilities.subtractVectors(point, previousPoint), VectorUtilities.subtractVectors(gradient, previousGradient)));
			if (previousItrations.size()>numberOfPreviousIterationsToMemorize)
			{
				previousItrations.removeFirst();
			}
			if (previousItrations.size()>numberOfPreviousIterationsToMemorize) {throw new CrfException("BUG");}

			
			// 3. Print log messages
			++forLogger_iterationIndex;
			if (value>previousValue) {logger.error("LBFGS: value > previous value");}
			if (logger.isInfoEnabled()) {logger.info("LBFGS iteration "+forLogger_iterationIndex+": value = "+String.format("%-3.3f", value));}
			if ( (debugInfo!=null) && (logger.isInfoEnabled()) )
			{
				logger.info(debugInfo.info(point));
			}
		}
		calculated = true;
	}

	@Override
	public double getValue()
	{
		if (!calculated) {throw new CrfException("Not calculated.");}
		return value;
	}

	@Override
	public double[] getPoint()
	{
		if (!calculated) {throw new CrfException("Not calculated.");}
		return point;
	}
	
	
	
	public static interface DebugInfo
	{
		public String info(double[] point);
	}
	
	
	
	private double[] twoLoopRecursion(double[] point)
	{
		ArrayList<Double> rhoList = new ArrayList<Double>(previousItrations.size());
		ArrayList<Double> alphaList = new ArrayList<Double>(previousItrations.size());
		
		double[] q = function.gradient(point); // Infinity check of this gradient has been performed by the caller.
		for (PointAndGradientSubstractions substractions : previousItrations)
		{
			double rho = safeDivide(1.0, VectorUtilities.product(substractions.getGradientSubstraction(), substractions.getPointSubstraction()));
			rhoList.add(rho);
			double alpha = safeMultiply(rho, VectorUtilities.product(substractions.getPointSubstraction(), q));
			alphaList.add(alpha);
			
			q = VectorUtilities.subtractVectors(q, VectorUtilities.multiplyByScalar(alpha, substractions.getGradientSubstraction()) );
		}
		
		double[] r = calculateInitial_r_forTwoLoopRecursion(q);

		ListIterator<PointAndGradientSubstractions> previousIterationsIterator = previousItrations.listIterator(previousItrations.size());
		ListIterator<Double> rhoIterator = rhoList.listIterator(rhoList.size());
		ListIterator<Double> alphaIterator = alphaList.listIterator(alphaList.size());
		while (previousIterationsIterator.hasPrevious()&&rhoIterator.hasPrevious()&&alphaIterator.hasPrevious())
		{
			PointAndGradientSubstractions substractions = previousIterationsIterator.previous();
			double rho = rhoIterator.previous();
			double alpha = alphaIterator.previous();
			
			double beta = safeMultiply(rho, VectorUtilities.product(substractions.getGradientSubstraction(), r));
			r = VectorUtilities.addVectors( r, VectorUtilities.multiplyByScalar(alpha-beta, substractions.getPointSubstraction()) );
		}
		if ((previousIterationsIterator.hasPrevious()||rhoIterator.hasPrevious()||alphaIterator.hasPrevious())) {throw new CrfException("BUG");}
		
		return r;
	}
	
	
	private double[] calculateInitial_r_forTwoLoopRecursion(double[] q)
	{
		double gamma = 1.0;
		if (previousItrations.size()>=1)
		{
			PointAndGradientSubstractions lastSubstraction = previousItrations.get(0);
			gamma = safeDivide(
					VectorUtilities.product(lastSubstraction.getPointSubstraction(), lastSubstraction.getGradientSubstraction())
					,
					VectorUtilities.product(lastSubstraction.getGradientSubstraction(), lastSubstraction.getGradientSubstraction())
					);
		}
		
		double[] r = VectorUtilities.multiplyByScalar(gamma, q);
		return r;
	}
	

	

	// input
	private final int numberOfPreviousIterationsToMemorize; // m
	private final double convergence;
	private final double convergenceSquare;
	
	private DebugInfo debugInfo = null;
	
	// internals
	private LinkedList<PointAndGradientSubstractions> previousItrations; // newest is pushed to the beginning.
	private boolean calculated = false;
	
	// output
	private double[] point = null;
	private double value = 0.0;
	
	
	


	private static final Logger logger = Logger.getLogger(LbfgsMinimizer.class);
}
