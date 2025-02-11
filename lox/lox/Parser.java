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

    // declaration -> funDec | varDec | classDec | statement
    private Statement parseDeclaration(){
       try{
            if (matchCurrentToken(TokenType.VAR)) return parseVarDeclaration();
            else if (matchCurrentToken(TokenType.FUN)) return parseFunctionDeclaration(); 
            else if (matchCurrentToken(TokenType.CLASS)) return parseClassDeclaration(); 
            else return parseStatement(); 
       } catch (ParserError err){
            synchronize();
       } catch (IndexOutOfBoundsException err){
            synchronize();
       }
        return null;
    }

    // funDec -> "fun" function
    private Statement parseFunctionDeclaration(){
        consumeToken(TokenType.FUN);
        return parseFunction(FuncType.FUNCTION);
    }

    // classDec -> "class" IDENTIFIER ( < IDENTIFIER ) ? "{" classFunction* "}"
    private Statement parseClassDeclaration(){
        consumeToken(TokenType.CLASS);
        Token className = getCurrentToken();
        consumeToken(TokenType.IDENTIFIER);

        VariableExpression superclass = null;
        if (matchCurrentToken(TokenType.LT)){
            consumeToken(TokenType.LT);
            superclass = new VariableExpression(getCurrentToken());
            consumeToken(TokenType.IDENTIFIER, "Expect superclass name");
        }

        consumeToken(TokenType.LEFT_BRACE);

        ArrayList<FunctionStatement> methods = new ArrayList<>();
        ArrayList<FunctionStatement> staticMethods = new ArrayList<>();
        while ((! noMoreTokensToConsume()) && !matchCurrentToken(TokenType.RIGHT_BRACE)){

            FunctionStatement function = (FunctionStatement) parseClassFunction();

            if (function.type == FuncType.STATIC) staticMethods.add(function);
            else methods.add(function);
        }

        consumeToken(TokenType.RIGHT_BRACE);
        return new ClassStatement(className, superclass, methods, staticMethods);
    }
    
    // classFunction -> function | "static" function ;
    private Statement parseClassFunction(){
        if (matchCurrentToken(TokenType.STATIC)){
            consumeToken();
            return parseFunction(FuncType.STATIC);
        } else {
            return parseFunction(FuncType.METHOD);
        }
    }

    // function -> IDENTIFIER "(" parameters ? ")" blockStatement
    private Statement parseFunction(FuncType type){
        Token funcName = getCurrentToken();
        consumeToken(TokenType.IDENTIFIER);

        consumeToken(TokenType.LEFT_PAREN);
        ArrayList<Token> parameters = new ArrayList<>();
        if (!matchCurrentToken(TokenType.RIGHT_PAREN)){
            parameters = parseParameters();
        }
        consumeToken(TokenType.RIGHT_PAREN);
        
        Statement blockStatement = parseBlockStatement();

        if (funcName.lexeme.equals("init") && type == FuncType.METHOD) type = FuncType.INIT;
        return new FunctionStatement(funcName, parameters, blockStatement, type);
    }

    // parameters -> IDENTIFIER ("," IDENTIFIER)*
    private ArrayList<Token> parseParameters(){

        ArrayList<Token> parameters = new ArrayList<>();
        do {
            if (matchCurrentToken(TokenType.COMMA)) consumeToken(TokenType.COMMA);

            parameters.add(getCurrentToken());
            consumeToken(TokenType.IDENTIFIER);
            if (parameters.size() >= 255) reportMaxLimitExceededError(getCurrentToken(), "Cannot have more than 255 parameters");

        } while (matchCurrentToken(TokenType.COMMA));
        return parameters;
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


    // statement -> expresssionStatment | printStatement | blockStatement | ifElseStatement | whileStatement | returnStatement
    private Statement parseStatement(){

        if (matchCurrentToken(TokenType.PRINT)) return parsePrintStatement();
        else if (matchCurrentToken(TokenType.LEFT_BRACE)) return parseBlockStatement();
        else if (matchCurrentToken(TokenType.IF)) return parseIfElseStatement();
        else if (matchCurrentToken(TokenType.WHILE)) return parseWhileStatement();
        else if (matchCurrentToken(TokenType.FOR)) return parseForStatement();
        else if (matchCurrentToken(TokenType.RETURN)) return parseReturnStatement();
        
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

    // blockStatement -> "{" statement* "}"
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

    // returnStatement -> "return" expression? ";"
    private Statement parseReturnStatement(){
        Token returnToken = getCurrentToken();
        consumeToken(TokenType.RETURN);

        Expression expression = null;
        if (!matchCurrentToken(TokenType.SEMICOLON)) expression = parseExpression();

        consumeToken(TokenType.SEMICOLON);
        return new ReturnStatement(returnToken, expression);
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

    // assignment -> or | (call ".")? IDENTIFIER "=" assignment | call "[" expression "]" "=" assignment;
    private Expression parseAssignment(){

        Expression expr = parseOr();

        if (expr instanceof VariableExpression && matchCurrentToken(TokenType.ASSIGNMENT)){
            consumeToken(TokenType.ASSIGNMENT);
            Expression expr2 = parseAssignment();
            return new AssignmentExpression(((VariableExpression) expr).name, expr2);

        } else if (matchCurrentToken(TokenType.ASSIGNMENT) && expr instanceof GetExpression){
            consumeToken(TokenType.ASSIGNMENT);
            Expression expr2 = parseAssignment();
            GetExpression expr1 = (GetExpression) expr;
            return new SetExpression(expr1.object, expr1.name, expr2);

        } else if (matchCurrentToken(TokenType.ASSIGNMENT) && expr instanceof ArrayElementExpression){
            consumeToken(TokenType.ASSIGNMENT);
            ArrayElementExpression expr1 = (ArrayElementExpression) expr;
            Expression expr2 = parseAssignment();
            return new ArrayElementAssignmentExpression(expr1.leftBracket, expr1.arrayExpression, expr1.index, expr2);

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

     // unary -> ( "!" | "-" ) unary | call
    private Expression parseUnary(){
        if (matchCurrentToken(TokenType.SUBTRACT, TokenType.NOT)){
            // consume matched token
            Token token = getCurrentToken();
            consumeToken();
            return new UnaryExpression(token, parseUnary());

        } else {
            return parseCall();
        }
    }

    // call -> primary ( ( "(" arguments ? ")" ) | ("." IDENTIFIER ) | "[" expression "]")* ;
    private Expression parseCall(){
        Expression expression = parsePrimary();

        while (matchCurrentToken(TokenType.LEFT_PAREN) || matchCurrentToken(TokenType.DOT) || matchCurrentToken(TokenType.LEFT_BRACKET)){

            if (matchCurrentToken(TokenType.LEFT_PAREN)){
                consumeToken(TokenType.LEFT_PAREN);

                ArrayList<Expression> arguments;
                if (!matchCurrentToken(TokenType.RIGHT_PAREN)) arguments = parseArguments();
                else arguments = new ArrayList<>();

                expression = new CallExpression(expression, getCurrentToken(), arguments);
                consumeToken(TokenType.RIGHT_PAREN);

            } else if (matchCurrentToken(TokenType.DOT)){
                consumeToken(TokenType.DOT);
                Token token = getCurrentToken();
                expression = new GetExpression(expression, token);
                consumeToken(TokenType.IDENTIFIER);

            } else {
                Token token = getCurrentToken();
                consumeToken(TokenType.LEFT_BRACKET);
                expression = new ArrayElementExpression(token, expression, parseExpression());
                consumeToken(TokenType.RIGHT_BRACKET);
            }
        }
        return expression;
    }

    private ArrayList<Expression> parseArguments(){
        ArrayList<Expression> arguments = new ArrayList<>();
        do {
            if (matchCurrentToken(TokenType.COMMA)) consumeToken(TokenType.COMMA);

            arguments.add(parseExpression());
            if (arguments.size() >= 255) reportMaxLimitExceededError(getCurrentToken(), "Cannot have more than 255 parameters");

        } while (matchCurrentToken(TokenType.COMMA));

        return arguments;
    }

    // primary -> NUMBER | STRING | IDENTIFIER | "true" | "false" | "nil" | "(" expression ")" | "super" "." IDENTIFIER | "[" (expression ("," expression)* )? "]" ;
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
            case "this":
                return new ThisExpression(currentToken);
            case "(":
                Expression expr = parseExpression();
                // consume ")"
                consumeToken(TokenType.RIGHT_PAREN);
                return new GroupingExpression(expr);
            case "super":
                consumeToken(TokenType.DOT, "'.' expected after super keyword");
                Token method = getCurrentToken();
                consumeToken(TokenType.IDENTIFIER, "Superclass method name expected");
                return new SuperExpression(currentToken, method);

            case "[":
                ArrayList<Expression> elements = new ArrayList<>();

                if (!matchCurrentToken(TokenType.RIGHT_BRACKET)) elements.add(parseExpression());
                while (matchCurrentToken(TokenType.COMMA)){
                    consumeToken(TokenType.COMMA);
                    elements.add(parseExpression());
                }

                consumeToken(TokenType.RIGHT_BRACKET, "']' bracket expected");
                return new ArrayExpression(elements);

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
        consumeToken(tokenType, String.format("Expected %s", tokenType));
    }

    private void consumeToken(TokenType tokenType, String message){
        if (getCurrentToken().type == tokenType) {
            consumeToken();
        } else {
            reportParserError(getCurrentToken(), message);
        }
    }
    // Error Handling methods 

    private void reportParserError(Token token, String message){
        ParserError err = new ParserError(token, message);
        Error.reportParserError(err);
        throw err;
    }

    private void reportMaxLimitExceededError(Token token, String message){
        ParserError err = new ParserError(token,message);
        Error.reportError(token.line, message);
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