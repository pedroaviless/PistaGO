package me.nacimiento.pistago.presentation.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.nacimiento.pistago.domain.model.DiaSemanaStat
import me.nacimiento.pistago.domain.model.Estadisticas
import me.nacimiento.pistago.domain.model.PistaStat
import me.nacimiento.pistago.domain.model.UsuarioStat
import me.nacimiento.pistago.presentation.viewmodel.EstadisticasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    onBack: () -> Unit = {},
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estadísticas",
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                uiState.error != null ->
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )

                uiState.datos != null ->
                    ContenidoEstadisticas(uiState.datos!!)
            }
        }
    }
}

@Composable
private fun ContenidoEstadisticas(d: Estadisticas) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // === Resumen: 4 tarjetas en grid 2x2 ===
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen(
                    icon = Icons.Default.Group,
                    titulo = "Usuarios",
                    valor = d.totalUsuarios.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TarjetaResumen(
                    icon = Icons.Default.CalendarMonth,
                    titulo = "Reservas",
                    valor = d.totalReservas.toString(),
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen(
                    icon = Icons.Default.EventAvailable,
                    titulo = "Hoy",
                    valor = d.reservasHoy.toString(),
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.weight(1f)
                )
                TarjetaResumen(
                    icon = Icons.Default.Cancel,
                    titulo = "% Cancelación",
                    valor = "${d.tasaCancelacion}%",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // === Top 3 pistas ===
        SeccionTitulo("Top pistas más reservadas")
        if (d.topPistas.isEmpty()) {
            TextoVacio("Aún no hay reservas confirmadas")
        } else {
            val maxValor = d.topPistas.maxOf { it.totalReservas }.coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                d.topPistas.forEachIndexed { idx, pista ->
                    FilaPistaConBarra(
                        posicion = idx + 1,
                        pista = pista,
                        proporcion = pista.totalReservas.toFloat() / maxValor
                    )
                }
            }
        }

        // === Top 3 usuarios ===
        SeccionTitulo("Top usuarios más activos")
        if (d.topUsuarios.isEmpty()) {
            TextoVacio("Aún no hay reservas confirmadas")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                d.topUsuarios.forEachIndexed { idx, usuario ->
                    FilaUsuario(posicion = idx + 1, usuario = usuario)
                }
            }
        }

        // === Gráfico por día de la semana ===
        SeccionTitulo("Reservas por día de la semana")
        if (d.reservasPorDiaSemana.isEmpty()) {
            TextoVacio("Aún no hay reservas confirmadas")
        } else {
            GraficoBarrasDiaSemana(datos = d.reservasPorDiaSemana)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ====== Componentes ======

@Composable
private fun TarjetaResumen(
    icon: ImageVector,
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = valor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun TextoVacio(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = texto,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilaPistaConBarra(
    posicion: Int,
    pista: PistaStat,
    proporcion: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgePosicion(posicion)
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.SportsTennis,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pista.nombrePista,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${pista.totalReservas}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(proporcion)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun FilaUsuario(posicion: Int, usuario: UsuarioStat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgePosicion(posicion)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = usuario.nombreUsuario,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${usuario.totalReservas} reservas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BadgePosicion(posicion: Int) {
    val color = when (posicion) {
        1 -> Color(0xFFFFB300)   // oro
        2 -> Color(0xFF9E9E9E)   // plata
        3 -> Color(0xFFCD7F32)   // bronce
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(50))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$posicion",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GraficoBarrasDiaSemana(datos: List<DiaSemanaStat>) {
    // Aseguramos que aparezcan los 7 días en orden lunes→domingo,
    // aunque el backend solo devuelva los días con reservas.
    val ordenDias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val mapaDatos = datos.associate { it.dia to it.total }
    val datosOrdenados = ordenDias.map { dia -> dia to (mapaDatos[dia] ?: 0L) }

    val maxValor = datosOrdenados.maxOf { it.second }.coerceAtLeast(1)
    val colorPrimario = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Canvas para las barras
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                dibujarBarras(
                    valores = datosOrdenados.map { it.second },
                    maxValor = maxValor,
                    colorBarra = colorPrimario
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Etiquetas debajo de las barras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                datosOrdenados.forEach { (dia, total) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = total.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dia.take(3), // Lun, Mar, Mié, Jue, Vie, Sáb, Dom
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.dibujarBarras(
    valores: List<Long>,
    maxValor: Long,
    colorBarra: Color
) {
    val n = valores.size
    val anchoTotal = size.width
    val altoTotal = size.height
    val anchoSeccion = anchoTotal / n
    val anchoBarra = anchoSeccion * 0.55f
    val radioEsquina = 8f

    valores.forEachIndexed { idx, valor ->
        val alturaBarra = (valor.toFloat() / maxValor) * altoTotal
        val x = idx * anchoSeccion + (anchoSeccion - anchoBarra) / 2
        val y = altoTotal - alturaBarra

        // Sombra suave de fondo (barra de altura completa muy clara)
        drawRoundRectCompat(
            color = colorBarra.copy(alpha = 0.1f),
            topLeft = Offset(x, 0f),
            size = Size(anchoBarra, altoTotal),
            cornerRadius = radioEsquina
        )

        // Barra real
        if (valor > 0) {
            drawRoundRectCompat(
                color = colorBarra,
                topLeft = Offset(x, y),
                size = Size(anchoBarra, alturaBarra),
                cornerRadius = radioEsquina
            )
        }
    }
}

private fun DrawScope.drawRoundRectCompat(
    color: Color,
    topLeft: Offset,
    size: Size,
    cornerRadius: Float
) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
}