package WindowsCalcTest;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.ptr.IntByReference;
import io.appium.java_client.windows.WindowsDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import static com.sun.jna.platform.win32.WinUser.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    public static boolean isOpen(String name) {
        return User32.INSTANCE.FindWindow(null, name) != null;
    }

    public static void closeWindow(String name) {
        User32.INSTANCE.PostMessage(User32.INSTANCE.FindWindow(null, name), WM_CLOSE, null, null);
    }

    public static void closeAllWindows(String name) {
        User32.INSTANCE.EnumWindows((hWnd, _) -> {
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
            String title = Native.toString(windowText);
            if (title.contains(name)) {
                User32.INSTANCE.PostMessage(hWnd, WM_CLOSE, null, null);
            }
            return true;
        }, null);
    }

    public static void fullScreen(String name) {
        if (isOpen(name)) {User32.INSTANCE.ShowWindow(User32.INSTANCE.FindWindow(null, name), SW_MAXIMIZE);}
    }

    public static void minimizeScreen(String name) {
        if (isOpen(name)) User32.INSTANCE.ShowWindow(User32.INSTANCE.FindWindow(null, name), SW_RESTORE);
    }

    public static String getClassName(String name) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, name);
        char[] className = new char[256];
        User32.INSTANCE.GetClassName(hwnd, className, className.length);
        return new String(className);
    }

    public static String getOutput(String name, String[] accessibilityIds) throws Exception {

        if(!isProcessRunning("WinAppDriver")) new ProcessBuilder("cmd.exe", "/c", "start /min WinAppDriver").start();

        Level previousLevel = Logger.getLogger("").getLevel();
        Logger.getLogger("").setLevel(Level.WARNING);

        String hwndHex = String.format("0x%08x", User32.INSTANCE.FindWindow(null, name).getPointer().hashCode());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("appTopLevelWindow", hwndHex);

        WindowsDriver<WebElement> appSession = new WindowsDriver<>(new URI("http://127.0.0.1:4723").toURL(), capabilities);
        appSession.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        StringBuilder result = new StringBuilder();
        for (String accessibilityId : accessibilityIds) {
            result.append(appSession.findElementByAccessibilityId(accessibilityId).getAttribute("Name")).append("\n");
        }

        appSession.quit();
        Logger.getLogger("").setLevel(previousLevel);
        return result.toString();

    }

    public static void turnOnCalcMode(String mode) throws Exception {

        if(!isProcessRunning("WinAppDriver")) new ProcessBuilder("cmd.exe", "/c", "start /min WinAppDriver").start();

        Level previousLevel = Logger.getLogger("").getLevel();
        Logger.getLogger("").setLevel(Level.WARNING);

        String hwndHex = String.format("0x%08x", User32.INSTANCE.FindWindow(null, "Калькулятор").getPointer().hashCode());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("appTopLevelWindow", hwndHex);

        WindowsDriver<WebElement> appSession = new WindowsDriver<>(new URI("http://127.0.0.1:4723").toURL(), capabilities);
        appSession.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        appSession.findElementByAccessibilityId("TogglePaneButton").click();
        appSession.findElementByAccessibilityId(mode).click();

        appSession.quit();
        Logger.getLogger("").setLevel(previousLevel);

    }

    public static void pushTheButton(String name, String accessibilityId) throws Exception {

        if(!isProcessRunning("WinAppDriver")) new ProcessBuilder("cmd.exe", "/c", "start /min WinAppDriver").start();

        Level previousLevel = Logger.getLogger("").getLevel();
        Logger.getLogger("").setLevel(Level.WARNING);

        String hwndHex = String.format("0x%08x", User32.INSTANCE.FindWindow(null, name).getPointer().hashCode());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("appTopLevelWindow", hwndHex);

        WindowsDriver<WebElement> appSession = new WindowsDriver<>(new URI("http://127.0.0.1:4723").toURL(), capabilities);
        appSession.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        appSession.findElementByAccessibilityId(accessibilityId).click();

        appSession.quit();
        Logger.getLogger("").setLevel(previousLevel);
    }

    public static String getAccessibilityIdByName(String name, String Name) throws Exception {
        if(!isProcessRunning("WinAppDriver")) new ProcessBuilder("cmd.exe", "/c", "start /min WinAppDriver").start();

        Level previousLevel = Logger.getLogger("").getLevel();
        Logger.getLogger("").setLevel(Level.WARNING);

        String hwndHex = String.format("0x%08x", User32.INSTANCE.FindWindow(null, name).getPointer().hashCode());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("appTopLevelWindow", hwndHex);

        WindowsDriver<WebElement> appSession = new WindowsDriver<>(new URI("http://127.0.0.1:4723").toURL(), capabilities);
        appSession.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        String result = appSession.findElementByName(Name).getAttribute("AutomationId");

        appSession.quit();
        Logger.getLogger("").setLevel(previousLevel);
        return result;
    }


    public static boolean isProcessRunning(String name) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder("tasklist").start().getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private static HWND getHWNDHexByProcess(Process process) {
        HWND[] hwnd = new HWND[1];
        System.out.println("Поиск окна " + process.pid());
        User32.INSTANCE.EnumWindows(((hWnd, _) -> {
            IntByReference currentProcessId = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, currentProcessId);
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
            String title = Native.toString(windowText);
            System.out.println("Проверка окна: " + hWnd + ", PID: " + currentProcessId.getValue() + ", Заголовок: " + title);
            if (currentProcessId.getValue() == process.pid()) {
                hwnd[0] = hWnd;
                return false;
            }
            return true;
        }), null);
        return hwnd[0];
    }

    public static void printAllProcesses() {
        ProcessHandle.allProcesses().forEach(processHandle -> {
            long pid = processHandle.pid();
            String name = processHandle.info().command().orElse("Unknown");
            processHandle.parent().ifPresent(parent -> {
                long parentPid = parent.pid();
                System.out.println("Process ID: " + pid + ", Name: " + name + ", Parent ID: " + parentPid);
            });
        });
    }

}