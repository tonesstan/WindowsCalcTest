package WindowsCalcTest;

import com.sun.jna.platform.win32.User32;
import io.appium.java_client.windows.WindowsDriver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static WindowsCalcTest.Button.*;
import static WindowsCalcTest.Graphics.*;
import static WindowsCalcTest.Utils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

public class WindowsCalcTest {
    private static WindowsDriver<WebElement> driver;
    private static Level previousLevel;
    private static Rectangle buttonsPlace;


    @BeforeAll
    public static void setUp() throws Exception {
        closeAllWindows("Калькулятор");
        new ProcessBuilder("calc").start();

        previousLevel = Logger.getLogger("").getLevel();
        Logger.getLogger("").setLevel(Level.WARNING);

        if(!isProcessRunning("WinAppDriver")) new ProcessBuilder("cmd.exe", "/c", "start /min WinAppDriver").start();
        DesiredCapabilities capabilities = new DesiredCapabilities();

        while (!isOpen("Калькулятор")) Thread.sleep(10);
        minimizeScreen("Калькулятор");
        Rectangle windowRect = getRectangle("Калькулятор");
        buttonsPlace = new Rectangle(windowRect.x + 11, windowRect.y + 387, 480, 364);

        String hwndHex = String.format("0x%08x", User32.INSTANCE.FindWindow(null, "Калькулятор").getPointer().hashCode());
        capabilities.setCapability("appTopLevelWindow", hwndHex);
        driver = new WindowsDriver<>(new URI("http://127.0.0.1:4723").toURL(), capabilities);
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        if(!driver.findElementByAccessibilityId("Header").getAttribute("Name").equals("Режим калькулятора Инженерный")) {
            driver.findElementByAccessibilityId("TogglePaneButton").click();
            driver.findElementByAccessibilityId("Scientific").click();
        }
    }


    @ParameterizedTest(name = "Тест кнопки {0}")
    @MethodSource("digitsTestData")
    @Tag("buttons")
    public void digitTest(Button button, String expectedResult) {
        if (button == zero) {
            driver.findElementByAccessibilityId("num1Button").click();
            System.out.println("Нажата кнопка 1");
        }
        try {
            button.click(buttonsPlace);
            System.out.println("Нажата кнопка " + button.name());
        } catch (Exception e) {
            fail("\nОшибка: кнопка " + button.name() + " не была нажата.\nТест провален! Подробности: \n" + e.getMessage());
        }
        if (button == point) {
            driver.findElementByAccessibilityId("num1Button").click();
            System.out.println("Нажата кнопка 1");
        }
        String result = driver.findElementByAccessibilityId("CalculatorResults").getAttribute("Name").replace("Отображать как ", "");
        System.out.println("Результат: " + result);
        assertEquals(expectedResult, result, "\nОшибка: кнопка " + button.name() + " сработала некорректно.\nТест провален!");
        System.out.println("Тест кнопки " + button.name() + " пройден!");
    }

    public static Stream<Arguments> digitsTestData() {
        return Stream.of(
                Arguments.of(zero, "10"), //0 отображается изначально, поэтому нужно нажать на 1 перед нажатием на 0
                Arguments.of(Button.one, "1"),
                Arguments.of(Button.two, "2"),
                Arguments.of(Button.three, "3"),
                Arguments.of(Button.four, "4"),
                Arguments.of(Button.five, "5"),
                Arguments.of(Button.six, "6"),
                Arguments.of(Button.seven, "7"),
                Arguments.of(Button.eight, "8"),
                Arguments.of(Button.nine, "9"),
                Arguments.of(Button.point, "0,1"),
                Arguments.of(Button.π, "3,1415926535897932384626433832795"),
                Arguments.of(Button.e, "2,7182818284590452353602874713527")
        );
    }


    @ParameterizedTest(name = "Тест кнопки {0}")
    @MethodSource("operationsTestData")
    @Tag("buttons")
    public void operationTest(Button button, String expectedResult) {
        if (button.getImage().charAt(27) == 'b') {
            driver.findElementByAccessibilityId("shiftButton").click();
            System.out.println("Нажата кнопка 2nd");
        }
        driver.findElementByAccessibilityId("num7Button").click();
        System.out.println("Нажата кнопка 7");
        try {
            button.click(buttonsPlace);
            System.out.println("Нажата кнопка " + button.name());
        } catch (Exception e) {
            fail("\nОшибка: кнопка " + button.name() + " не была нажата.\nТест провален! Подробности: \n" + e.getMessage());
        }
        EnumSet<Button> excludedButtons = EnumSet.of(tenPow, abs, sqr, sqrt, log, ln, inv, fact, ePow, cube, cubeRoot, twoPow);
        if (!excludedButtons.contains(button)) {
            driver.findElementByAccessibilityId("num5Button").click();
            System.out.println("Нажата кнопка 5");
        }
        driver.findElementByAccessibilityId("equalButton").click();
        System.out.println("Нажата кнопка =");
        String result = driver.findElementByAccessibilityId("CalculatorResults").getAttribute("Name").replace("Отображать как ", "").replace("\u00A0", " ");
        System.out.println("Результат: " + result);
        assertEquals(expectedResult, result, "\nОшибка: кнопка " + button.name() + " сработала некорректно.\nТест провален!");
        System.out.println("Тест кнопки " + button.name() + " пройден!");
    }

