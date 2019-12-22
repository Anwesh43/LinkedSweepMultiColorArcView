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
fun Float.cosify() : Float = 1f - Math.sin(Math.PI / 2 + this * (Math.PI / 2)).toFloat()

fun Canvas.drawColorArc(i : Int, scale : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify().divideScale(i, colors.size)
    val deg : Float = 360f / colors.size
    paint.style = Paint.Style.STROKE
    paint.color = Color.parseColor(colors[i])
    save()
    drawArc(RectF(-size, -size, size, size), deg * i, deg * sf, false, paint)
    restore()
}

fun Canvas.drawMainArc(scale : Float, size : Float, paint : Paint) {
    val sc : Float = scale.divideScale(1, 2).cosify()
    val deg : Float = 360f * sc
    paint.style = Paint.Style.FILL
    paint.color = mainColor
    drawArc(RectF(-size, -size, size, size), 360f - deg, deg, true, paint)
}

fun Canvas.drawSweepMultiColor(scale : Float, size : Float, paint : Paint) {
    for (j in 0..(colors.size - 1)) {
        drawColorArc(j, scale, size, paint)
    }
    drawMainArc(scale, size, paint)
}

fun Canvas.drawSMCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    save()
    translate(w / 2, gap * (i + 1))
    drawSweepMultiColor(scale, size, paint)
    restore()
}

class SweepMultiColorView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 0f) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }


    data class SMCNode(var i : Int, val state : State = State()) {

        private var next : SMCNode? = null
        private var prev : SMCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SMCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSMCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SMCNode {
            var curr : SMCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SweepMultiColorArc(var i : Int) {

        private val root : SMCNode = SMCNode(0)
        private var curr : SMCNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SweepMultiColorView) {

        private val scm : SweepMultiColorArc = SweepMultiColorArc(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            scm.draw(canvas, paint)
            animator.animate {
                scm.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            scm.startUpdating {
                animator.start()
            }
        }
    }
}

