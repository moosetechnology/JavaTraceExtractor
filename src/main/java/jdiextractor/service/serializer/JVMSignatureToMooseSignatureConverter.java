package jdiextractor.service.serializer;

import java.lang.reflect.GenericSignatureFormatError;

/**
 * Edit of the class SignatureParser created by Oracle, now used to recreate a method signature 
 * (in the Moose java model format) from the signature attribute used by the JVM. This class is necessary
 * because the signature (Moose) for methods using wildcards can only be created through the signature 
 * (JVM).
 * 
 * The class is only necessary for the parameters of a method's signature, so the only public methods are
 * `make()` to get an instance of the class, and `parseTypeSig(String)` to convert the JVM signature of a 
 * parameter to a Moose signature.
 */
public class JVMSignatureToMooseSignatureConverter {
    // The input is conceptually a character stream (though currently it's
    // a string). This is slightly different than traditional parsers,
    // because there is no lexical scanner performing tokenization.
    // Having a separate tokenizer does not fit with the nature of the
    // input format.
    // Other than the absence of a tokenizer, this parser is a classic
    // recursive descent parser. Its structure corresponds as closely
    // as possible to the grammar in the JVMS.
    //
    // A note on asserts vs. errors: The code contains assertions
    // in situations that should never occur. An assertion failure
    // indicates a failure of the parser logic. A common pattern
    // is an assertion that the current input is a particular
    // character. This is often paired with a separate check
    // that this is the case, which seems redundant. For example:
    //
    // assert(current() != x);
    // if (current != x {error("expected an x");
    //
    // where x is some character constant.
    // The assertion indicates, that, as currently written,
    // the code should never reach this point unless the input is an
    // x. On the other hand, the test is there to check the legality
    // of the input wrt to a given production. It may be that at a later
    // time the code might be called directly, and if the input is
    // invalid, the parser should flag an error in accordance
    // with its logic.

    private char[] input; // the input signature
    private int index = 0; // index into the input
    // used to mark end of input
    private static final char EOI = ':';
    private static final boolean DEBUG = false;
    private StringBuilder strBuilder = new StringBuilder();
    
    private boolean buildWithFullyQualifiedName = false;

    // private constructor - enforces use of static factory
    private JVMSignatureToMooseSignatureConverter(){}

    // Utility methods.
    // Most parsing routines use the following routines to access the
    // input stream, and advance it as necessary.
    // This makes it easy to adapt the parser to operate on streams
    // of various kinds as well as strings.

    // returns current element of the input and advances the input
    private char getNext(){
        assert(index <= input.length);
        try {
            return input[index++];
        } catch (ArrayIndexOutOfBoundsException e) { return EOI;}
    }

    // returns current element of the input
    private char current(){
        assert(index <= input.length);
        try {
            return input[index];
        } catch (ArrayIndexOutOfBoundsException e) { return EOI;}
    }

    // advance the input
    private void advance(){
        assert(index <= input.length);
        index++;
    }

    // For debugging, prints current character to the end of the input.
    private String remainder() {
        return new String(input, index, input.length-index);
    }

    // Match c against a "set" of characters
    private boolean matches(char c, char... set) {
        for (char e : set) {
            if (c == e) return true;
        }
        return false;
    }

    // Error handling routine. Encapsulates error handling.
    // Takes a string error message as argument.
    // Currently throws a GenericSignatureFormatError.

    private Error error(String errorMsg) {
        return new GenericSignatureFormatError("Signature Parse error: " + errorMsg +
                                               "\n\tRemaining input: " + remainder());
    }

    /**
     * Verify the parse has made forward progress; throw an exception
     * if no progress.
     */
    private void progress(int startingPosition) {
        if (index <= startingPosition)
            throw error("Failure to make progress!");
    }

    /**
     * Static factory method. Produces a parser instance.
     * @return an instance of <tt>SignatureParser</tt>
     */
    public static JVMSignatureToMooseSignatureConverter make() {
        return new JVMSignatureToMooseSignatureConverter();
    }

    private void appendToResultString(String str) {
    	strBuilder.append(str);
    }
 
