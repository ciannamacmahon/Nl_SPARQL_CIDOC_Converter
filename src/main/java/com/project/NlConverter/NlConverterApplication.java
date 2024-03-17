package com.project.NlConverter;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.apache.lucene.util.automaton.LimitedFiniteStringsIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@SpringBootApplication
public class NlConverterApplication {
	
	NlpAnalysis nlp=new NlpAnalysis();
	private static final Logger LOGGER=LoggerFactory.getLogger(NlConverterApplication.class);

	OpenAiApi openAiApi = new OpenAiApi(System.getenv("OPENAI_API_KEY"));
	private final ChatClient chatClient;

	public NlConverterApplication(ChatClient chatClient){
		this.chatClient=chatClient;
	}

	

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
		String promt=nlp.getChatGPTPrompt();
		LOGGER.info("greeting received");
		
	ChatResponse response = chatClient.call(
    new Prompt(promt,
	OpenAiChatOptions.builder()
	.withModel("gpt-3.5-turbo")
	.withTemperature((float) 0.4)
	.build()
	));
	AssistantMessage assistantMessage=response.getResult().getOutput();
	if(assistantMessage!=null){
		return assistantMessage.getContent();
	}else{
		return "No response Generated";
	}

	// laptop stopped working but full connection is made
	/*
	 * 
	 */
	}
}


