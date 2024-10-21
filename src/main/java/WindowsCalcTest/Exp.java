package WindowsCalcTest;

import static WindowsCalcTest.Utils.*;

public class Exp {

    public static void main() throws Exception {
        closeAllWindows("Калькулятор");
        new ProcessBuilder("calc").start();
        double waitTime = 0;
        while (!isOpen("Калькулятор")) {
            waitTime += 0.01;
            Thread.sleep(10);
        }
        minimizeScreen("Калькулятор");
        System.out.println("Калькулятор открыт!");
        System.out.println("Время загрузки: " + String.format("%.2f", waitTime) + " секунд");
        if (!getOutput("Калькулятор", new String[]{"Header"}).equals("Режим калькулятора Инженерный\n")) turnOnCalcMode("Scientific");

        pushTheButton("Калькулятор", "num1Button");
        pushTheButton("Калькулятор", "num0Button");
        pushTheButton("Калькулятор", "num0Button");
        pushTheButton("Калькулятор", "num0Button");
        pushTheButton("Калькулятор", "factorialButton");
        pushTheButton("Калькулятор", "equalButton");
        System.out.println(getOutput("Калькулятор", new String[]{"CalculatorResults"}).replace("\n", "").replace("\u00A0", " "));

        closeWindow("Калькулятор");
        new ProcessBuilder("cmd.exe", "/c", "taskkill /F /IM WinAppDriver.exe").start();
    }

}