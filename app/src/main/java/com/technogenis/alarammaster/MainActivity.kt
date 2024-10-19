package com.technogenis.alarammaster

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.technogenis.alarammaster.services.AppLifecycleObserver
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_EXACT_ALARM = 1001

    private lateinit var btnSetAlarm: Button
    private lateinit var setNormalRingButton: Button
    private lateinit var backImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var notificationManager: NotificationManager
    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: TimePicker

    private lateinit var lifecycleObserver: AppLifecycleObserver

    override fun onResume() {
        super.onResume()
        // App has come to the foreground
        Log.d("MainActivity", "App in Foreground")
    }

    override fun onPause() {
        super.onPause()
        // App is moving to the background
        Log.d("MainActivity", "App in Background")
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleObserver = AppLifecycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        btnSetAlarm = findViewById(R.id.btnSetAlarm)
        setNormalRingButton = findViewById(R.id.setNormalRingButton)
        backImage = findViewById(R.id.backImage)
        datePicker = findViewById(R.id.datePicker)
        timePicker = findViewById(R.id.timePicker)
        tvTitle = findViewById(R.id.tvTitle)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        backImage.setOnClickListener {
           onBackPressedDispatcher.onBackPressed()
        }

        val intent = intent

        // Retrieve the data using the keys you set before
        val title = intent.getStringExtra("title")
        val mode = intent.getStringExtra("mode")


        if(mode == "silent") {
            tvTitle.text = "Set Silent Alarm"
            btnSetAlarm.visibility = View.VISIBLE
            setNormalRingButton.visibility = View.GONE
        }else{
            tvTitle.text = "Set Ringing Alarm"
            btnSetAlarm.visibility = View.GONE
            setNormalRingButton.visibility = View.VISIBLE
        }


        checkExactAlarmPermission()
        btnSetAlarm.setOnClickListener {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                setAlarm("SILENT_MODE")
            } else {
                requestDoNotDisturbPermission()
            }
        }

        setNormalRingButton.setOnClickListener {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                setAlarm("NORMAL_MODE")
            } else {
                requestDoNotDisturbPermission()
            }
        }


    }

    private fun setAlarm(mode: String) {
        val calendar = Calendar.getInstance()
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth, timePicker.currentHour, timePicker.currentMinute, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please set a future time.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = mode
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        Toast.makeText(this, "Alarm set for $mode", Toast.LENGTH_SHORT).show()
    }


    private fun showDateTimePicker(mode: String) {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
//                setAlarm(calendar.timeInMillis, mode) // Pass the mode to setAlarm
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

            timePickerDialog.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }



//    private fun setAlarm(timeInMillis: Long, mode: String) {
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(this, AlarmReceiver::class.java).apply {
//            putExtra("mode", mode) // Pass the mode (silent or normal)
//        }
//        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
//        Toast.makeText(this, "Alarm set for $mode mode", Toast.LENGTH_SHORT).show()
//    }

    // Request Do Not Disturb (DND) permission
    private fun requestDoNotDisturbPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission("android.permission.SCHEDULE_EXACT_ALARM") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf("android.permission.SCHEDULE_EXACT_ALARM"), REQUEST_CODE_EXACT_ALARM)
            }
        }
    }

    // Handle the permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_EXACT_ALARM) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Exact Alarm permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "Exact Alarm permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}