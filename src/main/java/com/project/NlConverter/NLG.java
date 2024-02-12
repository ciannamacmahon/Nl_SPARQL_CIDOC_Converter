package com.project.NlConverter;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class NLG {

    public static String connectToChatGPT(String startPrompt) {
        String API_Key="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";

        String url="https://api.openai.com/v1/chat/completions";
        String model="gpt-3.5-turbo";

        try{
            URL obj=new URL(url);
            JSONObject respQ=null;
            HttpURLConnection connection=(HttpURLConnection)obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization","Bearer "+API_Key);
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestProperty("Accept","application/json");

                //request body
                String body="{\"model\": \""+model+"\",\"messages\":[{\"role\":\"user\", \"content\": \""+startPrompt+"\"}]}";
                connection.setDoOutput(true);
                OutputStreamWriter writer=new OutputStreamWriter(connection.getOutputStream());
                writer.write(body);
                writer.flush();
                writer.close();

                //Response
                BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                StringBuffer response=new StringBuffer();
                while((line=br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
               // System.out.println(response);

                return extractFromJSONResponse(response.toString());

            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
    }

        public static String extractFromJSONResponse(String response){
          //  System.out.println("Full JSON Response: " + response);
            JSONObject responseObject=new JSONObject(response);
            JSONArray choicesArray=responseObject.getJSONArray("choices");
            for(int i=0;i<choicesArray.length();i++){
                JSONObject choiceObject=choicesArray.getJSONObject(i);
                JSONObject messageObject=choiceObject.getJSONObject("message");

                String content=messageObject.getString("content");
                System.out.println("Target response from chatGPT: "+content);

            }
            return "";


        }
        /*
        public static void javaGPT(String promptFromUser){
            String API_Key="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";

            OpenAiService service=new OpenAiService(API_Key);
            CompletionRequest completionRequest= CompletionRequest.builder()
                    .prompt(promptFromUser)
                    .model("babbage-002")
                    .echo(true)
                    .build();
            service.createCompletion(completionRequest).getChoices().forEach(System.out::println);


        }

         */


        public static void main(String[] args){
            String startPrompt="Given this query: When was Abbadie, Jacques born?. " +
                    "And this result:bearDate: 1654-01-01. " +
                    "Turn the result into a natural language sententce";
       // String answerFromChatGPT=connectToChatGPT(startPrompt);
        //javaGPT(startPrompt);
        System.out.println(connectToChatGPT(startPrompt));
    }
    /*
    var chatRequest = ChatRequest.builder()
    .model("gpt-3.5-turbo-1106")
    .messages(List.of(
        new ChatMsgSystem("You are an expert in AI."),
        new ChatMsgUser("Write a technical article about ChatGPT, no more than 100 words.")))
    .temperature(0.0)
    .maxTokens(300)
    .build();
var futureChat = openai.chatCompletions().create(chatRequest);
var chatResponse = futureChat.join();
System.out.println(chatResponse.firstContent());
     */





}
