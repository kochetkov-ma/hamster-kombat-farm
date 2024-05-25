package org.brewcode.hamster.view.mine.block

import org.brewcode.hamster.util.element
import org.openqa.selenium.By.xpath

object AdditionalMenuBlock {

    val myCards = element(xpath("//*[@text='My cards']"))
    val newCards = element(xpath("//*[@text='New cards']"))
    val missedCards = element(xpath("//*[@text='Missed opportunities']"))
}
