package me.nacimiento.pistago.presentation.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PISTAS = "pistas"
    const val RESERVAR = "reservar/{pistaId}"
    const val MIS_RESERVAS = "mis_reservas"
    const val PERFIL = "perfil"

    fun reservar(pistaId: Long) = "reservar/$pistaId"
}