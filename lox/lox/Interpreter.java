package lox.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import lox.error.Error;
import lox.error.RuntimeError;
import lox.error.Return;

import lox.scanner.Token;

public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Object>{
   
    private final ArrayList<Statement> statements;
    Environment environment;
    final Environment globals;
    static final HashMap<Expression, Integer> locals = new HashMap<>();

    public Interpreter(ArrayList<Statement> stmnts, Environment env){
        statements = stmnts;
        globals = env;
        environment = globals;

        addNativeFunctions();
    }

    protected void interpret(){
        try{
            for (Statement stmt : statements){
                evaluate(stmt);
            }
        } catch(Return ret){
            Error.reportRuntimeError(ret);
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
    public Object visitWhileStatement(WhileStatement stmt) {
        while(truthOrFalse(evaluate(stmt.expr))){
            evaluate(stmt.statement);
        }
        return null;
    }

    @Override
    public Object visitIfElseStatement(IfElseStatement stmt) {
        if (truthOrFalse(evaluate(stmt.expr))) evaluate(stmt.ifStatement);
        else if (stmt.elseStatement != null) evaluate(stmt.elseStatement);

        return null;
    }

    @Override
    public Object visitFunctionStatement(FunctionStatement stmt) {
        LoxFunction function = new LoxFunction(stmt, environment, stmt.type);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitClassStatement(ClassStatement stmt){
        environment.define(stmt.name.lexeme, null);

        HashMap<String, LoxFunction> methods = new HashMap<>();
        for (FunctionStatement funcStatement : stmt.methods){
            methods.put(funcStatement.name.lexeme, new LoxFunction(funcStatement, environment, funcStatement.type));
        }

        LoxClass class_ = new LoxClass(stmt.name, methods);
        environment.assign(stmt.name, class_);
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

    @Override
    public Object visitReturnStatement(ReturnStatement stmt){
        Object value = null;
        if (stmt.returnValue != null) value = evaluate(stmt.returnValue);

        throw new Return(stmt.keyword, value);
    }

    // VISITOR PATTERN visit methods for expression

    @Override
    public Object visitThisExpression(ThisExpression expr){
        if (locals.get(expr) != null) return environment.getAt(expr.keyword, locals.get(expr));
        else throw Error.createRuntimeError(expr.keyword, "'this' keyword cannot be used outside of a class");
    }

    @Override
    public Object visitVariableExpression(VariableExpression expr) {
        if (locals.get(expr) == null){
            return globals.get(expr.name);
        } else {
            return environment.getAt(expr.name, locals.get(expr));
        }
    }

    @Override
    public Object visitAssignmentExpression(AssignmentExpression expr){
        Object value = evaluate(expr.value);
        Integer depth = locals.get(expr);

        if (depth == null) globals.assign(expr.name, value);
        else environment.assignAt(expr.name, value, depth);
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
                } else if ((leftValue instanceof Double) && (rightValue instanceof String)){
                    return stringify(leftValue) + (String)rightValue;
                } else if ((rightValue instanceof Double) && (leftValue instanceof String)){
                    return (String)leftValue + stringify(rightValue); 
                }

                RuntimeError err = new RuntimeError(expr.operator, "Both numbers, both strings, or one of each number and string expected");
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
    public Object visitCallExpression(CallExpression expr) {
        ArrayList<Object> arguments = new ArrayList<>();
        for (Expression argument: expr.arguments){
            arguments.add(evaluate(argument));
        }

        Object callee = evaluate(expr.callee);
        if (!(callee instanceof LoxCallable)) throw Error.createRuntimeError(expr.closingParen, "Object is not callable");

        LoxCallable function = (LoxCallable) callee;

        if (function.arity() != arguments.size())
            throw Error.createRuntimeError(expr.closingParen, String.format("Expected %d argument(s), but got %d of them", function.arity(), arguments.size()));

        Object functionCall = null;
        try {
            functionCall = function.call(this, arguments);
    
        } catch (NumberFormatException e) {
            // For "number" native function
            throw Error.createRuntimeError(expr.closingParen, "Cannot convert String to Number");
        }
        return functionCall;
    }

    @Override
    public Object visitGetExpression(GetExpression expr){
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance){
            return ((LoxInstance) object).get(expr.name);
        }
        // Error since not an instance
        throw Error.createRuntimeError(expr.name, "Cannot access properties of a non-instance");
    }

    @Override
    public Object visitSetExpression(SetExpression expr){

        Object object = evaluate(expr.object);

        if (object instanceof LoxInstance){
            Object value = evaluate(expr.value);
            ((LoxInstance) object).set(expr.name, value);
            return value;

        } else{
            // Error since not an instance
            throw Error.createRuntimeError(expr.name, "Cannot modify properties of a non-instance");
        }
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
        throw createOperandError(token, "Number Literal expected");
    }

    private void checkIfOperandsAreNumbers(Token token, Object a, Object b){
        if (a instanceof Double && b instanceof Double) return;
        throw createOperandError(token, "Numbers expected as operands");
    }

    private void checkIfRightValueZero(Token token, Object rightValue){
        if ((double) rightValue != (double) 0) return;
        throw createOperandError(token, "Second operand cannot be Zero");
    }

    private RuntimeError createOperandError(Token token, String message){
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

    void resolve(Expression expr, int depth){
        locals.put(expr, depth);
    }

    private void addNativeFunctions(){

        // Predefined clock function
        environment.define("clock",
            new LoxCallable(){

                @Override
                public int arity(){ return 0; }

                @Override
                public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
                    return (double) System.currentTimeMillis() / 1000;
                }

                public String toString(){
                    return "<native fn: clock -> returns current time in second(s)>";
                }
            });
        
            // Predefined inputInt function
            environment.define("input",
                new LoxCallable(){

                @Override
                public int arity(){ return 0; }

                @Override
                public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
                    Scanner scanner = new Scanner(System.in);
                    String input = "";
                    if (scanner.hasNext()) input = scanner.nextLine();
                    return input;

                    // VVIP: The scanner shouldn't be closed since it closes the System.in input stream as well
                }

                public String toString(){
                    return "<native fn: input -> returns user input as String>";
                }
            });

            // Predefined string to number function
            environment.define("number",
                new LoxCallable(){

                @Override
                public int arity(){ return 1; }

                @Override
                public Object call(Interpreter interpreter, ArrayList<Object> arguments) {
                    return Double.parseDouble(stringify(arguments.get(0)));
                }

                public String toString(){
                    return "<native fn: number -> returns the provided string argument as number if applicable>";
                }
            });
    }
}