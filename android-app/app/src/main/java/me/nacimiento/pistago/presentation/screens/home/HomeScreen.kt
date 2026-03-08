package me.nacimiento.pistago.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPistas: () -> Unit,
    onNavigateToMisReservas: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PistaGO") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¿Qué quieres hacer?",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            HomeButton(
                text = "Ver pistas",
                icon = { Icon(Icons.Default.SportsTennis, contentDescription = null) },
                onClick = onNavigateToPistas
            )

            Spacer(modifier = Modifier.height(16.dp))

            HomeButton(
                text = "Mis reservas",
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                onClick = onNavigateToMisReservas
            )

            Spacer(modifier = Modifier.height(16.dp))

            HomeButton(
                text = "Mi perfil",
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                onClick = onNavigateToPerfil
            )
        }
    }
}

@Composable
fun HomeButton(text: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}