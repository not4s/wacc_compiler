lexer grammar BasicLexer;

/* Keywords */
KW_BEGIN: 'begin' ;
KW_END: 'end' ;
KW_SKIP: 'skip' ;
KW_EXIT: 'exit' ;
KW_INT: 'int';
KW_BOOL: 'bool';
KW_CHAR: 'char';
KW_STRING: 'string';
KW_NULL: 'null';

/* Boolean */
BOOLEAN: 'true' | 'false' ;

/* Identifier */
IDENTIFIER: ID_CHAR (ID_CHAR | DIGIT)*;
fragment ID_CHAR: '_' | 'a'..'z' | 'A'..'Z';

/* Integer */
fragment DIGIT: [0-9] ;
INTEGER: ('+'|'-')? DIGIT+ ;

/* Chars, string */
CHARACTER: '\'' ASCII '\'';
STRING: '"' ASCII* '"';

fragment ASCII: ~('\\'|'\''|'"') | '\\' ESCAPED_CHAR;
fragment ESCAPED_CHAR: '0'|'b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\';

/* Symbols */
SYM_SEMICOLON: ';';
SYM_EQUALS: '=';
SYM_LBRACKET: '(';
SYM_RBRACKET: ')';
fragment WS: [ \t\r\n];

/* Ignore comments and white space */
COMMENT_IGNORE: '#' ~'\n'* '\n' -> skip;
WHITESPACE_IGNORE: WS+ -> skip;


/* Match anything */
ANY_IGNORE: . ;
