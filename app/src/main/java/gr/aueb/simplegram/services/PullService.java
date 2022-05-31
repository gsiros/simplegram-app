package gr.aueb.simplegram.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class PullService extends Service {


    Context context = this;
    Handler handler = null;
    static Runnable runnable = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "PullService created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                handler.postDelayed(runnable, 10000);
                // TODO: implement usernode's pull service here.
            }
        };
        handler.postDelayed(runnable, 15000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // We destroy the service if the app gets destroyed.
        handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }
}
