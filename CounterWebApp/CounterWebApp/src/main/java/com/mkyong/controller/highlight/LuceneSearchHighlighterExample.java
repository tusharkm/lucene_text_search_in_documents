package com.mkyong.controller.highlight;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mkyong.controller.LuceneConstants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneSearchHighlighterExample {

	public static CharArraySet stopSet = new CharArraySet(LuceneConstants.stopWords, false);

	
	public static ArrayList<Results> getDoc(String args, ArrayList<Results> resultsFetched) throws Exception {

		// Path of the Index directory
		String INDEX_DIR =LuceneConstants.INDEX_DIR;

		// Creating instance of Directory
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));

		// Index reader - an interface for accessing a point-in-time view of a lucene
		// index
		IndexReader reader = DirectoryReader.open(dir);

		// Create lucene searcher. It search over a single IndexReader.
		IndexSearcher searcher = new IndexSearcher(reader);

		// Instantiating the analyzer
		Analyzer analyzer = new StandardAnalyzer(stopSet);

		// Query parser to be used for creating TermQuery
		QueryParser qp = new QueryParser("text", analyzer);

		//Query query = qp.parse(args);
			Query query = null;
		// Handling Conjunction and Disjunction
		if (args.toLowerCase().contains("not")) {
			String[] split = args.toLowerCase().split("not");
			Query first = qp.parse(split[0]);
			Query second = qp.parse(split[1]);
			query = new BooleanQuery.Builder().add(first, BooleanClause.Occur.MUST)
					.add(second, BooleanClause.Occur.MUST_NOT).build();
		} else {
			if (args.toLowerCase().contains("and")) {
				String[] split = args.toLowerCase().split("and");
				Query first = qp.parse(split[0]);
				Query second = qp.parse(split[1]);
				query = new BooleanQuery.Builder().add(first, BooleanClause.Occur.MUST)
						.add(second, BooleanClause.Occur.MUST).build();
			} else {
				if (args.toLowerCase().contains("or")) {
					String[] split = args.toLowerCase().split("or");
					Query first = qp.parse(split[0]);
					Query second = qp.parse(split[1]);
					query = new BooleanQuery.Builder().add(first, BooleanClause.Occur.SHOULD)
							.add(second, BooleanClause.Occur.SHOULD).build();
				} else {
					query = qp.parse(args);
				}
			}
		}

		TopDocs hits = searcher.search(query, 20);

		// Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
		Formatter formatter = new SimpleHTMLFormatter();

		// It scores text fragments by the number of unique query terms found
		// Basically the matching score in layman terms
		QueryScorer scorer = new QueryScorer(query);

		// used to markup highlighted terms found in the best sections of a text
		Highlighter highlighter = new Highlighter(formatter, scorer);

		// It breaks text up into same-size texts but does not split up spans
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 20);

		// breaks text up into same-size fragments with no concerns over spotting
		// sentence boundaries.
		// Fragmenter fragmenter = new SimpleFragmenter(10);

		// set fragmenter to highlighter
		highlighter.setTextFragmenter(fragmenter);

		// Show hits for query
		System.out.println("hits for query: " + hits.totalHits);

		// Iterate over found results
		for (int i = 0; i < hits.scoreDocs.length; i++) {
			int docid = hits.scoreDocs[i].doc;
			Document doc = searcher.doc(docid);
			String title = doc.get("path");

			// Printing - to which document result belongs
			System.out.println("Document Path " + " : " + title);

			// Get stored text from found document
			String text = doc.get("text");

			// Create token stream
			TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "text", analyzer);

			System.out.println("(" + hits.scoreDocs[i].score + ")");

			Results giveResult = new Results();
			giveResult.setHits(hits.totalHits);
			giveResult.setScore(hits.scoreDocs[i].score);
			giveResult.setDocPath(doc.get("path"));

			// Get highlighted text fragments
			String[] frags = highlighter.getBestFragments(stream, text, 30);
			giveResult.setHighlightedText(frags);
			for (String frag : frags) {
				//System.out.println("*********************************************************");
				//System.out.println(frag);

			}
			resultsFetched.add(giveResult);
		}
		dir.close();
		return resultsFetched;
	}
}