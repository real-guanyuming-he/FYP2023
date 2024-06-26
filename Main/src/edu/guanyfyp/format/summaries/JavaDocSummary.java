/**
 * 
 */
package edu.guanyfyp.format.summaries;

import java.util.ArrayList;
import java.util.List;

import edu.guanyfyp.format.primitives.JavaDocBlock;

/**
 * 
 */
public final class JavaDocSummary extends FormatEvalSummary<JavaDocBlock>
{
	int numJavaDocs = 0;
	List<JavaDocBlock> badJavaDocsList;
	
	/**
	 * 
	 */
	public JavaDocSummary() 
	{
		super();
		badJavaDocsList = new ArrayList<JavaDocBlock>();
	}
	
	@Override
	public void include(JavaDocBlock b)
	{
		super.include(b);
		++numJavaDocs;
		
		// 1. if the javadoc's following's type does not match what it seems to want being followed by.
		if(b.isTypeUnMatched())
		{
			badJavaDocsList.add(b);
		}
		// 2. if the type matches, but the tags do not
		else
		{
			// return after each add to only add them once.
			if(!b.getUnmatchedCommentTags().isEmpty())
			{
				badJavaDocsList.add(b);
				return;
			}
			if(!b.getUnmatchedSyntaxObjects().isEmpty())
			{
				badJavaDocsList.add(b);
				return;
			}		
			if(b.getReturnNotProvided())
			{
				badJavaDocsList.add(b);
				return;
			}
		}
	}

	public int getNumJavaDocs() {
		return numJavaDocs;
	}

	public List<JavaDocBlock> getBadJavaDocsList() {
		return badJavaDocsList;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("Bad JavaDocs:\n");
		for(var jd : badJavaDocsList)
		{
			builder.append('\t');
			builder.append(jd.toString());
			builder.append('\n');
		}
		
		return builder.toString();
	}

}
