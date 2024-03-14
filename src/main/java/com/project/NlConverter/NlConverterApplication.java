package com.project.NlConverter;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class NlConverterApplication {
	NlpAnalysis nlp=new NlpAnalysis();

	public static void main(String[] args) {
		SpringApplication.run(NlConverterApplication.class, args);
	}
	/* Input received from the frontend must be passed to this function
	 * 	entryPoint(userInputQuery)

	 */

	@PostMapping("/searchEngine")
	public String processQuery(@RequestBody String userInputQuery) throws UnsupportedEncodingException{
		System.out.println("received from frontend");
		try{
			System.out.println(userInputQuery);
			nlp.entryPoint(userInputQuery);
			return "Query ok";
		}catch(FileNotFoundException e){
			e.printStackTrace();
			return "Error occured: "+ e.getMessage();
		}
	}
	@GetMapping("/sparqlQuery")
	public String retreiveSPARQLQuery() {
		return nlp.getSPARQLQuery();
	}
	@GetMapping("/sparqlResult")
	public String retreiveSPARQLResult() {
		return nlp.getGraphResult();
	}
	@GetMapping("/naturalLanguageResult")
	public String retreiveNLResult() {
		return "error check";
	}

}

