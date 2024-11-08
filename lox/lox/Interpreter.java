package lox.lox;

public class Interpreter implements Visitor<Object>{
   
    private Expression expression;

    public Interpreter(Expression expression){
        this.expression = expression;
    }

    protected void interpret(){
        Object value = expression.accept(this);
        System.out.println(value);
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
                break;
            case SUBTRACT:
                return (double)leftValue - (double)rightValue;
            case MULTIPLY:
                return (double)leftValue * (double)rightValue;
            case DIVIDE:
                // Check for rigthValue if it is zero too
                return (double)leftValue / (double)rightValue;
            case LT:
                return (double)leftValue < (double)rightValue;
            case GT:
                return (double)leftValue > (double)rightValue;
            case LEQ:
                return (double)leftValue <= (double)rightValue;
            case GEQ:
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

    private boolean isEqual(Object left, Object right){
        if (left == null && right == null) return true;
        if (left == null) return false;
        if (left == right) return true;

        return left.equals(right);
    }

    private boolean isNotEqual(Object left, Object right){
        return ! isEqual(left, right);
    }

}
