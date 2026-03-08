package me.nacimiento.pistago.presentation.screens.reservas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.nacimiento.pistago.presentation.viewmodel.ReservaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservarScreen(
    pistaId: Long,
    onReservaCreada: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReservaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    LaunchedEffect(uiState.reservaCreada) {
        if (uiState.reservaCreada != null) onReservaCreada()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar pista") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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
                text = "Selecciona fecha y hora",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("2026-03-10") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = hora,
                onValueChange = { hora = it },
                label = { Text("Hora (HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("10:00") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val fechaHora = "${fecha}T${hora}:00"
                    viewModel.crearReserva(pistaId, fechaHora)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && fecha.isNotBlank() && hora.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Confirmar reserva")
                }
            }
        }
    }
}