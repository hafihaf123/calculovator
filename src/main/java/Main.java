import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Main {

  public static void main(String[] args)
  {
     Scanner sc = new Scanner(System.in);
     String input;

     while (true) {
       input = sc.nextLine();
       if(Objects.equals(input, "exit")) break;
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
    
    for(i = 0 ; i < arr.length ; i++) {
      char c = arr[i];
	  if (c == ',') c = '.';
	  if (Utility.isDigit(c)) {
        if (i == 0) {
          isFirstNumber = true;
          numbers.add(String.valueOf(c));
        }
        else {
          if (Utility.isDigit(arr[i-1])){
            String numberInList = numbers.get(numi);
            numberInList += c;
            numbers.set(numi, numberInList);
          }
          else {
            numbers.add(String.valueOf(c));
            numi++;
          } 
        }
        
      }
      else {
        if (i == 0) {
          isFirstNumber = false;
          characters.add(String.valueOf(c));
        }
        else {
          if (Utility.isDigit(arr[i-1])){
            if (!characters.isEmpty()) 
              chari++;
            characters.add(String.valueOf(c));
          }
          else {
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
      }
      else if (firstCh.equals("(")) {
        numbers.add(0,"0");
        characters.set(0, "+(");
      }
      else if (firstCh.contains("(")){
        numbers.add(0, "0");
        characters.set(0, "+" + firstCh);
      }
    }
    
    List<Integer> braceStartI = new ArrayList<>();
    List<Integer> braceEndI = new ArrayList<>();
      
    for (i=0 ; i < characters.size() ; i++) {
      String char_i = characters.get(i);
      if (char_i.contains("(") ) {
        braceStartI.add(i);
      }
      if (char_i.contains(")") ) {
        braceEndI.add(i);
      }
    }
      
    if (!braceStartI.isEmpty()) {
      int braceIndex = 0;
      for (int item: braceStartI) {
        int start = item + 1 - braceIndex;
        int end = braceEndI.get(braceIndex) - braceIndex;
          
        List<String> braceNumList = new ArrayList<>(numbers.subList( start, end));
        braceNumList.add(numbers.get(end));
        List<String> braceCharList = new ArrayList<>(characters.subList( start, end));
        System.out.println(braceNumList);
        System.out.println(braceCharList);
        result = eval(braceNumList, braceCharList);
        Utility.replaceRange(numbers, start, end, result);
        
        String braceStartCh = characters.get(start-1);
        braceStartCh = braceStartCh.replaceFirst("\\(", "");
        String braceEndCh = characters.get(end);
        braceEndCh = braceEndCh.replaceFirst("\\)", "");
        
        if (braceStartCh.isEmpty()) {
          characters.remove(start-1);
        }
        else {
          characters.set(start-1, braceStartCh);
        }
        if (braceEndCh.isEmpty()) {
          characters.remove(end);
        }
        else {
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
    
    while (Utility.listContains(characters, "*/÷^abcdefghijklmnopqrstuvwxyz")) {
      for (int i = 0 ; i < lenC ; i++) {
        switch (characters.get(i)) {
          case "*", "x" -> Utility.priorityOperation(numbers, characters, i, priorityOperations.MULTIPLY);
          case "/", "÷" -> Utility.priorityOperation(numbers, characters, i, priorityOperations.DIVIDE);
          case "^", "**", "pow" -> Utility.priorityOperation(numbers, characters, i, priorityOperations.POWER);
        }
        lenC = characters.size();
      }
    }
    
    lenC = characters.size();
    
    BigDecimal result = new BigDecimal(numbers.get(0));
    
    for (int i = 0 ; i < lenC ; i++) {
      switch (characters.get(i)) {
        case "+" -> result = result.add(new BigDecimal(numbers.get(i + 1)));
        case "-" -> result = result.subtract(new BigDecimal(numbers.get(i + 1)));
      }
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
  
  public static boolean listContains(List<String> list, String x)
  {
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
  
  public static boolean isDigit(char c) {
    if (Character.isDigit(c)) return true;
    return c == '.' || c == ',';
  }

  public static void priorityOperation(List<String> numbers, List<String> characters, int i, priorityOperations op) {
    BigDecimal first = new BigDecimal(numbers.get(i));
    BigDecimal second = new BigDecimal(numbers.get(i+1));
    BigDecimal res = new BigDecimal(0);
    switch (op) {
      case DIVIDE -> {
        int scale = 10;
        res = first.divide(second, scale, RoundingMode.HALF_UP);
      }
      case MULTIPLY -> res = first.multiply(second);
      case POWER -> res = first.pow(second.intValue());
      default -> System.out.println("Error, priority operation not expected");
    }
    Utility.replaceRange(numbers, i, i+1, String.valueOf(res));
    characters.remove(i);
  }
  
}

enum priorityOperations {
  MULTIPLY,
  DIVIDE,
  POWER
}