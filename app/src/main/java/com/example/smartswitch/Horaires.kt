package com.example.smartswitch
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import java.time.LocalTime

object Horaires {
    val ranges = arrayOf(
        Pair(LocalTime.of(8, 30), LocalTime.of(11, 30)), // 8h30 -> 11h30
        Pair(LocalTime.of(11, 30), LocalTime.of(14, 30)), // 11h30 -> 14h30
        Pair(LocalTime.of(14, 30), LocalTime.of(16, 0)), // 14h30 -> 16h00
        Pair(LocalTime.of(16, 0), LocalTime.of(19, 0)) // 16h00 -> 19h00
    )

    val puissances = mapOf(
        LocalTime.of(8, 30) to listOf(-84, -78, -58, -60, -62, -75, -72),
        LocalTime.of(11, 30) to listOf(-81, -73, -64, -74, -80, -76, -65),
        LocalTime.of(14, 30) to listOf(-81, -78, -68, -73, -64, -76, -71),
        LocalTime.of(16, 0) to listOf(-80, -82, -57, -57, -78, -72, -63),
        LocalTime.of(19, 0) to listOf(-86, -76, -65, -67, -62, -56, -65)
    )
}