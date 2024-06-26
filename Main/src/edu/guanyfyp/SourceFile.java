/**
 * @author Guanyuming He
 */
package edu.guanyfyp;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.guanyfyp.format.FormatVerdict;
import edu.guanyfyp.format.primitives.CodeBlock;
import edu.guanyfyp.format.primitives.CommentBlock;
import edu.guanyfyp.format.primitives.FormatToken;
import edu.guanyfyp.format.primitives.JavaDocBlock;
import edu.guanyfyp.format.primitives.Line;
import edu.guanyfyp.format.primitives.PrimitiveContext;
import edu.guanyfyp.format.primitives.WsBlock;
import edu.guanyfyp.format.summaries.CodeBlockSummary;
import edu.guanyfyp.format.summaries.CommentBlockSummary;
import edu.guanyfyp.format.summaries.JavaDocSummary;
import edu.guanyfyp.format.summaries.LineSummary;
import edu.guanyfyp.format.summaries.WsBlockSummary;
import edu.guanyfyp.generated.JavaLexer;
import edu.guanyfyp.generated.JavaParser;
import edu.guanyfyp.syntax.SyntaxStructure;
import edu.guanyfyp.syntax.SyntaxStructureBuilder;
import edu.guanyfyp.syntax.ThrowExceptionErrorListener;

/**
 * Represents a source code file.
 * Handles the reading of source code files.
 * Stores all the information needed to judge the code quality of the file.
 * 
 * @implSpec 
 * Invariants:
 * 		1. The lines and the tokens in the field format_tokens 
 * 		are stored in the order they appear in the file.
 * 		2. syntaxStructureBuilder has finished building a SyntaxStructure.
 */
public class SourceFile 
{
//////////////////////////// Fields /////////////////////////
	
    // All the tokens for format checking.
    // Each element of the outmost array represents a line in the source file.
    // Each element inside a line array is a token.
	// The field in the end will be immutable, and each inner list will also be.
    private final List<List<FormatToken>> formatTokens;
    
	// To support fast random access of the format tokens,
	// this table that maps
	// token index -> {line index (starting from 0), token line index (from 0)}
	// is built.
    private final ArrayList<Pair<Integer, Integer>> formatTokenRandomAccessTable;
    
    // All the lines in the source file, stored in the order they appear.
    // 0..n-1
    private final List<Line> lines;
    
    // List of all java doc format tokens. They are also stored in formatTokens,
    // but for fast access I have another list just for them.
    private final List<JavaDocBlock> javaDocs;
    
    // Created at beginning and is used to build the syntax structure during parsing.
    private final SyntaxStructureBuilder syntaxStructureBuilder;
    
//////////////////////////// Static fields /////////////////////////
    
    // Channel numbers of the ANTLR lexer tokens
	public static final int DEFAULT_CHANNEL = 0;
	public static final int WHITESPACE_CHANNEL = 1;
	public static final int COMMENTS_CHANNEL = 2;
	public static final int JAVADOC_CHANNEL = 3;
    
//////////////////////////// Constructors /////////////////////////
	
