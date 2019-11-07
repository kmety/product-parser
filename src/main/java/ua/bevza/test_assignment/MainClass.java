package ua.bevza.test_assignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class MainClass {
    private static final String URL = "https://www.aboutyou.de/maenner/bekleidung";
    private static final String CHROME_DRIVER_PATH = "src\\main\\resources\\chromedriver.exe";

    private static final String STOP_SCROLLING_ELEMENT_SELECTOR = "div._paginationWrapper_557b1";
    private static final String PRODUCT_TILE_ELEMENTS_SELECTOR = "a.ProductTile__Wrapper-sc-1qheze-0.eZqmvf";
    private static final String CONTAINS_BRAND_NAME_ELEMENT_SELECTOR = ".//p[@data-test-id='BrandName']";
    private static final String CONTAINS_PRICE_ELEMENT_SELECTOR = "div.ProductTileContent__StyledPriceBox-sc-1gv4rhx-5.KnODe > span";
    private static final String CONTAINS_COLOR_ELEMENTS_SELECTOR = ".//li[@data-test-id='ColorBubble']";

    private static final long BROWSERS_WINDOW_MAXIMIZING_WAIT_TIME = 4000L;
    private static final long WAITING_TIME_BETWEEN_SCROLLINGS = 100L;

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        WebDriver driver = new ChromeDriver();
        driver.get(URL);
        driver.manage().window().maximize();
        //maximizing web browser's window needs some time
        try {
            Thread.sleep(BROWSERS_WINDOW_MAXIMIZING_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int scrollingStep = driver.manage().window().getSize().getHeight();
        long currentScrollPosition = 0;
        WebElement stopScrollingElement = driver.findElement(By.cssSelector(STOP_SCROLLING_ELEMENT_SELECTOR));
        long stopScrollingElementPosition = stopScrollingElement.getLocation().getY();
        int scrollsDone = 0;
        Set<WebElement> uniqueElementsSet = new HashSet<>();
        List<Product> products = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        while (stopScrollingElementPosition > currentScrollPosition) {
            js.executeScript("window.scrollBy(0," + scrollingStep + ")");
            scrollsDone++;
            List<WebElement> productTileElements
                    = driver.findElements(By.cssSelector(PRODUCT_TILE_ELEMENTS_SELECTOR));
            for (WebElement productTileElement : productTileElements) {
                if (uniqueElementsSet.contains(productTileElement)) {
                    continue;
                }
                Product product = new Product();
                product.setArticleId(getArticleId(productTileElement));
                product.setProductName(getProductName(productTileElement));
                product.setBrand(getBrand(productTileElement));
                product.setPrice(getPrice(productTileElement));
                product.setColors(getColors(productTileElement));
                products.add(product);

                uniqueElementsSet.add(productTileElement);
            }
            //this method may return either Long or Double so I used instanceof operator and explicit type casting
            Object currentScrollPositionObject = js.executeScript("return window.scrollY");
            if (currentScrollPositionObject instanceof Double) {
                currentScrollPosition = ((Double) currentScrollPositionObject).longValue();
            } else {
                currentScrollPosition = (Long) currentScrollPositionObject;
            }
            try {
                Thread.sleep(WAITING_TIME_BETWEEN_SCROLLINGS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Products found: " + products.size());
        System.out.println("Scrolls done: " + scrollsDone);
        ResultWritingUtil.saveToFileAsJson(products);
        driver.close();
    }

    private static Long getArticleId(WebElement webElement) {
        return Long.parseLong(webElement.getAttribute("id"));
    }

    private static String getBrand(WebElement webElement) {
        WebElement tempElement = webElement.findElement(By.xpath(CONTAINS_BRAND_NAME_ELEMENT_SELECTOR));
        return tempElement == null ? "" : tempElement.getText();
    }

    private static String getPrice(WebElement webElement) {
        WebElement tempElement = webElement.findElement(By.cssSelector(CONTAINS_PRICE_ELEMENT_SELECTOR));
        return tempElement == null ? "" : tempElement.getText();
    }

    private static String getProductName(WebElement webElement) {
        String href = webElement.getAttribute("href");
        int fromIndex = href.lastIndexOf('/');
        int toIndex = href.lastIndexOf('-');
        return href.substring(fromIndex + 1, toIndex);
    }

    private static List<String> getColors(WebElement webElement) {
        List<String> colors = new ArrayList<>();
        List<WebElement> tempElements = webElement.findElements(By.xpath(CONTAINS_COLOR_ELEMENTS_SELECTOR));
        for (WebElement element : tempElements) {
            String color = element.getAttribute("color");
            colors.add(color);
        }
        return colors;
    }
}
