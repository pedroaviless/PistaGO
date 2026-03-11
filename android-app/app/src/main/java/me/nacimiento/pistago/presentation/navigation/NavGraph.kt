package me.nacimiento.pistago.presentation.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PISTAS = "pistas"
    const val RESERVAR = "reservar/{pistaId}"
    const val MIS_RESERVAS = "mis_reservas"
    const val PERFIL = "perfil"
    const val LISTA_ESPERA = "lista_espera"

    const val ADMIN_PISTAS = "admin_pistas"

    const val ADMIN_RESERVAS = "admin_reservas"

    const val ADMIN_MENU = "admin_menu"
    fun reservar(pistaId: Long) = "reservar/$pistaId"
}