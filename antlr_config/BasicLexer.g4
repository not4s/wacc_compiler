lexer grammar BasicLexer;

/* Control */
BEGIN: 'begin' ;
END: 'end' ;
SKIP_: 'skip' ;
EXIT: 'exit' ;


fragment DIGIT: '0'..'9' ;
INTEGER: DIGIT+ ;
NEGATIVE: '-'DIGIT+ ;

/* Temporary token that should not be matched by anything */
TEMPORARY: 'This shouldnt be matched' ;

/* Ignore comments and white space */
COMMENT_IGNORE: '#' ~'\n'* '\n' -> skip;
WHITESPACE_IGNORE: [ \t\r\n]+ -> skip;


/* Match anything */
ANY_IGNORE: . ;