    /**
     * Reads the source code from a local file.
     * And parses the source file to create necessary objects to evaluate the format.
     * 1. FormatTokens are created and stored.
     * 2. Lines are created and stored.
     * 3. A SyntaxStructureBuilder is created and the syntax structure is built with that.
     * 
     * @param file_path The path to the file.
     * @throws IOException on reading error.
     * @throws UnsupportedOperationException if the source code contains some syntax error.
     */
    public SourceFile(String file_path) throws IOException, UnsupportedOperationException
    {
    	// Initialise temp fields
    	// Temp fields are used because the final fields can only be assigned to once.
    	// The final fields will be created by calling Collections.unmodifiableList().
    	List<List<FormatToken>> temp_format_tokens = new ArrayList<>();
    	List<Line> temp_lines = new ArrayList<>();
    	List<JavaDocBlock> tempJavaDocs = new ArrayList<>();
    	
    	// Parse the source file with ANTLR4
    	CharStream inputStream = CharStreams.fromFileName(file_path);
    	JavaLexer lexer = new JavaLexer(inputStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		// Create all FormatTokens based on the lexer result
		{
			tokens.fill();
			List<Token> lexer_token_list = tokens.getTokens();
			
			// Container for the tokens in the current line.
			var cur_line_tokens = new ArrayList<FormatToken>();
			// Current line number (starting from 1)
			int cur_line = 1;
			// The current visual position in the line.
			int visual_pos = 0;
			// Number of tokens processed in this line.
			int cur_line_token_number = 0;
			// The tokens in the list should be in the order that they appear in the text.
			// Use a loop to iterate through them
			for(Token t : lexer_token_list)
			{
				// Do not accept empty tokens
				if(t.getText().isEmpty()) 
				{
					continue;
				}
				// Do not accept the final EOF token
				if(t.getType() == Token.EOF)
				{
					continue;
				}
				
				// Check if it's getting into another line.
				int tLine = t.getLine();
				if(cur_line != tLine)
				{
					// This should be true
					assert cur_line < tLine;
					
					// There may be lines that have no tokens since I ignore line separators.
					// Therefore, do this until I reach the next non-empty line.
					while(cur_line < tLine)
					{
						// Push the tokens in the current line in the immutable version.
						// And creates a new container for tokens in the next line.
						temp_format_tokens.add(Collections.unmodifiableList(cur_line_tokens));
						cur_line_tokens = new ArrayList<FormatToken>();
						
						// go to the next line.
						++cur_line;
					}
					// Now, cur_line = tLine
					assert cur_line == tLine;
					
					// Refresh variables
					visual_pos = 0;
					cur_line_token_number = 0;
				}
				
				FormatToken ft = null;
				switch(t.getChannel())
				{
				case DEFAULT_CHANNEL:
					ft = new CodeBlock
					(
						t, visual_pos, 
						cur_line_token_number // index of the token in the line
					);
					
					// At this stage certain code block types are already deduced by the lexer.
					// Assign such types now to reduce further deductions.
					((CodeBlock)ft).additionalAttr.setTypeFromLexerTokenType(t.getType());
					break;
					
				case WHITESPACE_CHANNEL:
					ft = 
					new WsBlock
					(
						t, visual_pos, 
						cur_line_token_number // index of the token in the line
					);
					break;	
					
				case COMMENTS_CHANNEL:
					ft =
					new CommentBlock
					(
						t, visual_pos, 
						cur_line_token_number // index of the token in the line
					);
					break;	
					
				case JAVADOC_CHANNEL:
					ft =
					new JavaDocBlock
					(
						t, visual_pos, 
						cur_line_token_number // index of the token in the line
					);
					
					tempJavaDocs.add((JavaDocBlock)ft);
					break;	
					
				default:
					throw new RuntimeException("Unexpected channel number. Check my grammar.");
				}
				
				// Update the variables
				cur_line_tokens.add(ft);
				visual_pos += ft.visualLength;
				++cur_line_token_number;
			}
			// Now the cur_line_tokens that is for the last line has yet to be added.
			temp_format_tokens.add(Collections.unmodifiableList(cur_line_tokens));
			
			// Finally, turn format_tokens immutable
			formatTokens = Collections.unmodifiableList(temp_format_tokens);		
			
			// turn javaDocs immutable
			javaDocs = Collections.unmodifiableList(tempJavaDocs);
		}
		
		// Build the format token random access table
		{
			formatTokenRandomAccessTable = new ArrayList<Pair<Integer,Integer>>();
			for(int i = 0; i < formatTokens.size(); ++i)
			{
				var line = formatTokens.get(i);
				for(int j = 0; j < line.size(); ++j)
				{
					formatTokenRandomAccessTable.add(new Pair<Integer, Integer>(i, j));
				}
			}
		}
		
		// Create the lines
		{
			for (int i = 0; i < formatTokens.size(); ++i)
			{
				var line = formatTokens.get(i);
				if(!line.isEmpty())
				{
					// A line is created with the first and the last format token.
					temp_lines.add(new Line(i+1, line.get(0), line.get(line.size()-1), this));
				}
				else // the line is empty
				{
					temp_lines.add(new Line(i+1, null, null, this));
				}
			}
			
			lines = Collections.unmodifiableList(temp_lines);
		}
		
		// Parse the program and fill missing attributes of the format tokens
		{
			// Reset the token stream because I retrieved all tokens once.
			tokens.seek(0);
			
			// Parse the program and get a parse tree
			JavaParser parser = new JavaParser(tokens);
			// Use my own error listener to ALWAYS throw a exception
			// on a syntax error
			parser.removeErrorListeners(); // remove the default one
			parser.addErrorListener(new ThrowExceptionErrorListener());
			
			// On syntax error,
			// throw an UnsupportedOperationException with the error message
			ParseTree parse_tree = null;
			try
			{
				parse_tree = parser.compilationUnit();
			}
			catch(RuntimeException e)
			{
				throw new UnsupportedOperationException(e.getMessage());
			}
			
			// Walk the parse tree and build the syntax context by using ContextBuilder
			ParseTreeWalker tree_walker = new ParseTreeWalker();
			this.syntaxStructureBuilder = new SyntaxStructureBuilder(this);
			tree_walker.walk(syntaxStructureBuilder, parse_tree);
			
			// Apply the additional token attributes after the syntax and partial semantic analysis
			syntaxStructureBuilder.applyAdditionalTokenAttributes();
		}
    }
    
//////////////////////////// Observers /////////////////////////
    
    /**
     * @return the SyntaxStructureBuilder created with the source file,
     * in which the syntax structure built is contained.
     */
    public SyntaxStructureBuilder getSyntaxStructureBuilder()
    {
    	return syntaxStructureBuilder;
    }
    
    /**
     * @return The SyntaxStructure built during the SourceFile's construction
     */
    public SyntaxStructure getSyntaxStructure()
    {
    	return syntaxStructureBuilder.getSyntaxStructure();
    }
    
    // The format tokens can only be accessed through the following methods
    // or through the lines.
    
    /**
     * Because the formatTokens is immutable, one can get it.
     * @return this.formatTokens
     */
    public List<List<FormatToken>> getFormatTokens()
    {
    	return formatTokens;
    }
    
    public List<JavaDocBlock> getJavaDocs() 
    {
		return javaDocs;
	}

	/**
     * @return The number of format tokens in the source file.
     */
    public int numFormatTokens()
    {
    	return formatTokenRandomAccessTable.size();
    }
    
    /**
     * If the source file has a format token at the given location.
     * @param line line number, starting from 1
     * @param index starting from 0.
     * @return true iff so.
     */
    public boolean hasFormatToken(int line, int index)
    {
    	int line_ind = line - 1;
    	if (line_ind < 0 || line_ind >= formatTokens.size())
    	{
    		return false;
    	}
    	var line_array = formatTokens.get(line_ind);
    	if(index < 0 || index >= line_array.size())
    	{
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Gets the index^th (starting from 0) FormatToken in the line (starting from 1).
     * @param line line number, starting from 1
     * @param index starting from 0.
     * @return the token or null if such a token does not exist.
     */
    public FormatToken getFormatToken(int line, int index)
    {
    	/*
    	 * Does not invoke hasFormatToken as hasFormatToken
    	 * gets a copy of the line_array, which I need to use here as well,
    	 * so it would be done twice that way.
    	 */
    	
    	int line_ind = line - 1;
    	if (line_ind < 0 || line_ind >= formatTokens.size())
    	{
    		return null;
    	}
    	var line_array = formatTokens.get(line_ind);
    	if(index < 0 || index >= line_array.size())
    	{
    		return null;
    	}
    	
    	return line_array.get(index);
    }
    
    /**
     * Random access to the format tokens
     * 
     * @param index the index of the token with respect to the whole tokens stream 
     * (starting from 0)
     * @return the token corresponding to the index
     * @throws ArrayIndexOutOfBoundsException if index is out of bound
     */
    public FormatToken getFormatToken(int index)
    {
    	if(index < 0 || index >= formatTokenRandomAccessTable.size())
    	{
    		throw new ArrayIndexOutOfBoundsException("index is out of range");
    	}
    	
    	var pair = formatTokenRandomAccessTable.get(index);
    	return formatTokens.get(pair.a).get(pair.b);
    }
    
    /**
     * Returns the FormatToken that corresponds to the Antlr token
     * @param antlr_token whose token index will be used
     * @return the FormatToken
     * @apiNote The behaviour is undefined if antlr_token is not part of the source file.
     */
    public FormatToken getFormatToken(Token antlr_token)
    {
    	return getFormatToken(antlr_token.getTokenIndex());
    }
    
    /**
     * As a FormatToken contains its complete position information by design,
     * I can get tokens based on a given one.
     * This method gets the token immediately before the given one.
     * 
     * @param given the token given.
     * @return the token immediately before given (the method goes back to previous lines if necessary)
     * or null if there is no token before it.
     */
    public FormatToken getPrevFormatToken(FormatToken given)
    {
    	int line = given.line();
    	int index = given.indexInLine - 1;
    	
    	// If the token is the first in the line
    	if(index < 0)
    	{
    		// This should be true.
    		assert index == -1;
    		
        	int line_ind = line-1;
    		// Keep going back until the previous line is non-empty
    		// or until I have reached the first line.
    		while(line_ind >= 1)
    		{
        		--line_ind;
        		
        		var line_array = formatTokens.get(line_ind);
        		if(line_array.isEmpty())
        		{
        			continue;
        		}
        		
        		// now the line is not empty
        		// return the last token in the line.
        		return line_array.get(line_array.size()-1);
    		}
    		
    		// now line = 1 but it's still empty
    		// or the token is already the first one.
    		return null;    		
    	}
    	
    	// Otherwise, the prev token is still in the line.
    	return formatTokens.get(line-1).get(index);
    }
    
    /**
     * As a FormatToken contains its complete position information by design,
     * I can get tokens based on a given one.
     * This method gets the token immediately after the given one.
     * 
     * @param given the token given.
     * @return the token immediately after given (the method goes forth to further lines if necessary)
     * or null if there is no token after it.
     */
    public FormatToken getNextFormatToken(FormatToken given)
    {
    	int line = given.line();
    	int index = given.indexInLine + 1;
    	
    	int line_index = line-1;
    	var cur_line_array = formatTokens.get(line_index);
    	
    	// If the token is the last in the line
    	if(index >= cur_line_array.size())
    	{
    		// This should be true.
    		assert index == cur_line_array.size();
    		
    		// Keep going back until the previous line is non-empty
    		// or until I have reached the first line.
    		while(line_index+1 < formatTokens.size())
    		{
        		++line_index;
        		
        		var line_array = formatTokens.get(line_index);
        		if(line_array.isEmpty())
        		{
        			continue;
        		}
        		
        		// now the line is not empty
        		// return the first token in the line.
        		return line_array.get(0);
    		}
    		
    		// now line_index = size-1 but it's still empty
    		return null;    		
    	}
    	
    	// Otherwise, the next token is still in the line.
    	return cur_line_array.get(index);
    }

    /**
     * @param tk the token.
     * @return true iff tk is one of the tokens in the SourceFile.
     */
    public boolean includes(FormatToken tk)
    {
    	if(tk == null)
    	{
    		return false;
    	}
    	
    	FormatToken t = getFormatToken(tk.line(), tk.indexInLine);
    	
    	// If I have a token at the location.
    	if(null == t)
    	{
    		return false;
    	}
    	
    	// I have a token at the location.
    	// If they are the same.
    	// Since a FormatToken cannot be deeply copied,
    	// from one ANTLR token there can only be one FormatToken.
    	return t == tk;
    }

    // The lines can only be accessed through the following methods.
    
    /**
     * Can return this.lines as the field is immutable.
     * @return the lines, represented by class Line, in the source file.
     */
    public List<Line> getLines()
    {
    	return this.lines;
    }
    
    /**
     * @return the number of lines in the source file.
     */
    public int numLines()
    {
    	return lines.size();
    }
    
    /**
     * @param line_num 1..numLines()
     * @return the Line
     * @throws ArrayIndexOutOfBoundsException if line_num is out of bound.
     */
    public Line getLine(int line_num)
    {
    	int line_ind = line_num - 1;
    	if(line_ind < 0 || line_ind >= lines.size())
    	{
    		throw new ArrayIndexOutOfBoundsException("line_num is out of bound");
    	}
    	
    	return lines.get(line_ind);
    }
    
//////////////////////////// analyze() deserves a separator /////////////////////////
    
    /**
     * Analyzes the source code in the file and give a result.
     * @return the result given
     */
    public FormatVerdict analyze()
    {
		FormatVerdict verdict = new FormatVerdict();
		
		// Get the syntax structure for syntax contexts
		final var syntax_structure = getSyntaxStructure();
		
		// Evaluate format tokens
		{
			// TODO: after summary classes for different CodeBlocks are written,
			// add the evaluated code blocks to them.
			
			CodeBlockSummary cbSummary = new CodeBlockSummary();
			WsBlockSummary wsBlockSummary = new WsBlockSummary();
			CommentBlockSummary commentBlockSummary = new CommentBlockSummary();
			JavaDocSummary javaDocSummary = new JavaDocSummary();
			
			// For each ft, evaluate it and add it to the corresponding summary.
			for(int i = 0; i < numFormatTokens(); ++i)
			{
				var pair = formatTokenRandomAccessTable.get(i);
		    	var tk = formatTokens.get(pair.a).get(pair.b);
		    	
		    	var tk_ctx = new PrimitiveContext(syntax_structure.getSyntaxContext(tk));
		    	
		    	tk.evaluateFormat(this, tk_ctx);
		    	
		    	if(tk instanceof WsBlock)
		    	{
		    		wsBlockSummary.include((WsBlock)tk);
		    	}
		    	else if(tk instanceof JavaDocBlock)
		    	{
		    		javaDocSummary.include((JavaDocBlock)tk);
		    	}
		    	else if(tk instanceof CommentBlock)
		    	{
		    		commentBlockSummary.include((CommentBlock)tk);
		    	}
		    	else if(tk instanceof CodeBlock)
		    	{
		    		cbSummary.include((CodeBlock)tk);
		    	}
			}
			
			cbSummary.summarize();
			wsBlockSummary.summarize();
			commentBlockSummary.summarize();
			javaDocSummary.summarize();
			
			// Include the summaries in the verdict
			verdict.include(wsBlockSummary);
			verdict.include(commentBlockSummary);
			verdict.include(javaDocSummary);
			verdict.include(cbSummary);
		}
		
		// Evaluate lines
		{
			LineSummary lineSummary = new LineSummary();
			
			// For each line, evaluate it and add it to the summary.
			for(int i = 0; i < lines.size(); ++i)
			{
				var line = lines.get(i);
		    	
				var line_ctx = new PrimitiveContext(syntax_structure.getSyntaxContext(line));
		    	line.evaluateFormat(this, line_ctx);
		    	
		    	lineSummary.include(line);
			}
			
			lineSummary.summarize();
			
			// Include the summary in the verdict
			verdict.include(lineSummary);
		}
		
		// Don't forget to give verdict
		verdict.giveVerdict();
		
		return verdict;
    }
}
