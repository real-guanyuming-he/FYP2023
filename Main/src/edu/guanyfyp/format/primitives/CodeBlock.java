/**
 * @author Guanyuming He
 */
package edu.guanyfyp.format.primitives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.Token;

import edu.guanyfyp.SourceFile;
import edu.guanyfyp.generated.JavaLexer;
import edu.guanyfyp.generated.JavaParser.AnnotationContext;
import edu.guanyfyp.syntax.SyntaxContext;

/**
 * A code block represents the format influence of a code token generated by a parser.
 * Here the type of the token will also be taken into consideration because
 * different code blocks fit into different format restrictions.
 */
public class CodeBlock extends FormatToken 
{

	/**
	 * When additional attributes are not available
	 * @param characters
	 * @param position
	 * @param act_pos
	 * @param line
	 * @param index_in_line
	 */
	public CodeBlock
	(
		Token antlr_token,
		int visual_pos,
		int index_in_line
	) 
	{
		super
		(
			antlr_token, visual_pos, index_in_line
		);	
		this.additionalAttr = new AdditionalAttributes();
	}
	
	/**
	 * When additional attributes are available
	 * @param characters
	 * @param position
	 * @param act_pos
	 * @param line
	 * @param index_in_line
	 */
	public CodeBlock
	(
		Token antlr_token,
		int visual_pos,
		int index_in_line,
		AdditionalAttributes additional_attributes
	) 
	{
		super
		(
			antlr_token, visual_pos, index_in_line
		);
		this.additionalAttr = additional_attributes;
	}

//////////////////// Abstract overrides ///////////////////////////////
	
	/**
	 * A code block should always be visible.
	 */
	@Override
	public boolean isVisible()
	{
		return true;
	}
	
//////////////////// Syntax and Semantics Related ///////////////////////////////
	public enum Type
	{
		// Unknown for now.
		// Perhaps unassigned.
		UNKNOWN,
		
		// Keywords
		// TODO: Perhaps divide keywords
		// into groups of finer granularity
		// that can reflect OOP and other programming styles.
		KEYWORD_UNCLASSIFIED,
		
		// Literals
		STRING_LITERAL,
		NUMBER_LITERAL,
		BOOL_LITERAL,
		NULL_LITERAL,
		
		// Punctuation
		SEMICOLON,
		COMMA,
		DOT,
		// Parentheses
		L_PARENTHESIS,
		R_PARENTHESIS,
		// square brackets
		L_SBRACKET,
		R_SBRACKET,
		// curly brackets
		L_CBRACKET,
		R_CBRACKET,
		
		// Identifiers
		// IDENTIFIER_UNCLASSIFIED is a token that is known to be an identifier at the lexer level,
		// but remains to be classified by the parser
		IDENTIFIER_UNCLASSIFIED,
		CLASS_NAME,
		INTERFACE_NAME,
		ENUM_NAME,
		CONSTRUCTOR_NAME,
		METHOD_NAME,
		// variable of some class
		FIELD_NAME,
		// variable defined inside a for-loop head
		FOR_VARIABLE_NAME,
		// variable of other things
		VARIABLE_NAME,
		// parameter of some method
		PARAMETER_NAME,
		
		// Operators
		OPERATOR_LOW_PRECEDENCE,
		OTHER_OPERATORS,
		
		// Others
		OTHERS,
	}
	
	/**
	 * Additional attributes that are only known after syntax or semantic analysis
	 */
	public static final class AdditionalAttributes
	{
		public AdditionalAttributes()
		{
			this.type = Type.UNKNOWN;
			this.oopModifiers = 0;
			this.otherModifiers = 0;
			this.annotationModifiers = new ArrayList<>();
		}
		/**
		 * (deep) copy constructor
		 * @param other who will be deep-copied
		 */
		public AdditionalAttributes(AdditionalAttributes other)
		{
			this.type = other.type;
			this.oopModifiers = other.oopModifiers;
			this.otherModifiers = other.otherModifiers;
			this.annotationModifiers = new ArrayList<AnnotationContext>(other.annotationModifiers);
		}
		
		/**
		 * Construct directly from the field variables.
		 * @param type
		 * @param OOP_modifiers
		 * @param other_modifiers
		 * @param annotation_modifiers will be deep-copied
		 */
		public AdditionalAttributes
		(
			CodeBlock.Type type,
			int OOP_modifiers,
			int other_modifiers,
			List<AnnotationContext> annotation_modifiers
		)
		{
			this.type = type;
			this.oopModifiers = OOP_modifiers;
			this.otherModifiers = other_modifiers;
			this.annotationModifiers = new ArrayList<AnnotationContext>(annotation_modifiers);
		}
		