    public static Stream<Arguments> operationsTestData() {
        return Stream.of(
                Arguments.of(Button.negate, "-75"),
                Arguments.of(Button.add, "12"),
                Arguments.of(Button.sub, "2"),
                Arguments.of(Button.mul, "35"),
                Arguments.of(Button.div, "1,4"),
                Arguments.of(Button.pow, "16 807"),
                Arguments.of(Button.tenPow, "10 000 000"),
                Arguments.of(Button.abs, "7"),
                Arguments.of(Button.exp, "700 000"),
                Arguments.of(Button.mod, "2"),
                Arguments.of(Button.sqr, "49"),
                Arguments.of(Button.sqrt, "2,6457513110645905905016157536393"),
                Arguments.of(Button.log, "0,84509804001425683071221625859264"),
                Arguments.of(Button.ln, "1,9459101490553133051053527434432"),
                Arguments.of(Button.inv, "0,14285714285714285714285714285714"),
                Arguments.of(Button.fact, "5 040"),
                Arguments.of(Button.ePow, "1 096,6331584284585992637202382881"),
                Arguments.of(Button.equal, "5"),
                Arguments.of(Button.CE, "5"),
                Arguments.of(Button.BS, "5"),
                Arguments.of(Button.cube, "343"),
                Arguments.of(Button.cubeRoot, "1,9129311827723891011991168395488"),
                Arguments.of(Button.yRoot, "1,4757731615945520692769166956322"),
                Arguments.of(Button.twoPow, "128"),
                Arguments.of(Button.logBase, "1,2090619551221675567633163455474")
        );
    }


    @ParameterizedTest(name = "Контрольный тест: неправильная кнопка {0}")
    @MethodSource("controlButtonsTestData")
    @Tag("buttons")
    public void controlButtonTest(Button button, String expectedResult) {
        try {
            button.click(buttonsPlace);
            System.out.println("Нажата кнопка " + button.name().replace("fake", ""));
        } catch (Exception e) {
            System.out.println("Фальшивая кнопка " + button.name().replace("fake", "") + " не была распознана и нажата.\nКонтрольный тест пройден! Подробности: \n" + e.getMessage());
            driver.findElementByAccessibilityId("num1Button").click();
            assumeFalse(false);
            return;
        }
        String result = driver.findElementByAccessibilityId("CalculatorResults").getAttribute("Name").replace("Отображать как ", "");
        System.out.println("Результат: " + result);
        driver.findElementByAccessibilityId("num1Button").click();
        assertNotEquals(expectedResult, result, "Ошибка: фальшивая кнопка " + button.name().replace("fake", "") + " была корректно распознана и нажата.\nКонтрольный тест провален!");
        assertEquals("0", result, "Ошибка: фальшивая кнопка " + button.name().replace("fake", "") + " была некорректно распознана и нажата.\nКонтрольный тест провален!");
        System.out.println("Контрольный тест кнопки " + button.name().replace("fake", "") + " пройден!");
    }

    public static Stream<Arguments> controlButtonsTestData() {
        return Stream.of(
                Arguments.of(Button.fake1, "1"),
                Arguments.of(Button.fakeE, "2,7182818284590452353602874713527")
        );
    }


    @ParameterizedTest(name = "Тест математического выражения {0}")
    @MethodSource("expressionTestData")
    @Tag("expression")
    public void expressionTest(String expression, String expectedResult) {
        try {
            input(expression, buttonsPlace);
            System.out.println("Введено выражение: " + expression);
        } catch (Exception e) {System.out.println("\nОшибка: выражение не было введено.\nТест провален! Подробности: " + e.getMessage());}
        String result = driver.findElementByAccessibilityId("CalculatorResults").getAttribute("Name").replace("Отображать как ", "").replace("\u00A0", " ");
        System.out.println("Результат: " + result);
        assertEquals(expectedResult, result, "\nОшибка: выражение " + expression + " сработало некорректно.\nТест провален!");
        System.out.println("Математическое выражение " + expression + expectedResult + " успешно вычислено! Тест пройден!");
    }

    public static Stream<Arguments> expressionTestData() {
        return Stream.of(
                Arguments.of("(43+8,69^2,28)/(-13)=", "-13,949655145004920764905957378509"),
                Arguments.of("37,7*574,63%919,12=", "523,791"),
                Arguments.of("((709^0,5-30)*(3/7-10^0,5=", "9,2206437412772854183664146368828"),
                Arguments.of("29!=", "8 841 761 993 739 701 954 543 616 000 000"),
                Arguments.of("1000!=", "4,02387260077093773543702433923e+2567"),
                Arguments.of("e^π-π=", "19,999099979189475767266442984669"),
                Arguments.of("(e*π)!\\π=", "10,287794933863227806645615951789")
        );
    }

    @AfterEach
    public void clearResultField() {
        driver.findElementByAccessibilityId("clearEntryButton").click();
        driver.findElementByAccessibilityId("clearButton").click();
    }


    @AfterAll
    public static void tearDown() throws IOException {
        closeWindow("Калькулятор");
        new ProcessBuilder("cmd.exe", "/c", "taskkill /F /IM WinAppDriver.exe").start();
        driver.quit();
        Logger.getLogger("").setLevel(previousLevel);
    }
}