package com.example.smartswitch
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel

object Zones {
    val coordinatesAmphiAda = arrayOf(
        Coordinate(4.888942850, 43.909750019),
        Coordinate(4.888802034, 43.909597363),
        Coordinate(4.888951567, 43.909536011),
        Coordinate(4.889077631, 43.909687701),
        Coordinate(4.888942850, 43.909750019) // fermer le polygone
    )

    val coordinatesAmphiBlaise = arrayOf(
        Coordinate(4.889077631, 43.909687701),
        Coordinate(4.889243258, 43.909615721),
        Coordinate(4.889095736, 43.909443258),
        Coordinate(4.888936815, 43.909513789),
        Coordinate(4.889077631, 43.909687701) // fermer le polygone
    )

    val coordinatesAccueil = arrayOf(
        Coordinate(4.889243258, 43.909615721),
        Coordinate(4.889420981, 43.909531167),
        Coordinate(4.889367337, 43.909469331),
        Coordinate(4.889186958, 43.909549524),
        Coordinate(4.889243258, 43.909615721) // fermer le polygone
    )

    val coordinatesCafeteria = arrayOf(
        Coordinate(4.889347192, 43.909567886),
        Coordinate(4.889390777, 43.909620059),
        Coordinate(4.889473256, 43.909583827),
        Coordinate(4.889420282, 43.909531654),
        Coordinate(4.889347192, 43.909567886) // fermer le polygone
    )

    val coordinatesDebutCouloir = arrayOf(
        Coordinate(4.889307630, 43.909586243),
        Coordinate(4.889347192, 43.909567886),
        Coordinate(4.889502090, 43.909754841),
        Coordinate(4.889467221, 43.909767884),
        Coordinate(4.889307630, 43.909586243) // fermer le polygone
    )

    val coordinatesMilieuCouloir = arrayOf(
        Coordinate(4.889599326, 43.909928268),
        Coordinate(4.889635531, 43.909913294),
        Coordinate(4.889502090, 43.909754841),
        Coordinate(4.889467221, 43.909767884),
        Coordinate(4.889599326, 43.909928268) // fermer le polygone
    )

    val coordinatesFinCouloir = arrayOf(
        Coordinate(4.889728067, 43.910082857),
        Coordinate(4.889770982, 43.910063534),
        Coordinate(4.889599326, 43.909928268),
        Coordinate(4.889635531, 43.909913294),
        Coordinate(4.889728067, 43.910082857) // fermer le polygone
    )

    val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    val polygonAmphiAda = geometryFactory.createPolygon(coordinatesAmphiAda)
    val polygonAmphiBlaise = geometryFactory.createPolygon(coordinatesAmphiBlaise)
    val polygonAccueil = geometryFactory.createPolygon(coordinatesAccueil)
    val polygonCafeteria = geometryFactory.createPolygon(coordinatesCafeteria)
    val polygonDebutCouloir = geometryFactory.createPolygon(coordinatesDebutCouloir)
    val polygonMilieuCouloir = geometryFactory.createPolygon(coordinatesMilieuCouloir)
    val polygonFinCouloir = geometryFactory.createPolygon(coordinatesFinCouloir)

    val dictionnaireZones = mapOf(0 to polygonAmphiAda, 1 to polygonAmphiBlaise, 2 to polygonAccueil, 3 to polygonCafeteria, 4 to polygonDebutCouloir, 5 to polygonMilieuCouloir, 6 to polygonFinCouloir)
}