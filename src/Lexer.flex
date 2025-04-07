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
  public int      tokenColumn;

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

"num"                               { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.NUM    ; }
"bool"                              { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.BOOL   ; }
"new"                               { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.NEW    ; }
"size"                              { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.SIZE   ; }
"if"                                { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.IF     ; }
"else"                              { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.ELSE   ; }
"while"                             { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.WHILE  ; }
"return"                            { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RETURN ; }
"print"                             { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.PRINT  ; }
"true"|"false"                      { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.BOOL_LIT; }
"{"                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.BEGIN  ; }
"}"                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.END    ; }
"("                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.LPAREN ; }
")"                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RPAREN ; }
"["                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.LBRACKET; }
"]"                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RBRACKET; }
";"                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.SEMI   ; }
","                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.COMMA  ; }
"."                                 { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.DOT    ; }
"<-"                                { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.ASSIGN ; }
"+"|"-"|"or"                        { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.EXPROP ; }
"*"|"/"|"and"                       { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.TERMOP ; }
"<"|">"|"<="|">="|"="|"<>"          { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RELOP  ; }
{num}                               { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.NUM_LIT; }
{identifier}                        { tokenColumn = column; parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.IDENT  ; }
{linecomment}                       { column += yytext().length(); /* skip */ }
{newline}                           { lineno++; column = 1; /* skip */ }
{whitespace}                        { column += yytext().length(); /* skip */ }
{blockcomment}                      {
                                        // update lineno and column if comment contains newlines
                                        for (int i = 0; i < yytext().length(); i++) {
                                           if(yytext().charAt(i)=='\n') { lineno++; column = 1; }
                                           else { column++; }
                                        }
                                    }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
