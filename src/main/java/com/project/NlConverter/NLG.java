package com.project.NlConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class NLG {

    public static String connectToChatGPT() {
        String startPrompt="Given this query: When was Abbadie, Jacques born?. " +
                "And this result:bearDate: 1654-01-01. " +
                "Turn the result into a natural language sententce";
        String url="https://api.openai.com/v1/chat/completions";
        String API_Key="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";
        String model="gpt-3.5-turbo";

        try{
            URL obj=new URL(url);
            HttpURLConnection connection=(HttpURLConnection)obj.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization","Bearer "+API_Key);
                connection.setRequestProperty("Content-Type","application/json");

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

                return extractFromJSONResponse(response.toString());

            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
    }


        public static String extractFromJSONResponse(String response){
        int start=response.indexOf("content ")+11;
        int end=response.indexOf("\"",start);

        return response.substring(start,end);


        }
        public static void main(String[] args){
        String answerFromChatGPT=connectToChatGPT();
        System.out.println(answerFromChatGPT);
    }





}
