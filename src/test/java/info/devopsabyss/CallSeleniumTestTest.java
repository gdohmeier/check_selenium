package info.devopsabyss;
// 3

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.firefox.FirefoxDriver;


public class CallSeleniumTestTest {
	private WebDriver driver;
	private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();
	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		baseUrl = "http://www.google.com/";
		//driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

/*	@Test
	public void testCallSeleniumTestCase1() throws Exception {
		System.out.println("get url");
		driver.get(baseUrl + "/");
		driver.findElement(By.cssSelector("#hptl > a:nth-child(1)")).click();
		System.out.println("sleep 2");
		Thread.sleep(2000);
		System.out.println("done");
	}
*/
	@Test
	public void testCallSeleniumTestCase2() throws Exception {
		System.out.println("get url");
		driver.get(baseUrl + "/");
		System.out.println("sleep 1");
		Thread.sleep(1000);
		System.out.println("send string (selenium hq) and enter");
		driver.findElement(By.name("q")).sendKeys("selenium hq");
		driver.findElement(By.name("q")).sendKeys(Keys.RETURN);
		System.out.println("sleep 2");
		Thread.sleep(2000);
		System.out.println("done");
	}

/*	@Test
	public void testCallSeleniumTestCase3() throws Exception {
		System.out.println("get url");
		driver.get(baseUrl + "/");
		System.out.println("sleep 1");
		Thread.sleep(1000);
		System.out.println("Force failure.");
		Assert.fail("force failure");
	}*/


	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			 Assert.fail(verificationErrorString);
		}
	}

	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
}
