import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/*
TODO
[x] handle whitespace
[ ] handle multiple consecutive operators (++, +-, *+, *-, /+, /-)
[ ] handle redundant parenthesis '((2+3)*4)'
[ ] handle operations with 'e'
*/

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input;

        //noinspection SpellCheckingInspection
        System.out.println("Welcome to calculovator!\nto exit, type 'exit' or 'ex'");

        while (true) {
            input = sc.nextLine();
            if ("exit".equals(input)) break;
            if ("ex".equals(input)) break;
            try {
                String result = Expression1.parse(input);
                System.out.println(result);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("bye");
        sc.close();
    }
}

class Expression1 {

    static String parse(@NotNull String expression) {

        if (expression.isEmpty()) throw new IllegalArgumentException("expression can't be empty");

        String result;

        List<String> numbers = new ArrayList<>();
        List<String> characters = new ArrayList<>();

        char[] arr = expression.toCharArray();
        boolean isFirstNumber = true;

        int i, numIndex = 0, charIndex = 0, whitespaceNumber = 0;

        StringBuilder numberInList = new StringBuilder();
        StringBuilder characterInList = new StringBuilder();

        for (i = 0; i < arr.length; i++) {
            char c = arr[i];
            
            if (Character.isWhitespace(c)) {
                if (whitespaceNumber < i-1) whitespaceNumber++;
	            continue;
            }
            if (c == ',') c = '.';
            
            if (Utility.isDigit(c)) {
                if (i == 0) {
                    isFirstNumber = true;
                    numberInList.append(c);
                    numbers.add(String.valueOf(c));
                } else {
                    if (Utility.isDigit(arr[i - 1 - whitespaceNumber])) {
                        numberInList.append(c);
                        numbers.set(numIndex, numberInList.toString());
                    } else {
                        numberInList.setLength(0);
                        numberInList.append(c);
                        numbers.add(String.valueOf(c));
                        numIndex++;
                    }
	                whitespaceNumber = 0;
                }

            } else {
                if (i == 0) {
                    isFirstNumber = false;
                    characterInList.append(c);
                    characters.add(String.valueOf(c));
                } else {
                    if (Utility.isDigit(arr[i - 1 - whitespaceNumber])) {
                        if (!characters.isEmpty()) charIndex++;
                        characterInList.setLength(0);
                        characterInList.append(c);
                        characters.add(String.valueOf(c));
                    } else {
                        characterInList.append(c);
                        characters.set(charIndex, characterInList.toString());
                    }
	                whitespaceNumber = 0;
                }

            }
        }
        

        if (!isFirstNumber) {
            
            if (numbers.isEmpty()) throw new IllegalArgumentException("expression not expected: '" + expression + "'");

            String firstCh = characters.get(0);

            if (firstCh.contains("+")) {
                firstCh = firstCh.replaceAll("\\+", "");
                if (firstCh.isEmpty()) characters.remove(0);
                else {
                    characters.set(0, firstCh);
                    System.out.println(characters.get(0));
                }
            }
            if (firstCh.contains("-")) {
                numbers.add(0, "0");
            } else if (firstCh.equals("(")) {
                numbers.add(0, "0");
                characters.set(0, "+(");
            } else if (firstCh.contains("(")) {
                numbers.add(0, "0");
                characters.set(0, "+" + firstCh);
            }
        }
        
        if (!characters.isEmpty() && numbers.size()<2) throw new IllegalArgumentException("expression not expected: '" + expression + "'");

        List<Integer> braceStartI = new ArrayList<>();
        List<Integer> braceEndI = new ArrayList<>();

        for (i = 0; i < characters.size(); i++) {
            char[] c = characters.get(i).toCharArray();
            for (char value : c) {
                if (value == '(') {
                    braceStartI.add(i);
                }
                if (value == ')') {
                    braceEndI.add(i);
                }
            }
        }

        if (braceStartI.size() > braceEndI.size()) {
            throw new IllegalArgumentException("Missing closing parenthesis");
        } else if (braceStartI.size() < braceEndI.size()) {
            throw new IllegalArgumentException("Missing opening parenthesis");
        }

        if (!braceStartI.isEmpty()) {
            int braceIndex = 0;
            for (int item : braceStartI) {
                int start = item + 1 - braceIndex;
                int end = braceEndI.get(braceIndex) - braceIndex;

                List<String> braceNumList = new ArrayList<>(numbers.subList(start, end));
                braceNumList.add(numbers.get(end));
                List<String> braceCharList = new ArrayList<>(characters.subList(start, end));
                result = eval(braceNumList, braceCharList);
                Utility.replaceRange(numbers, start, end, result);

                String braceStartCh = characters.get(start - 1);
                braceStartCh = braceStartCh.replaceFirst("\\(", "");
                String braceEndCh = characters.get(end);
                braceEndCh = braceEndCh.replaceFirst("\\)", "");

                if (braceStartCh.isEmpty()) {
                    characters.remove(start - 1);
                } else {
                    characters.set(start - 1, braceStartCh);
                }
                if (braceEndCh.isEmpty()) {
                    characters.remove(end);
                } else {
                    characters.set(end, braceEndCh);
                }

                if (end > start) {
                    characters.subList(start, end).clear();
                }

                braceIndex++;
            }
        }

        return eval(numbers, characters);

    }

