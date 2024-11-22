package lox.lox;

import lox.error.Error;
import lox.error.ParserError;

import lox.scanner.Token;
import lox.scanner.TokenType;

import java.util.ArrayList;

public class Parser {
    
    ArrayList<Token> tokens;
    private int currentToken=0;

    public Parser(ArrayList<Token> tokens){
        this.tokens = tokens;
    }

    public ArrayList<Statement> parse(){

        ArrayList<Statement> statements = new ArrayList<>();
        while (!noMoreTokensToConsume()){
            if (getCurrentToken().type == TokenType.EOF) break;
                statements.add(parseDeclaration());
        }
        return statements;

    }

    // Methods for grammar definitions

    // declaration -> varDec | statement
    private Statement parseDeclaration(){
       try{
            if (matchCurrentToken(TokenType.VAR)) return parseVarDeclaration();
            else return parseStatement(); 
       } catch (ParserError err){
            synchronize();
       } catch (IndexOutOfBoundsException err){
            synchronize();
       }
        return null;
    }

    // varDec -> "var" IDENTIFIER ("=" expression)? ";"
    private Statement parseVarDeclaration() {

       consumeToken(TokenType.VAR);
       if (! matchCurrentToken(TokenType.IDENTIFIER)) consumeToken(TokenType.IDENTIFIER);

       VariableExpression varName = (VariableExpression) parsePrimary();

       Expression initializer = null;
       if (matchCurrentToken(TokenType.ASSIGNMENT)) {
            consumeToken(TokenType.ASSIGNMENT);
            initializer = parseExpression();
       }

       consumeToken(TokenType.SEMICOLON);
       return new VarDecStatement(varName.name, initializer);

    }


    // statement -> expresssionStatment | printStatement | blockStatement | ifElseStatement | whileStatement
    private Statement parseStatement(){

        if (matchCurrentToken(TokenType.PRINT)) return parsePrintStatement();
        else if (matchCurrentToken(TokenType.LEFT_BRACE)) return parseBlockStatement();
        else if (matchCurrentToken(TokenType.IF)) return parseIfElseStatement();
        else if (matchCurrentToken(TokenType.WHILE)) return parseWhileStatement();
        else if (matchCurrentToken(TokenType.FOR)) return parseForStatement();
        
        return parseExpressionStatement();
    }
    
    // for -> "for" "(" (varDeclaration | expressionStatement | ";") expression? ";" expression? ")" statement
    // Desugaring approach 
    // Desugaring `for` into `while` statement
    private Statement parseForStatement(){
        consumeToken(TokenType.FOR);
        consumeToken(TokenType.LEFT_PAREN);

        Statement initializer = null;
        if (matchCurrentToken(TokenType.VAR)) initializer = parseVarDeclaration();
        else if (matchCurrentToken(TokenType.SEMICOLON)) consumeToken(TokenType.SEMICOLON);
        else initializer = parseExpressionStatement();

        Expression condition = null;
        if (!matchCurrentToken(TokenType.SEMICOLON)) condition = parseExpression();
        consumeToken(TokenType.SEMICOLON);

        Expression increment = null;
        if (!matchCurrentToken(TokenType.RIGHT_PAREN)) increment = parseExpression();

        consumeToken(TokenType.RIGHT_PAREN);
        Statement statementBody = parseStatement();
        
        ArrayList<Statement> newStatements = new ArrayList<>();
        if (increment != null){
            newStatements.add(statementBody);
            newStatements.add(new ExpressionStatement(increment));
            statementBody = new BlockStatement(newStatements);
        }

        if (condition == null) condition = new LiteralExpression(true);
        Statement whileStatement = new WhileStatement(condition, statementBody);

        if (initializer != null){

            ArrayList<Statement> initAndWhile = new ArrayList<>();
            initAndWhile.add(initializer);
            initAndWhile.add(whileStatement);

            whileStatement = new BlockStatement(initAndWhile);
        }
        return whileStatement;
    }

    // whileStatement -> "while" "(" expression ")" statement
    private Statement parseWhileStatement(){
        
        consumeToken(TokenType.WHILE);

        consumeToken(TokenType.LEFT_PAREN);
        Expression condition = parseExpression();
        consumeToken(TokenType.RIGHT_PAREN);

        Statement statementBody = parseStatement();

        return new WhileStatement(condition, statementBody);
    }

    // ifElseStatement -> "if" "(" expression ")" statement ("else" statement)?
    private Statement parseIfElseStatement(){

        consumeToken(TokenType.IF);
        consumeToken(TokenType.LEFT_PAREN);
        Expression expression = parseExpression();
        consumeToken(TokenType.RIGHT_PAREN);

        Statement ifStatement = parseStatement();
        Statement elseStatement = null;
        if (matchCurrentToken(TokenType.ELSE)){
            consumeToken(TokenType.ELSE);
            elseStatement = parseStatement();
        }

        return new IfElseStatement(expression, ifStatement, elseStatement);
    }

    // printStatement -> "print" expression ";"
    private Statement parseBlockStatement(){
        consumeToken(TokenType.LEFT_BRACE);
        ArrayList<Statement> statements = new ArrayList<>();

        while (!matchCurrentToken(TokenType.RIGHT_BRACE) && !noMoreTokensToConsume()){
            statements.add(parseDeclaration());
        }

        consumeToken(TokenType.RIGHT_BRACE);
        return new BlockStatement(statements); 
    }

    // printStatement -> "print" expression ";"
    private Statement parsePrintStatement(){

        consumeToken(TokenType.PRINT);
        Expression expr = parseExpression();
        consumeToken(TokenType.SEMICOLON);

        return new PrintStatement(expr);
    }

