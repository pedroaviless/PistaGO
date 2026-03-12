package me.nacimiento.pistago.presentation.screens.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
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
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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

    val today = LocalDate.now()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDate by remember { mutableStateOf(today) }
    var selectedHora by remember { mutableStateOf<String?>(null) }

    val franjas = listOf(
        "08:00", "09:00", "10:00", "11:00", "12:00",
        "13:00", "16:00", "17:00", "18:00", "19:00", "20:00"
    )

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
                text = "Selecciona la hora",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val franjaChunks = franjas.chunked(4)
            franjaChunks.forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fila.forEach { hora ->
                        val ocupada = uiState.horasOcupadas.contains(hora)
                        val isSelected = hora == selectedHora
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        ocupada -> MaterialTheme.colorScheme.error
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                )
                                .clickable {
                                    if (!ocupada) {
                                        selectedHora = hora
                                    } else {
                                        val fechaHora = "${selectedDate}T${hora}:00"
                                        viewModel.apuntarseListaEspera(pistaId, fechaHora)
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = hora,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ocupada || isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                if (ocupada) {
                                    Text(
                                        text = "+ espera",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    repeat(4 - fila.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val fechaHora = "${selectedDate}T${selectedHora}:00"
                    viewModel.crearReserva(pistaId, fechaHora)
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
                        text = if (selectedHora != null) "CONFIRMAR — $selectedHora" else "SELECCIONA UNA HORA",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}