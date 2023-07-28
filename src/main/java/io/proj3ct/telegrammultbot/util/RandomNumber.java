package io.proj3ct.telegrammultbot.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class RandomNumber {
    public int firstNumber;
    public int secondNumber;
    public  int answer;
    public  String stringExample;


   {
        firstNumber = (int) (Math.random() * 4) + 1;
        secondNumber = (int) (Math.random() * 4) + 1;
        answer = firstNumber * secondNumber;
        stringExample = firstNumber + " x " + secondNumber;
    }
}
