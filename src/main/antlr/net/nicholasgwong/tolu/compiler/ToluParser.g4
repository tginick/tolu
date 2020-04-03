parser grammar ToluParser;

options { tokenVocab=ToluLexer; }

program             : TNPCPROGRAM function* EOF;

function            : TFUN identifier TOPENPAREN identifierList? TCLOSEPAREN block;

functionCall        : identifier TOPENPAREN expressionList? TCLOSEPAREN ;

extFunctionCall     : TTILDE functionCall ;

identifier          : TIDENTIFIER ;

identifierList      : identifier (TCOMMA identifier)* ;

expressionList      : expression (TCOMMA expression)* ;

block               : TOPENBRACE (statement)* TCLOSEBRACE ;

statement           : variableAssign TSEMI
                    | extFunctionCall TSEMI
                    | functionCall TSEMI
                    | conditional
                    | whileLoop
                    | returnStatement TSEMI
                    ;

variableAssign      : identifier TEQUAL expression;

conditional         : ifBranch (elsifBranch)* elseBranch;
ifBranch            : TIF exprAndBlock ;
elsifBranch         : TELSE TIF exprAndBlock ;
elseBranch          : TELSE block ;

whileLoop           : TWHILE exprAndBlock ;

returnStatement     : TRETN expression? ;

exprAndBlock        : TOPENPAREN expression TCLOSEPAREN block ;

literal             : TINTEGER
                    | TFLOATING
                    | TBOOLEAN
                    | TSCHARSEQ
                    | TSIGIL TOPENBRACE objKeyDecl* TCLOSEBRACE
                    | TAT identifier
                    ;

objKeyDecl          : identifier TEQUAL expression TCOMMA ;

expression          : TOPENPAREN expression TCLOSEPAREN                                        #parenExpr
                    | literal                                                                  #literalExpr
                    | identifier                                                               #identifierExpr
                    | expression TOPENBRACKET expression TCLOSEBRACKET                         #objAccessExpr
                    | extFunctionCall                                                          #extFunctionCallExpr
                    | functionCall                                                             #functionCallExpr

                    // arithmetic expressions
                    | expression bop=(TSTAR | TDIV) expression                                 #arithMulExpr
                    | expression bop=(TPLUS | TMINUS) expression                               #arithAddExpr

                    // boolean expressions
                    | expression bop=(TLTE | TGTE | TLT | TGT) expression                      #comparisonExpr
                    | expression bop=(TEQUALITY | TNOTEQUAL) expression                        #equalityExpr
                    | expression TLOGICALAND expression                                        #logicalAndExpr
                    | expression TLOGICALOR expression                                         #logicalOrExpr


                    ;


