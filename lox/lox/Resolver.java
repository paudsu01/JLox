package lox.lox;

import lox.scanner.Token;
import lox.error.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Resolver implements ExpressionVisitor<Void>, StatementVisitor<Void>{

    private Interpreter interpreter;
    private ArrayList<Statement> statements;
    private ClassType currentClassScope = ClassType.NONE;
    private FuncType currentFuncScope = FuncType.NONE;
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
        FuncType previous = currentFuncScope;
        currentFuncScope = stmt.type;

        declare(stmt.name);
        define(stmt.name);

        beginScope();
        for (Token token : stmt.parameters){
            declare(token);
            define(token);
        }
        resolve(stmt.body);
        endScope();

        currentFuncScope = previous;
        return null;
    }


    @Override
    public Void visitClassStatement(ClassStatement stmt) {

        ClassType previous = currentClassScope;
        currentClassScope = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        if (stmt.superclass != null){
            currentClassScope = ClassType.SUBCLASS;
            beginScope();
            scopes.getLast().put("super", true);
        }

        beginScope();
        scopes.getLast().put("this", true);

        if (stmt.superclass != null) {
            // cannot inherit from itself
            if (stmt.superclass.name.lexeme.equals(stmt.name.lexeme)) Error.reportResolverError(stmt.name, "Class cannot inherit from itself.");
            resolve(stmt.superclass);
        }

        for (FunctionStatement function: stmt.methods){
            resolve(function);
        }

        for (FunctionStatement function: stmt.staticMethods){
            resolve(function);
        }

        endScope();

        if (stmt.superclass != null){
            endScope();
        }

        currentClassScope = previous;
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
    public Void visitThisExpression(ThisExpression expr) {
        if (currentFuncScope == FuncType.STATIC) Error.reportResolverError(expr.keyword, "Cannot use `this` inside a static method");
        resolveLocalVariableUsage(expr, expr.keyword);
        return null;
    }


    @Override
    public Void visitArrayElementExpression(ArrayElementExpression expr) {
        resolve(expr.arrayExpression);
        resolve(expr.index);
        return null;
    }

    @Override
    public Void visitArrayElementAssignmentExpression(ArrayElementAssignmentExpression expr){
        resolve(expr.arrayExpression);
        resolve(expr.index);
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitArrayExpression(ArrayExpression expr) {
        for (Expression expression: expr.elements){
            resolve(expression);
        }
        return null;
    }

    @Override
    public Void visitSuperExpression(SuperExpression expr) {
        if (currentClassScope == ClassType.NONE){
            Error.reportResolverError(expr.keyword, "Cannot use `super` outside of a class");
       } else if (currentClassScope == ClassType.CLASS){
            Error.reportResolverError(expr.keyword, "Cannot use `super` in a class with no superclass");
       } else if (currentFuncScope == FuncType.STATIC){
            Error.reportResolverError(expr.keyword, "Cannot use `super` inside a static method");
       } else resolveLocalVariableUsage(expr, expr.keyword);
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

    @Override
    public Void visitSetExpression(SetExpression expr) {
        resolve(expr.object);
        resolve(expr.value);
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
