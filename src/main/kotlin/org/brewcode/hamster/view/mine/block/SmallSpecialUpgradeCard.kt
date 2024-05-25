package org.brewcode.hamster.view.mine.block

import org.brewcode.hamster.service.UpgradeSection
import org.brewcode.hamster.util.*
import org.openqa.selenium.By.xpath

open class SmallSpecialUpgradeCard(
    section: UpgradeSection,
    selfXpath: String
) : SmallUpgradeCard(section, selfXpath) {
    override val name = element(selfXpath.xChild("android.widget.TextView[2]").xBy())
    override val profit = element(xpath(selfXpath.xChild("android.view.View[2]").xAnyChild("android.widget.TextView").xIndex(2)))
    override val level = element(xpath(selfXpath.xChild("android.view.View[3]").xChild("android.widget.TextView[1]")))
    override val cost = element(xpath(selfXpath.xChild("android.view.View[3]").xChild("android.view.View").xAnyChild("android.widget.TextView[1]")))
    override val needs = element(xpath(selfXpath.xChild("android.view.View[3]").xChild("android.widget.TextView[2]")))
}
