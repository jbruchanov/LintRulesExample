package com.scurab.lintruleexample

import android.os.Bundle
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var obj: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerSampleComponent().inject(this)
    }

    override fun inject() {
        DaggerSampleComponent().inject(this)
    }
}