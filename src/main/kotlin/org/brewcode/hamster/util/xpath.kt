package org.brewcode.hamster.util

import org.openqa.selenium.By

fun String.xBy() = By.xpath(this)

fun String.xAnyChildTxt(text: String, tag: String = "*") = "$this//$tag[text()='$text']"
fun xText(text: String, tag: String = "*") = ".//$tag[@text='$text']"
fun xDesc(text: String, tag: String = "*") = ".//$tag[@content-desc='$text']"

val String.xParent get() = "$this/.."

fun String.xSiblingPrev(index: Int, tag: String = "*") = "$this/preceding-sibling::$tag[$index]"
fun String.xSibling(index: Int) = "$this/following-sibling::*[$index]"

fun String.xChild(tag: String) = "$this/$tag"
fun String.xChildTxt(text: String, tag: String = "*") = "$this/$tag[text()='$text']"
fun String.xChildTxtContains(text: String, tag: String = "*") = "$this/$tag[contains(text(), '$text')]"
fun String.xAnyChild(tag: String) = "$this//$tag"
fun String.xLastDescendant(tag: String = "*") = "$this//descendant::$tag[last()]"

fun String.xIndex(i: Int) = "($this)[$i]"
fun String.xAttr(attr: String, value: String) = "$this[@$attr='$value']"






