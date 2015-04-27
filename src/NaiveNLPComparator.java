import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class NaiveNLPComparator {
	
	private MongoClient mongo;
	private MongoDatabase db;
	private MongoCollection<Document> table;
	private List<ObjectId> trendIds;
	private List<HashMap<String,Integer>> wordFrequencies;
	private List<Integer> cumulativeWeight;
	private ArrayList<String> stopWords;
	private MongoCollection<Document> success_table;
	private MongoCollection<Document> failure_table;
	public NaiveNLPComparator() {
		trendIds = new ArrayList<ObjectId>();
		wordFrequencies = new ArrayList<HashMap<String,Integer>>();
		cumulativeWeight = new ArrayList<Integer>();
		stopWords = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(new File("StopWords.txt")));
			String word = null;
			while((word=br.readLine())!= null){
				stopWords.add(word.trim());
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String[] getWords(String tweet){
		Set<String> wordList = new HashSet<String>();
		String[] words = tweet.split(" ");
		for(String word:words){
			word = word.toLowerCase().replaceAll("\\(|\\)|!|,|\"|\\.+|\\d+|https?://.*|#|:", "");
			word = word.replaceAll("@", "at");
			if(!stopWords.contains(word) && !word.equals(""))
				wordList.add(word);
		}
		String[] strings = new String[wordList.size()]; 
		return (String[])wordList.toArray(strings);
	}
	public void processTweets(){
		FindIterable<Document> iterable = table.find();
		iterable.forEach(new Block<Document>() {

			@Override
			public void apply(final Document doc) {
				trendIds.add(doc.getObjectId(NameHelper.PRIMARY_KEY));
				HashMap<String, Integer> freq = new HashMap<>();
				int maxFrequency = 0;
				int sum = 0;
				ArrayList<String> tweets = (ArrayList<String>) 
						doc.get(NameHelper.TREND_TABLE_COLUMNS.TWEETS);
				for(String tweet: tweets){
					String[] keyWords = getWords(tweet);
					for(String key : keyWords){
						if(freq.get(key) == null){
							freq.put(key, 1);
							sum ++;
							maxFrequency = maxFrequency < 1? 1: maxFrequency;
						}
						else{
							int frequency = freq.get(key)+1;
							freq.put(key, frequency);
							sum ++;
							maxFrequency = maxFrequency < frequency? 
									frequency: maxFrequency;
						}
					}
				}
				freq.put(doc.getString(NameHelper.TREND_TABLE_COLUMNS.TREND_STRING),
						++maxFrequency);
				cumulativeWeight.add(sum);
				wordFrequencies.add(freq);
			}
			
		});
	}
	public double getMatchPercent(String desc,
			double weight,
			HashMap<String, Integer> frequency){
		double percent = 0;
		String[] words = desc.split(" ");
		for(String word: words){
			word = word.toLowerCase();
			if(frequency.containsKey(word))
				percent += frequency.get(word);
		}
		return weight>0?percent/weight:percent;
	}
	public void findMatches(){
		table = db.getCollection(NameHelper.HUFF_TABLE);
		Iterator<Document> iterable = table.find().iterator();
		while(iterable.hasNext()){
			List<Document> sub_cats = (List<Document>) 
						iterable.next().get(NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY);
				for(Document sub_cat:sub_cats){
					List<Document> articles = (List<Document>) sub_cat.get(
						NameHelper.HUFF_TABLE_COLUMNS.SUB_CATEGORY_COLUMNS.ARTICLES);
					if(articles == null)
						continue;
					for(Document article:articles){
						int i =0;
						for(HashMap<String, Integer> frequency: wordFrequencies){
							int weight = cumulativeWeight.get(i);
							ObjectId trendId = trendIds.get(i++);
							String desc = article.getString(NameHelper.
									HUFF_TABLE_COLUMNS.
									SUB_CATEGORY_COLUMNS.
									ARTICLE_COLUMNS.TITLE);
							double percent = getMatchPercent(desc,
									weight, frequency);	
							if(percent > 0.5){
								success_table.insertOne(new Document(NameHelper.
										SUC_FAIL_COLUMNS.ID,trendId.toString()).
										append(NameHelper.SUC_FAIL_COLUMNS.DESC,desc).
										append(NameHelper.SUC_FAIL_COLUMNS.LINK,
										article.getString(NameHelper.
												HUFF_TABLE_COLUMNS.
												SUB_CATEGORY_COLUMNS.
												ARTICLE_COLUMNS.LINK)).
										append(NameHelper.SUC_FAIL_COLUMNS.WEIGHT, 
												percent));
							}
							else
							{
								failure_table.insertOne(new Document(NameHelper.
										SUC_FAIL_COLUMNS.ID,trendId.toString()).
										append(NameHelper.SUC_FAIL_COLUMNS.DESC,desc).
										append(NameHelper.SUC_FAIL_COLUMNS.LINK,
										article.getString(NameHelper.
												HUFF_TABLE_COLUMNS.
												SUB_CATEGORY_COLUMNS.
												ARTICLE_COLUMNS.LINK)).
										append(NameHelper.SUC_FAIL_COLUMNS.WEIGHT, 
												percent));
							}
						}
					}
				}
				
			}
	}
	private void disconnectFromDB(){
		if(mongo != null)
			mongo.close();
	}
	private void connectToDB() {
		mongo = new MongoClient(NameHelper.DB_CLIENT, NameHelper.DB_PORT);
		db = mongo.getDatabase(NameHelper.DB_NAME);
		if(!GetLatestTweets.collectionExists(db, NameHelper.TREND_TABLE)){
			System.err.println("Trends table doesn't exist");
			System.exit(9999);
		}
		table = this.db.getCollection(NameHelper.TREND_TABLE);
		if(GetLatestTweets.collectionExists(db, NameHelper.SUCCESS_TABLE)){
			db.getCollection(NameHelper.SUCCESS_TABLE).drop();
		}
		if(GetLatestTweets.collectionExists(db, NameHelper.FAILURE_TABLE)){
			db.getCollection(NameHelper.FAILURE_TABLE).drop();
		}
		success_table = 
				db.getCollection(NameHelper.SUCCESS_TABLE); 
		failure_table = 
				db.getCollection(NameHelper.FAILURE_TABLE); 
	}
	public static void main(String[] args){
		NaiveNLPComparator nlp = new NaiveNLPComparator();
		try{
			nlp.connectToDB();
			nlp.processTweets();
			nlp.findMatches();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			nlp.disconnectFromDB();
		}
	}
	
}