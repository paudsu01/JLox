package lox.tool;

import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;

public class generateAST{

    public static void main(String [] commandLineArguments) throws IOException{
        
        if (commandLineArguments.length > 0) {
            System.err.println("Usage error: Run from base directory without arguments: Modify generateAST.java if needed");
        }

        // Define the classes and their fields for the output file for Expression.java
        String [] exprClasses = {"Binary", "Unary", "Grouping", "Literal", "Variable", "Assignment", "Logical", "Call", "Get", "Set"};
        HashMap<String, String> exprClassesToFields = new HashMap<>();
        exprClassesToFields.put(exprClasses[0], "Expression left:Token operator:Expression right");
        exprClassesToFields.put(exprClasses[1], "Token operator:Expression expression");
        exprClassesToFields.put(exprClasses[2], "Expression expression");
        exprClassesToFields.put(exprClasses[3], "Object value");
        exprClassesToFields.put(exprClasses[4], "Token name");
        exprClassesToFields.put(exprClasses[5], "Token name:Expression value");
        exprClassesToFields.put(exprClasses[6], "Expression left:Token operator:Expression right");
        exprClassesToFields.put(exprClasses[7], "Expression callee:Token closingParen:ArrayList<Expression> arguments");
        exprClassesToFields.put(exprClasses[8], "Expression object:Token name");
        exprClassesToFields.put(exprClasses[9], "Expression object:Token name:Expression value");


        // Define the classes and their fields for the output file for Statement.java
        String [] stmtClasses = {"Expression", "Print", "VarDec", "Block", "IfElse", "While", "Function", "Return", "Class"};
        HashMap<String, String> stmtClassesToFields = new HashMap<>();
        stmtClassesToFields.put(stmtClasses[0], "Expression expression");
        stmtClassesToFields.put(stmtClasses[1], "Expression expression");
        stmtClassesToFields.put(stmtClasses[2], "Token name:Expression initializer");
        stmtClassesToFields.put(stmtClasses[3], "ArrayList<Statement> statements");
        stmtClassesToFields.put(stmtClasses[4], "Expression expr:Statement ifStatement:Statement elseStatement");
        stmtClassesToFields.put(stmtClasses[5], "Expression expr:Statement statement");
        stmtClassesToFields.put(stmtClasses[6], "Token name:ArrayList<Token> parameters:Statement body:FuncType type");
        stmtClassesToFields.put(stmtClasses[7], "Token keyword:Expression returnValue");
        stmtClassesToFields.put(stmtClasses[8], "Token name:ArrayList<FunctionStatement> methods");

        // Generate files Expression.java and Statement.java
        String packageName = "lox.lox";
        defineAST(packageName, "lox/lox/Expression.java", "Expression", exprClasses, exprClassesToFields);
        defineAST(packageName, "lox/lox/Statement.java", "Statement", stmtClasses, stmtClassesToFields);

    }

    private static void defineAST(String packageName, String outputFilePath, String fileName, String [] classes, HashMap<String,String> classesToFields) throws IOException{

        PrintWriter writer = new PrintWriter(outputFilePath);

        writer.printf("package %s;\n\n", packageName);
        writer.println("import lox.scanner.Token;");
        writer.println("import java.util.ArrayList;");

        writer.printf("\nabstract class %s {\n", fileName);
        writer.printf("\tabstract <R> R accept(%sVisitor<R> visitor);\n", fileName);
        writer.println("}\n");

        for (String currentClass : classes){
           defineASTExtendedClass(writer, fileName, currentClass, classesToFields.get(currentClass));
        }
        defineVisitorInterface(writer, fileName, classes);

        writer.close();
    }

    private static void defineASTExtendedClass(PrintWriter writer, String fileName, String currentClass, String fields){

        writer.printf("class %s%s extends %s{\n", currentClass, fileName, fileName);
        String[] fieldsList = fields.split(":");

        for (String field : fieldsList){
            writer.printf("\tfinal %s;\n", field);
        }

        writer.printf("\n\t%s%s(", currentClass, fileName);
        for (int i=0; i < fieldsList.length; i++){
            writer.print(fieldsList[i]);
            if (i == (fieldsList.length - 1)) break;
            writer.print(", ");
        }
        writer.print("){\n");

        for (int i=0; i < fieldsList.length; i++){
            String field = fieldsList[i];
            String[] values = field.split("\s");
            field = values[values.length - 1];

            writer.printf("\t\tthis.%s = %s;\n", field, field);
        }

        writer.print("\t}\n");

        writer.println("\n\t@Override");
        writer.printf("\t<R> R accept(%sVisitor<R> visit){\n", fileName);
        writer.printf("\t return visit.visit%s%s(this);}\n", currentClass, fileName);
        writer.println("}\n");
    }

    private static void defineVisitorInterface(PrintWriter writer, String fileName, String[] classes){
        writer.printf("\ninterface %sVisitor<R>{\n", fileName);

        for (String eachClass : classes){
            String methodName = eachClass + fileName;
            if (fileName.equals("Expression")) writer.printf("\tR visit%s(%s expr);\n", methodName, methodName);
            else writer.printf("\tR visit%s(%s stmt);\n", methodName, methodName);
        }

        writer.println("}");
    }
}