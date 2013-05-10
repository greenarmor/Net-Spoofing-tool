package dvoph.apo.netspoof;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CancellationException;
import java.util.zip.GZIPInputStream;
import dvoph.apo.netspoof.config.Config;
import dvoph.apo.netspoof.misc.AsyncTaskHelper;

public class InstallService extends Service
  implements Config
{
  private static final int DL_NOTIFY = 1;
  public static final String INTENT_EXTRA_DLPROGRESS = "dvoph.apo.netspoof.InstallService.dlprogress";
  public static final String INTENT_EXTRA_DLSTATE = "dvoph.apo.netspoof.InstallService.dlprogress";
  public static final String INTENT_EXTRA_STATUS = "dvoph.apo.netspoof.InstallService.status";
  public static final String INTENT_START_FILE = "dvoph.apo.netspoof.config.InstallStatus.isFile";
  public static final String INTENT_START_URL = "dvoph.apo.netspoof.config.InstallStatus.URL";
  public static final String INTENT_START_URL_UNZIPPED = "dvoph.apo.netspoof.config.InstallStatus.URLUnzipped";
  public static final String INTENT_START_URL_UPGRADE = "dvoph.apo.netspoof.config.InstallStatus.isUpgrade";
  public static final String INTENT_STATUSUPDATE = "dvoph.apo.netspoof.config.ConfigChecker.StatusUpdate";
  public static final int STATUS_DL_CANCEL = 6;
  public static final int STATUS_DL_FAIL_DLERROR = 4;
  public static final int STATUS_DL_FAIL_IOERROR = 2;
  public static final int STATUS_DL_FAIL_MALFORMED_FILE = 1;
  public static final int STATUS_DL_FAIL_SDERROR = 3;
  public static final int STATUS_DL_SUCCESS = 0;
  public static final int STATUS_DOWNLOADING = 1;
  public static final int STATUS_FINISHED = 2;
  public static final int STATUS_STARTED = 0;
  public static final int STATUS_UPGRADE_ERROR = 5;
  private DLProgress dlProgress = new DLProgress(0, 1024);
  private int dlstatus = 0;
  public final DownloadTask downloadTask = new DownloadTask(null);
  private Notification notification;
  private NotificationManager notificationManager;
  boolean started = false;
  private int status = 0;

  private void broadcastStatus()
  {
    Intent localIntent = new Intent("dvoph.apo.netspoof.config.ConfigChecker.StatusUpdate");
    localIntent.putExtra("dvoph.apo.netspoof.InstallService.status", this.status);
    switch (this.status)
    {
    case 0:
    default:
    case 1:
    case 2:
    }
    while (true)
    {
      sendBroadcast(localIntent);
      return;
      localIntent.putExtra("dvoph.apo.netspoof.InstallService.dlprogress", this.dlProgress);
      continue;
      localIntent.putExtra("dvoph.apo.netspoof.InstallService.dlprogress", this.dlstatus);
    }
  }

  private void start(Intent paramIntent)
  {
    this.notificationManager = ((NotificationManager)getSystemService("notification"));
    this.notification = new Notification(2130837508, getString(2131099702), System.currentTimeMillis());
    RemoteViews localRemoteViews = new RemoteViews(getPackageName(), 2130903045);
    this.notification.contentView = localRemoteViews;
    this.notification.flags = (0x2 | this.notification.flags);
    PendingIntent localPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, InstallService.class), 0);
    this.notification.contentIntent = localPendingIntent;
    this.notificationManager.notify(1, this.notification);
    this.started = true;
    String str = paramIntent.getStringExtra("dvoph.apo.netspoof.config.InstallStatus.URL");
    boolean bool1 = paramIntent.getBooleanExtra("dvoph.apo.netspoof.config.InstallStatus.URLUnzipped", false);
    boolean bool2 = paramIntent.getBooleanExtra("dvoph.apo.netspoof.config.InstallStatus.isUpgrade", false);
    boolean bool3 = paramIntent.getBooleanExtra("dvoph.apo.netspoof.config.InstallStatus.isFile", false);
    if (str == null)
      throw new IllegalArgumentException("Start URL was null");
    Log.v("android-netspoof", "Downloading file " + str);
    DownloadTask localDownloadTask = this.downloadTask;
    DlStartData[] arrayOfDlStartData = new DlStartData[1];
    arrayOfDlStartData[0] = new DlStartData(str, bool1, bool2, bool3);
    AsyncTaskHelper.execute(localDownloadTask, arrayOfDlStartData);
  }

  public IBinder onBind(Intent paramIntent)
  {
    return null;
  }

  public void onDestroy()
  {
    this.notificationManager.cancel(1);
    this.downloadTask.cancel(false);
  }

  void onFinish()
  {
    stopSelf();
  }

  public void onStart(Intent paramIntent, int paramInt)
  {
    if (!this.started)
      start(paramIntent);
    broadcastStatus();
  }

  public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
  {
    if (!this.started)
      start(paramIntent);
    broadcastStatus();
    return 3;
  }

  public static final class DLProgress
    implements Serializable
  {
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_EXTRACTING = 2;
    public static final int STATUS_PATCHING = 3;
    public static final int STATUS_RECOVERING = 4;
    private static final long serialVersionUID = -5366348392979726959L;
    private int bytesDone;
    private int bytesTotal;
    private final int status;

    public DLProgress(int paramInt1, int paramInt2)
    {
      this.bytesDone = paramInt1;
      this.bytesTotal = paramInt2;
      this.status = 1;
    }

    public DLProgress(int paramInt1, int paramInt2, int paramInt3)
    {
      this.bytesDone = paramInt2;
      this.bytesTotal = paramInt3;
      this.status = paramInt1;
    }

    public int getBytesDone()
    {
      return this.bytesDone;
    }

    public int getBytesTotal()
    {
      return this.bytesTotal;
    }

    public int getKBytesDone()
    {
      return this.bytesDone / 1024;
    }

    public int getKBytesTotal()
    {
      return this.bytesTotal / 1024;
    }

    public int getStatus()
    {
      return this.status;
    }

    public void setBytesDone(int paramInt)
    {
      this.bytesDone = paramInt;
    }

    public void setBytesTotal(int paramInt)
    {
      this.bytesTotal = paramInt;
    }
  }

  public static abstract interface DLProgressPublisher
  {
    public abstract void publishDLProgress(InstallService.DLProgress paramDLProgress);
  }

  private static class DlStartData
    implements Serializable
  {
    private static final long serialVersionUID = 6287320665354658385S;
    public final boolean unzipped;
    public final boolean upgrade;
    public final String url;
    public final boolean useLocalFile;

    public DlStartData(String paramString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
    {
      this.url = paramString;
      this.unzipped = paramBoolean1;
      this.upgrade = paramBoolean2;
      this.useLocalFile = paramBoolean3;
    }
  }

  private final class DownloadTask extends AsyncTask<InstallService.DlStartData, InstallService.DLProgress, Integer>
    implements InstallService.DLProgressPublisher
  {
    private URLConnection connection;
    private File dlDestination;
    private FileOutputStream dlWriter;
    private URL downloadURL;
    private InputStream response;
    private File sd;
    int statusUpdate = 0;

    private DownloadTask()
    {
    }

    private int tryDownload(int paramInt1, int paramInt2)
      throws IOException, CancellationException
    {
      this.connection = this.downloadURL.openConnection();
      this.connection.setRequestProperty("Range", "bytes=" + paramInt1 + "-");
      this.connection.connect();
      this.response = new BufferedInputStream(this.connection.getInputStream());
      byte[] arrayOfByte = new byte[32768];
      int i = 0;
      InstallService.DLProgress localDLProgress = new InstallService.DLProgress(paramInt1, paramInt2);
      int j = 0;
      while (true)
        try
        {
          int k = this.response.read(arrayOfByte);
          if (k == -1)
            break;
          i += k;
          this.dlWriter.write(arrayOfByte, 0, k);
          m = j + 1;
          int n;
          if (j > 6)
          {
            m = 0;
            n = i + paramInt1;
          }
          try
          {
            localDLProgress.setBytesDone(n);
            InstallService.DLProgress[] arrayOfDLProgress = new InstallService.DLProgress[1];
            arrayOfDLProgress[0] = localDLProgress;
            publishProgress(arrayOfDLProgress);
            if (isCancelled())
            {
              this.response.close();
              this.dlWriter.close();
              new File(this.sd.getAbsolutePath() + "/" + "img/version").delete();
              new File(this.sd.getAbsolutePath() + "/" + "img/debian.img.gz").delete();
              throw new CancellationException();
            }
          }
          catch (IOException localIOException1)
          {
            localIOException1.printStackTrace();
            try
            {
              this.response.close();
            }
            catch (IOException localIOException3)
            {
              localIOException3.printStackTrace();
            }
          }
        }
        catch (IOException localIOException2)
        {
          int m;
          continue;
          j = m;
        }
      return i;
    }

    private String unzip(File paramFile)
      throws IOException
    {
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(paramFile)));
      InstallService.DLProgress localDLProgress = new InstallService.DLProgress(2, 0, 440320000);
      String str = paramFile.getAbsolutePath().replace(".gz", "");
      FileOutputStream localFileOutputStream = new FileOutputStream(str);
      byte[] arrayOfByte = new byte[32768];
      int i = 0;
      int j = 0;
      while (true)
      {
        int k = localBufferedInputStream.read(arrayOfByte);
        if (k <= 0)
        {
          localBufferedInputStream.close();
          localFileOutputStream.close();
          paramFile.delete();
          return str;
        }
        i += k;
        localFileOutputStream.write(arrayOfByte, 0, k);
        int m = j + 1;
        if (j > 60)
        {
          j = 0;
          localDLProgress.setBytesDone(i);
          InstallService.DLProgress[] arrayOfDLProgress = new InstallService.DLProgress[1];
          arrayOfDLProgress[0] = localDLProgress;
          publishProgress(arrayOfDLProgress);
        }
        else
        {
          j = m;
        }
      }
    }

    // ERROR //
    protected Integer doInBackground(InstallService.DlStartData[] paramArrayOfDlStartData)
    {
      //
    }

    protected void onCancelled()
    {
      try
      {
        this.response.close();
        return;
      }
      catch (IOException localIOException)
      {
        while (true)
          localIOException.printStackTrace();
      }
      catch (NullPointerException localNullPointerException)
      {
        while (true)
          localNullPointerException.printStackTrace();
      }
    }

    protected void onPostExecute(Integer paramInteger)
    {
      InstallService.this.status = 2;
      InstallService.this.dlstatus = paramInteger.intValue();
      InstallService.this.broadcastStatus();
      InstallService.this.onFinish();
    }

    protected void onProgressUpdate(InstallService.DLProgress[] paramArrayOfDLProgress)
    {
      InstallService.this.status = 1;
      InstallService.this.dlProgress = paramArrayOfDLProgress[0];
      int i = this.statusUpdate;
      this.statusUpdate = (i + 1);
      if (i > 71)
      {
        this.statusUpdate = 0;
        InstallService.this.notification.contentView.setProgressBar(2131230764, InstallService.access$3(InstallService.this).bytesTotal, InstallService.access$3(InstallService.this).bytesDone, false);
        InstallService.this.notificationManager.notify(1, InstallService.this.notification);
      }
      InstallService.this.broadcastStatus();
    }

    public void publishDLProgress(InstallService.DLProgress paramDLProgress)
    {
      InstallService.DLProgress[] arrayOfDLProgress = new InstallService.DLProgress[1];
      arrayOfDLProgress[0] = paramDLProgress;
      publishProgress(arrayOfDLProgress);
    }
  }
}
