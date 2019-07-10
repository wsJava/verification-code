package top.lvjp;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static top.lvjp.Equation.getEquation;

/**
 * 提供生成验证码功能
 * 有字符串验证码和算式验证码两中类型
 *
 * @author lvjp
 * @date 2019/4/19
 */
public class VerificationCode {


    /**
     * 存放验证码的 session 中的key
     */
    public static final String SESSION_CODE_KEY = "CODE-KEY";

    /**
     * 默认验证码类型
     */
    private static final CodeTypeEnum DEFAULT_CODE_TYPE = CodeTypeEnum.CHAR;

    /**
     * 默认字符验证码用到的字符
     */
    private static final String DEFAULT_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";


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
     * 随机数生成对象
     */
    private ThreadLocalRandom random;

    /**
     * 初始化一个默认的验证码生成器
     * 验证码类型默认为字符串类型
     */
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

    /**
     * @param type 验证码类型
     */
    public VerificationCode(CodeTypeEnum type) {

        Objects.requireNonNull(type, "codeType must be not null!");

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
     *
     * @return VerificationImage 验证码对象
     */
    public VerificationImage getVerificationImage() {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        char[] codes = null;
        String rightCode = null;
        Graphics graphics = bufferedImage.getGraphics();
        graphics.fillRect(0, 0, width, height);
        graphics.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, fontBasisSize));
        if (codeType == CodeTypeEnum.CHAR) {      //如果是字符类型验证码,则画干扰线
            graphics.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, fontBasisSize));
            for (int i = 0; i < lineCount; i++) {
                graphics.setColor(getRandColor());
                drawLine(graphics);
            }
            codes = getCharCode(codeSize);
            rightCode = String.valueOf(codes);
        } else if (codeType == CodeTypeEnum.EQUATION) {  //如果是算式类型验证码
            Equation equation = getEquation(operatorCount);
            codes = equation.getExpression();
            rightCode = String.valueOf(equation.getResult());
        }
        drawString(graphics, codes);
        graphics.dispose();
        return new VerificationImage(bufferedImage, rightCode);
    }

    /**
     * 在 http 中设置验证图片和验证码
     *
     * @param request  http 请求, 在其中设置验证码
     * @param response http 响应, 设置返回验证码图片
     */
    public void setHttpVerificationCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");//设置相应类型,输出的内容为图片
        response.setHeader("Pragma", "no-cache");//设置响应头信息，不要缓存此内容
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/jpeg");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession();
        session.removeAttribute(SESSION_CODE_KEY);
        VerificationImage verificationImage = getVerificationImage();
        session.setAttribute(SESSION_CODE_KEY, verificationImage.getRightCode());
        ImageIO.write(verificationImage.getBufferedImage(), "JPEG", response.getOutputStream());
    }

    private Font getFont() {
        return new Font("Ffixedsys", Font.CENTER_BASELINE, fontBasisSize + random.nextInt(6));
    }

    /**
     * 获取干扰线随机颜色
     *
     * @return 在 COLOR_BOUND 边界范围内随机生成的颜色
     */
    private Color getRandColor() {
        int r = random.nextInt(COLOR_BOUND);
        int g = random.nextInt(COLOR_BOUND);
        int b = random.nextInt(COLOR_BOUND);
        return new Color(r, g, b);
    }

    /**
     * 绘制干扰线
     *
     * @param graphics 图形
     */
    private void drawLine(Graphics graphics) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int x1 = random.nextInt(x + random.nextInt(width));
        int y1 = random.nextInt(y + random.nextInt(height));
        graphics.drawLine(x, y, x1, y1);
    }

    /**
     * 把验证码或算式画入验证图片中
     *
     * @param graphics 图像
     * @param codes    验证码或算式
     */
    private void drawString(Graphics graphics, char[] codes) {
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
     *
     * @param codeLength 验证码长度
     * @return codes 随机验证码
     */
    private char[] getCharCode(int codeLength) {
        char[] codes = new char[codeLength];
        for (int i = 0; i < codeLength; i++) {
            codes[i] = codeChars.charAt(random.nextInt(codeChars.length()));
        }
        return codes;
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