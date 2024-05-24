package org.brewcode.hamster.action

import java.awt.AWTException
import java.awt.Robot
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

object MoverAction {

    private val myRobot = robot()

    @Throws(AWTException::class)
    fun robot(): Robot {
        val robot = Robot()

        robot.autoDelay = 500
        robot.isAutoWaitForIdle = true
        return robot
    }

    fun mouseMove(robot: Robot = myRobot) {
        println("Moving starting at: " + LocalDateTime.now())

        robot.mouseMove(ThreadLocalRandom.current().nextInt(0, 100), ThreadLocalRandom.current().nextInt(0, 100))
        robot.delay(ThreadLocalRandom.current().nextInt(1000, 2000))

        robot.mouseMove(ThreadLocalRandom.current().nextInt(100, 1000), ThreadLocalRandom.current().nextInt(100, 1000))
        robot.delay(ThreadLocalRandom.current().nextInt(1000, 2000))

        println("Moving finished at: " + LocalDateTime.now())
    }

}
