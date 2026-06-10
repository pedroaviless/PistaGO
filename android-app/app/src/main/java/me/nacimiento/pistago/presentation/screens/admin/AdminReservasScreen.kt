package me.nacimiento.pistago.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.nacimiento.pistago.domain.model.Reserva
import me.nacimiento.pistago.presentation.viewmodel.ReservaViewModel
import java.time.LocalDateTime
import java.time.ZoneId

private enum class FiltroEstado(val etiqueta: String, val valor: String?) {
    TODAS("Todas", null),
    CONFIRMADAS("Confirmadas", "CONFIRMADA"),
    CANCELADAS("Canceladas", "CANCELADA")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReservasScreen(
    onBack: () -> Unit = {},
    viewModel: ReservaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var filtro by remember { mutableStateOf(FiltroEstado.TODAS) }

    LaunchedEffect(Unit) { viewModel.getTodasLasReservas() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestión de Reservas",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Fila de filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroEstado.entries.forEach { opcion ->
                    val seleccionado = filtro == opcion
                    val total = when (opcion) {
                        FiltroEstado.TODAS -> uiState.reservas.size
                        else -> uiState.reservas.count { it.estado == opcion.valor }
                    }
                    FilterChip(
                        selected = seleccionado,
                        onClick = { filtro = opcion },
                        label = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = opcion.etiqueta,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "($total)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    )
                }
            }

            val reservasFiltradas = when (filtro.valor) {
                null -> uiState.reservas
                else -> uiState.reservas.filter { it.estado == filtro.valor }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                    reservasFiltradas.isEmpty() ->
                        Text(
                            text = when (filtro) {
                                FiltroEstado.TODAS -> "No hay reservas"
                                FiltroEstado.CONFIRMADAS -> "No hay reservas confirmadas"
                                FiltroEstado.CANCELADAS -> "No hay reservas canceladas"
                            },
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    else ->
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(reservasFiltradas) { reserva ->
                                AdminReservaRow(
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
private fun AdminReservaRow(reserva: Reserva, onCancelar: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Cancelar la reserva de ${reserva.nombreUsuario} en ${reserva.nombrePista}?") },
            confirmButton = {
                TextButton(onClick = {
                    onCancelar()
                    showDialog = false
                }) {
                    Text("Cancelar reserva", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Volver") }
            }
        )
    }

    val esConfirmada = reserva.estado == "CONFIRMADA"
    val haExpirado = run {
        val ahora = LocalDateTime.now(ZoneId.of("Europe/Madrid"))
        // fechaHora viene como "2026-06-02T18:00:00", LocalDateTime.parse lo lee bien
        val fin = LocalDateTime.parse(reserva.fechaHora).plusMinutes(reserva.duracionMin.toLong())
        fin.isBefore(ahora)
    }
    val puedeCancelarse = esConfirmada && !haExpirado

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Línea 1: pista + estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = reserva.nombrePista,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(estado = reserva.estado)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Línea 2: usuario · fecha · hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconoTexto(
                    icon = Icons.Default.Person,
                    text = reserva.nombreUsuario,
                    modifier = Modifier.weight(1f)
                )
                IconoTexto(
                    icon = Icons.Default.CalendarToday,
                    text = reserva.fechaHora.substring(0, 10)
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconoTexto(
                    icon = Icons.Default.Schedule,
                    text = reserva.fechaHora.substring(11, 16)
                )
            }

            if (puedeCancelarse) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("CANCELAR RESERVA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun IconoTexto(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EstadoChip(estado: String) {
    val (bgColor, contentColor, icon, label) = when (estado) {
        "CONFIRMADA" -> Quad(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle,
            "Confirmada"
        )
        "CANCELADA" -> Quad(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Cancel,
            "Cancelada"
        )
        else -> Quad(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Schedule,
            estado
        )
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Helper para devolver 4 valores juntos (las component1..4 las genera data class). */
private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)