package WindowsCalcTest;

import java.awt.*;

import static WindowsCalcTest.Graphics.*;

public enum Button {
    Second("src/main/resources/Buttons/2nd.png"),
    π("src/main/resources/Buttons/12.png"),
    e("src/main/resources/Buttons/13.png"),
    C("src/main/resources/Buttons/14.png"),
    CE("src/main/resources/Buttons/CE.png"),
    BS("src/main/resources/Buttons/15.png"),
    sqr("src/main/resources/Buttons/21.png"),
    inv("src/main/resources/Buttons/22.png"),
    abs("src/main/resources/Buttons/23.png"),
    exp("src/main/resources/Buttons/24.png"),
    mod("src/main/resources/Buttons/25.png"),
    sqrt("src/main/resources/Buttons/31.png"),
    open("src/main/resources/Buttons/32.png"),
    close("src/main/resources/Buttons/33.png"),
    fact("src/main/resources/Buttons/34.png"),
    div("src/main/resources/Buttons/35.png"),
    pow("src/main/resources/Buttons/41.png"),
    seven("src/main/resources/Buttons/42.png"),
    eight("src/main/resources/Buttons/43.png"),
    nine("src/main/resources/Buttons/44.png"),
    mul("src/main/resources/Buttons/45.png"),
    tenPow("src/main/resources/Buttons/51.png"),
    four("src/main/resources/Buttons/52.png"),
    five("src/main/resources/Buttons/53.png"),
    six("src/main/resources/Buttons/54.png"),
    sub("src/main/resources/Buttons/55.png"),
    log("src/main/resources/Buttons/61.png"),
    one("src/main/resources/Buttons/62.png"),
    two("src/main/resources/Buttons/63.png"),
    three("src/main/resources/Buttons/64.png"),
    add("src/main/resources/Buttons/65.png"),
    ln("src/main/resources/Buttons/71.png"),
    negate("src/main/resources/Buttons/72.png"),
    zero("src/main/resources/Buttons/73.png"),
    point("src/main/resources/Buttons/74.png"),
    equal("src/main/resources/Buttons/75.png"),
    cube("src/main/resources/Buttons/b2.png"),
    cubeRoot("src/main/resources/Buttons/b3.png"),
    yRoot("src/main/resources/Buttons/b4.png"),
    twoPow("src/main/resources/Buttons/b5.png"),
    logBase("src/main/resources/Buttons/b6.png"),
    ePow("src/main/resources/Buttons/b7.png"),
    fake1("src/main/resources/Buttons/control1.png"),
    fakeE("src/main/resources/Buttons/controlE.png");

    private final String image;

    Button(String image) {this.image = image;}

    public String getImage() {return image;}

    public void click(Rectangle buttonsPlace) throws Exception {Graphics.click(getCenter(find(image, -1, 0.999, buttonsPlace)));}

    public static void input(String string, Rectangle buttonsPlace) throws Exception {
        Button button;
        for (int i = 0; i < string.length(); i++) {
            button = switch (string.charAt(i)) {
                case '0' -> Button.zero;
                case '1' -> Button.one;
                case '2' -> Button.two;
                case '3' -> Button.three;
                case '4' -> Button.four;
                case '5' -> Button.five;
                case '6' -> Button.six;
                case '7' -> Button.seven;
                case '8' -> Button.eight;
                case '9' -> Button.nine;
                case 'π' -> Button.π;
                case 'e' -> Button.e;
                case ',' -> Button.point;
                case '(' -> Button.open;
                case ')' -> Button.close;
                case '+' -> Button.add;
                case '-' -> Button.sub;
                case '*' -> Button.mul;
                case '/' -> Button.div;
                case '%' -> Button.mod;
                case '^' -> Button.pow;
                case '!' -> Button.fact;
                case '\\' -> Button.logBase;
                case '=' -> Button.equal;
                default -> throw new IllegalStateException("Неопознанный символ: " + string.charAt(i));
            };
            if (button == logBase) {
                Second.click(buttonsPlace);
                System.out.println("Нажата кнопка Second");
            }
            button.click(buttonsPlace);
            System.out.println("Нажата кнопка " + button.name());
        }
    }
}