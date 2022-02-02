parser grammar BasicParser;

options {
    tokenVocab=BasicLexer;
}

program
  : KW_BEGIN func* stat KW_END
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
  | IDENTIFIER #identExpr
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
