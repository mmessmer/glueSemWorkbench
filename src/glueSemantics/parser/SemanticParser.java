/*
 * Copyright 2018 Moritz Messmer and Mark-Matthias Zymla.
 * This file is part of the Glue Semantics Workbench
 * The Glue Semantics Workbench is free software and distributed under the conditions of the GNU General Public License,
 * without any warranty.
 * You should have received a copy of the GNU General Public License along with the source code.
 * If not, please visit http://www.gnu.org/licenses/ for more information.
 */

package glueSemantics.parser;

import glueSemantics.semantics.lambda.*;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticParser {
    public String getInput() {
        return input;
    }

    public SemRepresentation getParsed() {
        return parsed;
    }

    private String input;
    private SemRepresentation parsed;
    private HashMap<String,SemAtom> boundVars = new HashMap<>();

    private final char LAMBDA = '/';
    private final char EX = 'E';
    private final char UNI = 'A';
    private final char AND = '&';
    private final char OR = '|';
    private final char IMP = '~';

    private final Pattern typePattern = Pattern.compile("<(.+?),(.+)>");
    private final Pattern binTermPattern = Pattern.compile("(\\[ *(.+?) *([~|&]) *(.+) *]|\\( *(.?+) *([~|&]) *(.+) *\\))");
    private final Pattern unTermPattern = Pattern.compile("(\\[ *([^~&|]+) *]|\\( *([^~&|]+) *\\))");
    private final Pattern funcappPattern = Pattern.compile("(\\[ *([^~&|]+) *]|\\( *([^~&|]+) *\\))");



    // Initialize a parser with the standard settings
    public SemanticParser(){

    }


    /**
     *  Parses a semantics term. Recursively goes through the input String and creates
     *  semantic objects corresponding to the formula string.
     * @param input The input string to be parsed
     * @return A SemRepresenation object generated from the input string
     */
    SemRepresentation parseSemTerm(String input) throws ParserInputException {
        char[] inputChars = input.toCharArray();
        /*
        Example: \P_<e,t>.\x_e.P(x) & green(x)
         */
        for (int i = 0; i < inputChars.length; i++) {
            switch (inputChars[i]){
                case ' ': continue;
                case LAMBDA:
                    SemType binderType = parseType(input.substring(i+2));
                    SemAtom binder = new SemAtom(SemAtom.SemSort.VAR,Character.toString(inputChars[i+1]),binderType);
                    boundVars.put(binder.toString(),binder);
                    Matcher m = typePattern.matcher(input);
                    if(!m.find()) {throw new ParserInputException(i, "semantic type denominator expected");}
                    SemRepresentation body = parseSemTerm(input.substring(m.end()));
                    return new SemFunction(binder,body);
                case EX:
                    binderType = parseType(input.substring(i+2));
                    binder = new SemAtom(SemAtom.SemSort.VAR,Character.toString(inputChars[i+1]),binderType);
                    boundVars.put(binder.toString(),binder);
                    m = typePattern.matcher(input);
                    if(!m.find()) {throw new ParserInputException(i, "semantic type denominator expected");}
                    body = parseSemTerm(input.substring(m.end()));
                   return new SemQuantEx(SemQuantEx.SemQuant.EX,binder,body);
                case UNI:
                    binderType = parseType(input.substring(i+2));
                    binder = new SemAtom(SemAtom.SemSort.VAR,Character.toString(inputChars[i+1]),binderType);
                    boundVars.put(binder.toString(),binder);
                    m = typePattern.matcher(input);
                    if(!m.find()) {throw new ParserInputException(i, "semantic type denominator expected");}
                    body = parseSemTerm(input.substring(m.end()));
                    return new SemQuantEx(SemQuantEx.SemQuant.UNI,binder,body);
                case ('('|'['):
                    // it is a binary term
                    m = binTermPattern.matcher(input);
                    if(!m.find()) {throw new ParserInputException(i, "there might be a bracketing error");}
                    SemRepresentation left = parseSemTerm(m.group(1));
                    SemRepresentation right = parseSemTerm(m.group(3));
                    BinaryTerm bin = null;
                    switch (m.group(2).charAt(0)) {
                        case OR:
                            bin = new BinaryTerm(left, BinaryTerm.SemOperator.OR,right);
                            break;
                        case AND:
                            bin = new BinaryTerm(left, BinaryTerm.SemOperator.AND,right);
                            break;
                        case IMP:
                            bin = new BinaryTerm(left, BinaryTerm.SemOperator.IMP,right);
                            break;
                    }
                    if (bin == null) {throw new ParserInputException(i, "binary term expected");}
                    return bin;
                default:
                    // By default, try parsing a functional application

            }
        }
        return null;
    }


    private SemRepresentation parseBody(String substring) {
    // This method tries to parse a functional application
        return null;
    }

    SemType parseType(String type) throws ParserInputException {
        Matcher m = typePattern.matcher(type);
        SemType left;
        SemType right;
        if(m.find()) {
            String leftString = m.group(1);
            String rightString = m.group(2);
            if (typePattern.matcher(leftString).find()){
                left = parseType(leftString);
            }
            else {
                switch (leftString) {
                    case "e":
                        left = new SemType(SemType.AtomicType.E);
                        break;
                    case "v":
                        left = new SemType(SemType.AtomicType.V);
                        break;
                    case "i":
                        left = new SemType(SemType.AtomicType.I);
                        break;
                    default:
                        throw new ParserInputException("Type parsing error: " +
                                "invalid type denominator on left hand side of complex type");

                }
            }
            if (typePattern.matcher(rightString).find()){
                right = parseType(rightString);
            }
            else {
                if (rightString.equals("t"))
                    right = new SemType(SemType.AtomicType.T);
                else
                    throw new ParserInputException("Type parsing error: " +
                            "invalid type denominator on right hand side of complex type");
            }
        }
        else {
            switch (type) {
                case "e":
                    return new SemType(SemType.AtomicType.E);
                case "v":
                    return new SemType(SemType.AtomicType.V);
                case "i":
                    return new SemType(SemType.AtomicType.I);
                case "t":
                    return new SemType(SemType.AtomicType.T);
                default:
                    throw new ParserInputException("Type parsing error: " +
                            "there seems to be an ill-formed type specification");
            }
        }
        return new SemType(left,right);
    }
}
