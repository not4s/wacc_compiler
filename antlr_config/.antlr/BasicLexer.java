// Generated from c:\Users\M'Lord Mika\Desktop\WACC_06\antlr_config\BasicLexer.g4 by ANTLR 4.8
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BasicLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		KW_BEGIN=1, KW_END=2, KW_SKIP=3, KW_EXIT=4, KW_INT=5, KW_BOOL=6, KW_CHAR=7, 
		KW_STRING=8, KW_NULL=9, BOOLEAN=10, IDENTIFIER=11, INTEGER=12, CHARACTER=13, 
		STRING=14, SYM_SEMICOLON=15, SYM_EQUALS=16, SYM_LBRACKET=17, SYM_RBRACKET=18, 
		COMMENT_IGNORE=19, WHITESPACE_IGNORE=20, ANY_IGNORE=21;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"KW_BEGIN", "KW_END", "KW_SKIP", "KW_EXIT", "KW_INT", "KW_BOOL", "KW_CHAR", 
			"KW_STRING", "KW_NULL", "BOOLEAN", "IDENTIFIER", "ID_CHAR", "DIGIT", 
			"INTEGER", "CHARACTER", "STRING", "ASCII", "ESCAPED_CHAR", "SYM_SEMICOLON", 
			"SYM_EQUALS", "SYM_LBRACKET", "SYM_RBRACKET", "WS", "COMMENT_IGNORE", 
			"WHITESPACE_IGNORE", "ANY_IGNORE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'begin'", "'end'", "'skip'", "'exit'", "'int'", "'bool'", "'char'", 
			"'string'", "'null'", null, null, null, null, null, "';'", "'='", "'('", 
			"')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "KW_BEGIN", "KW_END", "KW_SKIP", "KW_EXIT", "KW_INT", "KW_BOOL", 
			"KW_CHAR", "KW_STRING", "KW_NULL", "BOOLEAN", "IDENTIFIER", "INTEGER", 
			"CHARACTER", "STRING", "SYM_SEMICOLON", "SYM_EQUALS", "SYM_LBRACKET", 
			"SYM_RBRACKET", "COMMENT_IGNORE", "WHITESPACE_IGNORE", "ANY_IGNORE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public BasicLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BasicLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\27\u00b6\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4"+
		"\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3"+
		"\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13o\n\13\3\f\3\f\3\f\7"+
		"\ft\n\f\f\f\16\fw\13\f\3\r\3\r\3\16\3\16\3\17\5\17~\n\17\3\17\6\17\u0081"+
		"\n\17\r\17\16\17\u0082\3\20\3\20\3\20\3\20\3\21\3\21\7\21\u008b\n\21\f"+
		"\21\16\21\u008e\13\21\3\21\3\21\3\22\3\22\3\22\5\22\u0095\n\22\3\23\3"+
		"\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\7\31\u00a5"+
		"\n\31\f\31\16\31\u00a8\13\31\3\31\3\31\3\31\3\31\3\32\6\32\u00af\n\32"+
		"\r\32\16\32\u00b0\3\32\3\32\3\33\3\33\2\2\34\3\3\5\4\7\5\t\6\13\7\r\b"+
		"\17\t\21\n\23\13\25\f\27\r\31\2\33\2\35\16\37\17!\20#\2%\2\'\21)\22+\23"+
		"-\24/\2\61\25\63\26\65\27\3\2\t\5\2C\\aac|\3\2\62;\4\2--//\5\2$$))^^\13"+
		"\2$$))\62\62^^ddhhppttvv\5\2\13\f\17\17\"\"\3\2\f\f\2\u00b9\2\3\3\2\2"+
		"\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3"+
		"\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\35\3\2\2"+
		"\2\2\37\3\2\2\2\2!\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2"+
		"\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\3\67\3\2\2\2\5=\3\2\2\2\7A\3"+
		"\2\2\2\tF\3\2\2\2\13K\3\2\2\2\rO\3\2\2\2\17T\3\2\2\2\21Y\3\2\2\2\23`\3"+
		"\2\2\2\25n\3\2\2\2\27p\3\2\2\2\31x\3\2\2\2\33z\3\2\2\2\35}\3\2\2\2\37"+
		"\u0084\3\2\2\2!\u0088\3\2\2\2#\u0094\3\2\2\2%\u0096\3\2\2\2\'\u0098\3"+
		"\2\2\2)\u009a\3\2\2\2+\u009c\3\2\2\2-\u009e\3\2\2\2/\u00a0\3\2\2\2\61"+
		"\u00a2\3\2\2\2\63\u00ae\3\2\2\2\65\u00b4\3\2\2\2\678\7d\2\289\7g\2\29"+
		":\7i\2\2:;\7k\2\2;<\7p\2\2<\4\3\2\2\2=>\7g\2\2>?\7p\2\2?@\7f\2\2@\6\3"+
		"\2\2\2AB\7u\2\2BC\7m\2\2CD\7k\2\2DE\7r\2\2E\b\3\2\2\2FG\7g\2\2GH\7z\2"+
		"\2HI\7k\2\2IJ\7v\2\2J\n\3\2\2\2KL\7k\2\2LM\7p\2\2MN\7v\2\2N\f\3\2\2\2"+
		"OP\7d\2\2PQ\7q\2\2QR\7q\2\2RS\7n\2\2S\16\3\2\2\2TU\7e\2\2UV\7j\2\2VW\7"+
		"c\2\2WX\7t\2\2X\20\3\2\2\2YZ\7u\2\2Z[\7v\2\2[\\\7t\2\2\\]\7k\2\2]^\7p"+
		"\2\2^_\7i\2\2_\22\3\2\2\2`a\7p\2\2ab\7w\2\2bc\7n\2\2cd\7n\2\2d\24\3\2"+
		"\2\2ef\7v\2\2fg\7t\2\2gh\7w\2\2ho\7g\2\2ij\7h\2\2jk\7c\2\2kl\7n\2\2lm"+
		"\7u\2\2mo\7g\2\2ne\3\2\2\2ni\3\2\2\2o\26\3\2\2\2pu\5\31\r\2qt\5\31\r\2"+
		"rt\5\33\16\2sq\3\2\2\2sr\3\2\2\2tw\3\2\2\2us\3\2\2\2uv\3\2\2\2v\30\3\2"+
		"\2\2wu\3\2\2\2xy\t\2\2\2y\32\3\2\2\2z{\t\3\2\2{\34\3\2\2\2|~\t\4\2\2}"+
		"|\3\2\2\2}~\3\2\2\2~\u0080\3\2\2\2\177\u0081\5\33\16\2\u0080\177\3\2\2"+
		"\2\u0081\u0082\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083\36"+
		"\3\2\2\2\u0084\u0085\7)\2\2\u0085\u0086\5#\22\2\u0086\u0087\7)\2\2\u0087"+
		" \3\2\2\2\u0088\u008c\7$\2\2\u0089\u008b\5#\22\2\u008a\u0089\3\2\2\2\u008b"+
		"\u008e\3\2\2\2\u008c\u008a\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008f\3\2"+
		"\2\2\u008e\u008c\3\2\2\2\u008f\u0090\7$\2\2\u0090\"\3\2\2\2\u0091\u0095"+
		"\n\5\2\2\u0092\u0093\7^\2\2\u0093\u0095\5%\23\2\u0094\u0091\3\2\2\2\u0094"+
		"\u0092\3\2\2\2\u0095$\3\2\2\2\u0096\u0097\t\6\2\2\u0097&\3\2\2\2\u0098"+
		"\u0099\7=\2\2\u0099(\3\2\2\2\u009a\u009b\7?\2\2\u009b*\3\2\2\2\u009c\u009d"+
		"\7*\2\2\u009d,\3\2\2\2\u009e\u009f\7+\2\2\u009f.\3\2\2\2\u00a0\u00a1\t"+
		"\7\2\2\u00a1\60\3\2\2\2\u00a2\u00a6\7%\2\2\u00a3\u00a5\n\b\2\2\u00a4\u00a3"+
		"\3\2\2\2\u00a5\u00a8\3\2\2\2\u00a6\u00a4\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7"+
		"\u00a9\3\2\2\2\u00a8\u00a6\3\2\2\2\u00a9\u00aa\7\f\2\2\u00aa\u00ab\3\2"+
		"\2\2\u00ab\u00ac\b\31\2\2\u00ac\62\3\2\2\2\u00ad\u00af\5/\30\2\u00ae\u00ad"+
		"\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\u00ae\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b1"+
		"\u00b2\3\2\2\2\u00b2\u00b3\b\32\2\2\u00b3\64\3\2\2\2\u00b4\u00b5\13\2"+
		"\2\2\u00b5\66\3\2\2\2\f\2nsu}\u0082\u008c\u0094\u00a6\u00b0\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}