    // expressionStatement -> expression ";"
    private Statement parseExpressionStatement(){

        Expression expr = parseExpression();
        consumeToken(TokenType.SEMICOLON);
        return new ExpressionStatement(expr);
    }

    // expression -> assignment
    private Expression parseExpression(){
        return parseAssignment();
    }

    // assignment -> or | IDENTIFIER "=" assignment
    private Expression parseAssignment(){

        Expression expr = parseOr();

        if (expr instanceof VariableExpression && matchCurrentToken(TokenType.ASSIGNMENT)){
            consumeToken(TokenType.ASSIGNMENT);
            Expression expr2 = parseAssignment();
            return new AssignmentExpression(((VariableExpression) expr).name, expr2);

        } else if (matchCurrentToken(TokenType.ASSIGNMENT)){
            // error since invalid assignment target
            reportParserError(getCurrentToken(), "Invalid assignment target");
            
        }
        return expr;
    }

    // or -> and ("or" and)*
    private Expression parseOr(){
        Expression expr = parseAnd();
        
        while (matchCurrentToken(TokenType.OR)) {
            Token operator = getCurrentToken();
            consumeToken(TokenType.OR);

            Expression expr2 = parseAnd();
            expr = new LogicalExpression(expr, operator, expr2);
        }

        return expr;
    }

    // and -> equality ("and" equality)*
    private Expression parseAnd(){
        Expression expr = parseEquality();

        while (matchCurrentToken(TokenType.AND)){
            Token operator = getCurrentToken();
            consumeToken(TokenType.AND);

            Expression expr2 = parseEquality();
            expr = new LogicalExpression(expr, operator, expr2);
        }

        return expr;
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )*
    private Expression parseEquality(){
        Expression expression = parseComparison();

        while (matchCurrentToken(TokenType.NEQ, TokenType.EQ)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseComparison();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }
    
    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* 
    private Expression parseComparison(){
        Expression expression = parseTerm();

        while (matchCurrentToken(TokenType.GEQ, TokenType.GT, TokenType.LEQ, TokenType.LT)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseTerm();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    // term -> factor ( ( "-" | "+" ) factor )*
    private Expression parseTerm(){
        Expression expression = parseFactor();

        while (matchCurrentToken(TokenType.SUBTRACT, TokenType.ADD)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseFactor();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

    // factor -> unary (("*" | "/") unary)*
    private Expression parseFactor(){
        Expression expression = parseUnary();

        while (matchCurrentToken(TokenType.MULTIPLY, TokenType.DIVIDE)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();

            Expression expression2 = parseUnary();
            expression = new BinaryExpression(expression, token, expression2);
        }
        return expression;
    }

     // unary -> ( "!" | "-" ) unary | primary
    private Expression parseUnary(){
        if (matchCurrentToken(TokenType.SUBTRACT, TokenType.NOT)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();
            return new UnaryExpression(token, parseUnary());

        } else {
            return parsePrimary();
        }
    }

    // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expression parsePrimary() {
        Token currentToken = getCurrentToken();
        consumeToken();
        switch (currentToken.lexeme) {
            case "true":
                return new LiteralExpression(true);
            case "false":
                return new LiteralExpression(false);
            case "nil":
                return new LiteralExpression(null);
            case "(":
                Expression expr = parseExpression();
                // consume ")"
                consumeToken(TokenType.RIGHT_PAREN);
                return new GroupingExpression(expr);
            default:
                if (currentToken.type == TokenType.NUMBER || currentToken.type == TokenType.STRING){
                    return new LiteralExpression(currentToken.value);
                } else if (currentToken.type == TokenType.IDENTIFIER){
                    return new VariableExpression(currentToken);
                } else {
                    reportParserError(currentToken, "Expected STRING or NUMBER or IDENTIFIER");
                }
        }
        return null;
    }

    // Helper methods
    private boolean matchCurrentToken(TokenType... toMatchTokens){
        if (noMoreTokensToConsume()) return false;

        Token current = getCurrentToken();
        for (TokenType toMatchToken : toMatchTokens) {
            if (toMatchToken == current.type) return true;
        }
        return false;
    }

    private Token getCurrentToken(){
        return tokens.get(currentToken);
    }

    // will return True if the last token has been consumed
    private boolean noMoreTokensToConsume(){
        return currentToken >= tokens.size();
    }

    private void consumeToken() {
        if (noMoreTokensToConsume()) throw new IndexOutOfBoundsException();
        currentToken++;
    }

    private void consumeToken(TokenType tokenType){
        if (getCurrentToken().type == tokenType) {
            consumeToken();
        } else {
            reportParserError(getCurrentToken(), String.format("Expected %s", tokenType));
        }
    }

    // Error Handling methods 

    private void reportParserError(Token token, String message){
        ParserError err = new ParserError(token, message);
        Error.reportParserError(err);
        throw err;
    }

    // consumes token until we get to a state we can start parsing from again
    private void synchronize(){
        if (noMoreTokensToConsume()) return;

        // consume the token that led to error
        consumeToken();

        // keep consuming tokens until we get either ; (end of that statement, expression) OR
        // one of keywords : CLASS, FUN, VAR, FOR, WHILE, PRINT, RETURN, IF 

        while (!noMoreTokensToConsume()){

            switch (getCurrentToken().type) {
                case SEMICOLON:
                    consumeToken();
                    return; 

                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case WHILE:
                case PRINT:
                case RETURN:
                case IF:
                    return;
                
                default:
                    consumeToken();
            }
        }
    }
}