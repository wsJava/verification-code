package top.lvjp;

import java.awt.image.BufferedImage;

/**
 * 验证码图片对象
 * 封装了验证码图片和验证码
 * 并对外提供访问属性的方法
 *
 * @author lvjp
 * @date 2019/7/10
 */
public class VerificationImage {

    /**
     * 验证码图片
     */
    private BufferedImage bufferedImage;

    /**
     * 验证码值
     */
    private String rightCode;

    protected VerificationImage(BufferedImage bufferedImage, String rightCode) {
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