		/**
		 * Deep-copies other to this.
		 * @param other
		 */
		public void assign(AdditionalAttributes other)
		{
			this.type = other.type;
			this.oopModifiers = other.oopModifiers;
			this.otherModifiers = other.otherModifiers;
			this.annotationModifiers = new ArrayList<AnnotationContext>(other.annotationModifiers);
		}
		
		/**
		 * shallow-copies other to this.
		 * @param other
		 */
		public void move(AdditionalAttributes other)
		{
			this.type = other.type;
			this.oopModifiers = other.oopModifiers;
			this.otherModifiers = other.otherModifiers;
			this.annotationModifiers = other.annotationModifiers;
		}
		
		// The type of the code token.
		// Not final because the information may not be available at creation.
		private CodeBlock.Type type;
		
		/*
		 * In java, modifiers are generally divided into two classes:
		 * Keyword modifiers and annotation modifiers.
		 * The following two are keyword modifiers
		 */
		
		// modifiers related to OOP
		private int oopModifiers;
		// other modifiers
		private int otherModifiers;
		
		// This is the list of annotation modifiers
		private List<AnnotationContext> annotationModifiers;
		public void addAnnotationModifier(AnnotationContext annotation)
		{
			annotationModifiers.add(annotation);
		}
		
		/**
		 * modifiers related to access and inheritance
		 * stored using bitwise operations
		 */
		// Access modifiers
		public static final int OOP_MODIFIER_PUBLIC = 0x1;
		public static final int OOP_MODIFIER_PROTECTED = 0x2;
		public static final int OOP_MODIFIER_PRIVATE = 0x4;
		public static final int OOP_MODIFIER_DEFAULT = 0x8;
		// Inheritance modifiers
		public static final int OOP_MODIFIER_ABSTRACT = 0x10;
		/**
		 * modifiers that do not intersect with the above
		 * stored using bitwise operations
		 */
		public static final int MODIFIER_FINAL = 0x1;
		public static final int MODIFIER_STATIC = 0x2;
		public static final int MODIFIER_STRICTFP = 0x4;
		public static final int MODIFIER_NATIVE = 0x8;
		public static final int MODIFIER_SYNCHRONIZED = 0x10;
		public static final int MODIFIER_TRANSIENT = 0x20;
		public static final int MODIFIER_VOLATILE = 0x40;
		
