package org.brewcode.hamster.view.base

import com.codeborne.selenide.Selenide.element
import io.appium.java_client.AppiumBy.ByAndroidUIAutomator
import org.brewcode.hamster.util.*

abstract class GameView {

    private object X {
        val app = xText("Hamster Kombat", "android.webkit.WebView")
    }

    val app = element(X.app.xBy())
    val common = CommonBlock
    val navigation = NavigationBlock
    val bottomMenu = BottomMenuBlock
    val confirm = ConfirmBlock

    object CommonBlock {

        val levelUpProcessing = element(ByAndroidUIAutomator("new UiSelector().textContains(\"Level up processing\")"))
        val goAheadButton = element(ByAndroidUIAutomator("new UiSelector().text(\"Go ahead\")"))
        val insufficientFunds = element(ByAndroidUIAutomator("new UiSelector().text(\"Insufficient funds\")"))
    }

    object NavigationBlock {

        val backButton = element(ByAndroidUIAutomator("new UiSelector().description(\"Go back\")"))
        val settings = element(ByAndroidUIAutomator("new UiSelector().className(\"android.widget.ImageView\").instance(1)"))
        val reload = element(ByAndroidUIAutomator("new UiSelector().text(\"Reload Page\")"))
    }

    object BottomMenuBlock {

        val exchange = element(ByAndroidUIAutomator("new UiSelector().description(\"Exchange\")"))
        val mine = element(ByAndroidUIAutomator("new UiSelector().description(\"Mine\")"))
        val friends = element(ByAndroidUIAutomator("new UiSelector().description(\"Friends\")"))
        val earn = element(ByAndroidUIAutomator("new UiSelector().description(\"Earn\")"))
        val airdrop = element(ByAndroidUIAutomator("new UiSelector().description(\"Airdrop\")"))
    }

    object ConfirmBlock {

        private object X {
            val self = GameView.X.app.xLastDescendant("android.view.View").xParent
        }

        val self = element(X.self.xBy())
        val closeButton = element(X.self.xSiblingPrev(1, "android.view.View").xBy())
        val title = element(X.self.xChild("android.widget.TextView").xBy())
        val actionButton = element(X.self.xChild("android.widget.Button").xBy())
    }
}
