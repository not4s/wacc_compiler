parser grammar WACCParser;

options {
    tokenVocab=WACCLexer;
}

program
  : KW_BEGIN func* stat KW_END EOF
  ;

type
  : baseType  #typeBaseType
  | arrayType #typeArrayType
  | pairType #typePairType
  ;

/* Need to expand to avoid mutual left-recursion */
arrayType
  : baseType SYM_SQ_LBRACKET SYM_SQ_RBRACKET #arrayTypeBaseType
  | arrayType SYM_SQ_LBRACKET SYM_SQ_RBRACKET #arrayTypeArrayType
  | pairType SYM_SQ_LBRACKET SYM_SQ_RBRACKET #arrayTypePairType
  ;

arrayElem
  : IDENTIFIER (SYM_SQ_LBRACKET expr SYM_SQ_RBRACKET)+
  ;

arrayLiter
  : SYM_SQ_LBRACKET (expr (SYM_COMMA expr)*)? SYM_SQ_RBRACKET # arrayLiterAssignRhs
  ;

pairElem
  : KW_FST expr #pairElemFst
  | KW_SND expr #pairElemSnd
  ;
pairType
  : KW_PAIR SYM_LBRACKET left=pairElemType SYM_COMMA right=pairElemType SYM_RBRACKET
  ;

pairElemType
  : baseType                               #pairElemTypeBaseType
  | arrayType                              #pairElemTypeArrayType
  | KW_PAIR                                #pairElemTypeKwPair
  ;

baseType
  : KW_INT                                 #baseTypeInt
  | KW_BOOL                                #baseTypeBool
  | KW_CHAR                                #baseTypeChar
  | KW_STRING                              #baseTypeString
  ;

literal
  : sign=(OP_ADD|OP_SUBT)? value=INTEGER   #literalInteger
  | value=(KW_TRUE | KW_FALSE)             #literalBoolean
  | CHAR                                   #literalChar
  | STRING                                 #literalString
  | KW_NULL                                #literalPair
  ;

expr
  : SYM_LBRACKET innerExpr=expr SYM_RBRACKET                   #exprBracket
  | arrayElem                                                  #exprArrayElem
  | literal                                                    #exprLiteral

  | unOp=OP_NOT operand=expr                                   #exprUnary
  | unOp=OP_ORD operand=expr                                   #exprUnary
  | unOp=OP_CHR operand=expr                                   #exprUnary
  | unOp=OP_LEN operand=expr                                   #exprUnary
  | unOp=OP_SUBT operand=expr                                  #exprUnary

  | left=expr binOp=(OP_MULT | OP_DIV | OP_MOD) right=expr     #exprBinary
  | left=expr binOp=(OP_ADD | OP_SUBT) right=expr              #exprBinary
  | left=expr binOp=(OP_GT | OP_GEQ |
             OP_LT | OP_LEQ) right=expr                        #exprBinary
  | left=expr binOp=(OP_EQ | OP_NEQ) right=expr                #exprBinary
  | left=expr binOp=OP_AND right=expr                          #exprBinary
  | left=expr binOp=OP_OR right=expr                           #exprBinary

  | IDENTIFIER                                                 #exprIdentifier
  ;

assignLhs
  : IDENTIFIER #assignLhsExpr
  | arrayElem #assignLhsArrayElem
  | pairElem #assignLhsPairElem
  ;

assignRhs
  : expr       #assignRhsExpr
  | arrayLiter                                                             #assignRhsArrayLiter
  | KW_NEWPAIR SYM_LBRACKET left=expr SYM_COMMA right=expr SYM_RBRACKET    #assignRhsNewPair
  | pairElem                                                               #assignRhsPairElem
  | KW_CALL IDENTIFIER SYM_LBRACKET argList? SYM_RBRACKET                  #assignRhsCall
  ;

argList
  : expr (SYM_COMMA expr)*
  ;

stat
  : KW_SKIP                                                                #statSkip
  | KW_EXIT expr                                                           #statExit
  | KW_FREE expr                                                           #statFree
  | KW_RETURN expr                                                         #statReturn
  | KW_PRINT expr                                                          #statPrint
  | KW_PRINTLN expr                                                        #statPrintln
  | KW_READ assignLhs                                                      #statRead
  | KW_IF ifCond=expr KW_THEN thenBlock=stat KW_ELSE elseBlock=stat KW_FI  #statIfThenElse
  | KW_WHILE whileCond=expr KW_DO doBlock=stat KW_DONE                     #statWhileDo
  | KW_BEGIN stat KW_END                                                   #statBeginEnd
  | type IDENTIFIER SYM_EQUALS assignRhs                                   #statInit
  | assignLhs SYM_EQUALS assignRhs                                         #statStore
  | left=stat SYM_SEMICOLON right=stat                                     #statJoin
  ;

param
  : type IDENTIFIER
  ;

paramList
  : param (SYM_COMMA param)*
  ;

func
  : type IDENTIFIER SYM_LBRACKET paramList? SYM_RBRACKET KW_IS stat KW_END;
