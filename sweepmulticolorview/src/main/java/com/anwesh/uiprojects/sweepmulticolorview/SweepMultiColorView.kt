package com.anwesh.uiprojects.sweepmulticolorview

/**
 * Created by anweshmishra on 22/12/19.
 */

import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Color
import android.view.View
import android.view.MotionEvent

val nodes : Int = 5
val colors : Array<String> = arrayOf("#673AB7", "#311B92", "#33691E")
val mainColor : Int = Color.parseColor("#009688")
val scGap : Float = 0.02f
val sizeFactor : Float = 2.9f
val delay : Long = 30
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
