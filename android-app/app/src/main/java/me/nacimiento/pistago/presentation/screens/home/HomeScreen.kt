package me.nacimiento.pistago.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.nacimiento.pistago.presentation.viewmodel.AuthViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import me.nacimiento.pistago.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPistas: () -> Unit,
    onNavigateToMisReservas: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToListaEspera: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val esAdmin = authState.usuario?.rol == "ADMINISTRADOR"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.pistago_symbol),
                            contentDescription = "PistaGO",
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PistaGO",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.SportsTennis, contentDescription = null) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToMisReservas()
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Reservas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToListaEspera()
                    },
                    icon = { Icon(Icons.Default.HourglassEmpty, contentDescription = null) },
                    label = { Text("Espera") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onNavigateToPerfil()
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
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
            Image(
                painter = painterResource(id = R.drawable.portada_pistago),
                contentDescription = "PistaGO",
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido${authState.usuario?.nombre?.let { ", $it" } ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reserva tu pista de tenis",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onNavigateToPistas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Default.SportsTennis, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reservar pista", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToMisReservas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mis reservas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (esAdmin) {
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    onClick = onNavigateToAdmin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1B5E20),
                                        Color(0xFF2E7D32)
                                    )
                                )
                            )
                    ) {
                        // Arco lima decorativo de fondo (esquina derecha)
                        Canvas(
                            modifier = Modifier.matchParentSize()
                        ) {
                            val diametro = size.height * 1.8f
                            drawArc(
                                color = Color(0xFFC6FF00).copy(alpha = 0.55f),
                                startAngle = 0f,
                                sweepAngle = 280f,
                                useCenter = false,
                                topLeft = Offset(
                                    x = size.width - diametro * 0.55f,
                                    y = -diametro * 0.15f
                                ),
                                size = Size(diametro, diametro),
                                style = Stroke(width = 5f)
                            )
                        }

                        // Contenido
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Panel de administración",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gestiona pistas, reservas y socios",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                            }

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}