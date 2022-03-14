import java.util.Scanner;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Interpreter implements Serializable{
    public static final long serialVersionUID = 0245556663L;
    static Deque<HashMap<String,LexToken>> callStack = new ArrayDeque<HashMap<String,LexToken>>();
    static Interpreter te; 
    static Scanner reader = new Scanner(System.in);
    static LexToken lookahead = null; 
    public static HashMap<String, LexToken> syTable = new HashMap<>(); 
    public static HashMap<String, LexToken> currentTable = new HashMap<>(); 
    public static HashMap<String, String> int2Lexeme = new HashMap<>(); 
    public static HashMap<String, HashMap<String, LexToken>> allTables = new HashMap<>(); 
    public static String funcName = ""; 
    public static boolean skip = false;
    public static String returnChar = ""; 
    static int id= 1; 
    public static boolean dot = true;
    static int tabNum = 0; 
    static String tabString = "\t"; 
    static boolean finished = false; 
    static RootNode root; 
    public static Deque<Object> returnStack = new ArrayDeque<>(); 
    static String errorString = ""; 
    static class RootNode{
        Node functions; 
        Node declarations; 
        Node statements; 
        RootNode(){

        }
    }

    static class Node{
        LexToken token; 
        String value; 
        Node child; 
        Node sibling;  
        Node(LexToken t, String val){
            this.token = t; 
            this.value = val;
            this.child = null; 
            this.sibling = null;  
        }        
        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder(50);
            print(buffer, "", "");
            return buffer.toString();
        }
    
        private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
            buffer.append(prefix);
            if (value != null) {
                buffer.append(value + ": ");
            }

            if (token != null && token.token != "ID"){
                //System.out.println("LINE 973");
                buffer.append(token.value );

            }else if (token != null){
                //System.out.println("LINE 974");
                buffer.append(int2Lexeme.get(token.value));
            }

            buffer.append('\n');
            Node current = this; 
 
            if (current.child != null) {
                // next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
                child.print(buffer, childrenPrefix + "|-- ", childrenPrefix + "|   ");
            }
            if (current.sibling != null){
                // next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
                sibling.print(buffer, childrenPrefix + "|__ ", childrenPrefix + "    ");
            }

        }
    }




    class LexToken implements Serializable {
        String value; 
        String token; 
        String attributes; 
        public static final long serialVersionUID = 12341234123L;
        Object compNum; 
        String type; 
        LexToken(String lexeme , String token , String attributes){
            LexToken temp = findEntry(lexeme); 
            if (temp != null ){
                this.value = temp.value;
                this.token = temp.token; 
                this.attributes = temp.attributes; 
                this.compNum = temp.compNum; 
                this.type= temp.type; 
                
            }else {
                if (token == "ID"){
                    this.value = "" + id; 
                }else {
                    this.value = lexeme; 
                }
                
                this.token = token;
                this.attributes = attributes;
                addValues(this, lexeme);
                if (this.token.contains("Integer") && this.attributes.contains("Constant")){
                    this.compNum = Integer.parseInt(lexeme); 
                    this.type = "int"; 
                }else if (this.token.contains("DOUBLE")&& this.attributes.contains("Constant")){
                    this.compNum = Double.parseDouble(lexeme); 
                    this.type = "double";
                }
            }
        }
        @Override
        public String toString(){
            String formattedOutput = "<" + this.token;
            if (this.attributes.contains("Variable")  || this.attributes.contains("Constant") || this.attributes.contains("Operators")|| this.attributes.contains("ERROR")) {
                formattedOutput = formattedOutput +" , "+  this.value; 
            }
            return formattedOutput + ">"; 
        }
        public boolean valueEquals(String temp){
            if (this.value.equals( temp) ){
                return true; 
            }
            return false; 

        }
    }

    public static LexToken CreateError(String word, Scanner reader){
        String errorWord = word; 
        return  te.new LexToken(errorWord, "ERROR", "ERROR");
    }

    public static void addValues(LexToken tok , String lexeme){
        currentTable.put(lexeme, tok); 
        int2Lexeme.put("" + id ,lexeme);
        id ++; 
    }

    public static String getLexeme(String ID){
        return int2Lexeme.get(ID);
    }
    public static LexToken findEntry(String lexeme){
        LexToken temp = syTable.get(lexeme); 
        return temp;  
    }

    public static LexToken isId(String firstChar, Scanner reader){
        String word = firstChar; 
        while (!finished && reader.hasNext()){
            String value = reader.next(); 
            switch(value.toLowerCase()){
                case "0": 
                case "1": 
                case "2": 
                case "3": 
                case "4": 
                case "5": 
                case "6": 
                case "7": 
                case "8": 
                case "9": 
                case "a":
                case "b":
                case "c":
                case "d":
                case "e":
                case "f":
                case "g":
                case "h":
                case "i":
                case "j":
                case "k":
                case "l":
                case "m":
                case "n":
                case "o":
                case "p":
                case "q":
                case "r":
                case "s":
                case "t":
                case "u":
                case "v":
                case "w":
                case "x":
                case "y":
                case "z": 
                    word = word + value; 
                    break; 
                case "<": 
                case ">":
                case "=":
                case "-":
                case "+":
                case "%":
                case "*":
                case "/":
                case "(": 
                case ")":
                case " ":
                case "\t":
                case "\n": 
                case ".": 
                case "\r": 
                case ",":
                case ";":    
                    returnChar = value; 
                    return te.new LexToken(word, "ID", "Variable");
                default : 
                    word = word + value; 
                    return CreateError(word, reader); 
            }


        }
        return null; 
    }

    public static LexToken IsRelop(String firstChar, Scanner reader){
        String newToken = firstChar; 
        String value; 
        if (firstChar.equals("<")){
            if (reader.hasNext()){
                value = reader.next(); 
            
                switch(value.toLowerCase()){
                    case "=":
                        newToken = newToken + value; 
                        return te.new LexToken(newToken,"LE", ""); 
                        
                    case ">":
                        newToken = newToken + value; 
                        return te.new LexToken(newToken,"NE", ""); 
                        
                    default: 
                        returnChar = value; 
                        return te.new LexToken(newToken,"LT", ""); 
                        
    
                }
            }else {
                return te.new LexToken(newToken,"LT", "");                     
            }

        }else if(firstChar.equals("=")){
            if (reader.hasNext()){
                value = reader.next();
                switch(value.toLowerCase()){
                    case "=":
                        newToken = newToken + value; 
                        return te.new LexToken(newToken,"EQ", ""); 
                    default: 
                        returnChar = value; 
                        return te.new LexToken(newToken,"ASSIGN", ""); 
                }
            }else{
                return te.new LexToken(newToken,"ASSIGN", ""); 
            }

        }else if (firstChar.equals(">")){    
            if (reader.hasNext()){
                value = reader.next(); 
                switch(value.toLowerCase()){
                    case "=":
                        newToken = newToken + value; 
                        return te.new LexToken(newToken,"GE", ""); 
                        
                    default : 
                        returnChar = value; 
                        return te.new LexToken(newToken,"GT", ""); 
                                          
                }
            }else {
                return te.new LexToken(newToken,"GT", ""); 
            }
        }
        return null;
    }
    public static LexToken IsInterger( String firstChar, Scanner reader){
        
        boolean makeInt = false; 
        boolean digit = true; 
        boolean skip = false; 
        String newVar = firstChar;
        String value = ""; 
        while(digit && reader.hasNext()){
            value = reader.next();
            switch(value){
                case "0": 
                case "1": 
                case "2": 
                case "3": 
                case "4": 
                case "5": 
                case "6": 
                case "7": 
                case "8": 
                case "9": 
                    newVar = newVar + value ; 
                    break; 
                case ".": 
                    newVar = newVar + value ; 
                    digit = false;
                    break; 
                case "E":
                case "e":  
                    newVar = newVar + value ;
                    skip = true ;
                    digit = false;
                    break;
                case " ": 
                case "\n": 
                case "\t":
                case "": 
                case "\r":
                case "-":
                case "+":
                case "%":
                case "*":
                case "/":    
                case ")":
                case ">":
                case "=":
                case "<":
                case ";":
                
                case ",": 
                    returnChar = value; 
                    return te.new LexToken(newVar, "Integer", "Constant"); 
                
                default : 
                    newVar = newVar + value; 
                    return CreateError(newVar, reader); 
            }
        }

        if ( !skip){
            if (reader.hasNext()){ 
               value = reader.next();
                switch(value){
                    case "0": 
                    case "1": 
                    case "2": 
                    case "3": 
                    case "4": 
                    case "5": 
                    case "6": 
                    case "7": 
                    case "8": 
                    case "9": 
                        newVar = newVar + value ;
                        break; 
                    default: 
                        newVar = newVar + value; 
                        return CreateError(newVar, reader); 

                
            }
                
                
            }else {
                return CreateError(newVar, reader); 
            }
            digit = true; 
            while(digit && reader.hasNext()){
                value = reader.next();
                switch(value){
                    case "0": 
                    case "1": 
                    case "2": 
                    case "3": 
                    case "4": 
                    case "5": 
                    case "6": 
                    case "7": 
                    case "8": 
                    case "9": 
                        newVar = newVar + value ; 
                        break; 
                    case "E": 
                    case "e": 
                        newVar = newVar + value ;
                        digit = false;    
                        break;
                        
                    case " ": 
                    case "\n": 
                    case "\t":
                    case "": 
                    case "\r":
                    case "-":
                    case "+":
                    case "%":
                    case "*":
                    case "/":    
                    case ")":
                    case ">":
                    case "=":
                    case "<": 
                    case ";": 
                    case ",":   
                        returnChar = value;
                        return te.new LexToken(newVar, "DOUBLE", "Constant"); 
                    
                    default : 
                        newVar = newVar + value; 
                        return CreateError(newVar, reader); 
                }
            }
                        
        }// E arrows going in at this point 
        if (reader.hasNext()){        
            value = reader.next(); 
            skip = false; 
            switch(value){
                case "0": 
                case "1": 
                case "2": 
                case "3": 
                case "4": 
                case "5": 
                case "6": 
                case "7": 
                case "8": 
                case "9": 
                    skip = true; 
                    newVar = newVar + value ;
                    break; 
                case "+":
                    makeInt = true; 
                case "-":
                    newVar = newVar + value ;
                    break;
    
                default : 
                    newVar = newVar + value; 
                    return CreateError(newVar, reader); 
            }
        
        }else {
            return CreateError(newVar, reader); 
        }
        if (!skip ){
            if(reader.hasNext()){
                            value = reader.next();
                switch(value){
                    case "0": 
                    case "1": 
                    case "2": 
                    case "3": 
                    case "4": 
                    case "5": 
                    case "6": 
                    case "7": 
                    case "8": 
                    case "9": 
                        newVar = newVar + value ;
                        break; 
    
                    default : 
                        newVar = newVar + value; 
                        return CreateError(newVar, reader); 
                }
            }else {
                return CreateError(newVar, reader);  
            }

        }
        while(!finished && reader.hasNext()){
            value = reader.next();
            switch(value){
                case "0": 
                case "1": 
                case "2": 
                case "3": 
                case "4": 
                case "5": 
                case "6": 
                case "7": 
                case "8": 
                case "9": 
                    newVar = newVar + value ; 
                    break; 
                    
                case " ": 
                case "\n": 
                case "\t":
                case "": 
                case "\r":
                case "-":
                case "+":
                case "%":
                case "*":
                case "/":    
                case ")":
                case ">":
                case "=":
                case "<":
                case ";":
                case ",": 
                    returnChar = value; 
                        return te.new LexToken(newVar, "DOUBLE", "Constant"); 
                    
                    
                
                default : 
                    newVar = newVar + value; 
                    return CreateError(newVar, reader); 
            }
        }

        return null; 

    }
    public static LexToken getNextToken(Scanner reader)throws Exception{
        LexToken token; 
         

        
        token = null;
        while (!finished &&  token == null ){
            String value = ""; 
            if (returnChar != "" ){
                value = returnChar; 
                returnChar = "" ;
            }else {
                value = reader.next();
            }
            switch(value.toLowerCase()){
                case "0": 
                case "1": 
                case "2": 
                case "3": 
                case "4": 
                case "5": 
                case "6": 
                case "7": 
                case "8": 
                case "9": 
                    token = IsInterger(value,reader);
                    break; 
                case "a":
                case "b":
                case "c":
                case "d":
                case "e":
                case "f":
                case "g":
                case "h":
                case "i":
                case "j":
                case "k":
                case "l":
                case "m":
                case "n":
                case "o":
                case "p":
                case "q":
                case "r":
                case "s":
                case "t":
                case "u":
                case "v":
                case "w":
                case "x":
                case "y":
                case "z":
                    token = isId(value, reader);
                    break; 
    
                case "<": 
                case ">":
                case "=":
                    token = IsRelop(value, reader);
                    break; 
                case "-":
                case "+":
                case "%":
                case "*":
                case "/":
                    token = te.new LexToken(value, "", "");
                    break; 
                case "(":
                    token = te.new LexToken("(", "", "");
                    break; 
                case ")":
                    token = te.new LexToken(")", "", "");
                    break; 
                
                case " ":
                    //token = te.new LexToken(" ", "", "");
                    break; 
                case "\t":
                    //token = te.new LexToken("\t", "", "");
                    break; 
                case "\n": 
                    //token = te.new LexToken("\n", "", "");
                    break; 
                case ".": 
                    token = te.new LexToken(".", "", "");
                    dot = false;
                    break;
                case "\r": 
                    skip = true; 
                    break;
                case ",":
                    token  = te.new LexToken(",","",""); 
                    break; 
                case ";": 
                    token = te.new LexToken(";", "", "");
                    break;
                default:
                    
                    te.new LexToken(value,"ERROR", "ERROR"); 
                    break; 
    
    
    
    
            }
        }
        if (token != null &&token.token.equals("ERROR")){
            throw new Exception("Error Token encounter.'" + token.value + "'is not a recognizable token. "); 
        }
        return token;
    }

    public static void main(String[] args) {
        //ADD keywords to symbol table 
        te = new Interpreter();
        currentTable = syTable; 
        te.new LexToken("def", "DEF", "Keyword");
        te.new LexToken("def", "DEF", "Keyword");
        te.new LexToken("fed", "FED", "Keyword");
        te.new LexToken("int", "INT", "Keyword");
        te.new LexToken("double", "DOUBLE", "Keyword");
        te.new LexToken("if", "IF", "Keyword");
        te.new LexToken("fi", "FI", "Keyword");
        te.new LexToken("then", "THEN", "Keyword");
        te.new LexToken("else", "ELSE", "Keyword");
        te.new LexToken("while", "WHILE", "Keyword");
        te.new LexToken("do", "DO", "Keyword");
        te.new LexToken("od", "OD", "Keyword");
        te.new LexToken("print", "PRINT", "Keyword");
        te.new LexToken("return", "RETURN", "Keyword");
        te.new LexToken("or", "OR", "Keyword");
        te.new LexToken("and", "AND", "Keyword");
        te.new LexToken("not", "NOT", "Keyword");

        //syntax
        te.new LexToken(";", "SEMICOLON", "Syntax");
        te.new LexToken(",", "COMMA", "Syntax");
        te.new LexToken(")", "RIGHTBRACK", "Syntax");
        te.new LexToken("(", "LEFTBRACK", "Syntax");
        te.new LexToken(".", "DOT", "Syntax");
        te.new LexToken("=", "ASSIGN", "Relop");
        //relop
        te.new LexToken("<", "LT", "Relop");
        te.new LexToken(">", "GT", "Relop");
        te.new LexToken("==", "EQ", "Relop");
        te.new LexToken("<=", "LE", "Relop");
        te.new LexToken(">=", "GE", "Relop");
        te.new LexToken("<>", "NE", "Relop");

        //Operations
        te.new LexToken("+", "Operator", "Operators");
        te.new LexToken("-", "Operator", "Operators");
        te.new LexToken("*", "Operator", "Operators");
        te.new LexToken("/", "Operator", "Operators");
        te.new LexToken("%", "Operator", "Operators");


        
        
        
        reader.useDelimiter("");
        String front = "Parsing Error: "; 
        try {
            lookahead = getNextToken(reader); 
            root = parse(); 
            System.out.println();
            System.out.println();
            System.out.println("================Trees================");
            System.out.println();
            System.out.println("================Functionc================");
            System.out.print(root.functions); 
            System.out.println();
            System.out.println("================Declarations================");
            System.out.print(root.declarations); 
            System.out.println();
            System.out.println("================Statements================");
            System.out.print(root.statements); 
            System.out.println();
            System.out.println("================Trees================");
            System.out.println("");
            System.out.println();
            System.out.println("\n\n******NOTE: Variables appear in order they appear in hashtable. NOT order of appearance.");
            System.out.println("================Symbol Tables================");
            System.out.println("MAIN Symbol Table");
            String tok = "token";
            String vals = "Values";  
            String paddingString = "                         |                         "; 
            System.out.printf("\n%s" + paddingString.substring(tok.length(), paddingString.length() - vals.length()) +"%s\n", tok,   vals);
            System.out.printf(new String(new char[paddingString.length()]).replace("\0", "-"));
            syTable.entrySet().forEach(entry ->{
                LexToken temp  = entry.getValue(); 
                if (!temp.attributes.contains("Operators") && !temp.attributes.contains("Relop") && !temp.attributes.contains("Syntax")  && !temp.attributes.contains("Keyword") ){
                    if (temp.attributes.contains("Variable")){
                        String ts = getLexeme(temp.value); 
                        System.out.printf("\n%s" + paddingString.substring(temp.toString().length(), paddingString.length()- ts.length()) +"%s", temp, ts   );
                    }else {
                        String ts = entry.getValue().value; 
                        System.out.printf("\n%s" + paddingString.substring(temp.toString().length(), paddingString.length()- ts.length()) +"%s", temp,  ts); 
                    }
                }
                
            });
            ArrayList<HashMap<String, LexToken>> hashList = new ArrayList<>();
            ArrayList<String> funcList = new ArrayList<>(); 
            allTables.entrySet().forEach(entry-> {
                funcList.add(entry.getKey()); 
                hashList.add(entry.getValue());
            });
            
            int n = funcList.size(); 
            for (int i = 0 ; i < n ; i ++ ){
                System.out.println();
                System.out.printf("\n\n================Symbol Table================\n");
                System.out.printf("%s Symbol Table\n", funcList.get(i));
                System.out.printf("\n%s" + paddingString.substring(tok.length(), paddingString.length() - vals.length()) +"%s\n", tok,   vals);
                System.out.printf(new String(new char[paddingString.length()]).replace("\0", "-"));
                HashMap<String, LexToken> thisMap = hashList.get(i); 
                thisMap.entrySet().forEach(entry ->{
                    LexToken temp  = entry.getValue(); 
                    if (!temp.attributes.contains("Operators") && !temp.attributes.contains("Relop") && !temp.attributes.contains("Syntax")  && !temp.attributes.contains("Keyword") ){
                        if (temp.attributes.contains("Variable")){
                            String ts = getLexeme(temp.value); 
                            System.out.printf("\n%s" + paddingString.substring(temp.toString().length(), paddingString.length()- ts.length()) +"%s", temp, ts   );
                        }else {
                            String ts = entry.getValue().value; 
                            System.out.printf("\n%s" + paddingString.substring(temp.toString().length(), paddingString.length()- ts.length()) +"%s", temp,  ts); 
                        }
                    }
                    
                }); 
            }
            System.out.println();
            System.out.println("Source Code Output: ");
            System.out.println();
            returnStack.add("null"); 
            front = "Runtime Error:"; 
            Eval(root, syTable); 
        }catch(Exception e ){
            System.out.println(errorString); 
            
            System.out.printf("\n%s %s\n",front, e.getMessage());
            e.printStackTrace();
        }            
        reader.close();
        //HTML Closing body
    }
    
    public static RootNode parse()throws Exception{
        RootNode root = program(); 
        return root; 

    }
    public static RootNode program() throws Exception{
      
        RootNode prog = new RootNode(); 
        Node temp = fdecls();
        if (temp != null && temp.token != null ){
            prog.functions = temp; 
        } 
        temp = declarations(); 
        if (temp != null && temp.token != null ){
            prog.declarations = temp;

        } 
        temp = statement_seq(); 
        if (temp != null && temp.token != null ){
            prog.statements = temp; 
        } 
        if ( !match(".")  && !reader.hasNext()){
            throw new Exception("Missing '.'  at end of program"); 
        } else if (reader.hasNext() && !finished){
            throw new Exception("program ends after <statements> Expected '.' at the end of program, instead received '" + lookahead.value + "'"); 
        }

        return prog; 
    }
    public static Node fdecls() throws Exception{
        Node current = fdec(); 
        
        if (current.token != null  && match(";")  ){
             
            Node temp = fdecls(); 
            if (temp != null ){
                if (current.sibling == null){
                    current.sibling = temp;
                }else {
                    current.sibling.sibling = temp; 
                }
                 
            } 
        }
        return current;  
    }

    public static Node fdec() throws Exception{
        Node current = new Node(null, "fdec"); 
        Node child = null;  
        if(match("def")){


            Node temp = type(); 
            if (temp == null  ){//not valid type
                throw new Exception("Expected <type>"); 
            }else {
                current.child = temp ;
                child = temp;  
            }
            temp = fname();
            Node fname; 
            if (temp == null ){//not valid fname
                throw new Exception("Expected <fname>"); 
            }else {
                current.token = temp.token; 
                child.sibling = temp; 
                child = temp; 
                fname= temp; 
                
            }

            if (!match("(")){//not valid match
                throw new Exception("Expected '('"); 
            }
            temp = params();

            if (child.sibling == null ){
                child.sibling = temp;
            }else {
                Node prev = child; 
                while (prev.sibling != null ){
                    prev = prev.sibling; 
                }
                prev.sibling = temp; 
            }
                
            child = temp;  
        
            if (!match(")")){//not valid match
                throw new Exception("Expected ')'"); 
            }

            errorString = errorString.concat("\n"); 
            //System.out.println();
            tabNum ++ ; 
            errorString = errorString.concat(new String(new char[tabNum]).replace("\0", tabString)); 
            //System.out.print(new String(new char[tabNum]).replace("\0", tabString)); 
            temp = declarations(); 
            if (temp != null){//not valid declaration
                
                if (fname.child == null ){
                    fname.child = temp;
                }else {
                    Node prev = fname; 
                    while (prev.child != null ){
                        prev = prev.child; 
                    }
                    prev.child = temp; 
                } 
                child = temp;  
            }
            temp = statement_seq(); 
            if (fname.child == null){
                
                if (temp != null){//not valid declaration
                    
                    if (fname.child == null ){
                        fname.child = temp;
                    }else {
                        Node prev = fname; 
                        while (prev.child != null ){
                            prev = prev.child; 
                        }
                        prev.child = temp; 
                    } 
                    child = temp;  
                }
                
            }else if (temp != null ){//not valid declaration
                if (child.sibling == null ){
                    child.sibling = temp;
                }else {
                    Node prev = child; 
                    while (prev.sibling != null ){
                        prev = prev.sibling; 
                    }
                    prev.sibling = temp; 
                }
                child = temp;  
            }
            if (!match("fed")){//not valid declaration
                throw new Exception("Expected <fed>"); 
            }
            currentTable = syTable; 
            
            return current; 
        }
        return current; 
        
    }
    public static Node type() throws Exception{
        LexToken temp = lookahead; 
        if(match("int")){
            
            return new Node(temp  , "int"); 


        }else if (match("double") ){
            return new Node(temp, "double"); 
        }
        return null; 
    }
    public static Node fname() throws Exception{
        Node current = id();
        if (current != null) {
            HashMap<String, LexToken> temp = new HashMap<>(); 
            allTables.put(funcName, temp); 
            currentTable = temp; 
            return current; 
        }
        return null ; 
    }
    public static Node id() throws Exception{

        if (lookahead.token.equals("ID")){
            funcName = int2Lexeme.get(lookahead.value); 
            errorString = errorString.concat(int2Lexeme.get(lookahead.value));
            //System.out.printf("%s", int2Lexeme.get(lookahead.value));
            Node current = new Node(lookahead ,"ID"); 
            if (reader.hasNext()){
                lookahead = getNextToken(reader);
            }else {
                throw new Exception("Program unexpectedly ended");
            } 
            return current; 
        }
        return null; 
        
    }
    public static Node params()throws Exception{
        Node current = null; 
        
        Node temp = type();
        if (temp != null ){
            Node var = var(); 
            if (var == null ){
                throw new Exception("Expected <var> after <type>"); 
            }
            if (temp != null  && var != null ){
                current= temp; 
                current.child = var; 
                current.sibling = params_rest(); 
            }
        }
        return current; 
        

    }
    public static Node params_rest() throws Exception{
        Node current = null; 
        if(match(",")){
            current = params();

        }
        return current; 
    }
    public static Node var()throws Exception{
        Node current = id();
        if(current != null ){
            current.child = var_rest(); 
        }
         return current; 
    }
    public static Node var_rest()throws Exception{
        Node current = null; 
        if(match("[")){
            current = expr(); 
            
            if(current == null) {
                throw new Exception("Expected  <expr>"); 
            }
            current.value = current.value.concat(": Index of Array");
            if (!match("]")){
                throw new Exception("Expected ']' after <expr>"); 
            }
        }
        return current; 
    }
    public static Node expr()throws Exception{
        Node child = term(); 

        Node parent = expr_rest(); 
        if (parent != null ){
            Node temp = parent.child; 
            child.sibling = temp; 
            parent.child = child;
            return parent;  
        }
        return child; 
    }
    public static Node expr_rest()throws Exception {
        Node current = null; 
        LexToken temp = lookahead; 
        if (match("+")){
            current = new Node(temp, "+"); 
           
            Node child = term();
            current.child = child; 
            Node sibling = expr_rest(); 
            if (sibling != null ){
                current.child = sibling; 
                sibling.child.sibling = child; 
            }
             
            
        }else if (match("-")){
            current = new Node(temp, "-"); 
            Node child = term(); 
            current.child = child;
            Node sibling = expr_rest(); 
            if (sibling != null ){
                current.child = sibling; 
                sibling.child.sibling = child; 
            } 
        }
        return current; 
    }
    public static Node term()throws Exception{
        Node child = factor(); 
        Node parent = term_rest(); 
        if (parent != null){
            Node temp = parent.child; 
            child.sibling = temp; 
            parent.child = child; 
            return parent; 
        }
        return child; 
    }
    public static Node term_rest() throws Exception{
        Node current = null; 
        LexToken temp = lookahead; 
        if (match("*")){
            current = new Node(temp, "*"); 

            Node child = factor(); 
            current.child = child; 
            child.sibling = term_rest(); 
        }else if(match("/") ){
            current = new Node(temp, "/"); 
            Node child = factor(); 
            current.child = child; 
            child.sibling = term_rest(); 
        }else if ( match("%")){
            current = new Node(temp, "%"); 
            Node child = factor(); 
            current.child = child; 
            child.sibling = term_rest(); 
        }
        return current; 
    }   
    public static Node factor() throws Exception{
        Node current = null ; 
        LexToken tok = lookahead; 
        Node temp = id(); 
        if (temp != null ) {
            current = new Node(tok ,"ID" );
            Node child; 
            if(match("(")){
                child = exprseq(); 
                if(!match(")")){
                    throw new Exception("Expecting <exprseq> after ')'");
                }
                current.value = "Function Call"; 
            }else {
                child = factor_rest(); 
            }
             
            current.child = child; 
            return current; 
        }
        temp = number(); 
        if (temp != null ){
            current = new Node(tok, "number"); 
            return current; 
        }
        if(match("(")){
            current = expr(); 
            if (current != null ) {
                if (match(")")){
                    return current; 
                }else {
                    throw new Exception("Expecting ')'"); 
                }
                 
            }
        }
        if (match("-")){
            current = new Node(tok, "-"); 
            Node child = number();
            if (child == null ){
                child = id(); 
            } 
            if (child == null ){
                throw new Exception("Expected type factor"); 
            }
            current.child = child;
            child.sibling = expr_rest(); 
            return current; 
        }
        return current;
        
    }
    public static Node factor_rest() throws Exception{
        Node current = null; 
        return current;
    }
    public static Node exprseq()throws Exception{
        Node current = expr(); 
        if(current != null ) {
            current.sibling = exprseq_rest(); 
        }
        return current; 
    }
    public static Node exprseq_rest() throws Exception{
        Node current = null; 
        if(match(",")){
            current = exprseq();
        }
        return current;
    }
    public static Node number() throws Exception{
        Node current = null; 
        LexToken f = lookahead; 
        if (lookahead.token.equals("Integer") || lookahead.token.equals("DOUBLE")){
            current = new Node(f, "Integer"); 
            errorString = errorString.concat(lookahead.value);
            //System.out.printf("%s", lookahead.value );
            if (reader.hasNext()){
                lookahead = getNextToken(reader); 
            }else{
                throw new Exception("Program Unexpectedly ended");
            }

            
        }
        return current; 
    }
    public static Node declarations() throws Exception{
        Node current = decl(); 
        if (current != null){
            if (!match(";")){
                throw new Exception("Expected ';' "); 
            }
            Node temp = declarations(); 
            if (temp != null ){
                Node sibling = current.sibling; 
                if (sibling == null ){
                    current.sibling = temp;
                }else {
                    while (sibling.sibling != null ){
                        sibling = sibling.sibling; 
                    }
                    sibling.sibling = temp; 
                }
                
                
            }
            
        }
        return current; 
    }
    public static Node decl() throws Exception{
        Node current = type(); 
        if (current == null   ){
            return current; 
        }
        Node sibling = varlist(); 
        if (sibling == null ) {
            throw new Exception("Expected <varlist> ");
        }
        current.child = sibling; 
        return current; 
    }
    public static Node varlist()throws Exception{
        Node current =  var(); 
        if (current != null ){
            
            current.sibling = varlist_rest(); 
            
            return current; 
        }
        throw new Exception("Expected type <var> in <varlist>");  
    }
    public static Node varlist_rest()throws Exception{
        Node current = null;  
        if (match(",")){
            current = varlist(); 
        }
        return current; 
    }
    public static Node statement_seq() throws Exception{
        Node current = statement(); 

        if (current != null ){
            Node temp = current.sibling; 
            if (temp != null ){
                current.sibling.sibling = statement_seq_rest(); 
            }else {
                current.sibling = statement_seq_rest(); 
            }
        }
        return current; 
    }
    public static Node statement() throws Exception{
        LexToken tok = lookahead; 
        Node current = var(); 
        if (current != null ) {
            tok = lookahead; 
            if (!match("=")){
                throw new Exception("Expected '=' after type <var>");  
            }
            Node temp = current; 
            current = new Node(tok, "Assign");
            current.child = temp; 
            Node cSibling = expr();  
            if (cSibling == null ) {
                throw new Exception("Expected <expr> after '=' ");
            }
            temp.sibling = cSibling; 

        }else if(match("if")){
            current = new Node(tok, "IF"); 
            Node child = bexpr();  
            if (child == null ){
                throw new Exception("Expected <bexpr> after if ");
            }
            current.child = child; 
            if(!match("then")){
                throw new Exception("Expected 'then' after <bexpr>"); 

            }
            Node temp2 = new Node(null, "IF_REST"); 
            Node temp  = statement_seq();
            child.sibling = temp2;
            temp2.child = temp;  
            
            temp2.sibling = statement_rest();
             
             
            
            if (!match("fi")){
                throw new Exception("Expected fi at end of if");
            }
            
        }else if(match("while")){
            current = new Node(tok, "WHILE"); 
            Node child = bexpr(); 
            if (child == null ) {
                throw new Exception("Expected <bexpr> after while ");
            }else if(!match("do")){
                throw new Exception("Expected 'do' after <bexpr>"); 

            }
            current.child = child; 
            Node temp =  statement_seq(); 
            child.sibling = temp; 
            
            if (!match("od")){
                throw new Exception("Expected od at end of do");
            }
            
        }else if(match("print")){
            current = new Node(tok, "PRINT"); 
            Node child = expr(); 
            current.child = child; 
            
        }else if(match("return")){
            current = new Node(tok, "RETURN");
            Node child = expr(); 
            if(child == null){
                throw new Exception("Expected <Expr> at the end of 'return'");
            }
            current.child = child; 
        }
        return current; 
    }
    public static Node statement_rest()throws Exception{
        Node current = null; 
        if (match("else")){
            current = statement_seq(); 
        }
        return current; 
    }
    public static Node statement_seq_rest()throws Exception{
        Node current = null; 
        if (match(";")){
            current = statement_seq(); 
        }
        return current; 
    }
    public static Node bexpr()throws Exception{
        
        Node child = bterm();
        Node parent = bexpr_rest();
        if (parent != null ){
            Node temp = parent.child; 
            child.sibling = temp; 
            parent.child = child; 
            return parent; 
        }
        return child;  
    }
    public static Node bexpr_rest()throws Exception{
        Node current = null; 
        LexToken temp = lookahead; 
        if (match("or")){
            current = new Node(temp, "or") ; 
            Node child = bterm();
            current.child = child; 
            child.sibling = bexpr_rest();
        }
        return current; 
    }
    public static Node bterm()throws Exception{
        Node child = bfactor(); 
        Node parent = bterm_rest();
        if (parent != null){
            Node temp = parent.child; 
            child.sibling = temp; 
            parent.child = child; 
            return parent; 
        }
        return child; 
    }
    public static Node bterm_rest()throws Exception{
        Node current = null; 
        LexToken temp = lookahead; 
        if(match("and")){
            current = new Node(temp,"and"); 
            Node child = bfactor();
            current.child = child; 
            child.sibling = bterm_rest(); 
        }
        return current; 
    }
    public static Node bfactor()throws Exception{
        Node current = null; 
        LexToken tok = lookahead; 
        if (match("(")){
            current = bfactor_rest();
            if(!match(")")){
                throw new Exception("Expected ')' after <bfactor-rest>");
            }

        }else if(match("not")){
            current = new Node(tok , "not"); 
            current.child = bfactor();

        }
        return current; 
    }
    public static Node bfactor_rest() throws Exception{
        Node current = null; 
        Node temp  = bexpr();
        if(temp != null){
            return temp;  
        }
        temp = expr(); 
        if(temp != null ){
            current = new Node(lookahead, "Relop"); 
            current.child = temp; 
            if (lookahead.attributes.equals("Relop")){
                errorString = errorString.concat(lookahead.value);
                //System.out.printf("%s",lookahead.value); 
                if (reader.hasNext()){
                    lookahead = getNextToken(reader); 
                }else {
                    throw new Exception("Program Unexpectedly ended");
                }

            }else {
                throw new Exception("Expected <comp> after <expr>"); 
            }
            Node child = expr(); 
            if(child == null ){
                throw new Exception("Expected <expr> after <comp>"); 
            }
            temp.sibling = child; 
            return current; 
        }
        
        throw new Exception("Expected <bfactor-rest>"); 
    }
    public static boolean match(String value) throws Exception{
        //System.out.println("LINE 969");
        if(lookahead.valueEquals(value)){
            //System.out.println("LINE 971");


            if (value.equals("fi") || value.equals("od")||value.equals("fed")|| value.equals("else")){
                errorString = errorString.concat("\n");
                //System.out.println(); 
                tabNum--; 
                errorString = errorString.concat(new String(new char[tabNum]).replace("\0", tabString));
                //System.out.print(new String(new char[tabNum]).replace("\0", tabString)); 
            } 
            if (lookahead.token != "ID"){
                //System.out.println("LINE 973");
                errorString = errorString.concat(lookahead.value);
                //System.out.printf("%s", lookahead.value );

            }else{
                //System.out.println("LINE 974");
                errorString = errorString.concat(int2Lexeme.get(lookahead.value));
                //System.out.printf("%s", int2Lexeme.get(lookahead.value));
            }
            if (lookahead.attributes.contains("Keyword")){
                errorString = errorString.concat(" ");
                //System.out.print(" ");
            }

            //printing
            if (value.equals(";")){
                errorString = errorString.concat("\n");
                //System.out.println();
                errorString = errorString.concat(new String(new char[tabNum]).replace("\0", tabString));
                //System.out.print(new String(new char[tabNum]).replace("\0", tabString)); 
            }else if ( value.equals("then")||value.equals("do")||value.equals("else") ){
                errorString = errorString.concat("\n");
                //System.out.println();
                tabNum ++; 
                errorString = errorString.concat(new String(new char[tabNum]).replace("\0", tabString));
                //System.out.print(new String(new char[tabNum]).replace("\0", tabString)); 
            }

            if (value == "."){
                finished = true; 
            }

            if (!finished && reader.hasNext()  ){
                lookahead = getNextToken(reader); 
            }else if (!value.equals(".")){
                throw new Exception("Program Unexpectedly ended");
            }
            return true; 
        }
        return false; 
    
    }
   public static void Eval(RootNode root,HashMap<String, LexToken> table)throws Exception {
        //put 'main' environment on stack

        callStack.add(table); 
        //declarations 
        Node top = root.declarations; 
        while (top != null ){
            Eval_decl(top.value, top.child); 
            top = top.sibling;
        }
        top = root.statements;
        while(top != null && (returnStack.peek() instanceof String && returnStack.peek().equals("null"))){
            switch(top.value.toLowerCase()){
                case "assign":
                    Eval_assign(top);
                    break; 
                case "while":
                    eval_While(top.child);
                    break; 
                case "if":
                    eval_if(top.child);
                    break;
                case "print":
                    eval_print(top.child);
                    break;
                case "Function call":
                    eval_func(top);
                    break; 
                case "return": 
                    eval_return(top.child);
                    break; 
            }
            top = top.sibling; 
        }
        callStack.pop(); 
   } 
   public static void Eval_decl(String type, Node current)throws Exception{
        LexToken leftVal ;
        HashMap<String,LexToken> thing =  callStack.peek();
        if (current.value.equals("ID")){
            leftVal = callStack.peek().get(   int2Lexeme.get(current.token.value));
        }else{
            leftVal = current.token; 
        }
        if(leftVal.type != null ){
            throw new Exception("Variable already declared");
        }
        leftVal.type = type;
       if (current.sibling  != null ){
           Eval_decl(type, current.sibling);
       }
   }
   public static void Eval_assign(Node current) throws Exception{
        Node assignee = current.child; 
        String val = int2Lexeme.get(assignee.token.value); 

        HashMap<String, LexToken> newTable = allTables.get(val) ;
        if (newTable != null ){
            throw new Exception("Cannot use function as variable");
        }

        Node assigned = assignee.sibling; 
        val = int2Lexeme.get(assigned.token.value); 

        newTable = allTables.get(val) ;
        if (newTable != null && !assigned.value.equals("Function Call")){
            throw new Exception("Cannot use function as a variable");
        }
        LexToken var = callStack.peek().get(   int2Lexeme.get(assignee.token.value));
        if (var.type == null ){
                var = syTable.get(   int2Lexeme.get(assignee.token.value));
                if (var.type == null ){
                    throw new Exception("Use of Undeclared Variable");
                }
                
        }
        if (assigned.value.equals("number") ){
            
            if (var.type.equals("int") ){
                if (assigned.token.value.contains(".")){
                    throw new Exception("Could not put type double in variablle int"); 
                }else {
                    
                    var.compNum = Integer.parseInt(assigned.token.value);
                }
            }else if (var.type.equals("double")){
                var.compNum = Double.parseDouble(assigned.token.value);
            }else {
                throw new Exception("Could not identify RHS type"); 
            }
        }else if (assigned.value.equals("ID")){
            LexToken var2 = callStack.peek().get(   int2Lexeme.get(assigned.token.value));
            if (var.type.equals("int") && var2.type.equals("double")){
                throw new Exception("Cannot Cast double to int"); 
            }else {
                var.compNum = var2.compNum;
            }
            
        }else if (assigned.value.equals("Function Call")){
            Object temp = eval_func(assigned); 
            if (var.type .equals("int") && temp instanceof Integer){
                var.compNum = temp; 
            }else if (var.type .equals("double")){
                var.compNum = temp; 
            }else {
                throw new Exception("Value does not match variable type"); 
            }           
        }else {
            Object temp = Eval_Operation(assigned); 
            if (var.type .equals("int") && temp instanceof Integer){
                var.compNum = temp; 
            }else if (var.type .equals("double")){
                var.compNum = temp; 
            }else {
                throw new Exception("Value does not match variable type"); 
            }
            
        }
   }
   public static Object Eval_Operation(Node current) throws Exception{
        Node lhs = current.child; 
        Node rhs = lhs.sibling; 
        LexToken operator = current.token;
        Object leftVal; 
        Object rightVal; 
        LexToken rightToken; 
        LexToken leftToken; 
        if (rhs == null ){
            rightVal = "0"; 
        }else if (rhs.value.equals("Function Call")){
            rightVal = eval_func(rhs); 
        }else if (rhs.value.equals("+")|| rhs.value.equals("-") || rhs.value.equals("/") || rhs.value.equals("*")|| rhs.value.equals("%")){
            rightVal = Eval_Operation(rhs); 
        }else if (rhs.value.equals("ID")){
            rightToken = callStack.peek().get(   int2Lexeme.get(rhs.token.value));
            rightVal = rightToken.compNum; 
        }else {
            rightToken = rhs.token; 
            rightVal = rightToken.compNum; 
        }
        if (lhs.value.equals("Function Call")){
            leftVal = eval_func(lhs); 
        }else if (lhs.value.equals("+")|| lhs.value.equals("-") || lhs.value.equals("/") || lhs.value.equals("*")|| lhs.value.equals("%")){
            leftVal = Eval_Operation(lhs); 
        }else if (lhs.value.equals("ID")){
            leftToken = callStack.peek().get(   int2Lexeme.get(lhs.token.value));
            leftVal = leftToken.compNum; 
        }else {
            leftToken = lhs.token; 
            leftVal = leftToken.compNum; 
        }
        switch (operator.value){
            case"*":
                if (leftVal instanceof Double|| rightVal instanceof Double){
                    return  Double.parseDouble(leftVal.toString()) * Double.parseDouble(rightVal.toString()) ; 
                }else {
                    return  Integer.parseInt(leftVal.toString() ) * Integer.parseInt(rightVal.toString()); 
                }
            case"+":
                if (leftVal instanceof Double|| rightVal instanceof Double){
                    return Double.parseDouble(leftVal.toString()) + Double.parseDouble(rightVal.toString()); 
                }else {
                    return Integer.parseInt(leftVal.toString()) + Integer.parseInt(rightVal.toString()); 
                }    
            case "/": 
                if (leftVal instanceof Double|| rightVal instanceof Double){
                    return Double.parseDouble(leftVal.toString()) / Double.parseDouble(rightVal.toString()); 
                }else {
                    return Integer.parseInt(leftVal.toString()) / Integer.parseInt(rightVal.toString()); 
                }
            case "-":
                if (leftVal instanceof Double|| rightVal instanceof Double){
                    return Double.parseDouble(leftVal.toString()) - Double.parseDouble(rightVal.toString()); 
                }else {
                    return Integer.parseInt(leftVal.toString()) - Integer.parseInt(rightVal.toString()); 
                }
            case "%": 
                if (leftVal instanceof Double|| rightVal instanceof Double){
                    return Double.parseDouble(leftVal.toString()) % Double.parseDouble(rightVal.toString()); 
                }else {
                    return Integer.parseInt(leftVal.toString()) % Integer.parseInt(rightVal.toString()); 
                }
        } 
        return null; 
   }
   public static void eval_While(Node current) throws Exception{
        while (eval_bool(current)){
            Node sibling = current.sibling; 
            while (sibling != null && (returnStack.peek() instanceof String && returnStack.peek().equals("null")) ){
                switch(sibling.value.toLowerCase()){
                    case "assign":
                        Eval_assign(sibling);
                        break; 
                    case "while":
                        eval_While(sibling.child);
                        break; 
                    case "if":
                        eval_if(sibling.child);
                        break;
                    case "print":
                        eval_print(sibling.child);
                        break;
                    case "return": 
                        eval_return(sibling.child);
                        break; 
                    case "function call":
                        eval_func(sibling);
                        break;
                }
                sibling = sibling.sibling; 
            }
        }


    
   }
   public static void eval_print(Node current) throws Exception{
    if (current.value.equals("ID")){
        System.out.println(callStack.peek().get(   int2Lexeme.get(current.token.value)).compNum);
    }else if (current.value.equals("Function Call")){
        System.out.println(eval_func(current));
    }else if(current.value.equals("*")||current.value.equals("+") ||current.value.equals("-")||current.value.equals("/")||current.value.equals("%")){
        System.out.println(Eval_Operation(current)); 
    }
   }
   public static Boolean eval_bool(Node current) throws Exception{
    LexToken relop = current.token; 
    Node lhs = current.child; 
    Node rhs = lhs.sibling; 
    if(current.value.equals("not")){
        return !eval_bool(lhs);
   
    }else if ((rhs.value.equals("Relop")||rhs.value.equals("or")|| rhs.value.equals("and")|| rhs.value.equals("not"))&& (lhs.value.equals("Relop")||lhs.value.equals("or")|| lhs.value.equals("and") || rhs.value.equals("not"))){
        Boolean val = eval_bool(rhs);
        Boolean val2 = eval_bool(lhs); 
        switch(relop.value ){
            case "and": 
                return val && val2; 
            case "or": 
                return val || val2;     
        }
    }else {
        LexToken leftVal ;
        LexToken RightVal;
        if (lhs.value.equals("ID")){
            leftVal= callStack.peek().get(   int2Lexeme.get(lhs.token.value));
        }else {
            leftVal = lhs.token; 
        }
        if (rhs.value.equals("ID")){
            RightVal = callStack.peek().get(   int2Lexeme.get(rhs.token.value));
        }else {
            RightVal = rhs.token; 
        }
        switch(relop.value ){
            case ">":
                if (leftVal.type.equals("double")|| RightVal.type.equals("double")){
                    return Double.parseDouble(leftVal.compNum.toString()) > Double.parseDouble(RightVal.compNum.toString()); 
                }else {
                    return Integer.parseInt(leftVal.compNum.toString()) > Integer.parseInt(RightVal.compNum.toString()); 
                }
            case "<": 
                if (leftVal.type.equals("double")|| RightVal.type.equals("double")){
                    return Double.parseDouble(leftVal.compNum.toString()) < Double.parseDouble(RightVal.compNum.toString()); 
                }else {
                    return Integer.parseInt(leftVal.compNum.toString()) < Integer.parseInt(RightVal.compNum.toString()); 
                }
            case "==":
                if (leftVal.type.equals("double")|| RightVal.type.equals("double")){
                    return Double.parseDouble(leftVal.compNum.toString()) == Double.parseDouble(RightVal.compNum.toString()); 
                }else {
                    return Integer.parseInt(leftVal.compNum.toString()) == Integer.parseInt(RightVal.compNum.toString()); 
                }
            case "<>":
                if (leftVal.type.equals("double")|| RightVal.type.equals("double")){
                    return Double.parseDouble(leftVal.compNum.toString()) !=  Double.parseDouble(RightVal.compNum.toString()); 
                }else {
                    return Integer.parseInt(leftVal.compNum.toString()) != Integer.parseInt(RightVal.compNum.toString()); 
                }
            case "<=":
                if (leftVal.type.equals("double")|| RightVal.type.equals("double")){
                    return Double.parseDouble(leftVal.compNum.toString()) <= Double.parseDouble(RightVal.compNum.toString()); 
                }else {
                    return Integer.parseInt(leftVal.compNum.toString()) <= Integer.parseInt(RightVal.compNum.toString()); 
                }
            case ">=":
            if (leftVal.type.equals("double")|| RightVal.type.equals("double")){
                return Double.parseDouble(leftVal.compNum.toString()) >= Double.parseDouble(RightVal.compNum.toString()); 
            }else {
                return Integer.parseInt(leftVal.compNum.toString()) >= Integer.parseInt(RightVal.compNum.toString()); 
            }
        }
    }

    return true; 
   }
   public static void eval_if(Node current) throws Exception{
       
       Node If_rest = current.sibling; 
       if (current.sibling == null ){
           throw new Exception("Cannot have <IF> with no statement"); 
       }
       if (eval_bool(current)){
           //if-rest
            Node temp = If_rest.child;  
            while (temp != null && (returnStack.peek() instanceof String && returnStack.peek().equals("null")) ){
                switch(temp.value.toLowerCase()){
                    case "assign":
                        Eval_assign(temp);
                        break; 
                    case "while":
                        eval_While(temp.child);
                        break; 
                    case "if":
                        eval_if(temp.child);
                        break;
                    case "print":
                        eval_print(temp.child);
                        break;
                    case "return": 
                        eval_return(temp.child);
                        break; 
                    case "function call":
                        eval_func(temp);
                        break;
                }
                temp = temp.sibling; 
            }
           
       }else if (If_rest != null ){
            Node temp = If_rest.sibling;
            while (temp != null && (returnStack.peek() instanceof String && returnStack.peek().equals("null")) ){
                switch(temp.value.toLowerCase()){
                    case "assign":
                        Eval_assign(temp);
                        break; 
                    case "while":
                        eval_While(temp.child);
                        break; 
                    case "if":
                        eval_if(temp.child);
                        break;
                    case "print":
                        eval_print(temp.child);
                        break;
                    case "return": 
                        eval_return(temp.child);
                        break; 
                    case "function call":
                        eval_func(temp);
                        break;
                }
                temp = temp.sibling; 
            }
       }
   }
   private static Object deepCopy(Object object) {
    try {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ObjectOutputStream out2 = new ObjectOutputStream(out1);
        out2.writeObject(object);
        ByteArrayInputStream in1 = new ByteArrayInputStream(out1.toByteArray());
        ObjectInputStream in2 = new ObjectInputStream(in1);
        return in2.readObject();
    }
    catch (Exception e) {
    e.printStackTrace();
    return null;
    }
}
   public static Object eval_func(Node current)throws Exception{
       String val = int2Lexeme.get(current.token.value); 

        HashMap<String, LexToken> newTable = allTables.get(val) ;
        if (newTable == null ){
            throw new Exception("Invalid Function Call");
        }
        HashMap<String, LexToken> tempTable = new HashMap<>();
        for (Map.Entry<String, LexToken> mapElement : newTable.entrySet()) { 

            tempTable.put(mapElement.getKey(), (LexToken) deepCopy(mapElement.getValue()));

        }

        Node child = current.child; 
        ArrayList<Object> params = new ArrayList<>(); 
        while (child != null ){
            LexToken tok; 
            if (child.value.equals("ID")){
                tok= callStack.peek().get(   int2Lexeme.get(child.token.value));
                params.add(tok.compNum);
            }else if (child.value.equals("Function Call")){
                params.add(eval_func(child));
            }else if(child.value.equals("*")||child.value.equals("+") ||child.value.equals("-")||child.value.equals("/")||child.value.equals("%")){
                params.add(Eval_Operation(child)); 
            }else {
                params.add(child.token.compNum);
            }
            
            child = child.sibling; 
        }
        Node func = root.functions; 
        while (func != null  && !int2Lexeme.get(func.token.value).equals(val) ){
            func = func.sibling; 
        }
        if (func == null ){
            throw new Exception("Invalid function call. function not recognized"); 
        }
        Node type = func.child; 
        Node head = func.child.sibling.child; 
        Node paramStart = type.sibling.sibling;
        int i = 0; 
        Node temp = paramStart; 
        while (temp  != null ){
            i = i+1; 
            temp = temp.sibling;
        }
        if (i != params.size()){
            throw new Exception("Number of arguments does not match number of parameters"); 
        }
        i = 0; 
        while (paramStart != null ){
            Node paramChild = paramStart.child;
            LexToken tempToken = tempTable.get(   int2Lexeme.get(paramChild.token.value));
            tempToken.compNum = params.get(i); 
            tempToken.type = paramStart.value; 
            i++; 
            paramStart = paramStart.sibling; 
        }
        Eval_aux(head, tempTable);
        if (returnStack.peek() instanceof String &&returnStack.peek().equals("null")){
            return null; 
        }
        Object returnValue = returnStack.pop(); 
        
        return returnValue; 
    //get return val from stack if it exists. 
    //if val exists, save to variable, pop callstack and add the val to corresponding variable in outer function or main.


   }
   public static void eval_return(Node current)throws Exception{
    if (current.value.equals("ID")){
        returnStack.push(    callStack.peek().get(   int2Lexeme.get(current.token.value)).compNum);
    }else if (current.value.equals("Function Call")){
        returnStack.push(eval_func(current));
    }else if(current.value.equals("*")||current.value.equals("+") ||current.value.equals("-")||current.value.equals("/")||current.value.equals("%")){
        returnStack.push(Eval_Operation(current)); 
    }else {
        returnStack.push(current.token.compNum);
    }
   }
   public static void Eval_aux(Node top,HashMap<String, LexToken> table)throws Exception {
    //put 'main' environment on stack
    HashMap<String, LexToken> ttttt =  table; 
    callStack.push(table); 
    //declarations 

    while (top != null && (top.value.equals("int")||top.value.equals("double")) ){
        Eval_decl(top.value, top.child); 
        top = top.sibling;
    }
    while(top != null && (returnStack.peek() instanceof String && returnStack.peek().equals("null"))){
        switch(top.value.toLowerCase()){
            case "assign":
                Eval_assign(top);
                break; 
            case "while":
                eval_While(top.child);
                break; 
            case "if":
                eval_if(top.child);
                break;
            case "print":
                eval_print(top.child);
                break;
            case "Function call":
                eval_func(top);
                break; 
            case "return": 
                eval_return(top.child);
                break; 
        }
        top = top.sibling; 
    }
    callStack.pop(); 
} 
}
