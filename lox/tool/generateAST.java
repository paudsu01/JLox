package lox.tool;

import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;

public class generateAST{

    public static void main(String [] commandLineArguments) throws IOException{
        
        if (commandLineArguments.length != 1) {
            System.err.println("Usage error: Provide outputASTFile as argument");
        }

        // Define the classes and their fields for the output file 
        String [] classes = {"Binary", "Unary", "Grouping", "Literal"};
        HashMap<String, String> classes_to_fields = new HashMap<>();
        classes_to_fields.put(classes[0], "Expression left:Token operator:Expression right");
        classes_to_fields.put(classes[1], "Token operator:Expression expression");
        classes_to_fields.put(classes[2], "Expression expression");
        classes_to_fields.put(classes[3], "Object value");

        String outputFile = commandLineArguments[0];
        String[] fileNamePath = outputFile.split("/");
        String fileName = fileNamePath[fileNamePath.length - 1].split("\\.")[0];
        String packageName = "";
        for (int i=0; i < fileNamePath.length -1;i++){
            packageName = packageName + fileNamePath[i];
            if (i == fileNamePath.length -2) break;
            packageName = packageName + ".";
        }

        defineAST(packageName, outputFile, fileName, classes, classes_to_fields);

    }

    private static void defineAST(String packageName, String outputFilePath, String fileName, String [] classes, HashMap<String,String> classes_to_fields) throws IOException{

        PrintWriter writer = new PrintWriter(outputFilePath);

        writer.printf("package %s;\n\n", packageName);
        writer.println("import lox.scanner.Token;");

        writer.printf("\nabstract class %s {\n", fileName);
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");
        writer.println("}\n");

        for (String currentClass : classes){
           defineASTExtendedClass(writer, fileName, currentClass, classes_to_fields.get(currentClass));
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
        writer.println("\t<R> R accept(Visitor<R> visit){");
        writer.printf("\t return visit.visit%s%s(this);}\n", currentClass, fileName);
        writer.println("}\n");
    }

    private static void defineVisitorInterface(PrintWriter writer, String fileName, String[] classes){
        writer.println("\ninterface Visitor<T>{");

        for (String eachClass : classes){
            String methodName = eachClass + fileName;
            writer.printf("\tT visit%s(%s expr);\n", methodName, methodName);
        }

        writer.println("}");
    }
}