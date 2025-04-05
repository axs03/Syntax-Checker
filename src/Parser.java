import java.util.List;
import java.util.ArrayList;

public class Parser
{
    public static final int ENDMARKER   =  0;
    public static final int LEXERROR    =  1;

    public static final int NUM         = 10;
    public static final int BEGIN       = 11;
    public static final int END         = 12;
    public static final int LPAREN      = 13;
    public static final int RPAREN      = 14;
    public static final int SEMI        = 15;
    public static final int NUM_LIT     = 16;
    public static final int IDENT       = 17;

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

    public void Advance() throws Exception
    {
        int token_type = _lexer.yylex();                                    // get next/first token from lexer
        if(token_type ==  0)      _token = new Token(ENDMARKER , null);     // if  0 => token is endmarker
        else if(token_type == -1) _token = new Token(LEXERROR  , yylval);   // if -1 => there is a lex error
        else                      _token = new Token(token_type, yylval);   // otherwise, set up _token
    }

    public String Match(int token_type) throws Exception
    {
        boolean match = (token_type == _token.type);
        String lexeme = "";
        if(_token.attr != null) lexeme = (String)_token.attr.obj;

        if(match == false)                          // if token does not match
            throw new Exception("token mismatch");  // throw exception (indicating parsing error in this assignment)

        if(_token.type != ENDMARKER)    // if token is not endmarker,
            Advance();                  // make token point next token in input by calling Advance()

        return lexeme;
    }

    public int yyparse() throws Exception
    {
        try
        {
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
    //     fun_decl -> prim_type IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
    //    prim_type -> NUM
    //       params -> eps
    //  local_decls -> local_decls'
    // local_decls' -> eps
    //    stmt_list -> stmt_list'
    //   stmt_list' -> eps
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public ParseTree.Program program() throws Exception
    {
        //      program -> decl_list
        switch(_token.type)
        {
            case NUM:
            case ENDMARKER:
                List<ParseTree.FuncDecl> funcs = decl_list();
                String v1 = Match(ENDMARKER);
                return new ParseTree.Program(funcs);
        }
        throw new Exception("error");
    }
    public List<ParseTree.FuncDecl> decl_list() throws Exception
    {
        //    decl_list -> decl_list'
        switch(_token.type)
        {
            case NUM:
            case ENDMARKER:
                return decl_list_();
        }
        throw new Exception("error");
    }
    public List<ParseTree.FuncDecl> decl_list_() throws Exception
    {
        //   decl_list' -> fun_decl decl_list'  |  eps
        switch(_token.type)
        {
            case NUM:
                ParseTree.FuncDecl       v1 = fun_decl  ();
                List<ParseTree.FuncDecl> v2 = decl_list_();
                v2.add(0, v1);
                return v2;
            case ENDMARKER:
                return new ArrayList<ParseTree.FuncDecl>();
        }
        throw new Exception("error");
    }
    public ParseTree.FuncDecl fun_decl() throws Exception
    {
        //     fun_decl -> prim_type IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
        switch(_token.type)
        {
            case NUM:
                ParseTree.TypeSpec        v01 = prim_type(  );
                String                    v02 = Match(IDENT );
                String                    v03 = Match(LPAREN);
                List<ParseTree.Param>     v04 = params(     );
                String                    v05 = Match(RPAREN);
                String                    v06 = Match(BEGIN );
                List<ParseTree.LocalDecl> v07 = local_decls();
                List<ParseTree.Stmt>      v08 = stmt_list(  );
                String                    v09 = Match(END   );
                return new ParseTree.FuncDecl(v02, v01, v04, v07, v08);
        }
        throw new Exception("error");
    }
    public ParseTree.TypeSpec prim_type() throws Exception
    {
        //    prim_type -> NUM
        switch(_token.type)
        {
            case NUM:
            {
                String v1 = Match(NUM);
                return new ParseTree.TypeSpec(new ParseTree.PrimTypeNum(), new ParseTree.TypeSpec_Value());
            }
        }
        throw new Exception("error");
    }
    public List<ParseTree.Param> params() throws Exception
    {
        //       params -> eps
        switch(_token.type)
        {
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("error");
    }
    public List<ParseTree.LocalDecl> local_decls() throws Exception
    {
        //  local_decls -> local_decls'
        switch(_token.type)
        {
            case END:
                return local_decls_();
        }
        throw new Exception("error");
    }
    public List<ParseTree.LocalDecl> local_decls_() throws Exception
    {
        // local_decls' -> eps
        switch(_token.type)
        {
            case END:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("error");
    }
    public List<ParseTree.Stmt> stmt_list() throws Exception
    {
        //    stmt_list -> stmt_list'
        switch(_token.type)
        {
            case END:
                return stmt_list_();
        }
        throw new Exception("error");
    }
    public List<ParseTree.Stmt> stmt_list_() throws Exception
    {
        //   stmt_list' -> eps
        switch(_token.type)
        {
            case END:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("error");
    }
}
