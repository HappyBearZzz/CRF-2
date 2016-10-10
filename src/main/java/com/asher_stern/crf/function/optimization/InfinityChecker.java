package com.asher_stern.crf.function.optimization;

/**
 * Utility class to check whether some value is infinity, and throw exception if it is.
 * <br>
 * Use the static methods <code>check()</code>.
 * Chained call can be employed as well: <code> check(value1).check(value2);  </code>. The values can be either double or double[].
 *
 * <p>
 * Date: Oct 10, 2016
 * @author Asher Stern
 *
 */
public final class InfinityChecker
{
	/**
	 * Throws {@link InfinityException} if one of the given values is either infinity or NaN
	 */
	public static final NestedInfinityChecker check(double... values)
	{
		return NestedInfinityChecker.INSTANCE.check(values);
	}
	/**
	 * Throws {@link InfinityException} if one of the elements in one of the given vectors is either infinity or NaN
	 */
	public static final NestedInfinityChecker check(double[]... values)
	{
		return NestedInfinityChecker.INSTANCE.check(values);
	}
	
	
	
	
	
	/////////////// IMPLEMENTATION ///////////////
	
	public static class NestedInfinityChecker
	{
		public final NestedInfinityChecker check(double... values)
		{
			for (double value : values)
			{
				if (Double.isInfinite(value))
				{
					throw new InfinityException(true, false, false);
				}
				if (Double.isNaN(value))
				{
					throw new InfinityException(true, false, true);
				}
			}
			return INSTANCE;
		}
		
		public final NestedInfinityChecker check(double[]... values)
		{
			for (double[] value : values)
			{
				for (double d : value)
				{
					if (Double.isInfinite(d))
					{
						throw new InfinityException(false, true, false);
					}
					if (Double.isNaN(d))
					{
						throw new InfinityException(false, true, true);
					}
				}
			}
			return INSTANCE;
		}
		
		protected NestedInfinityChecker(){}
		private static final NestedInfinityChecker INSTANCE = new NestedInfinityChecker();
	}
}
