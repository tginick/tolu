lexer grammar ToluLexer;


// comment patterns
BLOCK_COMMENT        : '/*' .*? '*/' -> skip ;

// whitespace
TINTEGER             : DIGIT+ ;
TFLOATING            : DIGIT+'.'DIGIT* ;
TBOOLEAN             : TTRUE|TFALSE ;
TSIGIL               : '$' ;
TTILDE               : '~' ;
TNEWLINE             : '\r'? '\n' -> skip ;
TWHITESPACE          : [ \t]+ -> skip ;

TNPCPROGRAM          : 'npc:' ;

TOPENBRACE           : '{' ;
TCLOSEBRACE          : '}' ;
TOPENBRACKET         : '[' ;
TCLOSEBRACKET        : ']' ;

TAT                  : '@' ;
TSEMI                : ';' ;
TOPENPAREN           : '(' ;
TCLOSEPAREN          : ')' ;

TPLUS                : '+' ;
TMINUS               : '-' ;
TSTAR                : '*' ;
TDIV                 : '/' ;

TEQUAL               : '=' ;

TEQUALITY            : '==' ;
TNOTEQUAL            : '!=' ;
TLT                  : '<' ;
TLTE                 : '<=' ;
TGT                  : '>' ;
TGTE                 : '>=' ;

TQUOTE               : '"' ;

TCOMMA               : ',';

TFUN                 : 'fn' ;

TIF                  : 'if';
TELSE                : 'else';
TWHILE               : 'while';
TBREAK               : 'break';
TCONTINUE            : 'continue';
TTRUE                : 'true';
TFALSE               : 'false';
TLOGICALAND          : 'and';
TLOGICALOR           : 'or';
TRETN                : 'return';



TIDENTIFIER          : IDENTIFIER ;
TSCHARSEQ            : '"' SCHARSEQ '"' ;


// fragments

fragment DIGIT      : [0-9] ;
fragment IDENTIFIER : [A-Za-z_][A-Za-z0-9_]* ;
fragment ESCAPE     : '\\' ['"?abfnrtv\\] ;


fragment SCHAR      : ~["\\\r\n]
                    | ESCAPE
                    | '\\\n'
                    | '\\\r\n'
                    ;
fragment SCHARSEQ   : SCHAR*
                    ;
