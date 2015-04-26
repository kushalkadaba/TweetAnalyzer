import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TestArticles {

	public static void main(String[] args) {
		MongoClient mongo = new MongoClient(NameHelper.DB_CLIENT, NameHelper.DB_PORT);
		MongoDatabase db = mongo.getDatabase(NameHelper.DB_NAME);
		MongoCollection<Document> table = db.getCollection(NameHelper.TREND_TABLE);
		MongoCollection<Document> success = db.getCollection(NameHelper.SUCCESS_TABLE);
		MongoCollection<Document> failure = db.getCollection(NameHelper.FAILURE_TABLE);
		DistinctIterable<ObjectId> trends = table.distinct(NameHelper.PRIMARY_KEY, 
				ObjectId.class);
		for(ObjectId trend_id :trends){
			MongoCursor<Document> iterator = success.find(new Document(
					NameHelper.SUC_FAIL_COLUMNS.ID,trend_id))
			.sort(new Document(NameHelper.SUC_FAIL_COLUMNS.WEIGHT,-1)).iterator();
			if(!iterator.hasNext()){
				iterator = failure.find(new Document(
						NameHelper.SUC_FAIL_COLUMNS.ID,trend_id))
				.sort(new Document(NameHelper.SUC_FAIL_COLUMNS.WEIGHT,-1)).iterator();
			}
			System.out.println("Trend ID:"+trend_id);
			while(iterator.hasNext()){
				Document current = iterator.next();
				System.out.println(current.getString(NameHelper.SUC_FAIL_COLUMNS.DESC)
						+"  "+current.getString(NameHelper.SUC_FAIL_COLUMNS.LINK) );
			}
		}
	}

}
