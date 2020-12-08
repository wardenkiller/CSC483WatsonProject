

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

//import javax.management.Query;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class QueryEngine {
    boolean indexExists=false;
    //String inputFilePath ="src/main/resources/input.txt";
    static String inputFilePath ="/Users/guojunwei/Downloads/testfolder";
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Directory index = new RAMDirectory();
    private final String docid = "docid";
    public QueryEngine(String inputFile){
        inputFilePath =inputFile;
        buildIndex();
    }
     
    /**
     * This method builds index for the documents to the lucene framework
     */
    private void buildIndex() {
    	try {
            //String fileName = "input.txt";
        	IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w;
            w = new IndexWriter(index, config);
            String inputFilePath ="/Users/guojunwei/Downloads/testfolder";
            File folder = new File(inputFilePath);	
            File[] fs = folder.listFiles();
        	for (File f: fs) {
        		if (!f.isDirectory()) {
        			try (Scanner inputScanner = new Scanner(f)) {        	        	
        				String content = "";
        				String docTitle = "";
        				String categories = "";
        	            while (inputScanner.hasNextLine()) {
        	            	String temp = inputScanner.nextLine();
        	            	int tempLength = temp.length();
        	            	if (temp.startsWith("[[")&&temp.endsWith("]]")) {
        	            		if (!content.equals("")) {
        	            			System.out.println(docTitle);
        	            			System.out.println(categories);
        	            			System.out.println(content);
        	            			addDoc(w, docTitle, categories, content);
        	            			content = "";
        	            		} else {
        	            			docTitle = temp.substring(2, tempLength - 2);
        	            		}
        	            		docTitle = temp.substring(2, tempLength - 2);
        	            		//System.out.println(docTitle);
        	            	} else if (temp.equals("")) {
        	            		continue;
        	            	} else if (temp.indexOf("CATEGORIES") == 0){
        	            		categories = temp.substring(12);		//get all the text from temp after "CATEGORIES"
        	            	} else {
        	            		content += temp;
        	            		content += " ";
        	            	}
        	            	//addDoc(w, content, docID);
        	            }
        	            inputScanner.close();
        	            //w.close();
        	        } catch (IOException e) {
        	            e.printStackTrace();
        	        }
        	        
        	        indexExists = true;	
                    
        			//System.out.println(f);
        		}
        	}
            
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        indexExists = true;
    }
    
    /**
     * Your indexed documents should have two fields: 
     * a tokenized and searchable text field containing the text of the document (i.e., each line without the first token), 
     * and another non-tokenized field containing the document name (the first token in each line), e.g., Doc1.
     * @param w
     * @param title
     * @param isbn
     * @throws IOException
     */
    private void addDoc(IndexWriter w, String title, String categories,String content) throws IOException {
    	  Document doc = new Document();
    	  doc.add(new StringField("title", title, Field.Store.YES));
  		  doc.add(new TextField("categories", categories, Field.Store.YES));
  		  doc.add(new TextField("text", content, Field.Store.YES)); 
  		  w.addDocument(doc);
    }
    
    

    public static void main(String[] args ) throws FileNotFoundException, IOException {
    	QueryEngine objQueryEngine = new QueryEngine(inputFilePath);
    	String[] query13a = {"BSI", "certifies"};
        objQueryEngine.runQ1_1(query13a); 
    }

    public void createIndex() {
    	
        
    }
    /**
     * find docid and length for information retrieval
     * @param query
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_1(String[] query) throws java.io.FileNotFoundException,java.io.IOException{
        if(!indexExists) {
            buildIndex();
        } 
        String querystr = null;
        for (int i = 0; i < query.length; i++) {
			querystr += query[i] + " ";
		} 
        querystr.trim();
        Query q;
        
        List<ResultClass>  ans=new ArrayList<ResultClass>();
		try {
			q = new QueryParser("title", analyzer).parse(querystr);
			int hitsPerPage = 10;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Explanation e = searcher.explain(q, docId);
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t" +e.getValue()+"\t");
	            
	            ResultClass r = new ResultClass();
	            r.DocName = d;
	            r.docScore = e.getValue();	
	            ans.add(r);
	        }
	        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        
        //ans =returnDummyResults(3);
        return ans;
    }

    /**
     * find docid and length for information AND retrieval
     * @param query
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_a(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if(!indexExists) {
            buildIndex();
        }
        String querystr = query[0] + " AND " + query[1];
//        for (int i = 0; i < query.length; i++) {
//			querystr += query[i] + " ";
//		} 
//        querystr.trim();
        Query q;
        
        List<ResultClass>  ans=new ArrayList<ResultClass>();
		try {
			q = new QueryParser("title", analyzer).parse(querystr);
			int hitsPerPage = 4;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Explanation e = searcher.explain(q, docId);
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t" +e.getValue()+"\t");
	            
	            ResultClass r = new ResultClass();
	            r.DocName = d;
	            r.docScore = e.getValue();	
	            ans.add(r);
	        }
	        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        
        //ans =returnDummyResults(3);
        return ans;
    }

    /**
     * find docid and length for information AND NOT retrieval
     * @param query
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_b(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
    	if(!indexExists) {
            buildIndex();
        }
    	String querystr = query[0] + " AND NOT " + query[1];
//        for (int i = 0; i < query.length; i++) {
//			querystr += query[i] + " ";
//		} 
//        querystr.trim();
        Query q;
        
        List<ResultClass>  ans=new ArrayList<ResultClass>();
		try {
			q = new QueryParser("title", analyzer).parse(querystr);
			int hitsPerPage = 4;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Explanation e = searcher.explain(q, docId);
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t" +e.getValue()+"\t");
	            
	            ResultClass r = new ResultClass();
	            r.DocName = d;
	            r.docScore = e.getValue();	
	            ans.add(r);
	        }
	        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        
        //ans =returnDummyResults(3);
        return ans;
    }
     
    /**
     * find docid and length for information retrieval within one word
     * @param query
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public List<ResultClass> runQ1_2_c(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
    	if(!indexExists) {
            buildIndex();
        }
    	String querystr = "\"" + query[0] + " " + query[1] + "\"" + "~1";
    	//String querystr = "\"information retrieval\"~1";
//        for (int i = 0; i < query.length; i++) {
//			querystr += query[i] + " ";
//		} 
//        querystr.trim();
        Query q;
        
        List<ResultClass>  ans=new ArrayList<ResultClass>();
		try {
			q = new QueryParser("title", analyzer).parse(querystr);
			int hitsPerPage = 4;
	        IndexReader reader = DirectoryReader.open(index);
	        IndexSearcher searcher = new IndexSearcher(reader);
	        TopDocs docs = searcher.search(q, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        
	        System.out.println("Found " + hits.length + " hits.");
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Explanation e = searcher.explain(q, docId);
	            Document d = searcher.doc(docId);
	            System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t" +e.getValue()+"\t");
	            
	            ResultClass r = new ResultClass();
	            r.DocName = d;
	            r.docScore = e.getValue();	
	            ans.add(r);
	        }
	        
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        
        //ans =returnDummyResults(3);
        return ans;
    }

    public List<ResultClass> runQ1_3(String[] query) throws java.io.FileNotFoundException,java.io.IOException {

        if(!indexExists) {
            buildIndex();
        }
        StringBuilder result = new StringBuilder("");
        List<ResultClass>  ans=new ArrayList<ResultClass>();
        ans =returnDummyResults(2);
        return ans;
    }


    private  List<ResultClass> returnDummyResults(int maxNoOfDocs) {

        List<ResultClass> doc_score_list = new ArrayList<ResultClass>();
            for (int i = 0; i < maxNoOfDocs; ++i) {
                Document doc = new Document();
                doc.add(new TextField("title", "", Field.Store.YES));
                doc.add(new StringField("docid", "Doc"+Integer.toString(i+1), Field.Store.YES));
                ResultClass objResultClass= new ResultClass();
                objResultClass.DocName =doc;
                doc_score_list.add(objResultClass);
            }

        return doc_score_list;
    }

}
