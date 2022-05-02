package com.tiantian.eunomia.watermark;

import java.io.IOException;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageWaterMarkMain {

    static{
        //加载opencv动态库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    /**
     * 嵌入数字的盲水印
     */
    public static void number() {
//    	int[] waterMark= {1,0,1,1,1,0,1,1,0,1,1,0,1,0,1,1,1,0,0,1,1,0,1,0,1,0,1,1,1,0,1,0,1,1,0,
//    			1,1,1,1,0,1,1,1,1,0,1,1,1,1,0,1,1,1,0,1,1,0,1,0,1,0,1,1,1,1,0,1,1,1,0,1,0,1,0,1,0,1,1,1,0,1,1,1,0,1
//    			,1,1,0,1,1,1,0,1,1,1,0,1,0,1,1,0,1,1,0,1,1,0,1,1,0,1,1,0,1,1,1,0,1,1,1,0,1,1,0,1,0,1,1,0,1,1,1,0,1,1
//    			,1,1,0,1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1};

//    	int [] watermark = new int[320];


        //嵌入强度
        int p = 75;

        //---------嵌入数字水印信息-------------

        for(int i = 0;i<320;i++) {
//    		System.out.println(waterMark[i]);
            Mat image = Imgcodecs.imread("D:\\picture\\split\\img"+i+".bmp");
            Mat imageOut0 = ImageWaterMarkUtil1.addImageWatermarkWithText(image,0,p);
            Imgcodecs.imwrite("D:\\picture\\split1\\img0 "+i+".bmp", imageOut0);
            Mat imageOut1 = ImageWaterMarkUtil1.addImageWatermarkWithText(image,1,p);
            Imgcodecs.imwrite("D:\\picture\\split1\\img1 "+i+".bmp", imageOut1);
        }



        //---------提取数字水印信息-------------

//    	for(int i = 0;i<waterMark.length;i++) {
//        	Mat watermarkOut0 = Imgcodecs.imread("D:\\picture\\split1\\img0 "+i+".bmp");
//        	Mat watermarkOut1 = Imgcodecs.imread("D:\\picture\\split1\\img1 "+i+".bmp");
//        	int watermark_out0 = ImageWaterMarkUtil1.getImageWatermarkWithText(watermarkOut0, p);
//        	int watermark_out1 = ImageWaterMarkUtil1.getImageWatermarkWithText(watermarkOut1, p);
////        	watermark[i] = watermark_out;
//        	System.out.println("watermark:"+watermark_out0+watermark_out1);
//    	}


//    	计算提取率
//    	int val = 0;
//    	for(int i=0;i<13;i++) {
//    		for(int j=0;j<13;j++) {
//
//    			if(waterMark[i] == waterMark[i])
//    				val++;
//    		}
//    	}
//
//    	System.out.println("提取率: "+(val * 1.0 / 169));

    }

    public static void watermark_in() {
        //嵌入强度
        int p = 75;

        //---------嵌入数字水印信息-------------

        for(int i = 0;i<160;i++) {
//    		System.out.println(waterMark[i]);
            Mat image = Imgcodecs.imread("D:\\picture\\split\\img"+i+".bmp");
            Mat imageOut0 = ImageWaterMarkUtil1.addImageWatermarkWithText(image,0,p);
            Imgcodecs.imwrite("D:\\picture\\split1\\img0 "+i+".bmp", imageOut0);
            Mat imageOut1 = ImageWaterMarkUtil1.addImageWatermarkWithText(image,1,p);
            Imgcodecs.imwrite("D:\\picture\\split1\\img1 "+i+".bmp", imageOut1);
        }
        System.out.println("嵌入水印完成");
    }

    public static void watermark_out() {
        //嵌入强度
        int p = 75;

        //---------提取数字水印信息-------------

        for(int i = 0;i<160;i++) {
            Mat watermarkOut0 = Imgcodecs.imread("D:\\picture\\split1\\img0 "+i+".bmp");
            Mat watermarkOut1 = Imgcodecs.imread("D:\\picture\\split1\\img1 "+i+".bmp");
            int watermark_out0 = ImageWaterMarkUtil1.getImageWatermarkWithText(watermarkOut0, p);
            int watermark_out1 = ImageWaterMarkUtil1.getImageWatermarkWithText(watermarkOut1, p);
//        	watermark[i] = watermark_out;
            System.out.println("watermark:"+watermark_out0+watermark_out1);
        }

    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub

        //嵌入数字的盲水印
        number();

        //如果没有64*64的二值图可调用方法
        //ImageWaterMarkUtil.getBinaryPhoto(srcPath, dstPath);

        //如果没有512*512的原图可调用方法
        //ImageWaterMarkUtil.thumbnail(srcImagePath, desImagePath, 512, 512);


    }
}
