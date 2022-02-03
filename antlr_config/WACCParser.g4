parser grammar WACCParser;

options {
    tokenVocab=WACCLexer;
}

program
  : KW_BEGIN func* stat KW_END EOF
  ;

type
  : baseType #typeBaseType
  ;

baseType
  : KW_INT #baseTypeInt
  | KW_BOOL #baseTypeBool
  | KW_CHAR #baseTypeChar
  | KW_STRING #baseTypeString
  ;

literal
  : INTEGER #integerLiteral
  | BOOLEAN #booleanLiteral
  | CHARACTER #charLiteral
  | STRING #stringLiteral
  | KW_NULL #pairLiteral
  ;

expr
  : literal #literalExpr
  | IDENTIFIER                                #identExpr
  | OP_NOT expr                               #unaryNotExpr
  | expr (OP_MULT | OP_DIV | OP_MODULO) expr  #binaryExprFirstPrecedence
  | expr (OP_ADD | OP_DIV) expr               #binaryExprSecondPrecedence
  | expr (OP_GREATER | OP_GREATER_OR_EQUAL | 
             OP_LESS | OP_LESS_OR_EQUAL) expr #logicalExprFirstPrecedence
  | expr (OP_EQUAL | OP_NOT_EQUAL) expr       #logicalExprSecondPrecedence
  | expr OP_AND expr                          #logicalExprThirdPrecedence
  | expr OP_OR expr                           #logicalExprFourthPrecedence
  | SYM_LBRACKET expr SYM_RBRACKET            #bracketExpr
  ;

assignLhs
  : ident #assignLhsExpr
  ;

assignRhs
  : expr #assignRhsExpr
  ;

stat
  : KW_SKIP #skipStat
  | KW_EXIT expr #exitStat
  | KW_PRINT expr #printStat
  | KW_PRINTLN expr #printlnStat
  | type IDENTIFIER SYM_EQUALS assignRhs #assignRhsStat
  | assignLhs SYM_EQUALS assignRhs #assignLhsStat
  | stat SYM_SEMICOLON stat #joinStat
  ;

func: KW_BEGIN KW_BEGIN KW_BEGIN;
