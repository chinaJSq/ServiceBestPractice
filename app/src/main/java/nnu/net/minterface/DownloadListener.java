package nnu.net.minterface;

/**
 * Created by Administrator on 2017/11/5.
 */

public interface DownloadListener {
    public  void onProgress(int progree);
    public void onSucess();
    public void onFailed();
    public void onPaused();
    public void onCanceled();
}
