package lox.lox;

import lox.scanner.Token;
import lox.error.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Resolver implements ExpressionVisitor<Void>, StatementVisitor<Void>{

    private Interpreter interpreter;
    private ArrayList<Statement> statements;
    private LinkedList<HashMap<String, Boolean>> scopes = new LinkedList<>();

    // Constructor

    Resolver(Interpreter interpreter, ArrayList<Statement> statements){
        this.interpreter = interpreter;
        this.statements = statements;
    }

    // Overloading for resolve

    void resolve(){
        resolve(statements);
    }

    private void resolve(ArrayList<Statement> statements){
        for (Statement statement: statements){
            resolve(statement);
        }
    }

    private void resolve(Statement statement){
        statement.accept(this);
    }

    private void resolve(Expression expression){
        expression.accept(this);
    }

    // Visitor methods for statement

    @Override
    public Void visitExpressionStatement(ExpressionStatement stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStatement(PrintStatement stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarDecStatement(VarDecStatement stmt) {
        if (scopes.isEmpty()) return null;

        declare(stmt.name);
        if (stmt.initializer != null) resolve(stmt.initializer);
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitBlockStatement(BlockStatement stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfElseStatement(IfElseStatement stmt) {
        resolve(stmt.expr);
        resolve(stmt.ifStatement);
        if (stmt.elseStatement != null) resolve(stmt.elseStatement);
        return null;
    }

    @Override
    public Void visitWhileStatement(WhileStatement stmt) {
        resolve(stmt.expr);
        resolve(stmt.statement);
        return null;
    }

    @Override
    public Void visitFunctionStatement(FunctionStatement stmt) {

        declare(stmt.name);
        define(stmt.name);

        beginScope();
        for (Token token : stmt.parameters){
            declare(token);
            define(token);
        }
        resolve(stmt.body);
        endScope();

        return null;
    }


    @Override
    public Void visitClassStatement(ClassStatement stmt) {
        declare(stmt.name);
        define(stmt.name);

        for (Statement function: stmt.methods){
            resolve(function);
        }
        return null; 
    }

    @Override
    public Void visitReturnStatement(ReturnStatement stmt) {
        if (stmt.returnValue != null) resolve(stmt.returnValue);
        return null;
    }

    // Visitor methods for expression

    @Override
    public Void visitBinaryExpression(BinaryExpression expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitGroupingExpression(GroupingExpression expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpression(LiteralExpression expr) {
        return null;
    }

    @Override
    public Void visitVariableExpression(VariableExpression expr) {

        if ((!scopes.isEmpty())
            && scopes.getLast().get(expr.name.lexeme) == Boolean.FALSE) 
                Error.reportResolverError(expr.name, "Cannot read a local variable in its own initializer");
        
        resolveLocalVariableUsage(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignmentExpression(AssignmentExpression expr) {
        resolve(expr.value);
        resolveLocalVariableUsage(expr, expr.name);
        return null;
    }

    @Override
    public Void visitLogicalExpression(LogicalExpression expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression expr) {
        resolve(expr.callee);

        for (Expression argument : expr.arguments){
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpression(GetExpression expr) {
        resolve(expr.object);
        return null;
    }

    // Helper methods

    private void beginScope(){
        scopes.addLast(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.removeLast();
    }

    private void declare(Token token){
        if (scopes.isEmpty()) return;
        HashMap<String, Boolean> scope = scopes.getLast();

        if (scope.containsKey(token.lexeme)) Error.reportResolverError(token, "Cannot re-declare variables in this scope");
        scope.put(token.lexeme, Boolean.FALSE);
    }

    private void define(Token token){
        if (scopes.isEmpty()) return;
        scopes.getLast().put(token.lexeme, Boolean.TRUE);
    }

    private void resolveLocalVariableUsage(Expression varExpression, Token token){
        Integer numberOfHops = calculateHops(token.lexeme, 0);
        // Assume it is a global variable if numberOfHops is null
        if (numberOfHops == null) return;

        interpreter.resolve(varExpression, numberOfHops);
    }

    private Integer calculateHops(String lexeme, int currentHops){
        if ((scopes.size() -1 - currentHops) < 0 ) return null;
        if (scopes.get(scopes.size() -1 -currentHops).containsKey(lexeme)) return currentHops;

        return calculateHops(lexeme, currentHops+1);
    }
}
