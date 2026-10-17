package com.example.appproyecto.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date(millis))