package me.nacimiento.pistago.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.nacimiento.pistago.domain.model.Pista
import me.nacimiento.pistago.presentation.viewmodel.PistaViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import me.nacimiento.pistago.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPistasScreen(
    onBack: () -> Unit = {},
    viewModel: PistaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCrearDialog by remember { mutableStateOf(false) }
    var pistaEditar by remember { mutableStateOf<Pista?>(null) }

    LaunchedEffect(Unit) { viewModel.loadTodasLasPistas() }

    if (showCrearDialog) {
        PistaDialog(
            pista = null,
            onDismiss = { showCrearDialog = false },
            onConfirm = { nombre, tipo, descripcion ->
                viewModel.crearPista(nombre, tipo, descripcion)
                showCrearDialog = false
            }
        )
    }

    pistaEditar?.let { pista ->
        PistaDialog(
            pista = pista,
            onDismiss = { pistaEditar = null },
            onConfirm = { nombre, tipo, descripcion ->
                viewModel.actualizarPista(pista.id, nombre, tipo, descripcion)
                pistaEditar = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestión de Pistas",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCrearDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir pista", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.pistas.isEmpty() -> Text("No hay pistas", modifier = Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.pistas) { pista ->
                            AdminPistaCard(
                                pista = pista,
                                onEditar = { pistaEditar = pista },
                                onToggleActiva = { viewModel.setActivaPista(pista.id, !pista.activa) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPistaCard(pista: Pista, onEditar: () -> Unit, onToggleActiva: () -> Unit) {
    val imageRes = if (pista.tipo == "TIERRA_BATIDA") {
        R.drawable.pista_tierra
    } else {
        R.drawable.pista_dura
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = pista.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
                // Badge activa/inactiva
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(50),
                    color = if (pista.activa) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (pista.activa) "Activa" else "Inactiva",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pista.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pista.tipo.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    pista.descripcion?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = pista.activa,
                    onCheckedChange = { onToggleActiva() }
                )
            }
        }
    }
}

@Composable
fun PistaDialog(
    pista: Pista?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(pista?.nombre ?: "") }
    var tipo by remember { mutableStateOf(pista?.tipo ?: "TIERRA_BATIDA") }
    var descripcion by remember { mutableStateOf(pista?.descripcion ?: "") }
    val tipos = listOf("TIERRA_BATIDA", "PISTA_DURA")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pista == null) "Nueva pista" else "Editar pista") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Tipo de pista", style = MaterialTheme.typography.labelMedium)
                tipos.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = tipo == t, onClick = { tipo = t })
                        Text(t.replace("_", " "))
                    }
                }
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombre, tipo, descripcion.ifBlank { null }) },
                enabled = nombre.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
    tipos.forEach { t ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = tipo == t, onClick = { tipo = t })
            Text(t.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
        }
    }
}