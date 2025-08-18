package com.automation.marqvision.util.selenium;

import org.openqa.selenium.*;
import java.util.List;

public class SeleniumUtils {
    public static WebElement firstPresent(WebDriver d, By... candidates) {
        for (By by : candidates) {
            List<WebElement> els = d.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("No candidate matched");
    }

    public static void highlight(WebDriver d, WebElement el) {
        ((JavascriptExecutor) d).executeScript(
                "arguments[0].style.outline='3px solid magenta'; arguments[0].scrollIntoView({block:'center'});", el);
        try { Thread.sleep(160); } catch (InterruptedException ignored) {}
    }
}
