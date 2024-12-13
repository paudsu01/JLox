package lox.lox;

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
        return null;
    }

    @Override
    public Void visitBlockStatement(BlockStatement stmt) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAssignmentExpression(AssignmentExpression expr) {
        // TODO Auto-generated method stub
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
}
