package com.neu.controller;

import java.nio.file.Paths;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.neu.controller.highlight.LuceneSearchHighlighterExample;
import com.neu.controller.highlight.Results;

@Controller
public class BaseController {

	private static int counter = 0;
	private static final String VIEW_INDEX = "index";
	private static final String SEARCH_INDEX = "searchedDocs";
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(BaseController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome(ModelMap model) {
		System.out.println("in main");
		logger.debug("[welcome] counter main : {} ", counter);
		model.addAttribute("message", "Welcome");
		model.addAttribute("counter", ++counter);
		logger.debug("[welcome] main : {}", counter);


		return VIEW_INDEX;

	}


	
	@RequestMapping(value = "/searchWords.htm", method = RequestMethod.GET)
	public String getSearchedWords(ModelMap model, HttpServletRequest req) throws Exception {
		System.out.println("start search highlight");
		System.out.println("req attr is: "+req.getParameter("searchWord"));
		ArrayList<Results> resultsFetched = new ArrayList<Results>();		
		  
		
		logger.debug("[welcomeName] search : {}", counter);
		String INDEX_DIR = "indexedFiles";
	    System.out.println("index_dir "+Paths.get(INDEX_DIR));   
		ArrayList<Results> displayData = LuceneSearchHighlighterExample.getDoc(req.getParameter("searchWord"), resultsFetched);
	   
		if(displayData.size() != 0) {

		logger.debug("search highlight", SEARCH_INDEX);
		HttpSession session = req.getSession();
		session.setAttribute("foundResults", displayData);
		}
		return SEARCH_INDEX;
		

	}

}