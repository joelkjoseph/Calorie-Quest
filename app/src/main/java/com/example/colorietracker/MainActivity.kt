package com.example.colorietracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.colorietracker.ui.theme.ColorietrackerTheme
import com.example.colorietracker.ui.theme.SoftGreen
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE = 2
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 3
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "calorie-tracker_database"
        ).fallbackToDestructiveMigration().build()

        val factory = MainViewModelFactory(appDatabase.calorieDao())



        setContent {
            ColorietrackerTheme {
                val preferencesManager = PreferencesManager(applicationContext)
                val sharedViewModel: SharedViewModel = viewModel(factory = SharedViewModelFactory(preferencesManager))
                AppNavigation(factory, preferencesManager, sharedViewModel)
            }
        }
        checkAndRequestLocationPermission()
        checkAndRequestNotificationPermission()
        timeNotificationWork(this)
    }



@Composable
fun AppNavigation(factory: MainViewModelFactory, preferencesManager: PreferencesManager, sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(navController = navController, startDestination = "main", modifier = Modifier.padding(innerPadding)) {
            composable("main") {
                MainScreen(factory, navController, sharedViewModel)
            }
            composable("settings") {
                SettingsScreen(navController, preferencesManager, sharedViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(factory: MainViewModelFactory, navController: NavController, sharedViewModel: SharedViewModel){
    LocalContext.current
    val viewModelStoreOwner = LocalViewModelStoreOwner.current ?: throw IllegalStateException("No ViewModelStoreOwner was provided")

    val mainViewModel: MainViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = factory
    )
    val calorieGoal by sharedViewModel.calorieGoal.collectAsState()

    Scaffold(
        topBar = {
            val topAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = SoftGreen,
                titleContentColor = Color.Black,
                actionIconContentColor = Color.White
            )

            CenterAlignedTopAppBar(
                title = { Text("CalorieQuest") },
                colors = topAppBarColors,
                actions = {
                    var showMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { navController.navigate("settings") },
                            leadingIcon = {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        MainContent(mainViewModel = mainViewModel, navController = navController, calorieGoal = calorieGoal, modifier = Modifier.padding(0.dp))
    }
}

@Composable
fun MainContent(
    mainViewModel: MainViewModel,
    navController: NavController,
    calorieGoal: Int,
    modifier: Modifier = Modifier
) {
    var calorieInput by remember { mutableStateOf("") }
    val totalCalories by mainViewModel.totalCalories.collectAsState()


    Column(modifier = modifier.offset(30.dp,80.dp)) {
        Text("Enter your calorie intake:", modifier = Modifier.padding(bottom = 10.dp))
        OutlinedTextField(
            value = calorieInput,
            onValueChange = { calorieInput = it },
            label = { Text("Calories") },
            singleLine = true,
            modifier = Modifier.padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Column(modifier = modifier.offset(90.dp)) {
            Button(
                onClick = {
                    mainViewModel.addCalories(calorieInput.toIntOrNull() ?: 0)
                    calorieInput = ""
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftGreen,
                    contentColor = Color.Black
                )

            ) {
                Text("Add ")
                Icon(Icons.Filled.Add, contentDescription = "Add")

            }
        }
        Column(modifier = modifier.offset(10.dp)){
        // Displaying the total calories and progress
        Text("Total calories today:$totalCalories", modifier = Modifier.padding(top = 30.dp))
        val progress = if (calorieGoal != 0) {
            (totalCalories.toFloat() / calorieGoal) * 100
        } else {
            0f
        }
        Text("Goal progress :${"%.2f".format(progress)}%", modifier = Modifier.padding(top = 15.dp))

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, preferencesManager: PreferencesManager, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val factory = SettingsViewModelFactory(preferencesManager)

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = factory
    )

    val geofenceHelper = GeofenceHelper(context)
    val scope = rememberCoroutineScope()
    val calorieGoal by sharedViewModel.calorieGoal.collectAsState()
    var goal by remember { mutableStateOf(calorieGoal.toString()) }
    val savedGoal by settingsViewModel.userGoal.collectAsState(initial = 0)
    val motivationEnabled by settingsViewModel.motivationEnabled.collectAsState(initial = false)
    val abilityModeEnabled by settingsViewModel.abilityEnabled.collectAsState(initial = false)
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    val savedLatitude by settingsViewModel.storeLatitude.collectAsState(initial = 0.0)
    val savedLongitude by settingsViewModel.storeLongitude.collectAsState(initial = 0.0)

    LaunchedEffect(savedLatitude, savedLongitude) {
        latitude = savedLatitude.toString()
        longitude = savedLongitude.toString()
    }

    Scaffold(
        topBar = {
            val topAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = SoftGreen,
                titleContentColor = Color.Black,
                actionIconContentColor = Color.White
            )
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                colors = topAppBarColors,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .padding(innerPadding)) {

            Text("Calorie Goal:", color = Color.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = goal,
                onValueChange = { newValue ->
                    goal = newValue
                },
                label = { Text("Enter your target calorie intake") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Previously saved Calorie Goal: $savedGoal", color = Color.Black, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Toggle for High Motivation ")

                Switch(
                    checked = motivationEnabled,
                    onCheckedChange = { isEnabled ->
                        scope.launch {
                            settingsViewModel.saveMotivation(isEnabled)
                        }
                        val message = if (isEnabled) "High Motivation" else "Low Motivation"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                            colors = SwitchDefaults.colors(
                            checkedThumbColor = SoftGreen,
                            uncheckedThumbColor = Color.Black,
                            checkedTrackColor = SoftGreen.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.5f)
                )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Toggle for High Ability        ")

                Switch(
                    checked = abilityModeEnabled,
                    onCheckedChange = { isEnabled ->
                        scope.launch {
                            settingsViewModel.saveAbility(isEnabled)
                        }
                        val message = if (isEnabled) "High Ability" else "Low Ability"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } ,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SoftGreen,
                    uncheckedThumbColor = Color.Black,
                    checkedTrackColor = SoftGreen.copy(alpha = 0.5f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.5f)
                    )
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Favorite Food Store Location :", modifier = Modifier.padding(top = 16.dp, bottom = 10.dp))
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = {
                    scope.launch {
                        val lat = latitude.toDoubleOrNull()
                        val long = longitude.toDoubleOrNull()
                        if (lat != null && long != null) {
                            settingsViewModel.saveStoreLatitude(lat)
                            settingsViewModel.saveStoreLongitude(long)
                            geofenceHelper.setupGeofence(lat, long)
                        }

                        val newGoal = goal.toIntOrNull() ?: 0
                        sharedViewModel.saveGoal(newGoal)
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftGreen,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

        }}}

    private fun timeNotificationWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<TimeWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)

        val periodicWorkRequest = PeriodicWorkRequestBuilder<TimeWorker>(2, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "timeNotificationWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }




    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkAndRequestLocationPermission() {
        val foregroundLocationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val backgroundLocationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        val missingForegroundPermissions = foregroundLocationPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        val shouldRequestBackgroundLocationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, backgroundLocationPermission) != PackageManager.PERMISSION_GRANTED

        if (missingForegroundPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingForegroundPermissions, LOCATION_PERMISSION_REQUEST_CODE)
        }

        else if (shouldRequestBackgroundLocationPermission) {
            ActivityCompat.requestPermissions(this, arrayOf(backgroundLocationPermission), LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }

}





