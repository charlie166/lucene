package cn.charlie166.word.lucene.db;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @ClassName: IndexSearch 
* @Description: 索引查询类
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月25日 
*
 */
public class IndexSearch {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void queryString(String str) throws IOException, ParseException{
		this.queryString(str, 10);
	}
	
	/**
	 * @Title: queryString 
	 * @Description: 查询
	 * @param str 查询字符串
	 * @param hitsPerPage 每页条数
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public void queryString(String str, int hitsPerPage) throws IOException, ParseException{
		if(str == null || "".equals(str.trim()))
			return;
	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(CommonAttributes.INDEX_STORE)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    JcsegAnalyzer analyzer = new JcsegAnalyzer(JcsegTaskConfig.COMPLEX_MODE);
//	    Analyzer analyzer = new SmartChineseAnalyzer();
	    QueryParser titleParser = new QueryParser(CommonAttributes.KEY_TITLE, analyzer);
	    QueryParser contentParser = new QueryParser(CommonAttributes.KEY_CONTENT, analyzer);
	    Query titleQuery = titleParser.parse(str);
	    logger.debug("title:searching for:" + titleQuery.toString());
	    BooleanQuery.Builder bqb = new BooleanQuery.Builder();
	    bqb.add(titleQuery, BooleanClause.Occur.SHOULD);
	    Query contentQuery = contentParser.parse(str);
	    if(contentQuery instanceof BooleanQuery){
	    	BooleanQuery.Builder newQuery = new BooleanQuery.Builder();
	    	BooleanQuery bq = (BooleanQuery) contentQuery;
	    	for(BooleanClause bc : bq.clauses()){
	    		BooleanClause nbc = new BooleanClause(bc.getQuery(), Occur.MUST);
	    		newQuery.add(nbc);
	    	}
	    	BooleanQuery thisQuery = newQuery.build();
	    	logger.debug("[1]content: Searching for: " + thisQuery.toString());
	    	bqb.add(thisQuery, BooleanClause.Occur.SHOULD);
	    } else {
	    	logger.debug("[2]content: Searching for: " + contentQuery.toString());
	    	bqb.add(contentQuery, BooleanClause.Occur.SHOULD);
	    }
	    this.doPagingSearch(searcher, bqb.build(), hitsPerPage);
	}
	
	public void doPagingSearch(IndexSearcher searcher, Query query, int hitsPerPage) throws IOException{
		TopDocs results = searcher.search(query, 2 * hitsPerPage);
	    ScoreDoc[] hits = results.scoreDocs;
	    int numTotalHits = results.totalHits;
	    logger.debug(numTotalHits + " total matching documents");
	    int start = 0;
	    int end = Math.min(numTotalHits, hitsPerPage);
	    if (end > hits.length) {
	        hits = searcher.search(query, numTotalHits).scoreDocs;
	    }
	    end = Math.min(hits.length, start + hitsPerPage);
	    DbUtils db = new DbUtils();
	    for (int i = start; i < end; i++) {
	    	Document doc = searcher.doc(hits[i].doc);
	        String idStr = doc.get(CommonAttributes.KEY_ID);
	        if (idStr != null) {
	        	String title = doc.get(CommonAttributes.KEY_TITLE);
	        	if (title != null) {
	        		logger.debug((i + ". " + idStr + " [score=" + hits[i].score + "]") + "--" + CommonAttributes.KEY_TITLE + ": " + title);
	        	} else {
	        		logger.debug((i + ". " + idStr + " [score=" + hits[i].score + "]") + "--no" + CommonAttributes.KEY_TITLE);
	        	}
	        	Map<String, Object> map = new HashMap<String, Object>();
	        	String two[] = idStr.split(":");
	        	String tableName = two[0];
	        	map.put("table_name", tableName);
	        	map.put("id", two[1]);
	        	logger.debug("索引数据[" + i + "][table:" + tableName + "]:" + CommonAttributes.convertMapToString(db.selectById(map)));
	        } else {
	        	logger.debug(i + ". " + "No " + CommonAttributes.KEY_ID + " for this document");
	        }
	    }
	}
}