		// type methods
		public Type getType() { return type; }
		public void setType(Type type) { this.type = type; }
		/**
		* Assigns this CodeBlock a type based on what the lexer can tell me about it.
		* @param tokenType
		*/
		public void setTypeFromLexerTokenType(int tokenType)
		{
			switch(tokenType)
			{
			// Keywords
			case JavaLexer.ABSTRACT:
			case JavaLexer.ASSERT:
			case JavaLexer.BOOLEAN:
			case JavaLexer.BREAK:
			case JavaLexer.BYTE:
			case JavaLexer.CASE:
			case JavaLexer.CATCH:
			case JavaLexer.CHAR:	
			case JavaLexer.CLASS:
			case JavaLexer.CONST:
			case JavaLexer.CONTINUE:
			case JavaLexer.DEFAULT:	
			case JavaLexer.DO:
			case JavaLexer.DOUBLE:
			case JavaLexer.ELSE:
			case JavaLexer.ENUM:	
			case JavaLexer.EXTENDS:
			case JavaLexer.FINAL:
			case JavaLexer.FINALLY:
			case JavaLexer.FLOAT:
			case JavaLexer.FOR:
			case JavaLexer.IF:
			case JavaLexer.GOTO:
			case JavaLexer.IMPLEMENTS:	
			case JavaLexer.IMPORT:
			case JavaLexer.INSTANCEOF:
			case JavaLexer.INT:
			case JavaLexer.INTERFACE:	
			case JavaLexer.LONG:
			case JavaLexer.NATIVE:
			case JavaLexer.NEW:
			case JavaLexer.PACKAGE:	
			case JavaLexer.PRIVATE:
			case JavaLexer.PROTECTED:
			case JavaLexer.PUBLIC:
			case JavaLexer.RETURN:
			case JavaLexer.SHORT:
			case JavaLexer.STATIC:
			case JavaLexer.STRICTFP:
			case JavaLexer.SUPER:	
			case JavaLexer.SWITCH:
			case JavaLexer.SYNCHRONIZED:
			case JavaLexer.THIS:
			case JavaLexer.THROW:	
			case JavaLexer.THROWS:
			case JavaLexer.TRANSIENT:
			case JavaLexer.TRY:
			case JavaLexer.VOID:	
			case JavaLexer.VOLATILE:
			case JavaLexer.WHILE:
			// Module-related keywords
			case JavaLexer.MODULE:
			case JavaLexer.OPEN:
			case JavaLexer.REQUIRES:
			case JavaLexer.EXPORTS:
			case JavaLexer.OPENS:
			case JavaLexer.TO:	
			case JavaLexer.USES:
			case JavaLexer.PROVIDES:
			case JavaLexer.WITH:
			case JavaLexer.TRANSITIVE:	
			// var
			case JavaLexer.VAR:
			// switch expressions (Java17)
			case JavaLexer.YIELD:
			// records
			case JavaLexer.RECORD:
			// Sealed classes
			case JavaLexer.SEALED:	
			case JavaLexer.PERMITS:
			case JavaLexer.NON_SEALED:
				this.type = Type.KEYWORD_UNCLASSIFIED;
				break;
			
			// Literals
			case JavaLexer.DECIMAL_LITERAL:
			case JavaLexer.HEX_LITERAL:
			case JavaLexer.OCT_LITERAL:
			case JavaLexer.BINARY_LITERAL:
			case JavaLexer.FLOAT_LITERAL:
			case JavaLexer.HEX_FLOAT_LITERAL:
				this.type = Type.NUMBER_LITERAL;
				break;
			case JavaLexer.CHAR_LITERAL:
			case JavaLexer.STRING_LITERAL:
			case JavaLexer.TEXT_BLOCK:
				this.type = Type.STRING_LITERAL;
				break;
			case JavaLexer.BOOL_LITERAL:
				this.type = Type.BOOL_LITERAL;
				break;
			case JavaLexer.NULL_LITERAL:
				this.type = Type.NULL_LITERAL;
				break;
			
			// Punctuation
			case JavaLexer.SEMI:
				this.type = Type.SEMICOLON;
				break;
			case JavaLexer.COMMA:
				this.type = Type.COMMA;
				break;
			case JavaLexer.DOT:
				this.type = Type.DOT;
				break;
			case JavaLexer.LPAREN:
				this.type = Type.L_PARENTHESIS;
				break;
			case JavaLexer.RPAREN:
				this.type = Type.R_PARENTHESIS;
				break;
			case JavaLexer.LBRACE:
				this.type = Type.L_CBRACKET;
				break;
			case JavaLexer.RBRACE:
				this.type = Type.R_CBRACKET;
				break;
			case JavaLexer.LBRACK:
				this.type = Type.L_SBRACKET;
				break;
			case JavaLexer.RBRACK:
				this.type = Type.R_SBRACKET;
				break;
			
			// Operators
			// from lowest precedence to highest. See https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html
			// lowest: assignment
			case JavaLexer.ASSIGN:
			case JavaLexer.ADD_ASSIGN:	
			case JavaLexer.SUB_ASSIGN:
			case JavaLexer.MUL_ASSIGN:
			case JavaLexer.DIV_ASSIGN:
			case JavaLexer.AND_ASSIGN:	
			case JavaLexer.OR_ASSIGN:
			case JavaLexer.XOR_ASSIGN:
			case JavaLexer.LSHIFT_ASSIGN:
			case JavaLexer.RSHIFT_ASSIGN:	
			case JavaLexer.URSHIFT_ASSIGN:
			case JavaLexer.MOD_ASSIGN:
			// then, ternary
			case JavaLexer.QUESTION:
			case JavaLexer.COLON:
			// then, logical and or
			case JavaLexer.AND:	
			case JavaLexer.OR:
			// then, bitwise and, or, exclusive or (^, a caret)
			case JavaLexer.BITAND:
			case JavaLexer.BITOR:
			case JavaLexer.CARET:
			// then, equality and relational
			case JavaLexer.EQUAL:
			case JavaLexer.NOTEQUAL:
			case JavaLexer.GT:
			case JavaLexer.LT:
			case JavaLexer.LE:
			case JavaLexer.GE:
			// then, shift
			// but these are implemented using multiple LTs or GTs
				this.type = Type.OPERATOR_LOW_PRECEDENCE;
				break;
			// all those under are considered having high precedence
			case JavaLexer.BANG:
			case JavaLexer.TILDE:
			case JavaLexer.INC:
			case JavaLexer.DEC:
			case JavaLexer.ADD:	
			case JavaLexer.SUB:
			case JavaLexer.MUL:
			case JavaLexer.DIV:
			case JavaLexer.MOD:
				this.type = Type.OTHER_OPERATORS;
				break;
				
			// Identifier
			case JavaLexer.IDENTIFIER:
				this.type = Type.IDENTIFIER_UNCLASSIFIED;
				break;
				
			// Misc
			case JavaLexer.ARROW:	
			case JavaLexer.COLONCOLON:
			case JavaLexer.AT:
			case JavaLexer.ELLIPSIS:
				this.type = Type.OTHERS;
				break;
			
			default:
			// Should never reach here because I should have handled all types
			assert false;
			}
		}
		
