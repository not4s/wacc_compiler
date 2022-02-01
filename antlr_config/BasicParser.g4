parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

prog: 'begin' func* stat 'end' ;

stat: 'skip'
      | type ident '=' assign_rhs
      | assign_lhs '=' assign_rhs
      | 'read' assign_lhs
      | 'free' expr
      | 'return' expr
      | 'exit' expr
      | 'print' expr
      | 'println' expr
      | 'if' expr 'then' stat 'else' stat 'fi'
      | 'while' expr 'do' stat 'done'
      | 'begin' stat 'end'
      | stat ';' stat
      ;

binaryOper: PLUS | MINUS | MULT | DIV ;

assign_rhs: 'expr'
            | array_liter
            | 'new pair' OPEN_PARENTHESES expr ',' expr CLOSE_PARENTHESES
            | pair_elem
            | 'call' ident OPEN_PARENTHESES arg_list? CLOSE_PARENTHESES
            ;

pair_type: 'pair' OPEN_PARENTHESES
                  pair_elem_type ',' pair_elem_type
                  CLOSE_PARENTHESES
                  ;

expr: expr binaryOper expr
| INTEGER
| OPEN_PARENTHESES expr CLOSE_PARENTHESES
;

/*
 TODO: Parser Rules for
       expr
       func
       type
       ident
       assign_lhs
       array_liter
       pair_elem
       pair_elem_type
       arg_list
 */