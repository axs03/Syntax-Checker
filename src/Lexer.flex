/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

%%

%class Lexer
%byaccj

%{

  public Parser   parser;
  public int      lineno;
  public int      column;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
  }
%}

num          = [0-9]+("."[0-9]+)?
identifier   = [a-zA-Z][a-zA-Z0-9_]*
newline      = \n
whitespace   = [ \t\r]+
linecomment  = "%%".*
blockcomment = "%*"[^]*"*%"

%%

"num"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NUM    ; }
"bool"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL   ; }
"new"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NEW    ; }
"size"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.SIZE   ; }
"if"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.IF     ; }
"else"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.ELSE   ; }
"while"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.WHILE  ; }
"return"                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.RETURN ; }
"print"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.PRINT  ; }
"true"|"false"                      { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL_LIT; }
"{"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.BEGIN  ; }
"}"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.END    ; }
"("                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LPAREN ; }
")"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RPAREN ; }
"["                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LBRACKET; }
"]"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RBRACKET; }
";"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.SEMI   ; }
","                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.COMMA  ; }
"."                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.DOT    ; }
"<-"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.ASSIGN ; }
"+"|"-"|"or"                        { parser.yylval = new ParserVal((Object)yytext()); return Parser.EXPROP ; }
"*"|"/"|"and"                       { parser.yylval = new ParserVal((Object)yytext()); return Parser.TERMOP ; }
"<"|">"|"<="|">="|"="|"<>"          { parser.yylval = new ParserVal((Object)yytext()); return Parser.RELOP  ; }
{num}                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NUM_LIT; }
{identifier}                        { parser.yylval = new ParserVal((Object)yytext()); return Parser.IDENT  ; }
{linecomment}                       { /* skip */ }
{newline}                           { /* skip */ }
{whitespace}                        { /* skip */ }
{blockcomment}                      { /* skip */ }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
