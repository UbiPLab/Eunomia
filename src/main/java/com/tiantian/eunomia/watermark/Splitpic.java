package com.tiantian.eunomia.watermark;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author shubham
 */
public class Splitpic {

    public static void split_picture(String file_path) throws IOException {
        // 图片路径
        File file = new File(file_path);
        FileInputStream fis = new FileInputStream(file);

        //把文件读到图片缓冲流中
        BufferedImage image = ImageIO.read(fis);

        //定义图片要切分成多少块
        int rows = 13;

        int cols = 13;

        int chunks = rows * cols;

        // 计算每一块小图片的高度和宽度
        int chunkWidth = image.getWidth() / cols;

        int chunkHeight = image.getHeight() / rows;

        int count = 0;

        BufferedImage imgs[] = new BufferedImage[chunks];

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                //初始化BufferedImage

                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                //画出每一小块图片

                Graphics2D gr = imgs[count++].createGraphics();

                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);

                gr.dispose();

            }
        }

        System.out.println("切分完成");

        //保存小图片到文件中

        for (int i = 0; i < imgs.length; i++) {
            ImageIO.write(imgs[i], "bmp", new File("D:\\picture\\split\\img" + i + ".bmp"));
        }

        System.out.println("小图片创建完成");
    }


}
