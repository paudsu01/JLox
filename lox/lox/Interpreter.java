package lox.lox;

import java.util.ArrayList;

import lox.error.Error;
import lox.error.RuntimeError;

import lox.scanner.Token;
import lox.scanner.TokenType;

public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Object>{
   
    private final ArrayList<Statement> statements;
    private Environment environment;

    public Interpreter(ArrayList<Statement> stmnts, Environment env){
        statements = stmnts;
        environment = env;
    }

    protected void interpret(){
        try{
            for (Statement stmt : statements){
                evaluate(stmt);
            }
        } catch (RuntimeError error){
        }
    }

    // VISITOR PATTERN visit methods for statement

    @Override
	public Object visitExpressionStatement(ExpressionStatement stmt){
        evaluate(stmt.expression);
        return null;
    }

    @Override
	public Object visitPrintStatement(PrintStatement stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitIfElseStatement(IfElseStatement stmt) {
        if (truthOrFalse(evaluate(stmt.expr))) evaluate(stmt.ifStatement);
        else if (stmt.elseStatement != null) evaluate(stmt.elseStatement);

        return null;
    }

    @Override
    public Object visitVarDecStatement(VarDecStatement stmt) {
        Object value = null;
        if (stmt.initializer != null) value = evaluate(stmt.initializer);
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitBlockStatement(BlockStatement stmt) {
        // Setup new environment
        Environment superEnvironment = environment;
        environment = new Environment(environment);

        for (Statement statement : stmt.statements){
            evaluate(statement);
        }

        // Change environment back
        environment = superEnvironment;
        return null;
    }

    // VISITOR PATTERN visit methods for expression

    @Override
    public Object visitVariableExpression(VariableExpression expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignmentExpression(AssignmentExpression expr){
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitLogicalExpression(LogicalExpression expr){
        Token token = expr.operator;
        Object leftValue = evaluate(expr.left);
        switch (token.lexeme) {
            case "and":
                if (truthOrFalse(leftValue)) return evaluate(expr.right);
                return leftValue;

            case "or":
                if (truthOrFalse(leftValue)) return leftValue;
                return evaluate(expr.right);

        // Unreachable
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression expr) {
        Object leftValue = evaluate(expr.left);
        Object rightValue = evaluate(expr.right);

        switch (expr.operator.type) {
            case ADD:
                if ((leftValue instanceof Double) && (rightValue instanceof Double)){
                    return (double)leftValue + (double)rightValue;
                } else if ((leftValue instanceof String) && (rightValue instanceof String)){
                    return (String)leftValue + (String)rightValue;
                }

                RuntimeError err = new RuntimeError(expr.operator, "Both numbers or both strings expected");
                throw err;

            case SUBTRACT:
                checkIfOperandsAreNumbers(expr.operator, leftValue, rightValue);
                return (double)leftValue - (double)rightValue;

            case MULTIPLY:
                checkIfOperandsAreNumbers(expr.operator, leftValue, rightValue);
                return (double)leftValue * (double)rightValue;

            case DIVIDE:
                // Check for rigthValue if it is zero too
                checkIfOperandsAreNumbers(expr.operator, leftValue, rightValue);
                checkIfRightValueZero(expr.operator, rightValue);
                return (double)leftValue / (double)rightValue;

            case LT:
                checkIfOperandsAreNumbers(expr.operator, leftValue, rightValue);
                return (double)leftValue < (double)rightValue;
            case GT:
                checkIfOperandsAreNumbers(expr.operator, leftValue, rightValue);
                return (double)leftValue > (double)rightValue;
            case LEQ:
                checkIfOperandsAreNumbers(expr.operator, leftValue, rightValue);
                return (double)leftValue <= (double)rightValue;
            case GEQ:
                checkIfOperandsAreNumbers(expr.operator, leftValue, rightValue);
                return (double)leftValue >= (double)rightValue;
            case EQ:
                return isEqual(leftValue, rightValue);
            case NEQ:
                return isNotEqual(leftValue, rightValue);
            // Unreachable
            default:
                break;
        }
        // Unreachable
        return null;
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression expr) {
        Object rightValue = evaluate(expr.expression);

        switch (expr.operator.type) {
            case SUBTRACT:
                checkIfOperandIsANumber(expr.operator, rightValue);
                return -(double) rightValue;
            case NOT:
                return ! truthOrFalse(rightValue);
            default:
                return null;
        }
    }

    @Override
    public Object visitGroupingExpression(GroupingExpression expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpression(LiteralExpression expr) {
        return expr.value;
    }

    // HELPER METHODS 

    private Object evaluate(Statement stmt){
        return stmt.accept(this);
    }

    private Object evaluate(Expression expr){
        return expr.accept(this);
    }

    private boolean truthOrFalse(Object value){
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean)value;
        return true;
    }

    private boolean isEqual(Object left, Object right){
        if (left == null && right == null) return true;
        if (left == null) return false;
        if (left == right) return true;

        return left.equals(right);
    }

    private boolean isNotEqual(Object left, Object right){
        return ! isEqual(left, right);
    }

    private void checkIfOperandIsANumber(Token token, Object value){
        if (value instanceof Double) return;
        throw createRuntimeError(token, "Number Literal expected");
    }

    private void checkIfOperandsAreNumbers(Token token, Object a, Object b){
        if (a instanceof Double && b instanceof Double) return;
        throw createRuntimeError(token, "Numbers expected as operands");
    }

    private void checkIfRightValueZero(Token token, Object rightValue){
        if ((double) rightValue != (double) 0) return;
        throw createRuntimeError(token, "Second operand cannot be Zero");
    }

    private RuntimeError createRuntimeError(Token token, String message){
        RuntimeError err = new RuntimeError(token, message);
        Error.reportOperandError(err.token, err.message);
        return err;
    }

    private String stringify(Object value){

        if (value instanceof String) return (String)value;
        if (value == null) return "nil";
        if (value instanceof Boolean) return ((boolean) value == true) ? "true" : "false";

        String stringValue = value.toString();
        if (stringValue.endsWith(".0")) return stringValue.substring(0, stringValue.length()-2);
        else return stringValue;

    }
}