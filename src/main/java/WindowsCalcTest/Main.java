package WindowsCalcTest;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;

import static WindowsCalcTest.Graphics.*;
import static WindowsCalcTest.Utils.*;

public class Main {
    public static void main() throws Exception {
        closeAllWindows("Калькулятор");
        new ProcessBuilder("calc").start();
        double waitTime = 0;
        while (!isOpen("Калькулятор")) {
            waitTime += 0.01;
            Thread.sleep(10);
        }
        System.out.println("Калькулятор открыт!");
        System.out.println("Время загрузки: " + String.format("%.2f", waitTime) + " секунд");
        //minimizeScreen("Калькулятор");
        if (!getOutput("Калькулятор", new String[]{"Header"}).equals("Режим калькулятора Инженерный\n")) turnOnCalcMode("Scientific");
        Rectangle windowRect = getRectangle("Калькулятор");
        System.out.println("Окно калькулятора:\n" + windowRect);
        Rectangle searchButtonsRect = new Rectangle(windowRect.x + 11, windowRect.y + 387, 480, 364);

        System.out.println("Область расположения кнопок:\n" + searchButtonsRect);
        System.out.println("Поиск кнопок...");
        String searchButton = "2nd";
        Rectangle buttonRect = find("src/main/resources/Buttons/" + searchButton + ".png", -1, 0.9991, searchButtonsRect);
        System.out.println("Кнопка " + searchButton + " найдена! Область расположения:\n" + buttonRect);

        System.out.println("Используем найденную область расположения для создания скриншотов всех остальных кнопок...");
        Rectangle newButtonRect = buttonRect;
        Mat img;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 0 && j == 0) continue;
                else if (i == 0 && j == 3) {
                    click(getCenter(newButtonRect));
                    Rectangle CEButtonRect = new Rectangle(newButtonRect.x + buttonRect.width + 3, buttonRect.y, buttonRect.width, buttonRect.height);
                    img = saveScreen(CEButtonRect);
                    Imgcodecs.imwrite("src/main/resources/Buttons/CE.png", img);
                    System.out.println("Скриншот CE создан!");
                    click(getCenter(CEButtonRect));
                    move(getCenter(newButtonRect));
                }
                newButtonRect = new Rectangle(buttonRect.x + (buttonRect.width + 3) * j, buttonRect.y + (buttonRect.height + 3 ) * i, buttonRect.width, buttonRect.height);
                if (i == 2 && j == 1) newButtonRect = new Rectangle(newButtonRect.x + 38, newButtonRect.y + 10, newButtonRect.width - 76, newButtonRect.height - 20);
                img = saveScreen(newButtonRect);
                Imgcodecs.imwrite("src/main/resources/Buttons/" + (i + 1) + (j + 1) + ".png", img);
                System.out.println("Скриншот " + (i + 1) + (j + 1) + " создан!");
            }
        }
        click(getCenter(buttonRect));
        for (int i = 1; i < 7; i++) {
            newButtonRect = new Rectangle(buttonRect.x, buttonRect.y + (buttonRect.height + 3 ) * i, buttonRect.width, buttonRect.height);
            img = saveScreen(newButtonRect);
            Imgcodecs.imwrite("src/main/resources/Buttons/b" + (i + 1) + ".png", img);
            System.out.println("Скриншот b" + (i + 1) + " создан!");
        }

        //Thread.sleep(10000);
        //System.out.println("Результат:\n" + getOutput("Калькулятор", new String[]{"CalculatorResults"}).replace("Отображать как ", ""));
        closeWindow("Калькулятор");
        new ProcessBuilder("cmd.exe", "/c", "taskkill /F /IM WinAppDriver.exe").start();
    }
}