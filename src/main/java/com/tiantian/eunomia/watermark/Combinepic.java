package com.tiantian.eunomia.watermark;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author shubham
 */
public class Combinepic {
    public static void main(String[] args) throws IOException {
//		int rows = 13;   //初始化有小图片的数量
//	    int cols = 13;
//	    int chunks = rows * cols;
//
//	    int chunkWidth, chunkHeight;
//	    int type;
//	    //读取图片文件
//	    File[] imgFiles = new File[chunks];
//	    for (int i = 0; i < chunks; i++) {
//	        imgFiles[i] = new File("D:\\picture\\dec\\img0" + i + ".bmp");
//	    }
//
//	   //缓存图片文件
//	    BufferedImage[] buffImages = new BufferedImage[chunks];
//	    for (int i = 0; i < chunks; i++) {
//	        buffImages[i] = ImageIO.read(imgFiles[i]);
//	    }
//	    type = buffImages[0].getType();
//	    chunkWidth = buffImages[0].getWidth();
//	    chunkHeight = buffImages[0].getHeight();
//
//	    //初始化最终的图片缓存器
//	    BufferedImage finalImg = new BufferedImage(chunkWidth*cols, chunkHeight*rows, type);
//
//	    int num = 0;
//	    for (int i = 0; i < rows; i++) {
//	        for (int j = 0; j < cols; j++) {
//	            finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
//	            num++;
//	        }
//	    }
//	    System.out.println("图片组合完成");
//	    ImageIO.write(finalImg, "jpeg", new File("D:\\picture\\combine.bmp"));
    }

    public static String combine_picture() throws IOException {
        int rows = 13;   //初始化有小图片的数量
        int cols = 13;
        int chunks = rows * cols;

        int chunkWidth, chunkHeight;
        int type;
        //读取图片文件
        File[] imgFiles = new File[chunks];
        for (int i = 0; i < 160; i++) {
            imgFiles[i] = new File("D:\\picture\\dec\\img0 " + i + ".bmp");
        }
        for (int i = 0; i < chunks-160; i++) {
            imgFiles[i+160] = new File("D:\\picture\\split\\img" + (i+160) + ".bmp");
        }

        //缓存图片文件
        BufferedImage[] buffImages = new BufferedImage[chunks];
        for (int i = 0; i < chunks; i++) {
            buffImages[i] = ImageIO.read(imgFiles[i]);
        }
        type = buffImages[0].getType();
        chunkWidth = buffImages[0].getWidth();
        chunkHeight = buffImages[0].getHeight();

        //初始化最终的图片缓存器
        BufferedImage finalImg = new BufferedImage(chunkWidth*cols, chunkHeight*rows, type);

        int num = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }
        System.out.println("图片组合完成");
        String combinePath = "D:\\picture\\combine.jpg";
        ImageIO.write(finalImg, "jpeg", new File(combinePath));
        return combinePath;
    }
}

