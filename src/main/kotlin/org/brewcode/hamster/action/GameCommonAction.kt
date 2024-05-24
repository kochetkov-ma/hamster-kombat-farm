package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import org.brewcode.hamster.view.HamsterKombatGameView
import org.brewcode.hamster.view.MineView

object GameCommonAction {

    fun goToExchange() {
        HamsterKombatGameView.bottomMenuBlock.exchange.click()
        HamsterKombatGameView.hamsterButton.shouldBe(Condition.visible)
    }

    fun goToMine() {
        HamsterKombatGameView.bottomMenuBlock.mine.click()
        MineView.topMenuBlock.self.shouldBe(Condition.visible)
    }

    fun goToEarn() {
        HamsterKombatGameView.bottomMenuBlock.earn.click()
        HamsterKombatGameView.earnBlock.daily.shouldBe(Condition.visible)
    }
}
