package com.neu.controller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

//import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.neu.controller.LuceneConstants;

public class LuceneWriteIndexFromFileExample {
	// ==============================================================
	/* To create the index file for the input files given */
	public static void main(String[] args) {
	

		final Path docDir = Paths.get(LuceneConstants.docsPath);

		try {
			// Creating instance of Directory
			Directory dir = FSDirectory.open(Paths.get(LuceneConstants.indexPath));

			// Instantiating the analyzer 
			Analyzer analyzer = new StandardAnalyzer();

			// Configuring IndexWriter
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

			// IndexWriter writes new index files to the directory
			IndexWriter writer = new IndexWriter(dir, iwc);

			// Its recursive method to iterate all files and directories
			indexDocs(writer, docDir);

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// =========================================================================
	/*
	 * Index the document which are present in the file, and if the file is present
	 * in the folder it will create the index file for it and if the file is not
	 * present the it will check other folders present in the path
	 * 
	 */
	static void indexDocs(final IndexWriter writer, Path path) throws IOException {

		// Check if the file is directory
		if (Files.isDirectory(path)) {
			// Iterate through the directory
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						// index the files in the document
						indexDoc(writer, file, attrs.lastModifiedTime().toMillis());

						writer.commit();
						writer.deleteUnusedFiles();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			// Index the file
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}
	// =========================================================================
	/*
	 * Reading the document using Apache Tika and writing the updating the result in log
	 * 
	 */
	static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException { 
											
		File fileinDoc = new File(file.toAbsolutePath().toString());

		// Using Tika to read the file
		ContentHandler handler = new BodyContentHandler();
		ParseContext context = new ParseContext();
		Metadata metadata = new Metadata();
		Parser parser = new AutoDetectParser();
		InputStream stream = new FileInputStream(fileinDoc);
		Document doc = new Document();

		try {
			parser.parse(stream, handler, metadata, context);
		} catch (TikaException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			stream.close();
		}

		String text = handler.toString();
		String fileName = fileinDoc.getName();

		for (String key : metadata.names()) {
			String name = key.toLowerCase();
			String value = metadata.get(key);
			doc.add(new StringField("path", file.toString(), Field.Store.YES));
			doc.add(new LongPoint("modified", lastModified));
			if (StringUtils.isBlank(value)) {
				continue;
			}
			if ("keywords".equalsIgnoreCase(key)) {
				for (String keyword : value.split(",?(\\s+)")) {
					doc.add(new StringField(name, keyword, Field.Store.YES));
				}
			} else if ("title".equalsIgnoreCase(key)) {
				doc.add(new StringField(name, value, Field.Store.YES));
			} else {
				doc.add(new StringField(name, fileName, Field.Store.YES));
			}
		}
		doc.add(new TextField("text", text, Store.YES));
		writer.addDocument(doc);

		System.out.println(" File Created" + writer.maxDoc());
	}

}
