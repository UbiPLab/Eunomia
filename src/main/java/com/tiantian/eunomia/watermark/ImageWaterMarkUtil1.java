package com.tiantian.eunomia.watermark;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageWaterMarkUtil1 {
    static{
        //加载opencv动态库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    /**
     * 因为密钥的类型是int[],但是水印要int[][]
     * @param watermark
     * @param length
     * @return
     */
    public static int[][] transferWatermark(int [] watermark,int length) {
        int [][] result= new int[13][13];
        int i,j ;
        int k = 0;
        for(i=0;i<13;i++) {
            for(j = 0;j<13;j++) {
                if(k<length) {
                    result[i][j]=watermark[k];
                    System.out.print("watermark:"+watermark[k]);
                    k++;
                }else {
                    result[i][j]=0;
                }
            }
        }
//		System.out.println("i:"+i);
//		for(int m = i;i<64;i++) {
//
//		}
        return result;
    }


    /**
     * 嵌入水印信息
     * @param image：原图
     * @param watermark：水印信息
     * @param p：嵌入强度
     */
    public static Mat addImageWatermarkWithText(Mat image, int watermark,double p) {
        List<Mat> allPlanes = new ArrayList<Mat>();

        Mat Ycbcr=new Mat(image.rows(),image.cols(),CvType.CV_8UC1);
        Imgproc.cvtColor(image, Ycbcr,Imgproc.COLOR_RGB2YCrCb);
        Core.split(image, allPlanes);
        //获取YMat矩阵
        Mat YMat = allPlanes.get(0);
//		System.out.println("length:"+watermark.length);
//		System.out.println("length:"+watermark[0].length);
        //分成4096块
        for(int i=0;i<1;i++) {
            for(int j=0;j<1;j++) {

                //block 表示分块 而且为 方阵

                int length = image.rows()/1;
                Mat block = null;
                //提取每个分块
                block = getImageValue(YMat,i,j,length);



                double[] a = new double[1];
                double[] c = new double[1];

                int x1 = 1, y1 = 2;
                int x2 = 2, y2 = 1;

                a = block.get(x1,y1);
                c = block.get(x2,y2);

                //对分块进行DCT变换
                Core.dct(block, block);

                a = block.get(x1,y1);
                c = block.get(x2,y2);

                if(watermark  == 1) {
                    block.put(x1,y1, p);
                    block.put(x2,y2, 0);
                }


                if(watermark  == 0) {
                    block.put(x1,y1, 0);
                    block.put(x2,y2, p);
                }


                //对上面分块进行IDCT变换
                Core.idct(block, block);
                for(int m=0;m<length;m++) {
                    for(int t=0;t<length;t++) {
                        double[] e = block.get(m, t);
                        YMat.put(i*length + m,j*length + t, e);
                    }
                }
            }

        }

        Mat imageOut = new Mat();
        Core.merge(allPlanes,imageOut);

        return imageOut;
    }

    /**
     * 提取水印信息
     * @param image：带提取的图片
     * @return int[][]
     */
    public static int getImageWatermarkWithText(Mat image,double p) {

        List<Mat> allPlanes = new ArrayList<Mat>();


        Mat Ycbcr=new Mat(image.rows(),image.cols(),CvType.CV_8UC1);
        Imgproc.cvtColor(image, Ycbcr,Imgproc.COLOR_RGB2YCrCb);
        Core.split(image, allPlanes);

        Mat YMat = allPlanes.get(0);

        int watermark = 0;


        //分成64块，提取每块嵌入的水印信息
        for(int i=0;i<1;i++) {
            for(int j=0;j<1;j++) {
                //block 表示分块 而且为 方阵
                int length = image.rows()/1;
                Mat block = null;
                //提取每个分块
                block = getImageValue(YMat,i,j,length);


                //对分块进行DCT变换
                Core.dct(block, block);
                //用于容纳DCT系数
                double[] a = new double[1];
                double[] c = new double[1];

                int x1 = 1, y1 = 2;
                int x2 = 2, y2 = 1;

                a = block.get(x1,y1);
                c = block.get(x2,y2);

                if(a[0]>=c[0])
                    watermark = 1;
            }
        }
        return watermark;
    }

    /**
     * 提取每个分块
     * @param YMat：原分块
     * @param x：x与y联合表示第几个块
     * @param y：x与y联合表示第几个块
     * @param length：每个块的长度
     * @return
     */
    public static Mat getImageValue(Mat YMat,int x,int y,int length) {
        Mat mat = new Mat(length,length,CvType.CV_32F);
        for(int i=0;i<length;i++) {
            for(int j=0;j<length;j++) {

                double[] temp = YMat.get(x*length+i, y*length+j);
                mat.put(i, j, temp);
            }
        }
        return mat;
    }

    /**
     * 获取二值图的信息
     */
    public static int[][] getInformationOfBinaryGraph(String srcPath){
        int[][] waterMark = new int[64][64];

        Mat mat = Imgcodecs.imread(srcPath);

        int width = 64;
        int height = 64;
        double a[] = new double[3];
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++) {
                a = mat.get(i, j);
                if((int)a[0] == 255)
                    waterMark[i][j] = 1;
                else
                    waterMark[i][j] = 0;
            }
        }

        return waterMark;
    }


    /**
     *将水印信息的二维数组转换为一张图片
     */
    public static void matrixToBinaryPhoto(int[][] watermark,String dstPath) {

        int width = 64,height = 64;
        Mat binaryPhoto  = new Mat(width,height,Imgproc.THRESH_BINARY);

        double a[] = new double[] {255,255,255};
        double b[] = new double[] {0,0,0};

        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++) {
                if(watermark[i][j] == 1)
                    binaryPhoto.put(i, j, a);
                else
                    binaryPhoto.put(i, j, b);
            }
        }
        Imgcodecs.imwrite(dstPath, binaryPhoto);
    }


    /**
     * 将一张图片压缩成一张64x64的二值图
     * @param srcPath
     * @param dstPath
     */
    public static String getBinaryPhoto(String srcPath,String dstPath)  {

        srcPath = thumbnail(srcPath, dstPath,64,64);

        //得到原图
        File file = new File(srcPath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int width = image.getWidth();
        int height = image.getHeight();
        //创建原图的二值图
        BufferedImage binaryPhoto = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
        int min = new Color(0,0,0).getRGB();
        int max = new Color(255,255,255).getRGB();
        //判断标记
        int flag = 170;
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++) {
                //像素
                int pixel = image.getRGB(i, j);
                //得到 rgb通道对应的元素
                int r,g,b;
                r = (pixel & 0xff0000) >> 16;
                g = (pixel & 0xff00) >> 8;
                b = (pixel & 0xff);
                int avg = (r + g + b)/3;
                if(avg <= flag){
                    binaryPhoto.setRGB(i, j, min);
                }
                else{
                    binaryPhoto.setRGB(i, j, max);
                }
            }
        }
        try {
            ImageIO.write(binaryPhoto, "bmp", new File(dstPath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dstPath;
    }



    /**
     * 将图片变成指定大小的图片
     */
    public static String thumbnail(String srcImagePath, String desImagePath,int w,int h) {

        Mat src = Imgcodecs.imread(srcImagePath);
        Mat dst = src.clone();
        Imgproc.resize(src, dst, new Size(w, h));
        Imgcodecs.imwrite(desImagePath, dst);
        return desImagePath;
    }
}
