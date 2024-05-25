package org.brewcode.hamster.action

import com.codeborne.selenide.Condition
import org.brewcode.hamster.view.earn.EarnView
import org.brewcode.hamster.view.main.MainView
import org.brewcode.hamster.view.mine.MineView

object GameCommonAction {

    fun goToBack() {
        MainView.navigation.backButton.click()
    }

    fun goToExchange() {
        MainView.bottomMenu.exchange.click()
        MainView.hamsterButton.shouldBe(Condition.visible)
    }

    fun goToMine() {
        MainView.bottomMenu.mine.click()
        MineView.topMenuBlock.self.shouldBe(Condition.visible)
    }

    fun goToEarn() {
        MainView.bottomMenu.earn.click()
        EarnView.daily.shouldBe(Condition.visible)
    }
}
