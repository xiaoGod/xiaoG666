//
//  ImageUtil.java
//

package com.mobile.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import org.cocos2dx.javascript.AppActivity;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ImageUtil {
    private static final int TIME_OUT = 10 * 1000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码
    private static int uploadstate; //1 开始上传 2 上传成功 3 上传失败

    public static void CopyImageToJPG(String srcPath, String savePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
        if(options.outHeight > 2048 || options.outWidth > 2048) //egret渲染不得超过4096 * 4096
            options.inSampleSize = 3;

        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(srcPath, options);
        File outfile = new File(savePath);
        if(outfile.exists())
            outfile.delete();

        try {
            FileOutputStream out = new FileOutputStream(outfile);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap GetScaledBitmap(Bitmap bitmap, float sw, float sh) {
        Matrix matrix = new Matrix();
        matrix.postScale(sw, sh); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return resizeBmp;
    }

    public static void SaveImageToJPG(Bitmap photo, String savePath) {
        if(photo.getWidth() > 4000 || photo.getHeight() > 4000)
            photo = GetScaledBitmap(photo, 0.5f, 0.5f);

        File outfile = new File(savePath);
        if(outfile.exists())
            outfile.delete();

        try {
            FileOutputStream out = new FileOutputStream(outfile);
            if (photo.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩图片的质量，减少文件大小
     * @param filePath 图片路径
     * @param quality  图片压缩比例，比如是0.5就是压缩50%。
     * @return 压缩后的图片路径
     */
    public static String compressImage(String filePath, double quality)
    {
        //原文件
        File oldFile = new File(filePath);

        //照片路径
        String targetPath = oldFile.getPath();
        //压缩比例 0-100
        quality = quality > 1 ? 1 : quality;
        quality = quality < 0 ? 0 : quality;
        int iQuality = (int)(quality * 100);

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        //压缩后的文件
        File outputFile = new File(targetPath);
        try {
            if(!outputFile.exists())
                outputFile.getParentFile().mkdirs();
            else
                outputFile.delete();
            FileOutputStream outStream = new FileOutputStream(outputFile);
            //全部图片只能以JPG格式来压缩，PNG格式压缩无效
            bitmap.compress(Bitmap.CompressFormat.JPEG, iQuality, outStream);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return filePath;
        }
        return outputFile.getPath();
    }

    /**
     * 压缩一个图片，并返回对应的inputstream
     * @param bitmap 要压缩的bitmap
     * @param quality  图片的压缩率
     * @return 图片对应的字节流
     */
    public static InputStream bitmap2InputStream(Bitmap bitmap, double quality)
    {
        //压缩比例 0-100
        quality = quality > 1 ? 1 : quality;
        quality = quality < 0 ? 0 : quality;
        int iQuality = (int)(quality * 100);

        try
        {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            //全部图片只能以JPG格式来压缩，png格式压缩无效。
            bitmap.compress(Bitmap.CompressFormat.JPEG, iQuality, byteOutputStream);
            InputStream is = new ByteArrayInputStream(byteOutputStream.toByteArray());
            byteOutputStream.close();
            return is;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendToH5(int uploadstate, String path, String msg)
    {
        String param = String.format("{\"uploadstate\":%d, \"path\":\"%s\", \"msg\":\"%s\"}", uploadstate, path, msg);
        String strParam = String.format("{\"type\":%d, \"data\":%s}", MsgType.MsgImageUploadForAgent, param);
        AppActivity.getInstance().sendMessageToJS(strParam);
    }

    public static void uploadFile(final String picPath, final double quality, final String urlPath,final String cipherText,final String cipherKey,final String playerId)
    {
        sendToH5(1, picPath, "");
        new Thread() {
            @Override
            public void run() {
                String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
                String PREFIX = "--", LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data"; // 内容类型
                String RequestURL = urlPath;
                try {
                    URL url = new URL(RequestURL);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setReadTimeout(TIME_OUT);
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", CHARSET); // 设置编码
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                    //生成一个bitmap 用于压缩
                    Bitmap bitmap = BitmapFactory.decodeFile(picPath);
                    String fileName = picPath.substring(picPath.lastIndexOf("/") + 1);
                    String suffixStr = fileName.substring(fileName.lastIndexOf(".") + 1);

                    Log.d("ImageUpLoad","fileName is " + fileName + " suffixStr is " + suffixStr);
                    if(bitmap != null) {
                        //当文件不为空，把文件包装并且上传
                        OutputStream outputSteam = conn.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(outputSteam);

                        String lineStartStr = PREFIX + BOUNDARY + LINE_END;

                        //添加数据开始
                        StringBuilder strBuf1 = new StringBuilder();
                        strBuf1.append(lineStartStr);
                        strBuf1.append("Content-Disposition: form-data; name=\"cipherText\"" + LINE_END + LINE_END);
                        strBuf1.append(cipherText).append(LINE_END);

                        strBuf1.append(lineStartStr);
                        strBuf1.append("Content-Disposition: form-data; name=\"cipherKey\"" + LINE_END + LINE_END);
                        strBuf1.append(cipherKey).append(LINE_END);

                        strBuf1.append(lineStartStr);
                        strBuf1.append("Content-Disposition: form-data; name=\"playerId\"" + LINE_END + LINE_END);
                        strBuf1.append(playerId).append(LINE_END);

                        dos.write(strBuf1.toString().getBytes());
                        //添加数据结束

                        StringBuffer sb = new StringBuffer();
                        sb.append(lineStartStr);

                        /**
                         * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                         * filename是文件的名字，包含后缀名的 比如:abc.png
                         */
                        sb.append(String.format("Content-Disposition: form-data; name=\"pay_screen_shot\"; filename=\"%s\"%s",fileName,LINE_END));
                        sb.append(String.format("Content-Type: image/%s; charset=%s%s",suffixStr,CHARSET,LINE_END));
                        sb.append(LINE_END);
                        dos.write(sb.toString().getBytes());

                        InputStream is = bitmap2InputStream(bitmap, quality);
                        byte[] bytes = new byte[1024 * 4];
                        int len = 0;
                        long cuLen = 0;
                        long totLen = is.available();

                        //计算图片读取进度
                        while ((len = is.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                            cuLen = cuLen + (long)len;
                            double percent = (double)cuLen / (totLen);

                            Log.d("ImageUpLoad","ImageUpLoad percent is " + percent);
                        }
                        is.close();
                        dos.write(LINE_END.getBytes());

                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                        dos.write(end_data);
                        dos.flush();
                        dos.close();
                        Log.d("ImageUpLoad","fileName totLen is " + totLen );

                        /**
                         * 获取响应码 200=成功 当响应成功，获取响应的流
                         */
                        int res = conn.getResponseCode();
                        if (res == 200) {
                            InputStream backIs = conn.getInputStream();
                            ByteArrayOutputStream backBaos = new ByteArrayOutputStream();
                            int backLen = 0;
                            byte[] buffer = new byte[1024];
                            while ((backLen = backIs.read(buffer)) != -1)
                            {
                                backBaos.write(buffer,0,backLen);
                            }
                            String jsonStr = backBaos.toString();
                            backBaos.close();
                            backIs.close();
                            JSONObject backJson = new JSONObject(jsonStr);
                            //1表示成功，其他表示失败
                            int iStatus = backJson.getInt("status");
                            String msg = backJson.getString("msg");
                            if(iStatus == 1) {
                                Log.d("ImageUpLoad","上传成功");
                                sendToH5(2, picPath, msg);
                            }else {
                                Log.d("ImageUpLoad","上传失败");
                                sendToH5(3, picPath, msg);
                            }
                        }else {
                            Log.d("ImageUpLoad","访问上传地址失败");
                            sendToH5(3, picPath, "访问上传地址失败");
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
