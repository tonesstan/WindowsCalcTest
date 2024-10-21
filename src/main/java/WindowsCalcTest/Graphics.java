package WindowsCalcTest;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import static org.opencv.imgproc.Imgproc.rectangle;

public class Graphics {

    public static void move(Point p) {User32.INSTANCE.SetCursorPos(p.x, p.y);}

    public static void click(Point p) throws Exception {
        Robot robot = new Robot();
        move(p);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(200);
    }

    public static void enter_text(String keys) throws AWTException {
        Robot robot = new Robot();
        for (char c : keys.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                throw new RuntimeException(
                        "Key code not found for character '" + c + "'");
            }
            robot.keyPress(keyCode);
            robot.delay(100);
            robot.keyRelease(keyCode);
            robot.delay(100);
        }
    }

    public static Rectangle find(String templateFile, int match_method, double accuracy, Rectangle searchRect) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (match_method == -1 || match_method == Imgproc.TM_CCORR) match_method = Imgproc.TM_CCORR_NORMED;
        else if (match_method == Imgproc.TM_SQDIFF) match_method = Imgproc.TM_SQDIFF_NORMED;
        else if (match_method == Imgproc.TM_CCOEFF) match_method = Imgproc.TM_CCOEFF_NORMED;

        Mat img = saveScreen(searchRect);
        Mat templ = Imgcodecs.imread(templateFile);

        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        Imgproc.matchTemplate(img, templ, result, match_method);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        org.opencv.core.Point matchLoc;
        double best_result;
        if (match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
            best_result = 1 - mmr.minVal;
        } else {
            matchLoc = mmr.maxLoc;
            best_result = mmr.maxVal;
        }
        if (best_result < accuracy){
            throw new Exception(templateFile + " не найдено! Лучшее совпадение: " + (best_result * 100));
        }
        return new Rectangle((int) matchLoc.x + searchRect.x, (int) matchLoc.y + searchRect.y, templ.cols(),  templ.rows());

    }

    public static Rectangle find_with_mask(String templateFile, double accuracy, Rectangle searchRect) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = saveScreen(searchRect);
        Mat templ = Imgcodecs.imread(templateFile);
        Mat mask = new Mat(templ.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        rectangle(mask, new org.opencv.core.Point(8, 9), new org.opencv.core.Point(31, 31), new Scalar(0), -1);

        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        Imgproc.matchTemplate(img, templ, result, -1, mask);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        org.opencv.core.Point matchLoc = mmr.maxLoc;
        if (mmr.maxVal < accuracy){
            throw new Exception(templateFile + " не найдено! Лучшее совпадение: " + (mmr.maxVal * 100));
        }
        return new Rectangle((int) matchLoc.x + searchRect.x, (int) matchLoc.y + searchRect.y, templ.cols(),  templ.rows());

    }

    public static Rectangle getRectangle(String name) {
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(User32.INSTANCE.FindWindow(null, name), rect);
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    public static Point getCenter(Rectangle rectangle) {
        return new Point(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
    }

    public static Mat saveScreen(Rectangle screenRect) {return bufferedImageToMat(captureScreen(screenRect));}

    private static BufferedImage captureScreen (Rectangle screenRect) {

        WinDef.HDC hdcWindow = User32.INSTANCE.GetDC(null);
        WinDef.HDC hdcMemory = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);
        WinDef.HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, screenRect.width, screenRect.height);
        WinNT.HANDLE oldBitmap = GDI32.INSTANCE.SelectObject(hdcMemory, hBitmap);
        GDI32.INSTANCE.BitBlt(hdcMemory, 0, 0, screenRect.width, screenRect.height, hdcWindow, screenRect.x, screenRect.y, GDI32.SRCCOPY);
        GDI32.INSTANCE.SelectObject(hdcMemory, oldBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemory);

        BufferedImage image = new BufferedImage(screenRect.width, screenRect.height, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[screenRect.width * screenRect.height];

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = 40; // Установка размера заголовка
        bmi.bmiHeader.biWidth = screenRect.width;
        bmi.bmiHeader.biHeight = -screenRect.height; // Высота отрицательная для верхнего направления
        bmi.bmiHeader.biPlanes = (short) 1;
        bmi.bmiHeader.biBitCount = (short) 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
        bmi.bmiHeader.biSizeImage = 0;
        bmi.bmiHeader.biXPelsPerMeter = 0;
        bmi.bmiHeader.biYPelsPerMeter = 0;
        bmi.bmiHeader.biClrUsed = 0;
        bmi.bmiHeader.biClrImportant = 0;

        Pointer pointer = new Pointer(Native.malloc((long) screenRect.width * screenRect.height * Native.getNativeSize(int.class)));
        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, screenRect.height, pointer, bmi, WinGDI.DIB_RGB_COLORS);
        pointer.read(0, pixels, 0, pixels.length);
        image.setRGB(0, 0, screenRect.width, screenRect.height, pixels, 0, screenRect.width);

        GDI32.INSTANCE.DeleteObject(hBitmap);
        User32.INSTANCE.ReleaseDC(null, hdcWindow);
        Native.free(Pointer.nativeValue(pointer));
        return image;
    }

    private static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        int[] pixels = new int[bi.getWidth() * bi.getHeight()];
        bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), pixels, 0, bi.getWidth());
        byte[] byteData = new byte[bi.getWidth() * bi.getHeight() * 3];
        for (int i = 0; i < pixels.length; i++) {
            byteData[i * 3] = (byte) ((pixels[i] >> 16) & 0xFF);
            byteData[i * 3 + 1] = (byte) ((pixels[i] >> 8) & 0xFF);
            byteData[i * 3 + 2] = (byte) (pixels[i] & 0xFF);
        }
        mat.put(0, 0, byteData);
        return mat;
    }

    public static Mat matBGRtoMatRGB(Mat img) {
        Mat imgRgb = new Mat();
        Imgproc.cvtColor(img, imgRgb, Imgproc.COLOR_BGR2RGB);
        return imgRgb;
    }

}
