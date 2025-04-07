import java.util.*;

public class Parser {
    public static final int ENDMARKER = 0;
    public static final int LEXERROR = 1;

    public static final int NUM = 10;
    public static final int BOOL = 11;
    public static final int BEGIN = 12;
    public static final int END = 13;
    public static final int LPAREN = 14;
    public static final int RPAREN = 15;
    public static final int LBRACKET = 16;
    public static final int RBRACKET = 17;
    public static final int SEMI = 18;
    public static final int COMMA = 19;
    public static final int DOT = 20;
    public static final int ASSIGN = 21;
    public static final int RELOP = 22;
    public static final int EXPROP = 23;
    public static final int TERMOP = 24;
    public static final int IF = 25;
    public static final int ELSE = 26;
    public static final int WHILE = 27;
    public static final int RETURN = 28;
    public static final int PRINT = 29;
    public static final int NEW = 30;
    public static final int SIZE = 31;
    public static final int NUM_LIT = 32;
    public static final int BOOL_LIT = 33;
    public static final int IDENT = 34;

    public class Token
    {
        public int       type;
        public ParserVal attr;

        public Token(int type, ParserVal attr) {
            this.type   = type;
            this.attr   = attr;
        }
    }

    public ParserVal yylval;
    Token _token;
    Lexer _lexer;
    Compiler _compiler;
    public ParseTree.Program _parsetree;
    public String            _errormsg;
    public Parser(java.io.Reader r, Compiler compiler) throws Exception
    {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _lexer     = new Lexer(r, this);
        _token     = null;                  // _token is initially null
        Advance();                          // make _token to point the first token by calling Advance()
    }

    public void Advance() throws Exception {
        int token_type = _lexer.yylex();                                    // get next/first token from lexer
        if (token_type == 0) _token = new Token(ENDMARKER, null);     // if  0 => token is endmarker
        else if (token_type == -1) _token = new Token(LEXERROR, yylval);   // if -1 => there is a lex error
        else _token = new Token(token_type, yylval);   // otherwise, set up _token
    }