    /**
     * Parses a continuous stream of method parameters from a JNI signature.
     * Example input: "TK;ILjava/lang/String;"
     * Example output: "K,int,java.lang.String"
     * @param paramsSignature the string containing ONLY the parameters (between parentheses)
     * @return A comma-separated string of Moose-formatted types
     */
    public String parseMethodParameters(String paramsSignature) {
        if (paramsSignature == null || paramsSignature.trim().isEmpty()) {
            return "";
        }
        
        if (DEBUG) System.out.println("Parsing method parameters:" + paramsSignature);
        
        input = paramsSignature.toCharArray();
        index = 0;
        strBuilder = new StringBuilder();
        
        boolean isFirstParameter = true;
        
        // The parser reads the input stream until exhaustion
        while (index < input.length) {
            if (!isFirstParameter) {
                strBuilder.append(",");
            }
            
            // Consumes exactly one type (primitive, object, array, or generic)
            // and advances the index to the exact starting position of the next type.
            parseTypeSignature(); 
            
            isFirstParameter = false;
        }
        
        return strBuilder.toString();
    }
    
    /**
     * Parses a type signature and produces a signature 
     * in the format used by FamixJavaMethod entities.
     *
     * @param s a string representing the input type signature
     * @return A string in the format used by FamixJavaMethod entities
     * @throws GenericSignatureFormatError if the input is not a valid
     * type signature
     */
    public String parseTypeSig(String s) {
        if (DEBUG) System.out.println("Parsing type sig:" + s);
        input = s.toCharArray();
        strBuilder = new StringBuilder();
        parseTypeSignature(); 
        
        return strBuilder.toString();
    }

    // Parsing routines.
    // As a rule, the parsing routines access the input using the
    // utilities current(), getNext() and/or advance().
    // The convention is that when a parsing routine is invoked
    // it expects the current input to be the first character it should parse
    // and when it completes parsing, it leaves the input at the first
    // character after the input parses.

    /*
     * Note on grammar conventions: a trailing "*" matches zero or
     * more occurrences, a trailing "+" matches one or more occurrences,
     * "_opt" indicates an optional component.
     */

    private String parseIdentifier(){
        StringBuilder result = new StringBuilder();
        while (!Character.isWhitespace(current())) {
            char c = current();
            switch(c) {
            case ';':
            case '.':
            case '/':
            case '[':
            case ':':
            case '>':
            case '<':
                return result.toString();
            default:{
                result.append(c);
                advance();
            }

            }
        }
        return result.toString();
    }
    /**
     * FieldTypeSignature:
     *     ClassTypeSignature
     *     ArrayTypeSignature
     *     TypeVariableSignature
     */
    private void parseFieldTypeSignature() {
        parseFieldTypeSignature(true);
    }

    private void parseFieldTypeSignature(boolean allowArrays) {
        switch(current()) {
        case 'L':
           parseClassTypeSignature();
           return;
        case 'T':
           parseTypeVariableSignature();
           return;
        case '[':
            if (allowArrays) {
                parseArrayTypeSignature();
            	return;
            }
            else
                throw error("Array signature not allowed here.");
        default: throw error("Expected Field Type Signature");
        }
    }

    /**
     * ClassTypeSignature:
     *     "L" PackageSpecifier_opt SimpleClassTypeSignature ClassTypeSignatureSuffix* ";"
     */
    private void parseClassTypeSignature(){
        assert(current() == 'L');
        if (current() != 'L') { throw error("expected a class type");}
        advance();
        //List<SimpleClassTypeSignature> scts = new ArrayList<>(5);
        //scts.add(parsePackageNameAndSimpleClassTypeSignature());
        parsePackageNameAndSimpleClassTypeSignature();

        //parseClassTypeSignatureSuffix(scts);
        if (current() != ';')
            throw error("expected ';' got '" + current() + "'");

        advance();
        return;
    }