    static String eval(@NotNull List<String> numbers, @NotNull List<String> characters) {

        int lenC = characters.size();
        

        while (Utility.contains(characters, "^")) {
            for (int i = 0; i < lenC; i++) {
                String c = characters.get(i);
                if (Utility.contains(c, "^")) {
                    continue;
                }

                if (c.equals("^")) {
                    Calculations.priorityOperation(numbers, characters, i, priorityOperations.POWER);
                    lenC = characters.size();
                }
            }
        }

        while (Utility.contains(characters, "*/÷")) {
            for (int i = 0; i < lenC; i++) {
                String c = characters.get(i);
                if (Utility.contains(c, "*x/÷")) {
                    continue;
                }
                priorityOperations op = switch (c) {
                    case "*", "x" -> priorityOperations.MULTIPLY;
                    case "/", "÷" -> priorityOperations.DIVIDE;
                    default -> throw new IllegalArgumentException("operation not expected: '" + c + "'");
                };
                Calculations.priorityOperation(numbers, characters, i, op);
                lenC = characters.size();
            }
        }

        BigDecimal result = new BigDecimal(numbers.get(0));

        for (int i = 0; i < lenC; i++) {
            String op = characters.get(i);
            BigDecimal number = new BigDecimal(numbers.get(i+1));
            
            op = Calculations.handleConsecutiveOperators(op);
            
            result = switch (op) {
                case "+" -> result.add(number);
                case "-" -> result.subtract(number);
                default -> throw new IllegalArgumentException("operation not expected: '" + op + "'");
            };
            
        }

        return String.valueOf(result);
    }
    
}

class Utility {

    static boolean contains(@NotNull List<String> list, @NotNull String x) {
        String listI;
        char[] arr = x.toCharArray();

        for (String s : list) {
            listI = s;
            for (char c : arr) {
                if (listI.contains(String.valueOf(c))) return true;
            }
        }
        return false;
    }

    static boolean contains(String checking, @NotNull String x) {
        char[] arr = x.toCharArray();

        for (char s : arr) {
            if (checking.contains(String.valueOf(s))) return true;
        }
        return false;
    }
	
	@Contract(pure = true)
    static int countOccurrences(@NotNull String checking, char x) {
        char[] arr = checking.toCharArray();
        int count = 0;
        for (char c: arr) {
            if (c == x) count++;
        }
        return count;
    }

    static boolean isDigit(char c) {
        if (Character.isDigit(c)) return true;
        return c == '.' || c == ',';
    }
    
    static void replaceRange(@NotNull List<String> list, int from, int to, final String toReplace) {
        Collections.fill(list.subList(from, to + 1), toReplace);
        if (to >= from + 1) {
            list.subList(from + 1, to + 1).clear();
        }
    }

}

class Calculations {
    
    @NotNull
    static String handleConsecutiveOperators(@NotNull String op) {
        if (op.length()>1 && Utility.contains(op, "+") || Utility.contains(op, "-")) {
            int plusCount = Utility.countOccurrences(op, '+');
            int minusCount = Utility.countOccurrences(op, '-');
            if (plusCount%2 == 0 && minusCount%2 == 0) op = "+";
            else op = "-";
        }
        return op;
    }
    
    static void priorityOperation(
            @NotNull List<String> numbers,
            List<String> characters,
            int i,
            @NotNull priorityOperations op
    ) {
        BigDecimal first = new BigDecimal(numbers.get(i));
        BigDecimal second = new BigDecimal(numbers.get(i + 1));
        BigDecimal res = new BigDecimal(0);
        switch (op) {
            case DIVIDE -> {
                int scale = 10;
                try {
                    res = first.divide(second, scale, RoundingMode.HALF_UP);
                } catch (ArithmeticException e) {
                    throw new IllegalArgumentException("Division by zero");
                }
            }
            case MULTIPLY -> res = first.multiply(second);
            case POWER -> {
                try {
                    int secondInt = second.intValueExact();
                    res = first.pow(secondInt);
                } catch (ArithmeticException e) {
                    res = BigDecimal.valueOf(Math.exp(second.doubleValue() * Math.log(first.doubleValue())));
                }
            }
        }
        Utility.replaceRange(numbers, i, i + 1, String.valueOf(res));
        characters.remove(i);
    }
}

enum priorityOperations {
    MULTIPLY,
    DIVIDE,
    POWER
}