package cn.charlie166.word.lucene.file;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @ClassName: ChineseSearch 
* @Description: 中文查找
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月20日 
*
 */
public class ChineseSearch {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void query(String queryString) throws IOException, ParseException{
		String index = "index";
	    String field = "contents";
	    boolean raw = false;
	    int hitsPerPage = 10;
	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
//	    Analyzer analyzer = new SmartChineseAnalyzer();
	    JcsegAnalyzer analyzer = new JcsegAnalyzer(JcsegTaskConfig.SEARCH_MODE);
	    QueryParser parser = new QueryParser(field, analyzer);
	    Query query = parser.parse(queryString);
	    if(query instanceof BooleanQuery){
	    	BooleanQuery.Builder bqb = new BooleanQuery.Builder();
	    	BooleanQuery bq = (BooleanQuery) query;
	    	for(BooleanClause bc : bq.clauses()){
	    		BooleanClause nbc = new BooleanClause(bc.getQuery(), Occur.MUST);
	    		bqb.add(nbc);
	    	}
	    	BooleanQuery thisQuery = bqb.build();
	    	logger.debug("Searching for: " + thisQuery.toString(queryString));
	    	doPagingSearch(searcher, thisQuery, hitsPerPage, raw);
	    } else {
	    	logger.debug("Searching for: " + query.toString(queryString));
	    	doPagingSearch(searcher, query, hitsPerPage, raw);
	    }
//	    FuzzyQuery query = new FuzzyQuery(new Term(field, queryString), 2);
	    reader.close();
	}
	
	/**
	* @Title: doPagingSearch 
	* @Description:
	* @param searcher
	* @param query
	* @param hitsPerPage
	* @param raw
	* @throws IOException
	 */
	public void doPagingSearch(IndexSearcher searcher, Query query, 
            int hitsPerPage, boolean raw) throws IOException{
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
	    ScoreDoc[] hits = results.scoreDocs;
	    int numTotalHits = results.totalHits;
	    logger.debug(numTotalHits + " total matching documents");
	    int start = 0;
	    int end = Math.min(numTotalHits, hitsPerPage);
	    if (end > hits.length) {
	        hits = searcher.search(query, numTotalHits).scoreDocs;
	    }
	    end = Math.min(hits.length, start + hitsPerPage);
	    for (int i = start; i < end; i++) {
	    	if (raw) {
	    		logger.debug("doc=" + hits[i].doc + " score=" + hits[i].score);
	        	continue;
	        }
	    	Document doc = searcher.doc(hits[i].doc);
	        String path = doc.get("path");
	        if (path != null) {
	        	logger.debug((i + 1) + ". " + path);
	        	String title = doc.get("title");
	        	if (title != null) {
	        		logger.debug("Title: " + doc.get("title"));
	        	}
	        	logger.debug("content:" + FileUtils.readFile(path));
	        } else {
	        	logger.debug((i+1) + ". " + "No path for this document");
	        }
	    }
	}
}