    /**
     * PackageSpecifier:
     *     Identifier "/" PackageSpecifier*
     */
    private void parsePackageNameAndSimpleClassTypeSignature() {
        // Parse both any optional leading PackageSpecifier as well as
        // the following SimpleClassTypeSignature.

        String id = parseIdentifier();
        StringBuilder qualifiedNameBuilder = new StringBuilder();
        
        if (current() == '/') { // package name
            qualifiedNameBuilder.append(id);

            while(current() == '/') {
                advance();
                qualifiedNameBuilder.append(".");
                qualifiedNameBuilder.append(id = parseIdentifier());
            }
        }
        
        if (buildWithFullyQualifiedName) this.appendToResultString(qualifiedNameBuilder.toString());
        else this.appendToResultString(id); // last identifier is always the class name

        switch (current()) {
        case ';':
            return; // all done!
        case '<':
            if (DEBUG) System.out.println("\t remainder: " + remainder());
            parseTypeArguments();
            return;
        default:
            throw error("expected '<' or ';' but got " + current());
        }
    }

    /**
     * SimpleClassTypeSignature:
     *     Identifier TypeArguments_opt
     */
    private void parseSimpleClassTypeSignature(boolean dollar){
        String id = parseIdentifier();
        char c = current();

        switch (c) {
        case ';':
        case '.':
            //return SimpleClassTypeSignature.make(id, dollar, new TypeArgument[0]) ;
        	return;
        case '<':
            //return SimpleClassTypeSignature.make(id, dollar, parseTypeArguments());
        	return;
        default:
            throw error("expected '<' or ';' or '.', got '" + c + "'.");
        }
    }

    /**
     * ClassTypeSignatureSuffix:
     *     "." SimpleClassTypeSignature
     */
    /**private void parseClassTypeSignatureSuffix(List<SimpleClassTypeSignature> scts) {
        while (current() == '.') {
            advance();
            scts.add(parseSimpleClassTypeSignature(true));
        }
    }*/

    /**
     * TypeArguments:
     *     "<" TypeArgument+ ">"
     */
    private void parseTypeArguments() {
        assert(current() == '<');
        if (current() != '<') { throw error("expected '<'");}
        this.appendToResultString("<");
        advance();
        parseTypeArgument();
        while (current() != '>') {
                //(matches(current(),  '+', '-', 'L', '[', 'T', '*')) {
            parseTypeArgument();
        }
        advance();
        this.appendToResultString(">");
        return;
    }

    /**
     * TypeArgument:
     *     WildcardIndicator_opt FieldTypeSignature
     *     "*"
     */
    private void parseTypeArgument() {
        char c = current();
        switch (c) {
        case '+': {
            advance();
            this.appendToResultString("? extends ");
            return;
        }
        case '*':{
            advance();
            this.appendToResultString("?");
            return;
        }
        case '-': {
            advance();
            this.appendToResultString("? super ");
            return;
        }
        default:
            parseFieldTypeSignature();
            return;
        }
    }

    /**
     * TypeVariableSignature:
     *     "T" Identifier ";"
     */
    private void parseTypeVariableSignature() {
        assert(current() == 'T');
        if (current() != 'T') { throw error("expected a type variable usage");}
        advance();
        String id = parseIdentifier();
        this.appendToResultString(id);
        if (current() != ';') {
            throw error("; expected in signature of type variable named" +
                  id);
        }
        advance();
        return;
    }

    /**
     * ArrayTypeSignature:
     *     "[" TypeSignature
     */
    private void parseArrayTypeSignature() {
        if (current() != '[') {throw error("expected array type signature");}
        advance();
        parseTypeSignature();
        this.appendToResultString("[]");
        return;
    }

    /**
     * TypeSignature:
     *     FieldTypeSignature
     *     BaseType
     */
    private void parseTypeSignature() {
        switch (current()) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
            parseBaseType();
            return;
        default:
            parseFieldTypeSignature();
        }
    }

    private void parseBaseType() {
        switch(current()) {
        case 'B':
            advance();
            this.appendToResultString("byte");
            return;
        case 'C':
            advance();
            this.appendToResultString("char");
            return;
        case 'D':
            advance();
            this.appendToResultString("double");
            return;
        case 'F':
            advance();
            this.appendToResultString("float");
            return;
        case 'I':
            advance();
            this.appendToResultString("int");
            return;
        case 'J':
            advance();
            this.appendToResultString("long");
            return;
        case 'S':
            advance();
            this.appendToResultString("short");
            return;
        case 'Z':
            advance();
            this.appendToResultString("boolean");
            return;
        default: {
            assert(false);
            throw error("expected primitive type");
        }
        }
    }

 }
