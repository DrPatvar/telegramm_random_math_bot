package io.proj3ct.telegrammultbot.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class RandomNumber {
    public static int firstNumber;
    public static int secondNumber;
    public  static int answer;
    public static String stringExample;



    public static void randomNumber(String levelSelection){
        int numberLevel = 0;
        switch (levelSelection){
            case "EASY"->{numberLevel = 4;}
            case "MEDIUM"->{numberLevel =7;}
            case "HARD" ->{numberLevel =9;}

        }
        firstNumber = (int) (Math.random() * numberLevel) + 1;
        secondNumber = (int) (Math.random() * 9) + 1;
        answer = firstNumber * secondNumber;
        stringExample = firstNumber + " x " + secondNumber;
    }
}
