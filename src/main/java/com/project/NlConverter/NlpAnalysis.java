package com.project.NlConverter;
//nlpPipeline.java
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.checkerframework.checker.units.qual.s;

public class NlpAnalysis {
    public static LinkedList<String> depQualities = new LinkedList<>();
    public static LinkedList<String[]> quest_ans_targets=new LinkedList<>();
    public static LinkedList<String> question_entity=new LinkedList<>();
    public static LinkedList<String> question_entity_type=new LinkedList<>();
    public static String subject="";
    public static String predicate="";
    public static LinkedList <String> object=new LinkedList<>();
    public static String questionWord="";
    public static String targetAnswer="";
    public static String unknown="";
    static StanfordCoreNLP pipeline;
    public static String[] QuestionArray=new String[6];
    public static boolean containsFilter=false;
    public static boolean dateRange=false;
    public static String filterCondition="";
    public static LinkedList<LinkedList> logicalExpressions=new LinkedList<>();

    public static void QuestionList(){
        QuestionArray[0]="When";
        QuestionArray[1]="Where";
        QuestionArray[2]="Who";
        QuestionArray[3]="How much";
        QuestionArray[4]="How many";
        QuestionArray[5]="Count";

    }

    public static void main(String args[]) throws FileNotFoundException{

        entryPoint("How many people were born between 1600 and 1601?");
    }
    public static void entryPoint(String userInputQuery) throws FileNotFoundException {
       // QuestionList();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        pipeline = new StanfordCoreNLP(props);
        //String question = "Who was born in Dublin? ";
        languageAnalysis(userInputQuery);
        System.out.println(userInputQuery);
        populateTargetQuestions();
        predictTarget();
        NlpAnalysis.pringQual(depQualities);
        object.remove("and");
        String sparlCIDOCQuery=Sparql.createSPARQLQuery(question_entity_type,question_entity,questionWord,subject,predicate,object,containsFilter,filterCondition,logicalExpressions,dateRange);
      //  //if endpoint could compile
       // EndpointExecution.searchGraph(sparlCIDOCQuery);
    }


    public static void populateTargetQuestions() throws FileNotFoundException{
        File file= new File("src/main/java/com/project/NlConverter/QA_Target.txt");
        Scanner scanner= new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
          //  System.out.println(line);
            String[] parts = line.split(", ");

            // Add the array to the list
            quest_ans_targets.add(parts);
        }
        scanner.close();
    }

    public static void predictTarget(){
        for (String[] q:quest_ans_targets){
           // System.out.println(questionWord+"hihiih"+q[0]);
            if(q[0].equals(questionWord)){
                targetAnswer=q[1];
            }
        }
        if (questionWord.equals("Who")|| questionWord.equals("How many")|| questionWord.equals("Count")){
            unknown="subject";
        }
        else if (questionWord.equals("When")||questionWord.equals("Where")){
            unknown="object";
        }
        System.out.println("Target Answer: "+targetAnswer+" Word type: "+unknown);
    }
    public static void SPOExtraction(LinkedList<String> qualOfQuery) {
        LinkedList<String[]> listOfArray = intoArray(qualOfQuery);
        //More complex questions with more than one of these
        // LinkedList<String> subject = new LinkedList<>();
        //  LinkedList<String> predicate = new LinkedList<>();
        // LinkedList<String> object = new LinkedList<>();

        // this extracts it using the root as the predicate and the subject is the entity extraction
        if( unknown.equals("subject")){
            //entity is the object of the question
            for(String entity:question_entity){
                object.add(entity);
            }
           // System.out.print("objects: "+object.get(0));
            subject=targetAnswer;
        }
        else if(unknown.equals("object")){
            for(String entity:question_entity){
                subject=entity;
            }
            object.add(targetAnswer);
        }
        for (String[] array : listOfArray) {
            if (array[0].equals("root")) {
                String lemmaVersionPred=lemmaQuery(array[2].substring(0, array[2].indexOf("-")));
                predicate=lemmaVersionPred;
            }
        }
        System.out.println("Subject: "+ subject+ " Predicate: "+predicate+ " Object: "+object);
    }
    public static Boolean conatinsNsubj(String []array) {
        for (String element : array) {
            if ("nsubj".equals(element)) {
                return true;
            }
        }
        return false;
    }
    public static LinkedList<String[]> intoArray(LinkedList <String>qualOfQuery) {
        LinkedList<String[]> divArrays = new LinkedList<>();

        for (String parser : qualOfQuery) {
            // each line of the depParser is printed, split into an array of words to enable SPO extraction
          //  System.out.println(parser);
            divArrays.add(splitString(parser));
        }
        return divArrays;
    }
    public static String[] splitString(String input) {
        String word_type="";
        String[] result = new String[3];
        //Result with split the sentence into an array of 3 so SPO extraction can take place
        //Result=[wordType][associated word][evaluated word]
        //Eg:    [punct][born][?]


        // Sentence: aux:pass(born-4, was-2)
        word_type = input.substring(0, input.indexOf("("));
        // aux:pass
       // System.out.println(word_type);
        result[0]=word_type;
        String subtext=input.substring(input.indexOf("(")+1,input.indexOf(")"));
        //Subtext: born-4, was-2
      //  System.out.println(subtext);

        // Split the string using ',' as the delimiter
        String[] parts = subtext.split(", ");
        result[1]=parts[0];
        result[2]=parts[1];
        // for(String test1:parts){
        //     System.out.println(test1);
        // }


        return result;
    }

    //Test method; each item in the list is the characteristic of that word
    public static void pringQual(LinkedList <String>qualOfQuery){
        System.out.println("Depenecy parse");

        for (String line:qualOfQuery){

            System.out.println("\n"+line);

        }
        SPOExtraction(qualOfQuery);
    }

    public static void languageAnalysis(String query){
        //String withoutQ=removeQ(query);
        CoreDocument document = new CoreDocument(query);
        Annotation annDocument=new Annotation(query);
        pipeline.annotate(annDocument);
        pipeline.annotate(document);
        findNER(document);
        findPOStags(document);
       // constituencyParse(document);
        depQualities=dependancyParser(query);
      //  String lemmaVersionQuery=lemmaQuery(query);

    }
    
    public static String lemmaQuery(String query){
        CoreDocument doc=new CoreDocument(query);
        pipeline.annotate(doc);
        String lemmaVersion="";
        for(CoreLabel tok:doc.tokens()){
            System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
            lemmaVersion=tok.lemma();
        }
        System.out.println(lemmaVersion);
        return lemmaVersion;
    }
