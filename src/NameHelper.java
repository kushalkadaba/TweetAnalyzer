public final class NameHelper {
	static final String DB_NAME = "HPTweetAnalysis";
	static final String DB_CLIENT = "localhost";
	static final int DB_PORT = 27017;
	static final String HUFF_TABLE = "NewsLinks";
	static final String TREND_TABLE = "TrendingTweets";
	static final int NYID = 2459115;
	static final String PRIMARY_KEY = "_id";
	static final String SUCCESS_TABLE = "Success";
	static final String FAILURE_TABLE = "Failure";
	final class HUFF_TABLE_COLUMNS{
		static final String CATEGORY = "category";
		static final String SUB_CATEGORY = "sub_category";
		final class SUB_CATEGORY_COLUMNS{
			static final String TITLE = "title";
			static final String LINK = "link";
			static final String ARTICLES = "articles";
			final class ARTICLE_COLUMNS{
				static final String TITLE = "brief_description";
				static final String LINK = "link";
			}
		}
	}
	final class TREND_TABLE_COLUMNS{
		static final String TREND = "trend";
		static final String TREND_STRING = "trend_string";
		static final String QUERY = "query_string";
		static final String TWEETS = "tweets";
	}
	final class SUC_FAIL_COLUMNS{
		static final String ID = "_id";
		static final String WEIGHT = "weight";
		static final String LINK = "link";
		static final String DESC = "desc";
	}
}
