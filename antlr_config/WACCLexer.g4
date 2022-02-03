lexer grammar WACCLexer;

/*** Keywords ***/

/* Simple */
KW_BEGIN: 'begin' ;
KW_END: 'end' ;
KW_SKIP: 'skip' ;
KW_EXIT: 'exit' ;
KW_RETURN: 'return';

/* Base types */
KW_INT: 'int';
KW_BOOL: 'bool';
KW_CHAR: 'char';
KW_STRING: 'string';

/* Pair stuff */
KW_FREE: 'free';
KW_NEWPAIR: 'newpair';
KW_PAIR: 'pair';
KW_FST: 'fst';
KW_SND: 'snd';

/* Type literals */
KW_TRUE: 'true';
KW_FALSE: 'false';
KW_NULL: 'null';

/* IO */
KW_PRINT: 'print';
KW_PRINTLN: 'println';
KW_READ: 'read';

/* Control flow */
KW_IF: 'if';
KW_THEN: 'then';
KW_ELSE: 'else';
KW_FI: 'fi';

KW_WHILE: 'while';
KW_DO: 'do';
KW_DONE: 'done';

/* Operators */
OP_ORD: 'ord';
OP_CHR: 'chr';
OP_LEN: 'len';

OP_ADD: '+';
OP_SUBT: '-';
OP_MULT: '*';
OP_DIV: '/';
OP_MOD: '%';

OP_GT: '>';
OP_GEQ: '>=';
OP_LT: '<';
OP_LEQ: '<=';
OP_EQ: '==';
OP_NEQ: '!=';
OP_AND: '&&';
OP_OR: '||';
OP_NOT: '!';

/* Symbols */
SYM_SEMICOLON: ';';
SYM_EQUALS: '=';
SYM_LBRACKET: '(';
SYM_RBRACKET: ')';
SYM_SQ_LBRACKET: '[';
SYM_SQ_RBRACKET: ']';
SYM_COMMA: ',';
SYM_DOUBLEQUOTE: '"' -> more, mode(IN_STRING);
SYM_SINGLEQUOTE: '\'' -> more, mode(IN_CHAR);

/* Identifier */
IDENTIFIER: ID_CHAR (ID_CHAR | DIGIT)*;
fragment ID_CHAR: '_' | 'a'..'z' | 'A'..'Z';

/* Integer */
INTEGER: DIGIT+;
fragment DIGIT: [0-9];

fragment ESCAPED_CHAR: '0'|'b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\';

/* Ignore comments and white space */
COMMENT_IGNORE: '#' ~'\n'* '\n' -> skip;
WS: [ \t\r\n]+ -> channel(HIDDEN);

/* Match anything */
ANY_IGNORE: . ;

mode IN_STRING;
STRING: '"' -> mode(DEFAULT_MODE);
ASCII: (~('\\'|'\''|'"') | '\\' ESCAPED_CHAR) -> more;

mode IN_CHAR;
CHAR: (~('\\'|'\''|'"') | '\\' ESCAPED_CHAR) '\'' -> mode(DEFAULT_MODE);
