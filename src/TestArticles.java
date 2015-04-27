import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output.json")));

			for(ObjectId trend_id :trends){
				MongoCursor<Document> iterator = success.find(new Document(
						NameHelper.SUC_FAIL_COLUMNS.ID,trend_id.toString()))
						.sort(new Document(NameHelper.SUC_FAIL_COLUMNS.WEIGHT,-1)).iterator();
				if(!iterator.hasNext()){
					iterator = failure.find(new Document(
							NameHelper.SUC_FAIL_COLUMNS.ID,trend_id.toString())
					.append(NameHelper.SUC_FAIL_COLUMNS.WEIGHT,new Document("$ne",0)))
					.sort(new Document(NameHelper.SUC_FAIL_COLUMNS.WEIGHT,-1)).iterator();
				}
				bw.write("Trend ID:"+trend_id+" Trend Name: "
						+table.find(new Document(NameHelper.PRIMARY_KEY,trend_id)).first()
						.getString(NameHelper.TREND_TABLE_COLUMNS.TREND)
						+"\n");
				while(iterator.hasNext()){
					Document current = iterator.next();
					bw.write(current.getString(NameHelper.SUC_FAIL_COLUMNS.DESC)
							+"  "+current.getString(NameHelper.SUC_FAIL_COLUMNS.LINK)
							+"\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
