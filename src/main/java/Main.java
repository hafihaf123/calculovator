import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input;

        System.out.println("Welcome to calculovator!\nto exit, type 'exit' or 'ex'");

        while (true) {
            input = sc.nextLine();
            if ("exit".equals(input)) break;
            if ("ex".equals(input)) break;
            try {
                String result = Expression1.parse(input);
                System.out.println(result);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("bye");
        sc.close();
    }
}

class Expression1 {

    public static String parse(String expression) {

        String result;

        List<String> numbers = new ArrayList<>();
        List<String> characters = new ArrayList<>();

        char[] arr = expression.toCharArray();
        boolean isFirstNumber = true;

        int i, numi = 0, chari = 0;

        for (i = 0; i < arr.length; i++) {
            char c = arr[i];
            if (c == ',') c = '.';
            if (Utility.isDigit(c)) {
                if (i == 0) {
                    isFirstNumber = true;
                    numbers.add(String.valueOf(c));
                } else {
                    if (Utility.isDigit(arr[i - 1])) {
                        String numberInList = numbers.get(numi);
                        numberInList += c;
                        numbers.set(numi, numberInList);
                    } else {
                        numbers.add(String.valueOf(c));
                        numi++;
                    }
                }

            } else {
                if (i == 0) {
                    isFirstNumber = false;
                    characters.add(String.valueOf(c));
                } else {
                    if (Utility.isDigit(arr[i - 1])) {
                        if (!characters.isEmpty()) chari++;
                        characters.add(String.valueOf(c));
                    } else {
                        String characterInList = characters.get(chari);
                        characterInList += c;
                        characters.set(chari, characterInList);
                    }
                }

            }
        }

        if (!isFirstNumber) {
            String firstCh = characters.get(0);

            if (firstCh.contains("+")) {
                firstCh = firstCh.replaceAll("\\+", "");
                characters.set(0, firstCh);
                System.out.println(characters.get(0));
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

    public static String eval(List<String> numbers, List<String> characters) {

        int lenC = characters.size();
	    
	    while (Utility.contains(characters, "^")) {
		    for (int i = 0; i < lenC; i++) {
			    String c = characters.get(i);
			    if (Utility.doesNotContain(c, "^")) {
				    continue;
			    }
			    
			    if (c.equals("^")) {
				    Utility.priorityOperation(numbers, characters, i, priorityOperations.POWER);
				    lenC = characters.size();
			    }
		    }
	    }

        while (Utility.contains(characters, "*/÷")) {
            for (int i = 0; i < lenC; i++) {
                String c = characters.get(i);
                if (Utility.doesNotContain(c, "*x/÷")) {
                    continue;
                }
                priorityOperations op = switch (c) {
                    case "*", "x" -> priorityOperations.MULTIPLY;
                    case "/", "÷" -> priorityOperations.DIVIDE;
                    default -> throw new IllegalArgumentException("operation not expected: '" + c + "'");
                };
                Utility.priorityOperation(numbers, characters, i, op);
                lenC = characters.size();
            }
        }

        BigDecimal result = new BigDecimal(numbers.get(0));

        for (int i = 0; i < lenC; i++) {
            result = switch (characters.get(i)) {
                case "+" -> result.add(new BigDecimal(numbers.get(i + 1)));
                case "-" -> result.subtract(new BigDecimal(numbers.get(i + 1)));
                default -> throw new IllegalArgumentException("operation not expected: '" + characters.get(i) + "'");
            };
        }

        return String.valueOf(result);
    }

}

class Utility {

    public static void replaceRange(List<String> list, int from, int to, final String toReplace) {
        Collections.fill(list.subList(from, to + 1), toReplace);
        if (to >= from + 1) {
            list.subList(from + 1, to + 1).clear();
        }
    }

    public static boolean contains(List<String> list, String x) {
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

    public static boolean doesNotContain(String checking, String x) {
        char[] arr = x.toCharArray();

        for (char s : arr) {
            if (checking.contains(String.valueOf(s))) return false;
        }
        return true;
    }

    public static boolean isDigit(char c) {
        if (Character.isDigit(c)) return true;
        return c == '.' || c == ',';
    }

    public static void priorityOperation(
            List<String> numbers,
            List<String> characters,
            int i,
            priorityOperations op
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
		            throw new ArithmeticException("Division by zero");
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