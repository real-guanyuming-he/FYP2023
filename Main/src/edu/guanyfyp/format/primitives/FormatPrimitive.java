/**
 * 
 */
package edu.guanyfyp.format.primitives;

import edu.guanyfyp.SourceFile;

/**
 * 
 */
public abstract class FormatPrimitive
{
//////////////////////// Fields ////////////////////////
	private boolean evaluated = false;
	
//////////////////////// Methods ////////////////////////
	public boolean isEvaluated() { return evaluated; }
	
	/**
	 * Evaluates the format of the format primitive (i.e. the code quality of this primitive)
	 * Stores the results in the primitive's fields, which are accessed by its observers.
	 * After this, isEvaluated() = true.
	 * 
	 * The default implementation checks isEvaluated() and sets it to true if it was false.
	 * (If it was true it throws)
	 * 
	 * @param sf the source file in which the primitive is in
	 * @param context the context of the primitive
	 * 
	 * @throws IllegalStateException if the primitive has already been evaluated
	 */
	public void evaluateFormat(final SourceFile sf, final PrimitiveContext context)
	{
		if(isEvaluated())
		{
			throw new IllegalStateException("already evaluated.");
		}
		
		evaluated = true;
	}
}