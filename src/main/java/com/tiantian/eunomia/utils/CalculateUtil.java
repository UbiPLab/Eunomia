package com.tiantian.eunomia.utils;

/**
 * @author tiantian152
 */
public class CalculateUtil {

    /**
     * 按位异或
     *
     * @param a String a
     * @param b String b
     * @return 异或结果
     */
    public static String calculateXor(String a, String b) {
        StringBuilder c = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                c.append("0");
            } else {
                c.append("1");
            }
        }
        return c.toString();
    }
}