    public String Match(int token_type) throws Exception {
        boolean match = (token_type == _token.type);
        String lexeme = "";
        if (_token.attr != null) lexeme = (String) _token.attr.obj;

        if (!match) { // if token does not match
            String expected = tokenString(token_type);
            String found = tokenString(_token.type);
            if (_token.attr != null)
                throw new Exception("\"" + expected + "\" is expected instead of \"" + lexeme + "\" at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
            else
                throw new Exception("\"" + expected + "\" is expected instead of \"" + found + "\" at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
        }  // throw exception with the line col and token/lexeme (indicating parsing error in this assignment)

        if (_token.type != ENDMARKER)    // if token is not endmarker,
            Advance();                  // make token point next token in input by calling Advance()

        return lexeme;
    }

    private String tokenString(int token_type) {
        return switch (token_type) {
            case NUM -> "num";
            case BOOL -> "bool";
            case BEGIN -> "{";
            case END -> "}";
            case LPAREN -> "(";
            case RPAREN -> ")";
            case LBRACKET -> "[";
            case RBRACKET -> "]";
            case SEMI -> ";";
            case COMMA -> ",";
            case DOT -> ".";
            case ASSIGN -> "<-";
            case RELOP -> "relational operator";
            case EXPROP -> "expression operator";
            case TERMOP -> "term operator";
            case IF -> "if";
            case ELSE -> "else";
            case WHILE -> "while";
            case RETURN -> "return";
            case PRINT -> "print";
            case NEW -> "new";
            case SIZE -> "size";
            case NUM_LIT -> "number literal";
            case BOOL_LIT -> "boolean literal";
            case IDENT -> "identifier";
            case ENDMARKER -> "end of file";
            default -> "unknown token";
        };
    }

    public int yyparse() throws Exception {
        try {
            _parsetree = program();
            return 0;
        }
        catch(Exception e)
        {
            _errormsg = e.getMessage();
            return -1;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //      program -> decl_list
    //    decl_list -> decl_list'
    //   decl_list' -> fun_decl decl_list'  |  eps
    //     fun_decl -> type_spec IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
    //       params -> param_list | eps
    //   param_list -> param param_list'
    //  param_list' -> COMMA param param_list' | eps
    //        param -> type_spec IDENT
    //    type_spec -> prim_type type_spec'
    //   type_spec' -> LBRACKET RBRACKET | eps
    //    prim_type -> NUM | BOOL
    //  local_decls -> local_decls'
    // local_decls' -> local_decl local_decls' | eps
    //   local_decl -> type_spec IDENT SEMI
    //    stmt_list -> stmt_list'
    //   stmt_list' -> stmt stmt_list' | eps
    //         stmt -> assign_stmt | print_stmt | return_stmt | if_stmt | while_stmt | compound_stmt
    //  assign_stmt -> IDENT ASSIGN expr SEMI
    //   print_stmt -> PRINT expr SEMI
    //  return_stmt -> RETURN expr SEMI
    //      if_stmt -> IF LPAREN expr RPAREN stmt ELSE stmt
    //   while_stmt -> WHILE LPAREN expr RPAREN stmt
    //compound_stmt -> BEGIN local_decls stmt_list END
    //         args -> arg_list | eps
    //     arg_list -> expr arg_list'
    //    arg_list' -> COMMA expr arg_list' | eps
    //         expr -> term expr'
    //        expr' -> EXPROP term expr' | RELOP term expr' | eps
    //         term -> factor term'
    //        term' -> TERMOP factor term' | eps
    //       factor -> IDENT factor'
    //               | LPAREN expr RPAREN
    //               | NUM_LIT
    //               | BOOL_LIT
    //               | NEW prim_type LBRACKET expr RBRACKET
    //      factor' -> LPAREN args RPAREN
    //               | LBRACKET expr RBRACKET
    //               | DOT SIZE
    //               | eps
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public ParseTree.Program program() throws Exception {
        //      program -> decl_list
        switch(_token.type) {
            case NUM:
            case BOOL:
            case ENDMARKER:
                List<ParseTree.FuncDecl> funcs = decl_list();
                String v1 = Match(ENDMARKER);
                return new ParseTree.Program(funcs);
        }
        throw new Exception("No matching production in program at " + _lexer.lineno + ":" + _lexer.tokenColumn);
    }

    public List<ParseTree.FuncDecl> decl_list() throws Exception {
        //    decl_list -> decl_list'
        switch(_token.type) {
            case NUM:
            case BOOL:
            case ENDMARKER:
                return decl_list_();
        }
        throw new Exception("No matching production in decl_list at " + _lexer.lineno + ":" + _lexer.tokenColumn);
    }

    public List<ParseTree.FuncDecl> decl_list_() throws Exception {
        //   decl_list' -> fun_decl decl_list'  |  eps
        switch(_token.type) {
            case NUM:
            case BOOL:
                ParseTree.FuncDecl       v1 = fun_decl();
                List<ParseTree.FuncDecl> v2 = decl_list_();
                v2.add(0, v1);
                return v2;
            case ENDMARKER:
                return new ArrayList<ParseTree.FuncDecl>();
        }
        throw new Exception("No matching production in decl_list' at " + _lexer.lineno + ":" + _lexer.tokenColumn);
    }

    public ParseTree.FuncDecl fun_decl() throws Exception {
        //     fun_decl -> type_spec IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
        switch(_token.type)
        {
            case NUM:
            case BOOL:
                ParseTree.TypeSpec        v01 = type_spec();
                String                    v02 = Match(IDENT);
                String                    v03 = Match(LPAREN);
                List<ParseTree.Param>     v04 = params();
                String                    v05 = Match(RPAREN);
                String                    v06 = Match(BEGIN);
                List<ParseTree.LocalDecl> v07 = local_decls();
                List<ParseTree.Stmt>      v08 = stmt_list();
                String                    v09 = Match(END);
                return new ParseTree.FuncDecl(v02, v01, v04, v07, v08);
        }
        throw new Exception("No matching production in fun_decl at " + _lexer.lineno + ":" + _lexer.tokenColumn);
    }

    public List<ParseTree.Param> params() throws Exception {
        // params -> param_list | eps
        switch(_token.type) {
            case NUM:
            case BOOL:
                return param_list();
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("No matching production in params at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public List<ParseTree.Param> param_list() throws Exception {
        // param_list -> param param_list'
        switch(_token.type) {
            case NUM:
            case BOOL:
                ParseTree.Param v1 = param();
                List<ParseTree.Param> v2 = param_list_();
                v2.add(0, v1);
                return v2;
        }
        throw new Exception("No matching production in param_list at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public List<ParseTree.Param> param_list_() throws Exception {
        // param_list' -> COMMA param param_list' | eps
        // p.s : I gave up naming with v1 and v2 and so on in some functions
        switch(_token.type) {
            case COMMA:
                String com = Match(COMMA);
                ParseTree.Param p = param();
                List<ParseTree.Param> rest = param_list_();
                rest.add(0, p);
                return rest;
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("No matching production in param_list' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Param param() throws Exception {
        // param -> type_spec IDENT
        switch(_token.type) {
            case NUM:
            case BOOL:
                ParseTree.TypeSpec ts = type_spec();
                String id = Match(IDENT);
                return new ParseTree.Param(id, ts);
        }
        throw new Exception("No matching production in param at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.TypeSpec type_spec() throws Exception {
        //    type_spec -> prim_type type_spec'
        switch(_token.type) {
            case NUM:
            case BOOL:
                ParseTree.PrimType pt = prim_type();
                ParseTree.TypeSpec_ ts_ = type_spec_();
                return new ParseTree.TypeSpec(pt, ts_);
        }
        throw new Exception("No matching production in type_spec at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.TypeSpec_ type_spec_() throws Exception {
        // type_spec' -> LBRACKET RBRACKET | eps
        switch(_token.type) {
            case LBRACKET:
                String v1 = Match(LBRACKET);
                String v2 = Match(RBRACKET);
                return new ParseTree.TypeSpec_Array();
            case IDENT:  // or any token signaling follow of type_spec
            case LPAREN:
            case BEGIN:
            case END:
            case ENDMARKER:
                return new ParseTree.TypeSpec_Value();
        }
        throw new Exception("No matching production in type_spec' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.PrimType prim_type() throws Exception {
        //    prim_type -> NUM | BOOL
        switch(_token.type)
        {
            case BOOL:
                String v1 = Match(BOOL);
                return new ParseTree.PrimTypeBool();
            case NUM:
            {
                String v2 = Match(NUM);
                return new ParseTree.PrimTypeNum();
            }
        }
        throw new Exception("No matching production in prim_type at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public List<ParseTree.LocalDecl> local_decls() throws Exception {
        //  local_decls -> local_decls'
        switch(_token.type)
        {
            case BEGIN:
            case END:
            case RETURN:
            case PRINT:
            case IF:
            case WHILE:
            case NUM:
            case BOOL:
            case IDENT:
                return local_decls_();
        }
        throw new Exception("No matching production in local_decls at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public List<ParseTree.LocalDecl> local_decls_() throws Exception {
        // local_decls' -> local_decl local_decls' | eps
        switch(_token.type)
        {
            case NUM:
            case BOOL:
                ParseTree.LocalDecl ld = local_decl();
                List<ParseTree.LocalDecl> rest = local_decls_();
                rest.add(0, ld);
                return rest;
            case BEGIN:
            case END:
            case RETURN:
            case PRINT:
            case IF:
            case WHILE:
            case IDENT:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("No matching production in local_decls' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.LocalDecl local_decl() throws Exception {
        //   local_decl -> type_spec IDENT SEMI
        switch(_token.type) {
            case NUM:
            case BOOL:
                ParseTree.TypeSpec ts = type_spec();
                String id = Match(IDENT);
                String semi = Match(SEMI);
                return new ParseTree.LocalDecl(id, ts);
        }
        throw new Exception("No matching production in local_decl at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public List<ParseTree.Stmt> stmt_list() throws Exception {
        //    stmt_list -> stmt_list'
        switch(_token.type)
        {
            case BEGIN:
            case END:
            case RETURN:
            case PRINT:
            case IF:
            case WHILE:
            case IDENT:
                return stmt_list_();
        }
        throw new Exception("error");
    }

    public List<ParseTree.Stmt> stmt_list_() throws Exception {
        //   stmt_list' -> stmt stmt_list' | eps
        switch(_token.type) {
            case BEGIN:
            case RETURN:
            case PRINT:
            case IF:
            case WHILE:
            case IDENT:
                ParseTree.Stmt s = stmt();
                List<ParseTree.Stmt> rest = stmt_list_();
                rest.add(0, s);
                return rest;
            case END:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("No matching production in stmt_list' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Stmt stmt() throws Exception {
        // stmt -> assign_stmt | print_stmt | return_stmt | if_stmt | while_stmt | compound_stmt
        switch(_token.type) {
            case IDENT:
                return assign_stmt();
            case BEGIN:
                return compound_stmt();
            case RETURN:
                return return_stmt();
            case PRINT:
                return print_stmt();
            case IF:
                return if_stmt();
            case WHILE:
                return while_stmt();
        }
        throw new Exception("No matching production in stmt at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.StmtAssign assign_stmt() throws Exception {
        //  assign_stmt -> IDENT ASSIGN expr SEMI
        String id = Match(IDENT);
        String as = Match(ASSIGN);
        ParseTree.Expr e = expr();
        String semi = Match(SEMI);
        return new ParseTree.StmtAssign(id, e);
    }

    public ParseTree.StmtPrint print_stmt() throws Exception {
        //   print_stmt -> PRINT expr SEMI
        String pr = Match(PRINT);
        ParseTree.Expr e = expr();
        String semi = Match(SEMI);
        return new ParseTree.StmtPrint(e);
    }

    public ParseTree.StmtReturn return_stmt() throws Exception {
        //  return_stmt -> RETURN expr SEMI
        String re = Match(RETURN);
        ParseTree.Expr e = expr();
        String semi = Match(SEMI);
        return new ParseTree.StmtReturn(e);
    }

    public ParseTree.StmtIf if_stmt() throws Exception {
        //      if_stmt -> IF LPAREN expr RPAREN stmt ELSE stmt
        String ifs = Match(IF);
        String lp = Match(LPAREN);
        ParseTree.Expr cond = expr();
        String rp = Match(RPAREN);
        ParseTree.Stmt thenStmt = stmt();
        String el = Match(ELSE);
        ParseTree.Stmt elseStmt = stmt();
        return new ParseTree.StmtIf(cond, thenStmt, elseStmt);
    }

    public ParseTree.StmtWhile while_stmt() throws Exception {
        //   while_stmt -> WHILE LPAREN expr RPAREN stmt
        String wh = Match(WHILE);
        String lp = Match(LPAREN);
        ParseTree.Expr cond = expr();
        String rp = Match(RPAREN);
        ParseTree.Stmt body = stmt();
        return new ParseTree.StmtWhile(cond, body);
    }

    public ParseTree.Stmt compound_stmt() throws Exception {
        //compound_stmt -> BEGIN local_decls stmt_list END
        String bg = Match(BEGIN);
        List<ParseTree.LocalDecl> locals = local_decls();
        List<ParseTree.Stmt> stmts = stmt_list();
        String end = Match(END);
        return new ParseTree.StmtCompound(locals, stmts);
    }

    public List<ParseTree.Arg> args() throws Exception {
        //         args -> arg_list | eps
        switch(_token.type) {
            case LPAREN:
            case NEW:
            case BOOL_LIT:
            case NUM_LIT:
            case IDENT:
                return arg_list();
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
        }
        throw new Exception("No matching production in args at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public List<ParseTree.Arg> arg_list() throws Exception {
        //     arg_list -> expr arg_list'
        ParseTree.Arg a = new ParseTree.Arg(expr());
        List<ParseTree.Arg> rest = arg_list_();
        rest.add(0, a);
        return rest;
    }

    public List<ParseTree.Arg> arg_list_() throws Exception {
        //    arg_list' -> COMMA expr arg_list' | eps
        switch (_token.type) {
            case COMMA:
                String cm = Match(COMMA);
                ParseTree.Arg a = new ParseTree.Arg(expr());
                List<ParseTree.Arg> rest = arg_list_();
                rest.add(0, a);
                return rest;
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
        }
        throw new Exception("No matching production in arg_list' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Expr expr() throws Exception {
        //         expr -> term expr'
        switch(_token.type) {
            case LPAREN:
            case NEW:
            case BOOL_LIT:
            case NUM_LIT:
            case IDENT:
                ParseTree.Term t = term();
                ParseTree.Expr_ e_ = expr_();
                return new ParseTree.Expr(t, e_);
        }
        throw new Exception("No matching production in expr at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Expr_ expr_() throws Exception {
        //        expr' -> EXPROP term expr' | RELOP term expr' | eps
        switch(_token.type) {
            case EXPROP:
            case RELOP:
                String op = Match(_token.type); // EXPROP or RELOP accordingly
                ParseTree.Term t = term();
                ParseTree.Expr_ eRest = expr_();
                return new ParseTree.Expr_(op, t, eRest);

            case RPAREN:
            case RBRACKET:
            case SEMI:
            case COMMA:
                return new ParseTree.Expr_();
        }
        throw new Exception("No matching production in expr' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Term term() throws Exception {
        //         term -> factor term'
        switch(_token.type) {
            case LPAREN:
            case NEW:
            case BOOL_LIT:
            case NUM_LIT:
            case IDENT:
                ParseTree.Factor f = factor();
                ParseTree.Term_ t_ = term_();
                return new ParseTree.Term(f,t_);
        }
        throw new Exception("No matching production in term at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Term_ term_() throws Exception {
        //        term' -> TERMOP factor term' | eps
        switch(_token.type) {
            case TERMOP:
                String op = Match(TERMOP);
                ParseTree.Factor f = factor();
                ParseTree.Term_ rest = term_();
                return new ParseTree.Term_(op, f, rest);

            case RPAREN:
            case RBRACKET:
            case RELOP:
            case EXPROP:
            case SEMI:
            case COMMA:
            case END:
                return new ParseTree.Term_();
        }
        throw new Exception("No matching production in term' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Factor factor() throws Exception {
        //       factor -> IDENT factor'
        //               | LPAREN expr RPAREN
        //               | NUM_LIT
        //               | BOOL_LIT
        //               | NEW prim_type LBRACKET expr RBRACKET
        switch(_token.type) {
            case LPAREN:
                String lp = Match(LPAREN);
                ParseTree.Expr e = expr();
                String rp = Match(RPAREN);
                return new ParseTree.FactorParen(e);
            case NUM_LIT:
                String num = Match(NUM_LIT);
                return new ParseTree.FactorNumLit(Double.parseDouble(num));
            case BOOL_LIT:
                String boolVal = Match(BOOL_LIT);
                return new ParseTree.FactorBoolLit(Boolean.parseBoolean(boolVal));
            case IDENT:
                String id = Match(IDENT);
                ParseTree.Factor_ f_ = factor_();
                return new ParseTree.FactorIdentExt(id, f_);
            case NEW:
                String n = Match(NEW);
                ParseTree.PrimType pt = prim_type();
                String lb = Match(LBRACKET);
                ParseTree.Expr exprInside = expr();
                String rb = Match(RBRACKET);
                return new ParseTree.FactorNew(pt, exprInside);
        }
        throw new Exception("No matching production in factor at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

    public ParseTree.Factor_ factor_() throws Exception {
        //      factor' -> LPAREN args RPAREN
        //               | LBRACKET expr RBRACKET
        //               | DOT SIZE
        //               | eps
        switch(_token.type) {
            case LPAREN:
                String lp = Match(LPAREN);
                List<ParseTree.Arg> args = args();
                String rp = Match(RPAREN);
                return new ParseTree.FactorIdent_ParenArgs(args);
            case LBRACKET:
                String lb = Match(LBRACKET);
                ParseTree.Expr e = expr();
                String rb = Match(RBRACKET);
                return new ParseTree.FactorIdent_BrackExpr(e);
            case DOT:
                String dot = Match(DOT);
                String sze = Match(SIZE);
                return new ParseTree.FactorIdent_DotSize();

            case RPAREN:
            case RBRACKET:
            case RELOP:
            case EXPROP:
            case TERMOP:
            case SEMI:
            case COMMA:
                return new ParseTree.FactorIdent_Eps();
        }
        throw new Exception("No matching production in factor' at " + _lexer.lineno + ":" + _lexer.tokenColumn + ".");
    }

}
