package com.raywenderlich.android.targetpractice.common

import android.util.Log
import kotlin.random.Random

class Maze {
    val TAG = "maze"
    var mazeData = arrayOf<Array<Int>>()
    var mazeObj = ""
    var startPos = Pair(-1, -1)
    var endPos = Pair(-1, -1)

    fun showInLogCat(data: Array<Array<Int>>) {
        for (array in data) {
            var msg = ""
            for (value in array) {
                msg += "$value "
            }
            msg += "\n"
            Log.d(TAG, msg)
        }
    }

    fun generateMaze(sizeRows: Int = 9, sizeCols: Int = 11, seed: Int = 1): Array<Array<Int>> {
        val placementThreshold = 0 // chance of empty space
        var data = arrayOf<Array<Int>>()
        var maze = Array(sizeRows) {Array(sizeCols) {0} }
        if (sizeRows % 2 == 0 && sizeCols % 2 == 0) {
            Log.d(TAG, "Odd numbers work better for maze size!")
        }
        for (i in 0 until sizeRows) {
            for (j in 0 until sizeCols) {
                if (i == 0 || j == 0 || i == sizeRows -1 || j == sizeCols -1) {
                    maze[i][j] = 1
                }
            }
        }
        return maze
    }

    fun showMaze(data: Array<Array<Int>>) {
        for (array in data) {
            var msg = ""
            for (value in array) {
                if (value == 0) {
                    msg += ".."
                } else {
                    msg += "=="
                }
            }
            msg += "\n"
            Log.d(TAG, msg)
        }
    }

    fun showMazeWithPos(data: Array<Array<Int>>, start: Pair<Int, Int> = Pair(-1, -1), end: Pair<Int, Int> = Pair(-1, -1)) {
        for (i in data.indices) {
            var msg = ""
            for (j in data[i].indices) {
                if(start.first == i && start.second == j) {
                    msg += "•"
                    continue
                }
                if(end.first == i && end.second == j) {
                    msg += "○"
                    continue
                }
                if (data[i][j] == 0) {
                    msg += ".."
                } else {
                    msg += "=="
                }
            }
            msg += "\n"
            Log.d(TAG, msg)
        }
    }

    fun findStartPos(data: Array<Array<Int>>): Pair<Int, Int> {
        for(i in 0 until data.size) {
            for (j in 0 until data[0].size) {
                if (data[i][j] == 0) {
                    return Pair(i, j)
                }
            }
        }
        return Pair(-1, -1)
    }

    fun findEndPos(data: Array<Array<Int>>): Pair<Int, Int> {
        for(i in data.size-1 downTo  0) {
            for (j in data[0].size-1 downTo 0 ) {
                if (data[i][j] == 0) {
                    return Pair(i, j)
                }
            }
        }
        return Pair(-1, -1)
    }

    fun generateMazeObj(data: Array<Array<Int>>) : String {
        var obj = "mtllib Cube.mtl\n" +
                "vt 1.0 0.0 0.0\n" +
                "vt 1.0 1.0 0.0\n" +
                "vt 0.0 1.0 0.0\n" +
                "vt 0.0 0.0 0.0\n"
        var cubeCount = 0 // Cube count
        var c = 0
        for (i in 0 until data.size) {
            for (j in 0 until data[0].size) {
                if (data[i][j] == 1) {
                    obj += "o Cube_${i}_${j}\n"
                    obj += "v ${0.0+i} 0.0 ${0.0+j}\n" +
                            "v ${0.0+i} 0.0 ${1.0+j}\n" +
                            "v ${0.0+i} 1.0 ${0.0+j}\n" +
                            "v ${0.0+i} 1.0 ${1.0+j}\n" +
                            "v ${1.0+i} 0.0 ${0.0+j}\n" +
                            "v ${1.0+i} 0.0 ${1.0+j}\n" +
                            "v ${1.0+i} 1.0 ${0.0+j}\n" +
                            "v ${1.0+i} 1.0 ${1.0+j}\n"

                    obj += "vn ${0.0+i} 0.0 ${0.0+j}\n" +
                            "vn ${0.0+i} 0.0 ${1.0+j}\n" +
                            "vn ${0.0+i} 1.0 ${0.0+j}\n" +
                            "vn ${0.0+i} 1.0 ${1.0+j}\n" +
                            "vn ${1.0+i} 0.0 ${0.0+j}\n" +
                            "vn ${1.0+i} 0.0 ${1.0+j}\n" +
                            "vn ${1.0+i} 1.0 ${0.0+j}\n" +
                            "vn ${1.0+i} 1.0 ${1.0+j}\n"
                    obj += "g Cube_${i}_${j}_default\n" +
                            "usemtl default\n" +
                            "s 1\n"
                    c = 8 * cubeCount
                    obj += "f ${1+c}/1/${1+c} ${5+c}/2/${5+c} ${6+c}/3/${6+c} ${2+c}/4/${2+c}\n" +
                            "f ${2+c}/1/${2+c} ${4+c}/2/${4+c} ${3+c}/3/${3+c} ${1+c}/4/${1+c}\n" +
                            "f ${2+c}/1/${2+c} ${6+c}/2/${6+c} ${8+c}/3/${8+c} ${4+c}/4/${4+c}\n" +
                            "f ${3+c}/1/${3+c} ${7+c}/2/${7+c} ${5+c}/3/${5+c} ${1+c}/4/${1+c}\n" +
                            "f ${4+c}/1/${4+c} ${8+c}/2/${8+c} ${7+c}/3/${7+c} ${3+c}/4/${3+c}\n" +
                            "f ${5+c}/1/${5+c} ${7+c}/2/${7+c} ${8+c}/3/${8+c} ${6+c}/4/${6+c}\n"
                    cubeCount++
                }
            }
        }
        return obj
    }
}