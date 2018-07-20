package com.santhosh.nearby.ui.main.utils

import android.graphics.PointF


object Nearby {

    fun getNearby(pointArray: ArrayList<PointF>): NearByPlace {
        pointArray.sortWith(Comparator { a, b -> if (a.x < b.x) -1 else if (a.x > b.x) 1 else 0 })

        return getMinDistance(pointArray, pointArray.size)
    }

    private fun getMinDistance(pointArray: ArrayList<PointF>, size: Int): NearByPlace {

        if (size <= 4) {
            return tradiitonalWay(pointArray, size)
        }

        val midIndex = size / 2
        val midPoint = pointArray[midIndex]

        val leftRange = ArrayList(pointArray.subList(0, midIndex))
        val rightRange = ArrayList(pointArray.subList(midIndex + 1, size))

        val minLeft = getMinDistance(leftRange, leftRange.size)
        val minRight = getMinDistance(rightRange, rightRange.size)

        val nearByPlace = when {
            minLeft.minDist < minRight.minDist -> minLeft
            else -> minRight
        }


        val minPointMap: ArrayList<PointF> = ArrayList()
        (0 until size)
                .asSequence()
                .filter { Math.abs(pointArray[it].x - midPoint.x) < nearByPlace.minDist }
                .forEachIndexed { j, i ->
                    minPointMap.add(pointArray[i])
                }

        val consolidatedClosest = getConsolidatedClosest(minPointMap, minPointMap.size, minDist = nearByPlace.minDist)

        return when {
            nearByPlace.minDist < consolidatedClosest.minDist -> nearByPlace
            else -> consolidatedClosest
        }
    }

    private fun getConsolidatedClosest(pointArray: ArrayList<PointF>, size: Int, minDist: Double): NearByPlace {
        pointArray.sortWith(Comparator { a, b -> if (a.y < b.y) -1 else if (a.y > b.y) 1 else 0 })

        var minimalDistance = minDist
        var pointA: PointF? = null
        var pointB: PointF? = null
        var nearByPlace = NearByPlace()

        (0 until size).forEach { i ->
            (i + 1 until size)
                    .asSequence()
                    .takeWhile { it < size && (pointArray[it].y - pointArray[i].y) < minimalDistance }
                    .map {
                        pointA = pointArray[i]
                        pointB = pointArray[it]
                        distance(pointA = pointA!!, pointB = pointB!!)
                    }
                    .filter { it < minimalDistance }
                    .forEach {
                        nearByPlace = NearByPlace(pointA, pointB, it)
                        minimalDistance = it
                    }
        }
        return nearByPlace
    }

    private fun distance(pointA: PointF, pointB: PointF): Double = Math.sqrt(((pointA.x - pointB.x) * (pointA.x - pointB.x) + (pointA.y - pointB.y) * (pointA.y - pointB.y)).toDouble())

    private fun tradiitonalWay(pointArray: List<PointF>, size: Int): NearByPlace {
        var minDist = Double.MAX_VALUE
        var pointA: PointF? = null
        var pointB: PointF? = null
        var nearByPlace = NearByPlace()
        (0 until size).forEach { i ->
            (i + 1 until size)
                    .asSequence()
                    .map {
                        pointA = pointArray[i]
                        pointB = pointArray[it]
                        distance(pointA = pointA!!, pointB = pointB!!)
                    }
                    .filter { it < minDist }
                    .forEach {
                        nearByPlace = NearByPlace(pointA, pointB, it)
                        minDist = it
                    }
        }
        return nearByPlace
    }

    data class NearByPlace(var pointA: PointF? = null, var pointB: PointF? = null, var minDist: Double = 0.toDouble())
}
