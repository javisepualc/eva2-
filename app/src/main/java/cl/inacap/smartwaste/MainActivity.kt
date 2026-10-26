package cl.inacap.smartwaste

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import cl.inacap.smartwaste.ui.navigation.NavGraph
import cl.inacap.smartwaste.ui.theme.SmartWasteAppTheme
import cl.inacap.smartwaste.viewmodel.AuthViewModel
import cl.inacap.smartwaste.viewmodel.WasteBinViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val wasteBinViewModel: WasteBinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartWasteAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        wasteBinViewModel = wasteBinViewModel
                    )
                }
            }
        }
    }
}
