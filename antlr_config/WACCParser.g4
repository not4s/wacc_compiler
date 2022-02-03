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
  : sign=(OP_ADD|OP_SUBT)? value=INTEGER   #literalInteger
  | value=(KW_TRUE | KW_FALSE)             #literalBoolean
  | CHAR                                   #literalChar
  | STRING                                 #literalString
  | KW_NULL                                #literalPair
  ;

expr
  : SYM_LBRACKET expr SYM_RBRACKET                             #exprBracket
  | arrayElem                                                  #exprArrayElem

  | unOp=OP_NOT expr                                           #exprBoolUnary
  | unOp=OP_ORD expr                                           #exprIntUnary
  | unOp=OP_CHR expr                                           #exprCharUnary
  | unOp=OP_LEN expr                                           #exprIntUnary
  | unOp=OP_SUBT expr                                          #exprIntUnary

  | left=expr binOp=(OP_MULT | OP_DIV | OP_MOD) right=expr     #exprIntBinary
  | left=expr binOp=(OP_ADD | OP_SUBT) right=expr              #exprIntBinary
  | left=expr binOp=(OP_GT | OP_GEQ |
             OP_LT | OP_LEQ) right=expr                        #exprBoolBinary
  | left=expr binOp=(OP_EQ | OP_NEQ) right=expr                #exprBoolBinary
  | left=expr binOp=OP_AND right=expr                          #exprBoolBinary
  | left=expr binOp=OP_OR right=expr                           #exprBoolBinary

  | literal                                                    #exprLiteral
  | IDENTIFIER                                                 #exprIdentifier
  ;

assignLhs
  : IDENTIFIER #assignLhsExpr
  | arrayElem #assignLhsArrayElem
  ;

assignRhs
  : expr       #assignRhsExpr
  | arrayLiter #assignRhsArrayLiter
  ;

stat
  : KW_SKIP                                       #statSkip
  | KW_EXIT expr                                  #statExit
  | KW_FREE expr                                  #statFree
  | KW_RETURN expr                                #statReturn
  | KW_PRINT expr                                 #statPrint
  | KW_PRINTLN expr                               #statPrintln
  | KW_READ expr                                  #statRead
  | KW_IF ifCond=expr KW_THEN thenBlock=stat KW_ELSE doBlock=stat KW_FI    #statIfThenElse
  | KW_WHILE whileCond=expr KW_DO doBlock=stat KW_DONE                     #statWhileDo
  | KW_BEGIN stat KW_END                          #statBeginEnd
  | type IDENTIFIER SYM_EQUALS assignRhs          #statInit
  | assignLhs SYM_EQUALS assignRhs                #statStore
  | left=stat SYM_SEMICOLON right=stat            #statJoin
  ;

func: KW_BEGIN KW_BEGIN KW_BEGIN;

arrayElem
  : IDENTIFIER (SYM_SQ_LBRACKET expr SYM_SQ_RBRACKET)+
  ;

arrayLiter
  : SYM_SQ_LBRACKET (expr (SYM_COMMA expr)*)? SYM_SQ_RBRACKET # arrayLiterAssignRhs
  ;
