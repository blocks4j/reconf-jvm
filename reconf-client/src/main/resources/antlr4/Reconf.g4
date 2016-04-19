grammar Reconf;

value : primitive
       | structure;

structure: OPEN_BRACKET CLOSE_BRACKET
          |collection
          | map;

collection: '[' value (',' value)* ']';

map: '[' mapEntry (',' mapEntry)* ']';

mapEntry : primitive ':' value;

primitive : LITERAL;


LITERAL
   : '\'''\''
   | '\'' (ESC | ~ ['\\])+ '\''
   ;
fragment ESC
   : '\\' (['\\/bfnrt] | UNICODE)
   ;
fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;
fragment HEX
   : [0-9a-fA-F]
   ;

OPEN_BRACKET : '[';
CLOSE_BRACKET : ']';

WS : [ \t\r\n]+ -> skip ;