		// modifier methods
		public void setOopModifier(int mod)
		{
			oopModifiers |= mod;
		}
		public void setOtherModifiers(int mod)
		{
			otherModifiers |= mod;
		}
		public void assignOopModifiers(int mod)
		{
			oopModifiers = mod;
		}
		public void assignOtherModifiers(int mod)
		{
			otherModifiers = mod;
		}
		public int getOopModifiers()
		{
			return oopModifiers;
		}
		public int getOtherModifiers()
		{
			return otherModifiers;
		}
		public boolean hasOopModifier(int mod)
		{
			return (oopModifiers & mod) != 0;
		}
		public boolean hasOtherModifier(int mod)
		{
			return (otherModifiers & mod) != 0;
		}
	}

	public final AdditionalAttributes additionalAttr;
	
///////////////////////////// From FormatPrimitive /////////////////////////////
	/**
	 * 1. If it's identifier
	 * 	i. judge length
	 *  ii. check naming style.
	 *  ...
	 * 2. Punctuations
	 * 	2.1 {
	 *  2.2 }
	 *  2.3 ,
	 *  2.4 ;
	 * 3. operators
	 *  i. check spaces around some low precedence operators
	 */
	@Override
	public void evaluateFormat(SourceFile sf, PrimitiveContext context) 
	{
		super.evaluateFormat(sf, context);
		
		// types of codeblock
		// 1. identifier
		// 2. punctuation
		// 3. operators
		// 4. keywords (not useful for format evaluation)
		
		// set this to true in general
		// unless I set it to false later in specific case
		hasSpaceAroundWhenItShould = true;
		
		// 1.
		if(isIdentifier())
		{
			//throw new RuntimeException("TODO: only judge too short for some types.");
			// i.
			judgeLength();
			// ii.
			decideCurrentNamingStyle();
		}
		// 2. 
		// need to check { } , ; 
		// 2.1. {
		else if(antlrToken.getType() == JavaLexer.LBRACE)
		{
			var containingScope = context.syntaxContext.scope;
			if(containingScope.oneLine)
			{
				// get the next token and see if that's wsblock
				var next = sf.getNextFormatToken(this);
				// this is a oneline scope, so there must be some tokens after the {
				assert (next != null && next.line() == this.line());
				hasOneLineScopeSpace = next instanceof WsBlock;
				hasSpaceAroundWhenItShould = hasOneLineScopeSpace;
			}
			// It's a multiline scope
			else
			{
				// judge its style.
				// If it starts a new line, then there should only be one block
				// before it in the same line, and that block must be a wsblock.
				var prev = sf.getPrevFormatToken(this);
				if(prev == null)
				{
					// It's the first token in the file.
					currentScopeStyle = ScopeStyle.LBRACE_STARTS_NEW_LINE;
					return;
				}
				
				if(!(prev instanceof WsBlock))
				{
					currentScopeStyle = ScopeStyle.LBRACE_STAYS_IN_OLD_LINE;
					return;
				}
				
				// now the prev token is a ws block
				// check if the one before the prev is in the same line
				var prevPrev = sf.getPrevFormatToken(prev);
				if(prevPrev == null)
				{
					currentScopeStyle = ScopeStyle.LBRACE_STARTS_NEW_LINE;
				}					
				
			}
		}
		// 2.2 }
		else if(antlrToken.getType() == JavaLexer.RBRACE)
		{
			// only need to check the space if it's of a oneline scope
			var containingScope = context.syntaxContext.scope;
			if(containingScope.oneLine)
			{
				var prev = sf.getPrevFormatToken(this);
				// this is a oneline scope, so there must be some tokens before the }
				assert(prev != null && prev.line() == this.line());
				
				hasOneLineScopeSpace = prev instanceof WsBlock;
				hasSpaceAroundWhenItShould = hasOneLineScopeSpace;
			}
		}
		// 2.3 if there is a space after ,
		else if(antlrToken.getType() == JavaLexer.COMMA)
		{
			var next = sf.getNextFormatToken(this);
			if(next == null)
			{
				hasSpaceAfterComma = true;
			}
			else
			{
				hasSpaceAfterComma = next instanceof WsBlock;
			}
			hasSpaceAroundWhenItShould = hasSpaceAfterComma;
		}
		// 2.4 if there is space after ; or if ; ends a line
		else if(antlrToken.getType() == JavaLexer.SEMI)
		{
			var next = sf.getNextFormatToken(this);
			if(next == null)
			{
				hasSpaceOrNewLineAfterSemi = true;
			}
			else
			{
				hasSpaceOrNewLineAfterSemi = (next instanceof WsBlock || next.line() > this.line());
			}
			hasSpaceAroundWhenItShould = hasSpaceOrNewLineAfterSemi;
		}
		// 3.1 low precedence operators
		else if(additionalAttr.type == Type.OPERATOR_LOW_PRECEDENCE)
		{
			// should have spaces around
			var prev = sf.getPrevFormatToken(this);
			var next = sf.getNextFormatToken(this);
			
			boolean prevSpace = prev == null ? true : prev instanceof WsBlock;
			boolean nextSpace = next == null ? true : next instanceof WsBlock;
			hasSpaceAroundLowPrecOper = prevSpace && nextSpace;
			
			hasSpaceAroundWhenItShould = hasSpaceAroundLowPrecOper;
		}
		// other operators are not evaluated now.

	}
	
///////////////////////////// Format evaluation: Identifier /////////////////////////////
	/**
	 * @return true iff it's an identifier.
	 */
	public boolean isIdentifier() 
	{ 
		switch(additionalAttr.type)
		{
		case CLASS_NAME:
		case CONSTRUCTOR_NAME:
		case ENUM_NAME:
		case FIELD_NAME:
		case FOR_VARIABLE_NAME:
		case INTERFACE_NAME:
		case METHOD_NAME:
		case PARAMETER_NAME:
		case VARIABLE_NAME:
			return true;
		default:
			return false;
		}
	}
	
///////////////////////////// Format evaluation: Length /////////////////////////////
	// Only meaningful if it's an identifier.
	
