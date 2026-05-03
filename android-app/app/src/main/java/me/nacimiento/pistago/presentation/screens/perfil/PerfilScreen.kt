package me.nacimiento.pistago.presentation.screens.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import me.nacimiento.pistago.R
import me.nacimiento.pistago.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var loggedOut by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Selector de imagen (PhotoPicker Android 13+, fallback en versiones anteriores)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.subirFotoPerfil(uri)
        }
    }

    LaunchedEffect(uiState.usuario, uiState.isLoading) {
        if (!uiState.isLoading && uiState.usuario == null && loggedOut) onLogout()
    }

    LaunchedEffect(Unit) { viewModel.getPerfil() }

    LaunchedEffect(uiState.perfilActualizado) {
        if (uiState.perfilActualizado) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            viewModel.clearMensajes()
        }
    }

    LaunchedEffect(uiState.passwordActualizada) {
        if (uiState.passwordActualizada) {
            snackbarHostState.showSnackbar("Contraseña actualizada correctamente")
            viewModel.clearMensajes()
        }
    }

    LaunchedEffect(uiState.fotoSubida) {
        if (uiState.fotoSubida) {
            snackbarHostState.showSnackbar("Foto actualizada correctamente")
            viewModel.clearMensajes()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMensajes()
        }
    }

    if (showEditDialog) {
        EditarPerfilDialog(
            nombre = uiState.usuario?.nombre ?: "",
            telefono = uiState.telefono ?: "",
            onDismiss = { showEditDialog = false },
            onConfirm = { nombre, telefono ->
                viewModel.actualizarPerfil(nombre, telefono.ifBlank { null })
                showEditDialog = false
            }
        )
    }

    if (showPasswordDialog) {
        CambiarPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { actual, nueva ->
                viewModel.cambiarPassword(actual, nueva)
                showPasswordDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi perfil",
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header verde con avatar clickable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable(enabled = !uiState.isLoading) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.fotoUrl != null) {
                            // Foto real desde URL
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uiState.fotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            // Placeholder con logo
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pistago_logo),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        // Indicador de carga sobre el avatar
                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                            }
                        }

                        // Icono de cámara en la esquina inferior derecha
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.usuario?.let {
                        Text(
                            text = it.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = it.rol,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.usuario?.let { usuario ->
                        PerfilInfoRow(Icons.Default.Person, "Nombre", usuario.nombre)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PerfilInfoRow(Icons.Default.Email, "Email", usuario.email)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PerfilInfoRow(
                            Icons.Default.Phone,
                            "Teléfono",
                            uiState.telefono ?: "No especificado"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PerfilInfoRow(Icons.Default.Shield, "Rol", usuario.rol)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar perfil", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar contraseña", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        loggedOut = true
                        viewModel.logout()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PerfilInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EditarPerfilDialog(
    nombre: String,
    telefono: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nombreEdit by remember { mutableStateOf(nombre) }
    var telefonoEdit by remember { mutableStateOf(telefono) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombreEdit,
                    onValueChange = { nombreEdit = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = telefonoEdit,
                    onValueChange = { telefonoEdit = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombreEdit, telefonoEdit) },
                enabled = nombreEdit.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun CambiarPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    val noCoinciden = passwordNueva.isNotBlank() && passwordConfirm.isNotBlank() && passwordNueva != passwordConfirm
    val muyCorta = passwordNueva.isNotBlank() && passwordNueva.length < 8

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = passwordActual,
                    onValueChange = { passwordActual = it },
                    label = { Text("Contraseña actual") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = passwordNueva,
                    onValueChange = { passwordNueva = it },
                    label = { Text("Nueva contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = muyCorta,
                    supportingText = if (muyCorta) {
                        { Text("Mínimo 8 caracteres") }
                    } else null
                )
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = { Text("Confirmar contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = noCoinciden,
                    supportingText = if (noCoinciden) {
                        { Text("Las contraseñas no coinciden") }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(passwordActual, passwordNueva) },
                enabled = passwordActual.isNotBlank() &&
                        passwordNueva.isNotBlank() &&
                        !noCoinciden &&
                        !muyCorta
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}