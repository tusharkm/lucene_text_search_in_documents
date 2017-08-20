package com.mkyong.controller.highlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.apache.lucene.benchmark.quality.Judge;
import org.apache.lucene.benchmark.quality.QualityBenchmark;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.benchmark.quality.QualityStats;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class PrecisionRecall {
	// ========================================================
	/*
	 * Calculating the Precision and Recall
	 * 
	 */
	public static void main(String[] args) throws Throwable {
		// Path for the Topic File
		File topicsFile = new File("precisionRecall/topics.txt");
		// Path for the Qrels File
		File qrelsFile = new File("precisionRecall/qrels.txt");
		// Base class for Directory implementations that store index files in the file
		// system
		FSDirectory dir = FSDirectory.open(Paths.get("indexedFiles"));
		// Creating teh index reader
		IndexReader ir = DirectoryReader.open(dir);
		// Creainng Index searcher
		IndexSearcher indexSearcher = new IndexSearcher(ir);
		// Name of the index of file names
		String docNameField = "path";

		PrintWriter logger = new PrintWriter(System.out, true);

		TrecTopicsReader qReader = new TrecTopicsReader(); // #1

		// A QualityQuery has an ID and some name-value pairs. used for mapping the
		// quality of document created
		QualityQuery qqs[] = qReader.readQueries(new BufferedReader(new FileReader(topicsFile))); // #1
		// Judge if a document is relevant for a quality query.
		Judge judge = new TrecJudge(new BufferedReader(new FileReader(qrelsFile))); // #2
		// Validate that queries and this Judge match each other.
		judge.validateData(qqs, logger); // #3
		// Parse a QualityQuery into a Lucene query.
		QualityQueryParser qqParser = new SimpleQQParser("title", "text"); // #4

		QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, indexSearcher, docNameField);

		SubmissionReport submitLog = null;
		// Results of quality benchmark run for a single query or for a set of queries.
		QualityStats stats[] = qrun.execute(judge, submitLog, logger);// #5

		QualityStats avg = QualityStats.average(stats); // #6
		avg.log("SUMMARY", 2, logger, "  ");
		dir.close();
	}
}
