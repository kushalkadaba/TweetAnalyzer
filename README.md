# TweetAnalyzer
Collects news articles for trending topics

Dependencies:
1) Twitter4j 4.0.3
2) Selenium Java Driver 2.44.0
3) Selenium Html Unit Driver 2.44.0
4) Selenium Server Standalone Driver 2.44.0
5) Selenium Remote Driver 2.44.0
6) Mongo Java Driver 3.0.0

To run the project the following files should be executed in order:
1) GetLatestTweets.java
2) Scraper.java
3) NaiveNLPComparator.java
4) TestArticles.java
This will create a file "output.txt" which has Trend Names and the article links and descriptions.

On Linux, the following command can be executed from the project folder, to run the project
javac -cp ".:dependencies/lib/mongo-java-driver-3.0.0.jar:dependencies/lib/twitter4j-core-4.0.3.jar:dependencies/lib/selenium-java-2.44.0.jar:dependencies/lib/selenium-remote-driver-2.44.0.jar:dependencies/lib/selenium-api-2.44.0.jar:dependencies/lib/selenium-server-standalone-2.44.0.jar:dependencies/lib/selenium-firefox-driver-2.44.0.jar:dependencies/lib/selenium-htmlunit-driver-2.44.0.jar" src/NameHelper.java  src/GetLatestTweets.java src/Scraper.java src/NaiveNLPComparator.java src/TestArticles.java
java -cp ".:dependencies/lib/mongo-java-driver-3.0.0.jar:dependencies/lib/twitter4j-core-4.0.3.jar:dependencies/lib/selenium-java-2.44.0.jar:dependencies/lib/selenium-remote-driver-2.44.0.jar:dependencies/lib/selenium-api-2.44.0.jar:dependencies/lib/selenium-server-standalone-2.44.0.jar:dependencies/lib/selenium-firefox-driver-2.44.0.jar:dependencies/lib/selenium-htmlunit-driver-2.44.0.jar:src/*.class:./src" GetLatestTweets
java -cp ".:dependencies/lib/mongo-java-driver-3.0.0.jar:dependencies/lib/twitter4j-core-4.0.3.jar:dependencies/lib/selenium-java-2.44.0.jar:dependencies/lib/selenium-remote-driver-2.44.0.jar:dependencies/lib/selenium-api-2.44.0.jar:dependencies/lib/selenium-server-standalone-2.44.0.jar:dependencies/lib/selenium-firefox-driver-2.44.0.jar:dependencies/lib/selenium-htmlunit-driver-2.44.0.jar:src/*.class:./src" Scraper
java -cp ".:dependencies/lib/mongo-java-driver-3.0.0.jar:dependencies/lib/twitter4j-core-4.0.3.jar:dependencies/lib/selenium-java-2.44.0.jar:dependencies/lib/selenium-remote-driver-2.44.0.jar:dependencies/lib/selenium-api-2.44.0.jar:dependencies/lib/selenium-server-standalone-2.44.0.jar:dependencies/lib/selenium-firefox-driver-2.44.0.jar:dependencies/lib/selenium-htmlunit-driver-2.44.0.jar:src/*.class:./src" NaiveNLPComparator
java -cp ".:dependencies/lib/mongo-java-driver-3.0.0.jar:dependencies/lib/twitter4j-core-4.0.3.jar:dependencies/lib/selenium-java-2.44.0.jar:dependencies/lib/selenium-remote-driver-2.44.0.jar:dependencies/lib/selenium-api-2.44.0.jar:dependencies/lib/selenium-server-standalone-2.44.0.jar:dependencies/lib/selenium-firefox-driver-2.44.0.jar:dependencies/lib/selenium-htmlunit-driver-2.44.0.jar:src/*.class:./src" TestArticles

Algorithm:
1) Get the 10 latest trends at NYC from twitter.
2) For each trend get 4 most popular tweets and store in database.
3) Scrape the news section of huffingtonpost.com, to get all the news article links along with the short description. Only the news sub-section is scrapped.
4) Store these in a database.
5) NaiveNLPComparator retrieves the trends and the tweets from the database.
6) Every tweet is processed by removing stop words and a frequency table is created. Here the words which have a high frequency ultimately induces a high weight to an article.
7) For each article in the database, the description is matched with the frequency table to get a weight.
8) If this weight is 50% of the cumulative frequencies for a trend it is added to a Success table.
9) Else it is dumped in a failure table along with the trend_id, news_article, news_link and the weight it received.
10) TestArticles retrieves from the success table in descending order of weights (if there are no results) then the failure table is queried.
11) All news articles and links whose weight is not 0 are retrived in descending order of weights.
12) These are written into output.txt

Possible Improvements:
1) Could query for more than 4 tweets, so that there are more keywords.
2) Could get the entire article instead of just the breif descriptions, however this would increase the scraping time considerably.   
3) Get articles from all the sections including Entertainment, Life&Style, Tech&Science etc.


