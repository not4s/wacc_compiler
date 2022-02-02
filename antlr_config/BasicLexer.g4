lexer grammar BasicLexer;

/* Control */
BEGIN: 'begin' ;
END: 'end' ;
SKIP_: 'skip' ;
ASSIGN: '=' ;
FREE: 'free' ;
RETURN: 'return' ;
EXIT: 'exit' ;
IF: 'if' ;
THEN: 'then' ;
ELSE: 'else' ;
FI: 'fi' ;
WHILE: 'while' ;
DO: 'do' ;
DONE: 'done' ;
CALL: 'call' ;

/* I/O */
READ: 'read' ;
PRINT: 'print' ;
PRINTLN: 'println' ;

/* Operators */
PLUS: '+' ;
MINUS: '-' ;
MUL: '*' ;
DIV: '/' ;

/* Punctuations */
OPEN_PARENTHESES: '(' ;
CLOSE_PARENTHESES: ')' ;
COMMA: ',' ;
SEMICOLON: ';' ;
HASH: '#' ;

/* Types */
fragment DIGIT: '0'..'9' ; 

INTEGER: DIGIT+ ;
NEGATIVE: '-'DIGIT+ ;

NEWPAIR: 'newpair' ;
PAIR: 'pair' ;



