parser grammar WACCParser;

options {
    tokenVocab=WACCLexer;
}

program
  : KW_BEGIN func* stat KW_END EOF
  ;

type
  : baseType  #typeBaseType
  | type SYM_SQ_LBRACKET SYM_SQ_RBRACKET #typeArrayType
  ;

baseType
  : KW_INT    #baseTypeInt
  | KW_BOOL   #baseTypeBool
  | KW_CHAR   #baseTypeChar
  | KW_STRING #baseTypeString
  ;

literal
  : (OP_ADD|OP_SUBT)? INTEGER   #integerLiteral
  | BOOLEAN   #booleanLiteral
  | CHARACTER #charLiteral
  | STRING    #stringLiteral
  | KW_NULL   #pairLiteral
  ;

unaryOperator
  : OP_NOT     #unaryNotOperator
  | OP_ORD     #unaryOrdOperator
  | OP_CHR     #unaryChrOperator
  | OP_LEN     #unaryLenOperator
  | OP_SUBT    #unaryNegOperator
  ;

expr
  : unaryOperator expr                        #unaryExpr
  | SYM_LBRACKET expr SYM_RBRACKET            #bracketExpr
  | expr (OP_MULT | OP_DIV | OP_MODULO) expr  #binaryExprFirstPrecedence
  | expr (OP_ADD | OP_SUBT) expr              #binaryExprSecondPrecedence
  | expr (OP_GREATER | OP_GREATER_OR_EQUAL |
             OP_LESS | OP_LESS_OR_EQUAL) expr #logicalExprFirstPrecedence
  | expr (OP_EQUAL | OP_NOT_EQUAL) expr       #logicalExprSecondPrecedence
  | expr OP_AND expr                          #logicalExprThirdPrecedence
  | expr OP_OR expr                           #logicalExprFourthPrecedence
  | literal                                   #literalExpr
  | IDENTIFIER                                #identExpr
  ;

assignLhs
  : IDENTIFIER #assignLhsExpr
  ;

assignRhs
  : expr       #assignRhsExpr
  | arrayLiter #assignRhsArrayLiter
  ;

stat
  : KW_SKIP                                       #skipStat
  | KW_EXIT expr                                  #exitStat
  | KW_PRINT expr                                 #printStat
  | KW_PRINTLN expr                               #printlnStat
  | KW_READ expr                                  #readStat
  | KW_IF expr KW_THEN stat KW_ELSE stat KW_FI    #ifThenElseStat
  | KW_WHILE expr KW_DO stat KW_DONE              #whileDoDoneStat
  | type IDENTIFIER SYM_EQUALS assignRhs          #assignRhsStat
  | assignLhs SYM_EQUALS assignRhs                #assignLhsStat
  | stat SYM_SEMICOLON stat                       #joinStat
  ;

func: KW_BEGIN KW_BEGIN KW_BEGIN;

arrayLiter
  : SYM_SQ_LBRACKET (expr (SYM_COMMA expr)*)? SYM_SQ_RBRACKET # arrayLiterAssignRhs
  ;
