

import java.io.File;
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
    String inputFilePath ="/Users/guojunwei/Downloads/testfolder";
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
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(inputFilePath).getFile());

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w;
        
        try (Scanner inputScanner = new Scanner(file)) {
        	w = new IndexWriter(index, config);
//        	File[] fs = file.listFiles();
//        	for (File f: fs) {
//        		if (!f.isDirectory()) {
//        			System.out.println(f);
//        		}
//        	}
            while (inputScanner.hasNextLine()) {
                //System.out.println(inputScanner.nextLine());
            	String temp = inputScanner.nextLine();
            	String docID = temp.substring(0, temp.indexOf(' '));
            	String content = temp.substring(temp.indexOf(' ') + 1);
            	addDoc(w, content, docID);
            }
            inputScanner.close();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    private void addDoc(IndexWriter w, String title, String isbn) throws IOException {
    	  Document doc = new Document();
    	  doc.add(new TextField("title", title, Field.Store.YES));
    	  doc.add(new StringField(docid, isbn, Field.Store.YES));
    	  w.addDocument(doc);
    }

    public static void main(String[] args ) {
        try {
            String fileName = "input.txt";
            //System.out.println("********Welcome to  Homework 3!");
            //String[] query13a = {"information", "retrieval"};
            String inputFilePath ="/Users/guojunwei/Downloads/testfolder";
            //ClassLoader classLoader = getClass().getClassLoader();
            //File file = new File(classLoader.getResource(inputFilePath).getFile());
            File file = new File(inputFilePath);	
            File[] fs = file.listFiles();
        	for (File f: fs) {
        		if (!f.isDirectory()) {
        			System.out.println(f);
        		}
        	}
            QueryEngine objQueryEngine = new QueryEngine(fileName);
            //objQueryEngine.runQ1_1(query13a); 
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
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
