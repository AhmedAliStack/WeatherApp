package com.ahmedrafat.weather.utils

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.ahmedrafat.weather.model.IMAGE_URL
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

fun loadImage(icon:String,imageView: ImageView){
    Picasso.get().load("$IMAGE_URL$icon.png").into(imageView);
}

fun blurLocalImage(context: Context,icon:Int,imageView: ImageView){
    Picasso.get().load(icon).transform(BlurTransformation(context,25,1)).into(imageView);
}