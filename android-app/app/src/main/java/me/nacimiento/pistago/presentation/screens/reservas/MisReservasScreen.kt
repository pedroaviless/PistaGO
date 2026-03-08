package me.nacimiento.pistago.presentation.screens.reservas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.nacimiento.pistago.domain.model.Reserva
import me.nacimiento.pistago.presentation.viewmodel.ReservaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(
    viewModel: ReservaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMisReservas()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis reservas") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.reservas.isEmpty() -> {
                    Text(
                        text = "No tienes reservas",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.reservas) { reserva ->
                            ReservaCard(
                                reserva = reserva,
                                onCancelar = { viewModel.cancelarReserva(reserva.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservaCard(reserva: Reserva, onCancelar: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Estás seguro de que quieres cancelar esta reserva?") },
            confirmButton = {
                TextButton(onClick = {
                    onCancelar()
                    showDialog = false
                }) { Text("Cancelar reserva") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Volver") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reserva.nombrePista,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reserva.fechaHora.replace("T", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val estadoColor = if (reserva.estado == "CONFIRMADA")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
                Text(
                    text = reserva.estado,
                    style = MaterialTheme.typography.bodySmall,
                    color = estadoColor
                )
            }
            if (reserva.estado == "CONFIRMADA") {
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancelar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}