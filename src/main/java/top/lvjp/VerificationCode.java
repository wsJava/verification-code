package top.lvjp;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lvjp
 * @date 2019/4/19
 */
public class VerificationCode {

    /**
     * 验证码图片对象
     * 封装了验证码图片和验证码
     * 并对外提供访问属性的方法
     */
    public static class Verification {

        private BufferedImage bufferedImage;

        private String rightCode;

        private Verification(BufferedImage bufferedImage, String rightCode) {
            this.bufferedImage = bufferedImage;
            this.rightCode = rightCode;
        }

        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }

        public String getRightCode() {
            return rightCode;
        }
    }

    /**
     * 算式对象
     * 封装算术表达式和其结果
     */
    private static class Equation{
        private char[] expression;

        private int result;

        Equation(char[] expression, int result) {
            this.expression = expression;
            this.result = result;
        }

        char[] getExpression() {
            return expression;
        }

        int getResult() {
            return result;
        }
    }

    /**
     * 验证码类型枚举类
     */
    public enum CodeTypeEnum {

        /**
         * 字符串类型验证码
         */
        CHAR,

        /**
         * 算式验证码
         */
        EQUATION;
    }

    /**
     * 存放验证码的Map中的key
     */
    public static final String CODE_KEY = "CODE-KEY";

    /**
     * 默认验证码类型
     */
    private static final CodeTypeEnum DEFAULT_CODE_TYPE = CodeTypeEnum.CHAR;

    /**
     * 默认字符验证码用到的字符
     */
    private static final String DEFAULT_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 算数验证码需要的运算符
     */
    private static final String OPERATE_CHARS = "+-x";

    /**
     * 默认干扰线的数量
     */
    private static final int DEFAULT_LINE_COUNT = 15;

    /**
     * 默认验证码的长度
     */
    private static final int DEFAULT_CODE_SIZE = 4;

    /**
     * 默认算式运算符个数
     */
    private static final int DEFAULT_OPERATE_COUNT = 2;

    /**
     * 默认验证码图片的宽
     */
    private static final int DEFAULT_WIDTH = 80;

    /**
     * 默认验证码图片的高
     */
    private static final int DEFAULT_HEIGHT = 30;

    /**
     * 默认基础字体大小
     */
    private static final int DEFAULT_FONT_BASIS_SIZE = 16;

    /**
     * 颜色tgb值的边界范围, 防止出现浅色
     */
    private static final int COLOR_BOUND = 210;

    /**
     * 验证码类型
     */
    private CodeTypeEnum codeType;

    /**
     * 字符验证码用到的字符
     */
    private String codeChars;

    /**
     * 干扰线的数量
     */
    private int lineCount;

    /**
     * 验证码的长度
     */
    private int codeSize;

    /**
     * 算式运算符个数
     */
    private int operatorCount;

    /**
     * 验证码图片的宽
     */
    private int width;

    /**
     * 验证码图片的高
     */
    private int height;

    /**
     * 基础字体大小
     */
    private int fontBasisSize;

    /**
     * 算术运算符优先级
     */
    private static final Map<Character, Integer> operatorMap = new HashMap<>(5);

    static {
        operatorMap.put('+', 1);
        operatorMap.put('-', 1);
        operatorMap.put('x', 2);
    }

    private ThreadLocalRandom random;

    public VerificationCode() {
        random = ThreadLocalRandom.current();

        codeType = DEFAULT_CODE_TYPE;
        codeChars = DEFAULT_CODE_CHARS;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        lineCount = DEFAULT_LINE_COUNT;
        codeSize = DEFAULT_CODE_SIZE;
        operatorCount = DEFAULT_OPERATE_COUNT;
        fontBasisSize = DEFAULT_FONT_BASIS_SIZE;
    }

    public VerificationCode(CodeTypeEnum type) {
        random = ThreadLocalRandom.current();

        codeType = type;
        codeChars = DEFAULT_CODE_CHARS;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        lineCount = DEFAULT_LINE_COUNT;
        codeSize = DEFAULT_CODE_SIZE;
        operatorCount = DEFAULT_OPERATE_COUNT;
        fontBasisSize = DEFAULT_FONT_BASIS_SIZE;
    }

    /**
     * 获取验证码对象
     * @return Verification 验证码对象
     */
    public Verification getVerificationImage(){
        BufferedImage bufferedImage = new BufferedImage(width, height,BufferedImage.TYPE_INT_BGR);
        char[] codes = null;
        String rightCode = null;
        Graphics graphics = bufferedImage.getGraphics();
        graphics.fillRect(0,0, width, height);
        graphics.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, fontBasisSize));
        if (codeType == CodeTypeEnum.CHAR){      //如果是字符类型验证码,则画干扰线
            graphics.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, fontBasisSize));
            for (int i = 0; i < lineCount; i++) {
                graphics.setColor(getRandColor());
                drawLine(graphics);
            }
            codes = getCharCode(codeSize);
            rightCode = String.valueOf(codes);
        } else if (codeType == CodeTypeEnum.EQUATION){  //如果是算式类型验证码
            Equation equation = getEquation(operatorCount);
            codes = equation.getExpression();
            rightCode = String.valueOf(equation.getResult());
        }
        drawString(graphics,codes);
        graphics.dispose();
        return new Verification(bufferedImage, rightCode);
    }

    /**
     * 在 http 中设置验证图片和验证码
     * @param request http 请求
     * @param response http 响应
     */
    public void setHttpVerificationCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");//设置相应类型,输出的内容为图片
        response.setHeader("Pragma", "no-cache");//设置响应头信息，不要缓存此内容
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/jpeg");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession();
        session.removeAttribute(CODE_KEY);
        Verification verification = getVerificationImage();
        session.setAttribute(CODE_KEY, verification.getRightCode());
        ImageIO.write(verification.getBufferedImage(),"JPEG",response.getOutputStream());
    }

    private Font getFont(){
        return new Font("Ffixedsys",Font.CENTER_BASELINE, fontBasisSize + random.nextInt(6));
    }

    /**
     * 获取干扰线随机颜色
     */
    private Color getRandColor(){
        int r = random.nextInt(COLOR_BOUND);
        int g = random.nextInt(COLOR_BOUND);
        int b = random.nextInt(COLOR_BOUND);
        return new Color(r, g, b);
    }

    /**
     * 绘制干扰线
     */
    private void drawLine(Graphics graphics){
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int x1 = random.nextInt(x + random.nextInt(width));
        int y1 = random.nextInt(y + random.nextInt(height));
        graphics.drawLine(x, y, x1, y1);
    }

    /**
     * 把验证码或算式画入验证图片中
     * @param graphics 图像
     * @param codes 验证码或算式
     */
    private void drawString(Graphics graphics, char[] codes){
        for (int i = 0; i < codes.length; i++) {
            graphics.setFont(getFont());
            graphics.setColor(getRandColor());
            graphics.translate(random.nextInt(2), 0);
            graphics.drawString(String.valueOf(codes[i]),
                    fontBasisSize * i + random.nextInt(2), 20 + random.nextInt(8));
        }
    }

    /**
     * 获取随机字符验证码
     * @param codeLength 验证码长度
     * @return codes 随机验证码
     */
    private char[] getCharCode(int codeLength){
        char[] codes = new char[codeLength];
        for (int i = 0; i < codeLength; i++) {
            codes[i] = codeChars.charAt(random.nextInt(codeChars.length()));
        }
        return codes;
    }

    /**
     * 获取算式验证码, 10 以内运算
     * @param operatorConut 运算符数量
     * @return equation 算式验证码
     */
    private Equation getEquation(int operatorConut){
        int[] nums = new int[operatorConut + 1];
        char[] operates = new char[operatorConut];
        char[] expression = new char[operatorConut * 2 + 1];

        for (int i = 0; i < operatorConut + 1; i++) {
            nums[i] = random.nextInt(10);
            expression[i * 2] = (char) (nums[i] + 48);
        }
        for (int i = 0; i < operatorConut; i++) {
            operates[i] = OPERATE_CHARS.charAt(random.nextInt(OPERATE_CHARS.length()));
            expression[i * 2 + 1] = operates[i];
        }
        return new Equation(expression, getResult(nums, operates));
    }

    /**
     * 计算算式结果
     * @param nums 数字
     * @param operators 运算符
     * @return 计算结果
     */
    private int getResult(int[] nums,char[] operators){
        Stack<Integer> numStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();
        numStack.push(nums[0]);
        numStack.push(nums[1]);
        operatorStack.push(operators[0]);
        for (int i = 1; i < operators.length; i++) {
            if (operatorMap.get(operators[i]) <= operatorMap.get(operatorStack.peek())) {
                int num2 = numStack.pop();
                int num1 = numStack.pop();
                numStack.push(computerResult(num1, num2, operatorStack.pop()));
            }
            numStack.push(nums[i + 1]);
            operatorStack.push(operators[i]);
        }
        while (!operatorStack.empty()){
            int num2 = numStack.pop();
            int num1 = numStack.pop();
            numStack.push(computerResult(num1, num2, operatorStack.pop()));
        }
        return numStack.pop();
    }

    /**
     * 计算表达式
     * @param num1 第一个数字
     * @param num2 第二个数字
     * @param operator 运算符
     */
    private int computerResult(Integer num1, Integer num2, Character operator){
        int result = 0;
        switch (operator){
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '*':
                result = num1 * num2;
                break;
            default:
        }
        return result;
    }

    public CodeTypeEnum getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeTypeEnum codeType) {
        this.codeType = codeType;
    }

    public String getCodeChars() {
        return codeChars;
    }

    public void setCodeChars(String codeChars) {
        this.codeChars = codeChars;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public int getCodeSize() {
        return codeSize;
    }

    public void setCodeSize(int codeSize) {
        this.codeSize = codeSize;
    }

    public int getOperatorCount() {
        return operatorCount;
    }

    public void setOperatorCount(int operatorCount) {
        this.operatorCount = operatorCount;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFontBasisSize() {
        return fontBasisSize;
    }

    public void setFontBasisSize(int fontBasisSize) {
        this.fontBasisSize = fontBasisSize;
    }

}