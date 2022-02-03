lexer grammar WACCLexer;

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
KW_PRINT: 'print';
KW_PRINTLN: 'println';
KW_IF: 'if';
KW_THEN: 'then';
KW_ELSE: 'else';
KW_FI: 'fi';

/* Boolean */
BOOLEAN: 'true' | 'false' ;

/* Identifier */
IDENTIFIER: ID_CHAR (ID_CHAR | DIGIT)*;
fragment ID_CHAR: '_' | 'a'..'z' | 'A'..'Z';

/* Integer */
INTEGER: ('+'|'-')? DIGIT+;
fragment DIGIT: [0-9];

/* Characters and Strings */
CHARACTER: '\'' ASCII '\'';
STRING: '"' ASCII* '"';

fragment ASCII: ~('\\'|'\''|'"') | '\\' ESCAPED_CHAR;
fragment ESCAPED_CHAR: '0'|'b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\';

/* Operators */
OP_ADD: '+';
OP_SUBT: '-';
OP_MULT: '*';
OP_DIV: '/';
OP_MODULO: '%';
OP_GREATER: '>';
OP_GREATER_OR_EQUAL: '>=';
OP_LESS: '<';
OP_LESS_OR_EQUAL: '<=';
OP_EQUAL: '==';
OP_NOT_EQUAL: '!=';
OP_AND: '&&';
OP_OR: '||';
OP_NOT: '!';
OP_ORD: 'ord';
OP_CHR: 'chr';
OP_LEN: 'len';


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
