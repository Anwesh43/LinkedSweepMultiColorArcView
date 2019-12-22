package com.anwesh.uiprojects.linkedsweepmulticolorview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.sweepmulticolorview.SweepMultiColorView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SweepMultiColorView.create(this)
    }
}
