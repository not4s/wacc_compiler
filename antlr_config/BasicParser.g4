parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

prog: BEGIN .*? BEGIN .*?
      {notifyErrorListeners("Program cannot have multiple `begin` statements.");}
    | BEGIN END
      {notifyErrorListeners("Program is missing body.");}
    | BEGIN func* stat END
    | BEGIN func* .*? END
      {notifyErrorListeners("Program does not have valid body.");}
    | BEGIN func* .*?
      {notifyErrorListeners("Program is missing `end`.");}
    | .*?
      {notifyErrorListeners("Program is missing `begin`.");}
    ;

stat: SKIP_
    | EXIT INTEGER
    | EXIT NEGATIVE
    | .*?
      {notifyErrorListeners("Invalid statement.");}
    ;

func: TEMPORARY;