	private boolean tooLong = false;
	public boolean isTooLong() { return tooLong; }
	
	private boolean tooShort = false;
	public boolean isTooShort() { return tooShort; }
	
	private void judgeLength()
	{
		assert(!tooLong && !tooShort);
		
		if(characters().length() > settings.longestIdentifierLength)
		{
			tooLong = true;
		}
		else if(characters().length() < settings.shortestIdentifierLength)
		{
			tooShort = true;
		}
	}
	
///////////////////////////// Format evaluation: Naming style /////////////////////////////
	
	// How an identifier is named
	public static enum NamingStyle
	{
		// All allow numbers in between
		
		// First letter of each word is capitalised.
		// For classes
		PASCAL_CASE, 
		// First letter of each word, except the first, is capitalised
		// methods and variables
		CAMEL_CASE, 
		// Uppercase and underscores.
		// For constants and enumeration items
		UPPERCASE_UNDERSCORE, 
		// Not one of the above
		OTHER
	}
	
	public static final String PASCAL_REGEX = "[A-Z][\\da-z]*([\\dA-Z][\\da-z]*)*";
	public static final String CAMEL_REGEX = "[a-z][\\da-z]*([\\dA-Z][\\da-z]*)*";
	public static final String UPPERCASE_REGEX = "[A-Z\\d]+(_[A-Z\\d]+)*";
	private static final Pattern pascalRegexPattern = Pattern.compile(PASCAL_REGEX);
	private static final Pattern camelRegexPattern = Pattern.compile(CAMEL_REGEX);
	private static final Pattern uppercaseRegexPattern = Pattern.compile(UPPERCASE_REGEX);
	
	// Only meaningful if it's an identifier.
	
