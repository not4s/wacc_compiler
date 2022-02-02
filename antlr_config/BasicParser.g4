parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

prog: BEGIN func* stat END ;

stat: SKIP_
      | type ident ASSIGN assign_rhs
      | assign_lhs ASSIGN assign_rhs
      | READ assign_lhs
      | FREE expr
      | RETURN expr
      | EXIT expr
      | PRINT expr
      | PRINTLN expr
      | IF expr THEN stat ELSE stat FI
      | WHILE expr DO stat DONE
      | BEGIN stat END
      | stat SEMICOLON stat
      ;

binaryOper: PLUS | MINUS | MUL | DIV ;

assign_rhs: expr
            | array_liter
            | NEWPAIR OPEN_PARENTHESES expr COMMA expr CLOSE_PARENTHESES
            | pair_elem
            | CALL ident OPEN_PARENTHESES arg_list? CLOSE_PARENTHESES
            ;

pair_type: PAIR OPEN_PARENTHESES
                  pair_elem_type COMMA pair_elem_type
                  CLOSE_PARENTHESES
                  ;

expr: expr binaryOper expr
| INTEGER
| OPEN_PARENTHESES expr CLOSE_PARENTHESES
;

func: SKIP_;
type: SKIP_;
ident: SKIP_;
assign_lhs: SKIP_;
array_liter: SKIP_;
pair_elem: SKIP_;
arg_list: SKIP_;
pair_elem_type: SKIP_;


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