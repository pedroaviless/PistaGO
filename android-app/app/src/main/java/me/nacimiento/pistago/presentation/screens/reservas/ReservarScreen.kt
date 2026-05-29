package me.nacimiento.pistago.presentation.screens.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.nacimiento.pistago.presentation.viewmodel.PistaViewModel
import me.nacimiento.pistago.presentation.viewmodel.ReservaViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Franjas horarias de 1:30 (9 tramos: 5 mañana, pausa 16:00-16:30, 4 tarde)
private val FRANJAS = listOf(
    "08:30" to "10:00",
    "10:00" to "11:30",
    "11:30" to "13:00",
    "13:00" to "14:30",
    "14:30" to "16:00",
    "16:30" to "18:00",
    "18:00" to "19:30",
    "19:30" to "21:00",
    "21:00" to "22:30"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservarScreen(
    pistaId: Long,
    onReservaCreada: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReservaViewModel = hiltViewModel(),
    pistaViewModel: PistaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val today = LocalDate.now(ZoneId.of("Europe/Madrid"))
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDate by remember { mutableStateOf(today) }
    var selectedHora by remember { mutableStateOf<String?>(null) }

    val nombrePista = pistaViewModel.pistaSeleccionada?.nombre ?: "Pista $pistaId"

    LaunchedEffect(pistaId) {
        pistaViewModel.getPistaById(pistaId)
    }

    val dias = (0..6).map { today.plusDays(it.toLong()) }

    LaunchedEffect(uiState.reservaCreada) {
        if (uiState.reservaCreada != null) onReservaCreada()
    }

    LaunchedEffect(uiState.apuntadoListaEspera) {
        if (uiState.apuntadoListaEspera) {
            snackbarHostState.showSnackbar("¡Apuntado a la lista de espera!")
            viewModel.clearError()
        }
    }

    LaunchedEffect(selectedDate, pistaId) {
        viewModel.getDisponibilidad(selectedDate.toString(), pistaId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reservar pista",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = nombrePista,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Selecciona el día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dias.forEach { dia ->
                    val isSelected = dia == selectedDate
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                selectedDate = dia
                                selectedHora = null
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = dia.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedDate.format(
                        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es"))
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selecciona la franja",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Calcular qué franjas están "ya pasadas" (solo aplica al día de hoy)
            val ahora = LocalTime.now(ZoneId.of("Europe/Madrid"))
            val esHoy = selectedDate == today

            FRANJAS.forEach { (horaInicio, horaFin) ->
                val ocupada = uiState.horasOcupadas.contains(horaInicio)
                val pasada = esHoy && LocalTime.parse(horaInicio).isBefore(ahora)
                val isSelected = horaInicio == selectedHora

                FranjaItem(
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    ocupada = ocupada,
                    pasada = pasada,
                    seleccionada = isSelected,
                    onClick = {
                        when {
                            pasada -> { /* no hacer nada */ }
                            ocupada -> {
                                val fechaHora = "${selectedDate}T${horaInicio}:00"
                                viewModel.apuntarseListaEspera(pistaId, fechaHora)
                            }
                            else -> selectedHora = horaInicio
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val fechaHora = "${selectedDate}T${selectedHora}:00"
                    viewModel.crearReserva(pistaId, fechaHora, duracionMin = 90)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading && selectedHora != null,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (selectedHora != null) "CONFIRMAR — $selectedHora" else "SELECCIONA UNA FRANJA",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FranjaItem(
    horaInicio: String,
    horaFin: String,
    ocupada: Boolean,
    pasada: Boolean,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    // Colores según estado
    val (bgColor, contentColor, borderColor) = when {
        pasada -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            Color.Transparent
        )
        ocupada -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Color.Transparent
        )
        seleccionada -> Triple(
            MaterialTheme.colorScheme.primary,
            Color.White,
            Color.Transparent
        )
        else -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(enabled = !pasada, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según estado
        val icon = when {
            pasada -> Icons.Default.Lock
            ocupada -> Icons.Default.HourglassEmpty
            seleccionada -> Icons.Default.Check
            else -> null
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$horaInicio  —  $horaFin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            val estado = when {
                pasada -> "Hora no disponible"
                ocupada -> "Ocupada · toca para apuntarte a la lista de espera"
                seleccionada -> "Seleccionada"
                else -> "Disponible"
            }
            Text(
                text = estado,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.85f)
            )
        }
    }
}