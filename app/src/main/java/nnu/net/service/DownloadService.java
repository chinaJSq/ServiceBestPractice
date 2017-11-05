package nnu.net.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

import nnu.net.minterface.DownloadListener;
import nnu.net.servicebestpractice.MainActivity;
import nnu.net.servicebestpractice.R;
import nnu.net.util.DownloadTask;

/**
 * Created by Administrator on 2017/11/5.
 */

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;

    private  DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progree) {
            getNotificationManager().notify(1,getNotification("Downloading...",progree));
        }

        @Override
        public void onSucess() {
            downloadTask  = null;
            //下载成功时前台服务关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Sucess",-1));
            Toast.makeText(DownloadService.this,"Download Sucess",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Fialed",-1));
            Toast.makeText(DownloadService.this,"Download Fialed",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this,"Download Paused",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            Toast.makeText(DownloadService.this,"Download Canceled",Toast.LENGTH_SHORT).show();

        }
    };
    private   DownloadBinder mBinder = new DownloadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class DownloadBinder extends Binder{
        public  void startDownload(String url){
            if(downloadTask == null){
                downloadUrl =url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("Downloading...",0));
                Toast.makeText(DownloadService.this,"Downloading...",Toast.LENGTH_SHORT).show();

            }
        }
        public void pauseDownload(){
            if(downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if(downloadTask!=null){
                downloadTask.cancelDownload();
            }
            if(downloadTask!=null){
                //取消下载将文件删除，并关闭通知
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory+fileName);
                if(file.exists()){
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private NotificationManager getNotificationManager(){
        return  (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title,int progress){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if(progress>=0){
            //当progress大于等于0的时候才显示下载进度
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return  builder.build();
    }
}
