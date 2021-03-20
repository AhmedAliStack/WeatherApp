package com.ahmedrafat.weather.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.work.*
import com.ahmedrafat.weather.model.IMAGE_URL
import com.ahmedrafat.weather.model.apimodel.ApiError
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit


fun loadImage(icon: String, imageView: ImageView){
    Picasso.get().load("$IMAGE_URL$icon.png").into(imageView)
}

fun blurLocalImage(context: Context, icon: Int, imageView: ImageView){
    Picasso.get().load(icon).transform(BlurTransformation(context, 25, 1)).into(imageView)
}


fun convertError(response: ResponseBody, retrofit: Retrofit): ApiError? {
    val converter:Converter<ResponseBody, ApiError> = retrofit.responseBodyConverter(
        ApiError::class.java, arrayOfNulls<Annotation>(
            0
        )
    )
    return converter.convert(response)
}

fun hideKeyboard(activity: Activity) {
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun observeConnection(context: Context):LiveData<WorkInfo>{
    val workManager: WorkManager = WorkManager.getInstance(context)
    val connectedConstrains : Constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val connectedInternetWorker: OneTimeWorkRequest = OneTimeWorkRequest.Builder(InternetWorker::class.java).setConstraints(
        connectedConstrains
    ).build()
    workManager.enqueue(connectedInternetWorker)
    return workManager.getWorkInfoByIdLiveData(connectedInternetWorker.id)
}

