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
		OPERATOR_UNCLASSIFIED,
		
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
			case JavaLexer.ASSIGN:
			// Comparisons
			case JavaLexer.GT:
			case JavaLexer.LT:
			case JavaLexer.EQUAL:	
			case JavaLexer.LE:
			case JavaLexer.GE:
			case JavaLexer.NOTEQUAL:
			// ...
			case JavaLexer.BANG:
			case JavaLexer.TILDE:
			case JavaLexer.QUESTION:
			case JavaLexer.COLON:
			// Logical
			case JavaLexer.AND:	
			case JavaLexer.OR:
			// Math
			case JavaLexer.INC:
			case JavaLexer.DEC:
			case JavaLexer.ADD:	
			case JavaLexer.SUB:
			case JavaLexer.MUL:
			case JavaLexer.DIV:
			case JavaLexer.BITAND:
			case JavaLexer.BITOR:
			case JavaLexer.CARET:
			case JavaLexer.MOD:
			// Op-assign
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
				this.type = Type.OPERATOR_UNCLASSIFIED;
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
	 * 2. ...
	 */
	@Override
	public void evaluateFormat(SourceFile sf, PrimitiveContext context) 
	{
		super.evaluateFormat(sf, context);
		
		// 1.
		if(isIdentifier())
		{
			// i.
			judgeLength();
			// ii.
			decideCurrentNamingStyle();
		}
		// 2. ...
		else if(false)
		{
			
		}
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
	
	
//////////////////////// Settings ////////////////////////
	
	public static final class Settings
	{
		// Default values come from the Java coding convention by oracle.
		
		public int longestIdentifierLength = 15;
		public int shortestIdentifierLength = 4;
		public NamingStyle desiredClassNamingStyle = NamingStyle.PASCAL_CASE;
		public NamingStyle desiredMethodNamingStyle = NamingStyle.CAMEL_CASE;
		public NamingStyle desiredVariableNamingStyle = NamingStyle.CAMEL_CASE;
		public NamingStyle desiredConstantNamingStyle = NamingStyle.UPPERCASE_UNDERSCORE;
	}
	
	public static final Settings settings = new Settings();
	
}
