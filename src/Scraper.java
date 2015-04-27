

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.mongodb.BasicDBList;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Scraper  {
	private final static String baseURL = "http://www.huffingtonpost.com";
	private WebDriver driver;
	private StringBuffer verificationErrors;
	private MongoClient mongo;
	private MongoDatabase db;
	private MongoCollection<Document> table;
	public Scraper() throws Exception{
		driver = new HtmlUnitDriver();
	    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
	    verificationErrors = new StringBuffer();
	}
	public void collectLinks(){
		driver.get(baseURL);
		WebElement element = driver.findElement(By.id("all-sections"));
		List<WebElement> links = element.findElements(By.className("column"));
		for(WebElement we: links){
			String title = we.findElement(By.className("title")).getText();
			List<Document> clickableLinks= new ArrayList<Document>();
			for(WebElement li :we.findElements(By.tagName("a"))){
				Document subCategories = new Document();
				subCategories.append(
						NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.TITLE, 
						li.getText())
				.append(NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.LINK, 
						li.getAttribute("href"));
				clickableLinks.add(subCategories);
			}
			table.insertOne(new Document(NameHelper.HUFF_TABLE_COLUMNS.CATEGORY,title)
			.append(NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY,clickableLinks));
		}
	}
	public void tearDown() throws Exception {
	    driver.quit();
	    String verificationErrorString = verificationErrors.toString();
	    if (!"".equals(verificationErrorString)) {
	      System.err.println(verificationErrorString);
	    }
	  }
	private void getAllNewsArticles() throws Exception{
		
		ArrayList<Document> subCats = ((ArrayList<Document>) 
				db.getCollection(NameHelper.HUFF_TABLE)
				.find(new Document(NameHelper.HUFF_TABLE_COLUMNS.CATEGORY,"News"))
				.first() //since there is only one doc for a particular category
				.get(NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY));
		
		Iterator<Document> iterable = subCats.iterator();
		int index = 0;
		while(iterable.hasNext()){
			Document subCat = (Document)iterable.next();
			ArrayList<Document> pageLinks = fetchArticlesInPage(subCat);
			subCats.get(index++).
			append(NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.ARTICLES
					,pageLinks);
		}
		db.getCollection(NameHelper.HUFF_TABLE)
		.updateOne(new Document(NameHelper.HUFF_TABLE_COLUMNS.CATEGORY,"News"),
				new Document("$set", new Document(
								NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY,
						subCats)));
		
	}
	private ArrayList<Document> fetchArticlesInPage(Document subCat)throws Exception {
		ArrayList<Document> articles = new ArrayList<Document>();
		String link = subCat.getString(NameHelper.HUFF_TABLE_COLUMNS
				.SUB_CATEGORY_COLUMNS.LINK);
		driver.get(link);
		WebElement element;
		
		//Sometimes this may not be present.
		try{
			element = driver.findElement(By.id("top_featured_news"));
			List<WebElement> newsParts = element.findElements(By.tagName("a"));
			StringBuilder featuredNews = new StringBuilder();
			String featuredLink = null;
			for(WebElement we: newsParts){
				featuredNews.append(we.getText()+" ");
				featuredLink = we.getAttribute("href");
			}
			articles.add(new Document(
		NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.ARTICLE_COLUMNS.TITLE,
					featuredNews.toString())
	.append(NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.ARTICLE_COLUMNS.LINK, 
					featuredLink));
		}
		catch(Exception e){
			//Ignore and move on.
			System.err.println("No top featured content on "+ 
			subCat.getString(NameHelper.HUFF_TABLE_COLUMNS
					.SUB_CATEGORY_COLUMNS.TITLE));
		}
		//If this isn't there then it is a problem. However, just writing a fail safe.
		try{
			element = driver.findElement(By.id("center_entries_container"));
			List<WebElement> newsParts = element.findElements(By.tagName("h2"));
			for(WebElement we: newsParts){
				articles.add(new Document(
		NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.ARTICLE_COLUMNS.TITLE,
						we.findElement(By.tagName("a")).getText())
	.append(NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.ARTICLE_COLUMNS.LINK,
			we.findElement(By.tagName("a")).getAttribute("href")));
			}
		}
		catch(Exception e){
			//Big Problem, but what can I do?
			System.err.println("No content on "+ 
			subCat.getString(NameHelper.HUFF_TABLE_COLUMNS
					.SUB_CATEGORY_COLUMNS.TITLE));
		}
		return articles;
	}
	private void connectToDB() {
		mongo = new MongoClient(NameHelper.DB_CLIENT, NameHelper.DB_PORT);
		db = mongo.getDatabase(NameHelper.DB_NAME);
		if(GetLatestTweets.collectionExists(db, NameHelper.HUFF_TABLE)){
			db.getCollection(NameHelper.HUFF_TABLE).drop();
		}
		table = db.getCollection(NameHelper.HUFF_TABLE);
	}
	private void disconnectFromDB(){
		mongo.close();
	}
    public static void main(String[] args) {
    	Scraper example = null;
    	try
    	{
    		example = new Scraper();
    		example.connectToDB();
    		example.collectLinks();
    		example.getAllNewsArticles();
        	example.tearDown();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	finally{
    		example.disconnectFromDB();
    	}
    }
}