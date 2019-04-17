package com.music.player.lib.util;

/**
 * 2019/2/26
 */
public class MusicColorUtils {

    private static final String TAG = "ColorUtils";
    private static MusicColorUtils mInstance;

    public static synchronized MusicColorUtils getInstance() {
        synchronized (MusicColorUtils.class) {
            if (null == mInstance) {
                mInstance = new MusicColorUtils();
            }
        }
        return mInstance;
    }

    /**
     * 计算从startColor过度到endColor过程中百分比为franch时的颜色值
     * @param startColor 起始颜色 （格式#FFFFFFFF）
     * @param endColor 结束颜色 （格式#FFFFFFFF）
     * @param franch 百分比0.5
     * @return 返回String格式的color（格式#FFFFFFFF）
     */
    public String caculateColor(String startColor, String endColor, float franch){

        int startAlpha = Integer.parseInt(startColor.substring(1, 3), 16);
        int startRed = Integer.parseInt(startColor.substring(3, 5), 16);
        int startGreen = Integer.parseInt(startColor.substring(5, 7), 16);
        int startBlue = Integer.parseInt(startColor.substring(7), 16);

        int endAlpha = Integer.parseInt(endColor.substring(1, 3), 16);
        int endRed = Integer.parseInt(endColor.substring(3, 5), 16);
        int endGreen = Integer.parseInt(endColor.substring(5, 7), 16);
        int endBlue = Integer.parseInt(endColor.substring(7), 16);

        int currentAlpha = (int) ((endAlpha - startAlpha) * franch + startAlpha);
        int currentRed = (int) ((endRed - startRed) * franch + startRed);
        int currentGreen = (int) ((endGreen - startGreen) * franch + startGreen);
        int currentBlue = (int) ((endBlue - startBlue) * franch + startBlue);

        return "#" + getHexString(currentAlpha) + getHexString(currentRed)
                + getHexString(currentGreen) + getHexString(currentBlue);
    }

    /**
     * 将10进制颜色值转换成16进制。
     */
    public String getHexString(int value) {
        String hexString = Integer.toHexString(value);
        if (hexString.length() == 1) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    /**
     * 取差值
     * @param maxVar
     * @param var
     * @return
     */
    public float absVakue(float maxVar, float var) {
        return maxVar-var;
    }
}
