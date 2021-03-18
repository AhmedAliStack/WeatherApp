package com.ahmedrafat.weather.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.work.*
import com.ahmedrafat.weather.R
import com.ahmedrafat.weather.model.IMAGE_URL
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

fun loadImage(icon:String,imageView: ImageView){
    Picasso.get().load("$IMAGE_URL$icon.png").into(imageView);
}

fun blurLocalImage(context: Context,icon:Int,imageView: ImageView){
    Picasso.get().load(icon).transform(BlurTransformation(context,25,1)).into(imageView);
}

//fun isInternetConnected(context: Context) : Boolean{
//    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        val networkCapabilities = cm.activeNetwork ?: return false
//        val activeNetwork = cm.getNetworkCapabilities(networkCapabilities) ?: return false
//        return when {
//            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
//                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
//                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//            else -> false
//        }
//    }else{
//        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
//        return  activeNetwork?.isConnectedOrConnecting == true
//    }
//
//}

fun observeConnection(context: Context):LiveData<WorkInfo>{
    val workManager: WorkManager = WorkManager.getInstance(context)
    val connectedConstrains : Constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val connectedInternetWorker: OneTimeWorkRequest = OneTimeWorkRequest.Builder(InternetWorker::class.java).setConstraints(connectedConstrains).build()
    workManager.enqueue(connectedInternetWorker)
    return workManager.getWorkInfoByIdLiveData(connectedInternetWorker.id)
}