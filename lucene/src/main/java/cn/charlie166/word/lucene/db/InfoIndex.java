package cn.charlie166.word.lucene.db;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @ClassName: InfoIndex 
* @Description: 信息索引建立
* @company 
* @author liyang
* @Email charlie166@163.com
* @date 2017年4月25日 
*
 */
public class InfoIndex {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	* @Title: indexAdvice 
	* @Description: 为通知公告建立索引数据
	 */
	public void indexAdvice(){
		DbUtils db = new DbUtils();
		try {
			this.indexData(db.getAdviceList(), "ds_advice");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* @Title: indexData 
	* @Description: 为数据建立索引
	 */
	public void indexData(){
		String [] tables = {"ds_advice", "ds_activity", "ds_cooperation", "ds_association", "ds_group"};
		DbUtils db = new DbUtils();
		for(String s: tables){
			try {
				this.indexData(db.getDataList(s), s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	* @Title: indexData 
	* @Description: 建立索引
	* @param dataList
	 * @throws IOException 
	 */
	public void indexData(List<Map<String, Object>> dataList, String tableName)
			throws IOException {
		if (dataList == null || dataList.isEmpty())
			return;
		LocalDateTime startTime = LocalDateTime.now();
		logger.debug("索引[table:" + tableName + "][" + dataList.size() 	+ "],start begin:" +
			startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
		Directory dir = FSDirectory.open(Paths.get(CommonAttributes.INDEX_STORE));
		logger.debug("索引位置:" + dir.toString());
		Analyzer analyzer = new JcsegAnalyzer(JcsegTaskConfig.COMPLEX_MODE);
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		/** 追加 **/
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, iwc);
		indexData(writer, dataList, tableName);
		writer.close();
		LocalDateTime endTime = LocalDateTime.now();
		logger.debug("索引[table:" + tableName + "],end at:" + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
	}
	
	/**
	* @Title: indexData 
	* @Description: 
	* @param writer
	* @param dataList
	 * @throws IOException 
	 */
	private void indexData(IndexWriter writer, List<Map<String, Object>> dataList, String tableName) throws IOException{
		if(dataList != null && !dataList.isEmpty()){
			logger.debug("[tableName:" + tableName + "]所有条数:" + dataList.size());
			for(Map<String, Object> map: dataList){
				if(tableName != null && !"".equals(tableName) && map.get("id") != null){
					/**详情内容，根据数据表不同需分别读取**/
					String content = "";
					/**标题**/
					String title = "";
					switch(tableName){
						case "ds_advice": {/**通知公告***/
							content = map.get("description").toString();
							title = map.get("topic").toString();
							break;
						}
						case "ds_activity": {/**活动**/
							content = map.get("description").toString();
							title = map.get("topic").toString();
							break;
						}
						case "ds_cooperation": {/**合作**/
							content = map.get("content").toString();
							title = map.get("title").toString();
							break;
						}
						case "ds_association": {/**协会**/
							content = map.get("description").toString();
							title = map.get("name").toString();
							break;
						}
						case "ds_group": {/**小组**/
							content = map.get("description").toString();
							title = map.get("name").toString();
							break;
						}
						default: continue;
					}
					Document doc = new Document();
					String mainKey = tableName + ":" + map.get("id");
					Field idField = new StringField(CommonAttributes.KEY_ID, mainKey, Field.Store.YES);
					doc.add(idField);
					doc.add(new TextField(CommonAttributes.KEY_CONTENT, content, Field.Store.NO));
					doc.add(new StringField(CommonAttributes.KEY_TITLE, title, Field.Store.YES));
					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						logger.debug("adding " + CommonAttributes.convertMapToString(map));
						writer.addDocument(doc);
					} else {
						logger.debug("updating " + CommonAttributes.convertMapToString(map));
						writer.updateDocument(new Term(CommonAttributes.KEY_ID, mainKey), doc);
					}
				}
			}
		}
	}
}