// can also be used to find compound words as the tag will be compound
// again dep parsey was preferred as provides the link in the display
    public static void findPOStags(CoreDocument doc){
        //Method 1
        List<CoreLabel> tokens=doc.tokens();
        int y=0;
        for(int i=0; i<tokens.size()-1;i++){
            CoreLabel firstToken=tokens.get(i);
            y=i+1;
            CoreLabel nextToken=tokens.get(y);
            System.out.println(String.format("%s\t%s", firstToken.word(),firstToken.tag()));
            if(firstToken.tag().contains("W")){
                if(nextToken.word().contains("many")||nextToken.word().contains("much")){
                    questionWord= firstToken.word()+" "+nextToken.word();
                }
                else{
                    questionWord=firstToken.word();
                }
            }
            
        }
        System.out.println("question: "+questionWord);
    }
    public static void findNER(CoreDocument doc){
        //CoreMap sentence=doc.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        StringBuilder combinedEntity=new StringBuilder();
        String currEntityType=null;
        System.out.println("---");
        System.out.println("entities found");
        for (CoreEntityMention em :doc.entityMentions()){
            String entityType=em.entityType();
            String entityText=em.text();
            if(entityType.equals(currEntityType)){
                // combinedEntity.append(", ").append(entityText); person
                question_entity.add(entityText);
        }
            else{
                question_entity_type.add(entityType);
                question_entity.add(entityText);
                currEntityType=entityType;
                combinedEntity.append(entityText);
                if(question_entity_type.size()>1){
                    containsFilter=true;
                }
            }
          //  System.out.println("\tdetected entity: \t"+em.text()+"\t"+em.entityType());
          //  question_entity=em.text();
        }
     //   String combinedEntityFinal=combinedEntity.toString().trim();
     ////   System.out.println("tester: "+combinedEntityFinal);
     //   if(combinedEntityFinal.contains("and")){
    //        combinedEntityFinal.replace("and", "");
    //    }
    //    question_entity=combinedEntityFinal;
        System.out.println("Entity: "+question_entity);
        System.out.println("Entity type: "+question_entity_type);

    }
// no using constit parsing as doesnt indicate the recipient of the relationship like depencancy parsing does
//   public static void constituencyParse(CoreDocument doc){
//       CoreSentence sentence=doc.sentences().get(0);
//       //Method 1:
//       Tree constitParse1 = sentence.constituencyParse();
//       System.out.println("Example: constituency parse");
//       System.out.println(constitParse1);
//       System.out.println();

  //  }
    public static LinkedList<String> dependancyParser(String query)
    {
        LinkedList<String> depParse= new LinkedList<>();
        //Method 2:
        Annotation doc=new Annotation(query);
        pipeline.annotate(doc);
        CoreMap sentence=doc.get(CoreAnnotations.SentencesAnnotation.class).get(0);

        SemanticGraph depGraph =sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        //Gets the dependancy Parse
        for (TypedDependency typedDependency : depGraph.typedDependencies()) {
        
            String relation=typedDependency.reln().getShortName();
            String governor = typedDependency.gov().originalText();
            String dependent = typedDependency.dep().originalText();
            String conditionType="";
            String conditionFunctionailty="";
            if(relation.equals("conj")){
                LinkedList<String> conjunctionPairs=new LinkedList<>();
                //big and honest govenor=big dependant=honest
                System.out.println("Conjunction words:"+relation+":"+governor+":"+dependent);
                conjunctionPairs.add(relation);
                conjunctionPairs.add(governor);
                conjunctionPairs.add(dependent);
                logicalExpressions.add(conjunctionPairs);

            }
            if(relation.equals("cc")){
                conditionType=dependent;
                filterCondition=dependent;
                containsFilter=true;
                System.out.println("Logical Operator type: "+conditionType);
            }
            if(relation.equals("case")){
                LinkedList<String> rangeForDates=new LinkedList<>();

                // catches 'between' for range functionality of dates
                conditionFunctionailty=dependent;
                rangeForDates.add(relation);
                rangeForDates.add(conditionFunctionailty);
                logicalExpressions.add(rangeForDates);
                dateRange=true;
                System.out.println("Will help with range function: "+conditionFunctionailty);

            }
            String line = typedDependency.toString();
            depParse.add(line);

        }
        return depParse;
    }
}