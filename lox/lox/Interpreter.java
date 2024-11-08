package lox.lox;

public class Interpreter implements Visitor<Object>{
   
    private Expression expression;

    public Interpreter(Expression expression){
        this.expression = expression;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression expr) {
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression expr) {
        Object rightValue = evaluate(expr.expression);

        switch (expr.operator.type) {
            case SUBTRACT:
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

    private Object evaluate(Expression expr){
        return expr.accept(this);
    }

    private boolean truthOrFalse(Object value){
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean)value;
        return true;
    }

}
