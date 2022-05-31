package gr.aueb.simplegram.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import gr.aueb.simplegram.common.User;
import gr.aueb.simplegram.common.UserNode;

public class PullService extends Service {


    Context context = this;
    Handler handler = null;
    static Runnable runnable = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "PullService created!", Toast.LENGTH_LONG).show();

        runnable = new Runnable() {
            @Override
            public void run() {
                UserNode un = ((User) getApplication()).getUserNode();
                un.pull();
            }
        };
        new Thread(runnable).start();
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
