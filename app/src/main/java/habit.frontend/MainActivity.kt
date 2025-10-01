package habit.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habit.frontend.presentation.viewmodel.HabitViewModel
import habit.frontend.ui.theme.HabitTrackerTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import habit.frontend.data.api.HabitApi
import habit.frontend.data.repository.HabitRepository
import habit.frontend.domain.mapper.LocalDateAdapter
import habit.frontend.domain.mapper.LocalDateTimeAdapter
import habit.frontend.domain.model.Habit
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.LocalDate
import javax.inject.Singleton


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                val habitViewModel: HabitViewModel = hiltViewModel()
                HabitApp(viewModel = habitViewModel)
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HabitTrackerTheme {
        Greeting("Android")
    }
}
sealed class Screen(val route: String, val title: String) {
    object List : Screen("list", "Привычки")
    object Add : Screen("add", "Добавить")
    object About : Screen("about", "О приложении")
}
@Composable
fun HabitListScreen(viewModel: HabitViewModel, navController: androidx.navigation.NavHostController) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHabits() }

    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> Text("Ошибка: ${state.error}")
        else -> LazyColumn {
            items(state.habits) { habit ->
                HabitCard(
                    habit = habit,
                    onMarkDone = { id -> viewModel.markDone(id, null) },
                    onToggleDoneToday = { id -> viewModel.toggleDone(id, LocalDate.now().toString()) },
                    onEdit = { id -> navController.navigate("edit/$id") },
                    onDelete = { id -> viewModel.deleteHabitById(id) },
                    onDetail = { id -> navController.navigate("detail/$id") }
                )
            }
        }
    }
}



@Composable
fun AddHabitScreen(onSave: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = frequency,
            onValueChange = { frequency = it },
            label = { Text("Частота (раз/неделя)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (title.isNotBlank() && frequency.toIntOrNull() != null) {
                    onSave(title, description, frequency.toInt())
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Сохранить")
        }
    }
}


@Composable
fun AboutScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Habit Tracker", fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text("Приложение для отслеживания привычек.\nСоздано с использованием Jetpack Compose + Spring Boot.")
    }
}
@Composable
fun HabitApp(viewModel: HabitViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.List, Screen.Add, Screen.About)

    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry = navController.currentBackStackEntryAsState().value
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) },
                        icon = {},
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.List.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.List.route) {
                HabitListScreen(viewModel, navController)
            }
            composable(Screen.Add.route) {
                AddHabitScreen { title, desc, freq ->
                    viewModel.addHabit(title, desc, freq)
                    navController.navigate(Screen.List.route) {
                        popUpTo(Screen.List.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.About.route) { AboutScreen() }

            // новые экраны
            composable("edit/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLongOrNull()
                if (id != null) EditHabitScreen(id, viewModel, navController)
            }
            composable("detail/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLongOrNull()
                if (id != null) HabitDetailScreen(id, viewModel)
            }
        }
    }
}
@Composable
fun EditHabitScreen(
    habitId: Long,
    viewModel: HabitViewModel,
    navController: androidx.navigation.NavHostController
) {
    val state by viewModel.uiState.collectAsState()
    val habit = state.habits.find { it.id == habitId } ?: return Text("Привычка не найдена")

    var title by remember { mutableStateOf(habit.title) }
    var description by remember { mutableStateOf(habit.description) }
    var frequency by remember { mutableStateOf(habit.frequency.toString()) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") })
        OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Частота") })

        Button(
            onClick = {
                if (frequency.toIntOrNull() != null) {
                    viewModel.updateHabit(habit.id!!, title, description, frequency.toInt())
                    navController.popBackStack()
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onMarkDone: (Long) -> Unit,
    onToggleDoneToday: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onDetail: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .clickable { onDetail(habit.id!!) } // переход в detail при клике на название
                ) {
                    Text(habit.title, fontSize = 18.sp)
                    Text(habit.description, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${habit.completedDates.size} выполнено", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { onToggleDoneToday(habit.id!!) }) { Text("Toggle Today") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onMarkDone(habit.id!!) }) { Text("Mark Today") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onEdit(habit.id!!) }) { Text("Edit") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onDelete(habit.id!!) }) { Text("Delete") }
            }
        }
    }
}
@Composable
fun HabitDetailScreen(habitId: Long, viewModel: HabitViewModel) {
    val ui by viewModel.detailState.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val habit = state.habits.find { it.id == habitId }

    LaunchedEffect(habitId) { viewModel.loadStats(habitId) }

    if (habit == null) {
        Text("Привычка не найдена")
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(habit.title, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(habit.description, fontSize = 16.sp)

        Spacer(Modifier.height(16.dp))

        val stats = ui.stats
        when {
            ui.isLoading -> CircularProgressIndicator()
            ui.error != null -> Text("Ошибка: ${ui.error}")
            stats != null -> {
                Text("✅ Всего выполнено: ${stats.totalDone}")
                Text("📊 Успех: ${"%.1f".format(stats.successRate)}%")
                Text("🔥 Текущая серия: ${stats.currentStreak}")
                Text("🏆 Лучшая серия: ${stats.longestStreak}")

                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.toggleDone(habitId, LocalDate.now().toString()) }) {
                    Text("Toggle Today")
                }
            }
        }

    }
}



@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(): HabitApi {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(LocalDateAdapter())
            .add(LocalDateTimeAdapter())
            .build()

        return Retrofit.Builder()
            .baseUrl("http://83.136.235.215:5084/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HabitApi::class.java)
    }


    @Provides
    @Singleton
    fun provideRepository(api: HabitApi): HabitRepository = HabitRepository(api)
}






