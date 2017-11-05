package nnu.net.util;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import nnu.net.minterface.DownloadListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/11/5.
 */

public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    public static final int TYPE_SUCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;
    private DownloadListener listener;
    private boolean isCanceled = false;
    private boolean isPaused =false;
    private  int lastProgress;


    public DownloadTask(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCESS:
                listener.onSucess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if(progress>lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onCancelled(Integer integer) {
        super.onCancelled(integer);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file =null;
        try{
            long downloadedLength = 0;//记录已经下载的文件长度
            String downloadUrl = params[0];
            String fileName =downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory+fileName);
            if(file.exists()){
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);
            if (contentLength==0){
                return  TYPE_FAILED;
            }else if(contentLength==downloadedLength){
                return  TYPE_SUCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes="+downloadedLength+"-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null){
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file,"rw");
                saveFile.seek(downloadedLength);//跳过已经下载的
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while((len = is.read(b))!=-1){
                    if(isCanceled){
                        return  TYPE_CANCELED;
                    }else if(isPaused){
                        return  TYPE_PAUSED;
                    }else{
                        total +=len;
                        saveFile.write(b,0,len);
                        //计算下载的百分百
                        int progress = (int)((total+downloadedLength)*100/contentLength);
                        publishProgress(progress);
                    }

                }
                response.body().close();
                return  TYPE_SUCESS;
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(is!=null){
                    is.close();;
                }
                if(saveFile!=null){
                    saveFile.close();
                }
                if(isCanceled&&file!=null){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return TYPE_FAILED;
    }
    public  void pauseDownload(){
        isPaused = true;
    }
    public  void cancelDownload(){
        isCanceled = true;
    }
    private long getContentLength(String downloadUrl)throws IOException{
        OkHttpClient client =new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if(response!=null&&response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return  0;
    }
}