	private NamingStyle namingStyle = NamingStyle.OTHER;
	/** 
	 * @return The current naming style of this 
	 */
	public NamingStyle getNamingStyle() {return namingStyle; }
	/** 
	 * @return The correct naming style for this's type of identifier 
	 */
	public NamingStyle getCorrectNamingStyle() 
	{
		// Decide correct naming style
		switch(additionalAttr.type)
		{
		case CLASS_NAME:
		case INTERFACE_NAME:
		case ENUM_NAME:
			return settings.desiredClassNamingStyle;
		case CONSTRUCTOR_NAME:
		case METHOD_NAME:
			return settings.desiredMethodNamingStyle;
		case FIELD_NAME:
		case VARIABLE_NAME:
			// Check if it's a constant
			// A constant should have static final
			if (additionalAttr.hasOtherModifier(AdditionalAttributes.MODIFIER_STATIC) ||
					additionalAttr.hasOtherModifier(AdditionalAttributes.MODIFIER_FINAL))
			{
				return settings.desiredConstantNamingStyle;
			}
		case FOR_VARIABLE_NAME:
		case PARAMETER_NAME:
			return settings.desiredVariableNamingStyle;
		default:
			return NamingStyle.OTHER;
		}
	}
	/**
	 * @return true iff getNamingStyle() == getCorrectNamingStyle();
	 */
	public boolean isNamingCorrect() { return getNamingStyle() == getCorrectNamingStyle(); }
	
	/**
	 * Decides the current naming style used.
	 * 
	 * Can only be called inside evaluateFormat()
	 * Only meaningful if it's an identifier.
	 */
	private void decideCurrentNamingStyle()
	{
		var text = characters();
		assert(!text.isEmpty());
		
		// Decide current naming style
		// first character is uppercase ->
		// PASCAL, UPPERCASE_UNDERSCORE, or other
		{
			if(Character.isUpperCase(text.charAt(0)))
			{
				// Match uppercase underscore before pascal.
				// Imagine this name ABC, it could theoretically be pascal if A, B, and C were all words.
				// But it's better to treat it as uppercase underscore.
				if(uppercaseRegexPattern.matcher(text).matches())
				{
					namingStyle = NamingStyle.UPPERCASE_UNDERSCORE;
				}
				else if(pascalRegexPattern.matcher(text).matches())
				{
					namingStyle = NamingStyle.PASCAL_CASE;
				}
				else
				{
					namingStyle = NamingStyle.OTHER;
				}
			}
			// otherwise, camel or other
			else
			{
				if(camelRegexPattern.matcher(text).matches())
				{
					namingStyle = NamingStyle.CAMEL_CASE;
				}
				else
				{
					namingStyle = NamingStyle.OTHER;
				}
			}
		}
		
	}

///////////////////////////// Format evaluation: spaces before, after, starting new line or not /////////////////////////////
	// Some code blocks should have space around them or start a new line
	// these following fields are each for a specific case;
	// otherwise, they are all true. They are inited to true and only set false in specific situations.
	
	// When you don't care about which of them this is, but only whether spaces are around it when there should be,
	// then you query this value.
	public boolean hasSpaceAroundWhenItShould = true;
	
	// { <- space, space -> }
	// only meaningful for { or } of one-line scopes
	public boolean hasOneLineScopeSpace = true;
	// if this is a comma, then true iff there is space after it.
	public boolean hasSpaceAfterComma = true;
	// if it's a semi, then true iff there is space or newline after it
	public boolean hasSpaceOrNewLineAfterSemi = true;
	// if it's an operator of low precedence (lower than addition and subtraction)
	// then true iff there is space before and after it.
	public boolean hasSpaceAroundLowPrecOper = true;
	
	
///////////////////////////// Format evaluation: consistent coding style /////////////////////////////
	// Except that one-line scopes don't use a style,
	// whether a { occupies its own line is a matter of choice.
	// But you need to be consistent.
	public static enum ScopeStyle
	{
		LBRACE_STARTS_NEW_LINE,
		LBRACE_STAYS_IN_OLD_LINE
	}
	// only meaningful for { of multiline scopes.
	public ScopeStyle currentScopeStyle;
	
//////////////////////// Settings ////////////////////////
	
	public static final class Settings
	{
		// Default values come from the Java coding convention by oracle.
		
		// Identifier settings
		public int longestIdentifierLength = 15;
		public int shortestIdentifierLength = 4;
		public NamingStyle desiredClassNamingStyle = NamingStyle.PASCAL_CASE;
		public NamingStyle desiredMethodNamingStyle = NamingStyle.CAMEL_CASE;
		public NamingStyle desiredVariableNamingStyle = NamingStyle.CAMEL_CASE;
		public NamingStyle desiredConstantNamingStyle = NamingStyle.UPPERCASE_UNDERSCORE;
		
		// Punctuation settings
		// currently none
		
		// Operator settings
		// currently none
	}
	
	public static final Settings settings = new Settings();
	
}
