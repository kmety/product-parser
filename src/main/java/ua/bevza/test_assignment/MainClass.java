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

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        WebDriver driver = new ChromeDriver();
        driver.get(URL);
        driver.manage().window().maximize();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int scrollingStep = driver.manage().window().getSize().getHeight();
        long currentScrollPosition = 0;
        WebElement stopScrollingElement = driver.findElement(By.cssSelector("div._paginationWrapper_557b1"));
        long stopScrollingElementPosition = stopScrollingElement.getLocation().getY();
        int scrollsDone = 0;
        Set<WebElement> uniqueElements = new HashSet<>();
        List<Product> products = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        while (stopScrollingElementPosition > currentScrollPosition) {
            js.executeScript("window.scrollBy(0," + scrollingStep + ")");
            scrollsDone++;
            List<WebElement> productTileElements
                    = driver.findElements(By.cssSelector("a.ProductTile__Wrapper-sc-1qheze-0.eZqmvf"));
            for (WebElement productTileElement : productTileElements) {
                if (uniqueElements.contains(productTileElement)) {
                    continue;
                }
                Product product = new Product();
                product.setArticleId(getArticleId(productTileElement));
                product.setProductName(getProductName(productTileElement));
                product.setBrand(getBrand(productTileElement));
                product.setPrice(getPrice(productTileElement));
                product.setColors(getColors(productTileElement));
                products.add(product);

                uniqueElements.add(productTileElement);
            }
            Object currentScrollPositionObject = js.executeScript("return window.scrollY");
            if (currentScrollPositionObject instanceof Double) {
                currentScrollPosition = ((Double) currentScrollPositionObject).longValue();
            } else {
                currentScrollPosition = (Long) currentScrollPositionObject;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Products found: " + products.size());
        System.out.println("Scrolls done: " + scrollsDone);
        System.out.println();
        ResultWritingUtil.saveToFileAsJson(products);
        driver.close();
    }

    private static String getBrand(WebElement webElement) {
        return webElement.findElement(By.xpath(".//p[@data-test-id='BrandName']")).getText();
    }

    private static String getPrice(WebElement webElement) {
        WebElement tempElement = webElement.findElement(By.cssSelector("div.ProductTileContent__StyledPriceBox-sc-1gv4rhx-5.KnODe"));
        return tempElement.findElement(By.tagName("span")).getText();
    }

    private static Long getArticleId(WebElement webElement) {
        return Long.parseLong(webElement.getAttribute("id"));
    }

    private static String getProductName(WebElement webElement) {
        String href = webElement.getAttribute("href");
        int fromIndex = href.lastIndexOf('/');
        int toIndex = href.lastIndexOf('-');
        return href.substring(fromIndex + 1, toIndex);
    }

    private static List<String> getColors(WebElement webElement) {
        List<String> colors = new ArrayList<>();
        List<WebElement> tempElements = webElement.findElements(By.xpath(".//li[@data-test-id='ColorBubble']"));
        for (WebElement element : tempElements) {
            String color = element.getAttribute("color");
            colors.add(color);
        }
        return colors;
    }
}
