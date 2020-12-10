
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.tartarus.snowball.ext.PorterStemmer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import edu.stanford.nlp.simple.Sentence;

/**
 * @author guojunwei
 * CSC483 final project
 */
public class QueryEngine {
	String indexOutPath = "/Users/guojunwei/Downloads/plainIndex";
	boolean indexExists = false;
	boolean stem;
	boolean lemma;
	Similarity similarity;
	// String inputFilePath ="src/main/resources/input.txt";
	// static String inputFilePath =
	// "/Users/guojunwei/Downloads/wiki-subset-20140602";
	static String inputFilePath;
	StandardAnalyzer analyzer = new StandardAnalyzer();
	Directory index;
	private final String docid = "docid";
	
	public QueryEngine(String inputFile, boolean stem, boolean lemma, Similarity similarity) {
		this.lemma = lemma;
		this.stem = stem;
		this.similarity = similarity;
		inputFilePath = inputFile;
		// buildIndex();
	}

	/**
	 * This method builds index for the documents to the lucene framework
	 */
	private void buildIndex() {
		try {
			// String fileName = "input.txt";
			index = FSDirectory.open(new File(indexOutPath).toPath());
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter w;
			w = new IndexWriter(index, config);
			String inputFilePath = "/Users/guojunwei/Downloads/wiki-subset-20140602";
			// String inputFilePath ="/Users/guojunwei/Downloads/testfolder";

			File folder = new File(inputFilePath);
			File[] fs = folder.listFiles();
			for (File f : fs) {
				if (!f.isDirectory()) {

					Scanner inputScanner = new Scanner(f);
					String content = "";
					String docTitle = "";
					String categories = "";
					while (inputScanner.hasNextLine()) {
						// need to remove TPL&url
						String temp = inputScanner.nextLine();
						int tempLength = temp.length();
						if (temp.length() > 4 && temp.startsWith("[[") && temp.endsWith("]]")) {

							if (!docTitle.equals("")) {
								System.out.println("filename: " + f.getName());
								addDoc(w, docTitle, categories, content);
							}
							content = "";
							categories = "";
							docTitle = temp.substring(2, tempLength - 2);
							// System.out.println(docTitle);
						} else if (temp.equals("")) {
							continue;
						} else if (temp.indexOf("CATEGORIES") == 0) {
							categories = temp.substring(12); // get all the text from temp after "CATEGORIES"
							// categories = categories.replaceAll("\\p{Punct}","");
						} else if (temp.length() > 2 && temp.startsWith("=") && temp.endsWith("=")) {
							while (temp.length() > 2 && temp.startsWith("=") && temp.endsWith("=")) {
								temp = temp.substring(1, temp.length() - 1);
							}
							if (!temp.equals("See also") && !temp.equals("References")
									&& !temp.equals("Further reading") && !temp.equals("External links")
									&& !temp.equals("Examples")) {
								content += temp;
								content += " ";
							}
						} else {
							String removeTPL = temp.replaceAll("\\[tpl\\].*?\\[/tpl\\]", "");
							// String removePunctuation = removeTPL.replaceAll("\\p{Punct}","");
							content += removeTPL;
							content += " ";
						}
						// addDoc(w, content, docID);
					}
					inputScanner.close();
//        	        System.out.println("LASTtitle: " + docTitle);
//        	        System.out.println("LASTfilename: " + f.getName());
//            		System.out.println("LASTcategories: " + categories);
//            		System.out.println("LASTcontent: " + content);
					System.out.println();
					addDoc(w, docTitle, categories, content);

					indexExists = true;

					// System.out.println(f);
				}
			}

			w.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		indexExists = true;
	}

	// change regular word to lemma form
	private String makeLemma(String lemmaWord, String originWord) {
		Sentence sent = new Sentence(originWord.toLowerCase());
		List<String> lemmasList = sent.lemmas();
		for (String word : lemmasList) {
			lemmaWord += word;
			lemmaWord += " ";
		}
		return lemmaWord;
	}

	// change regular word to stem form
	private String makeStem(String stemWord, String originWord) {
		Sentence sent = new Sentence(originWord.toLowerCase());
		List<String> stemList = sent.words();
		for (String word : stemList) {
			stemWord += getStem(word);
			stemWord += " ";
		}
		return stemWord;
	}

	/**
	 * the documents are parsed, lemmatized or stemmed and then added to the lucene
	 * 
	 * @param w
	 * @param title
	 * @param categories
	 * @param content
	 * @throws IOException
	 */
	private void addDoc(IndexWriter w, String title, String categories, String content) throws IOException {
		Document doc = new Document();
		doc.add(new StringField("title", title, Field.Store.YES));
		String category = "";
		String cont = "";
		categories = categories.trim();
		content = content.trim();
		if (categories.equals("")) {
			categories = ":";
		}
		if (content.equals("")) {
			content = ":";
		}

		if (stem) {
			category = makeStem(category, categories);
			cont = makeStem(cont, content);
		} else if (lemma) {
			category = makeLemma(category, categories);
			cont = makeLemma(cont, content);
		} else {
			category = categories.toLowerCase();
			cont = content.toLowerCase();
		}

		doc.add(new TextField("categories", category, Field.Store.YES));
		doc.add(new TextField("content", cont, Field.Store.YES));
		w.addDocument(doc);
		System.out.println("title: " + title);
		System.out.println("categories: " + category);
		System.out.println("content: " + cont);
		System.out.println();
	}

	private String getStem(String word) {
		PorterStemmer stem = new PorterStemmer();
		stem.setCurrent(word);
		stem.stem();
		return stem.getCurrent();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		//build index for each algorithm
		//objQueryEngineStem.buildIndex();	
		//objQueryEngineLemma.buildIndex();
		//objQueryEnginePlain.buildIndex();
		
		//QueryEngine objQueryEngineStemBM25 = new QueryEngine(inputFilePath, true, false, new BM25Similarity());
		//QueryEngine objQueryEngineStemTFIDF = new QueryEngine(inputFilePath, true, false, new ClassicSimilarity());
		//QueryEngine objQueryEngineStemJM = new QueryEngine(inputFilePath, true, false, new LMJelinekMercerSimilarity((float) 0.5));
		//objQueryEngineStemBM25.compareAnswers();
		//objQueryEngineStemTFIDF.compareAnswers();
		//objQueryEngineStemJM.compareAnswers();
		
		//QueryEngine objQueryEngineLemmaBM25 = new QueryEngine(inputFilePath, false, true, new BM25Similarity());
		//QueryEngine objQueryEngineLemmaTFIDF = new QueryEngine(inputFilePath, false, true, new ClassicSimilarity());
		QueryEngine objQueryEngineLemmaJM = new QueryEngine(inputFilePath, false, true, new LMJelinekMercerSimilarity((float) 0.5));
		//objQueryEngineLemmaBM25.compareAnswers();
		//objQueryEngineLemmaTFIDF.compareAnswers();
		objQueryEngineLemmaJM.compareAnswers();
		
		//QueryEngine objQueryEnginePlainBM25 = new QueryEngine(inputFilePath, false, false, new BM25Similarity());
		//QueryEngine objQueryEnginePlainTFIDF = new QueryEngine(inputFilePath, false, false, new ClassicSimilarity());
		//QueryEngine objQueryEnginePlainJM = new QueryEngine(inputFilePath, false, false, new LMJelinekMercerSimilarity((float) 0.5));
		//objQueryEnginePlainBM25.compareAnswers();
		//objQueryEnginePlainTFIDF.compareAnswers();
		//objQueryEnginePlainJM.compareAnswers();
	}
	
	/**
	 * iterate through the questions.txt, compare with the lucence result and calculate the 
	 * 
	 * @throws IOException
	 */
	public void compareAnswers() throws IOException {
		// open index
		// Directory lemmaIndex = FSDirectory.open(new
		// File("/Users/guojunwei/Downloads/lemmaIndex").toPath());
		File questionList = new File("/Users/guojunwei/Downloads/questions/questions.txt");
		try (Scanner scanner = new Scanner(questionList)) {
			int i = 0; // track lines
			int correct = 0;
			int total = 0;
			double mmr = 0.0;
			Query q;
			String query = "";
			String category = "";
			String correctAns = "";
			while (scanner.hasNextLine()) {
				if (i % 4 == 0) {
					category = scanner.nextLine();
				} else if (i % 4 == 1) {
					query = scanner.nextLine().trim();
				} else if (i % 4 == 2) {
					correctAns = scanner.nextLine();
				} else {
					scanner.nextLine(); // skip empty line
					System.out.println(category);
					System.out.println(query);
					System.out.println(correctAns);
					// add category to query to narrow down the search
					List<ResultClass> myAnsList = runQuery(category, query);
					if (myAnsList.size() == 0) {
						System.out.println("NO SUCH RESULTS!");
					} else {
						String myAns = myAnsList.get(0).DocName.get("title");
						System.out.println("correct answer is " + correctAns);
						System.out.println("my answer is " + myAns);
						
						if (correctAns.equals(myAns)) {
							correct++;
							mmr += 1.0; // first position 1/1 = 1
							System.out.println("you are correct!!");
							System.out.println();
						} else {
							System.out.println("you are wrong!!");
							System.out.println();
							for (int j = 0; j < myAnsList.size(); j++) {
								String temp = myAnsList.get(j).DocName.get("title");
								if (temp.equals(correctAns)) {
									mmr += (double)1/(j + 1);
									break;
								}
							}
						}
						total++;
					}
				}
				i++;
			}
//			System.out.println("This is stem Algorithm");
//			System.out.println("This is Lemma Algorithm");
//			System.out.println("This is plain (neither stem nor lemma)");
			System.out.println("correct is " + correct);
			System.out.println("total is " + total);
			double correctRate = ((double)correct)/((double)total);
			System.out.println("correctRate is " + correctRate);
			System.out.println("Precision@1 is " + correctRate);
			System.out.println("mmr is " + mmr/((double)total));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * find query and content for information retrieval
	 * 
	 * @param category
	 * @param query
	 * @return ResultClass arraylist
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public List<ResultClass> runQuery(String category, String query)
			throws java.io.FileNotFoundException, java.io.IOException {
		
		String cat = "";
		String qry = "";
		category = category.trim();
		query = query.trim();
		if (stem) {
			category = makeStem(cat, category);
			query = makeStem(qry, query);
		} else if (lemma) {
			category = makeLemma(cat, category);
			query = makeLemma(qry, query);
		} else {
			category = category.toLowerCase();
			query = query.toLowerCase();
		}
		
		Query q;
		StandardAnalyzer analyzer = new StandardAnalyzer();
		List<ResultClass> ans = new ArrayList<ResultClass>();
		try {
			Directory index = FSDirectory.open(new File("/Users/guojunwei/Downloads/stemIndex").toPath()); // lemmaIndex
			// q = new QueryParser("content", analyzer).parse(query);
			//System.out.println("query category:" + category);
			//System.out.println("query query:" + query);
			List<String> list = new ArrayList<>();
			if (!category.equals("")) {
				list.add(QueryParser.escape(category));
			}
			if (!query.equals("")) {
				list.add(QueryParser.escape(query));
			}
			if (list.size() == 0) {
				System.out.println("no query");
			}
			// the string query array
			String[] stringQuery = {};
			stringQuery = list.toArray(stringQuery);
			// String [] stringQuery={category,query};
			// The string needs to look for
			String[] fields = { "categories", "content" };
			// Occur.MUST meaning query must be found 
			// Occur.MUST_NOT meaning query must not be found 
			// Occur.SHOULD meaning query should be found
			Occur[] occ = { Occur.SHOULD, Occur.SHOULD };
			q = MultiFieldQueryParser.parse(stringQuery, fields, occ, analyzer);
			int hitsPerPage = 10;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(similarity);
			TopDocs docs = searcher.search(q, hitsPerPage); // search
			ScoreDoc[] hits = docs.scoreDocs; // get result

			// System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				// Explanation e = searcher.explain(q, docId);
				Document d = searcher.doc(docId);
				// System.out.println((i + 1) + ". " + d.get(docid) + "\t" + d.get("title")+"\t"
				// + hits[i].score
				// + "\t");

				ResultClass r = new ResultClass();
				r.DocName = d;
				r.docScore = hits[i].score;
				ans.add(r);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// ans =returnDummyResults(3);
		return ans;
	}

}
