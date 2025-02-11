package lox.lox;

import java.util.ArrayList;

class LoxArray{

    ArrayList<Object> values;
    int capacity;

    LoxArray(int initialCapacity, ArrayList<Object> values){
        this.capacity = initialCapacity;
        this.values = values; 
    }

    void updateCapacity(){
        capacity = values.size();
    }

    @Override
    public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            if (values.size() > 0)
                sb.append(Interpreter.stringify(values.get(0)));
            for (int i=1; i<values.size() ;i++){
                sb.append(", ");
                sb.append(Interpreter.stringify(values.get(i)));
            }
            sb.append(" ]");
            return sb.toString();
    }

}