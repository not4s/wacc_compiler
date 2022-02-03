parser grammar BasicParser;

options {
    tokenVocab=BasicLexer;
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

binaryOperator
  : OP_ADD #binaryAdd
  | OP_SUBT #binarySubtract
  | OP_MULT #binaryMultiply
  | OP_DIV #binaryDivide
  | OP_MODULO #binaryModulo
  ;

  logicalOperator
  : OP_GREATER #logicalGreater
  | OP_GREATER_OR_EQUAL #logicalGreaterOrEqual
  | OP_LESS #logicalLess
  | OP_LESS_OR_EQUAL #logicalLessOrEqual
  | OP_EQUAL #logicalEqual
  | OP_NOT_EQUAL #logicalNotEqual
  | OP_AND #logicalAnd
  | OP_OR #logicalOr
  ;

expr
  : literal #literalExpr
  | IDENTIFIER #identExpr
  | expr binaryOperator expr #binaryExpr
  | expr logicalOperator expr #logicalExpr
  | SYM_LBRACKET expr SYM_RBRACKET #bracketExpr
  ;

assignRhs
  : expr #assignRhsExpr
  ;

stat
  : KW_SKIP #skipStat
  | type IDENTIFIER SYM_EQUALS assignRhs #assignRhsStat
  | KW_EXIT expr #exitStat
  | stat SYM_SEMICOLON stat #joinStat
  ;

func: KW_BEGIN KW_BEGIN KW_BEGIN;
