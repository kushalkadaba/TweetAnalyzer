import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class GetLatestTweets{
	//This method along with connect to Database should go in another class.
	//Move when you have time.
	public static boolean collectionExists(MongoDatabase db,final String collectionName){
		if( collectionName == null || collectionName.equalsIgnoreCase(""))
			return false;
		 MongoCursor<String> collections = db.listCollectionNames().iterator();
		 try{
			 while(collections.hasNext()){
				 if(collections.next().equals(collectionName)){
					 collections.close();
					 return true;
				 }
			 }
		 }finally{
			 collections.close();
		 }
		 return false;
	}
	public static void main(String[] args){
		ArrayList<String> tweetQuery = new ArrayList<>();
		ArrayList<String> trendNames = new ArrayList<>();
		try{
			Twitter twitter = new TwitterFactory().getInstance();
			Trends trends = twitter.getPlaceTrends(NameHelper.NYID);
			for(Trend trend: trends.getTrends()){
				 tweetQuery.add( trend.getQuery());
				 trendNames.add(trend.getName());
			}
			@SuppressWarnings("resource")
			MongoClient mongo = new MongoClient(NameHelper.DB_CLIENT, NameHelper.DB_PORT);
			MongoDatabase db = mongo.getDatabase(NameHelper.DB_NAME);
			if(collectionExists(db, NameHelper.TREND_TABLE)){
				db.getCollection(NameHelper.TREND_TABLE).drop();
			}
			MongoCollection<Document> table = db.getCollection(NameHelper.TREND_TABLE);
			for(String searchString: tweetQuery){
				Query query = new Query(searchString);
				String trendName = trendNames.remove(0);
				//Get 4 most popular tweets for each of the 10 trending topics
				query.setResultType(Query.ResultType.popular);
				query.setCount(4);
				QueryResult result;
	            //do {
                result = twitter.search(query);
                List<Status> tweetResults = result.getTweets();
                List<String> tweets = new ArrayList<String>();
                for (Status tweet : tweetResults) {
                    tweets.add(tweet.getText());
                }
				table.insertOne(new Document(NameHelper.TREND_TABLE_COLUMNS.TREND, 
						trendName)
				.append(NameHelper.TREND_TABLE_COLUMNS.TREND_STRING, 
						trendName.replaceAll("#|\\'s", ""))
				.append(NameHelper.TREND_TABLE_COLUMNS.QUERY, searchString)
				.append(NameHelper.TREND_TABLE_COLUMNS.TWEETS, tweets ));
	            //} while ((query = result.nextQuery()) != null);
			}
			mongo.close();
		}
		catch(TwitterException e){
			e.printStackTrace();
		}
	}
}