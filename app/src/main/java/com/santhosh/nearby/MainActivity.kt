package com.santhosh.nearby

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.santhosh.nearby.view.MainFragment

class MainActivity : AppCompatActivity() {

    companion object {
        public const val API_KEY = "<YOUR_API_KEY>"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }

}
