package com.example.administrator.locationupdates;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Administrator on 4/11/2018.
 */

public class FirebaseJobDispatcher extends JobService {
    @Override
    public boolean onStartJob(final JobParameters job) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                completeJob(job);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    public void completeJob(final JobParameters parameters) {
        try {
            Log.e("JobDispatcher", "JobStarted");
            Thread.sleep(2000);
            Log.e("JobDispatcher", "JobFinished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            jobFinished(parameters, false);
        }
    }
}
