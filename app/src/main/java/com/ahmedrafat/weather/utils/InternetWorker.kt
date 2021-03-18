package com.ahmedrafat.weather.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import javax.inject.Inject

class InternetWorker @Inject constructor(context: Context,
                                         workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return Result.success()
    }
}