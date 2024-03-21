import "./MainPage.css";
import React, { useState } from 'react';
import axios from 'axios';


function MainPage() {
    const [searchInput,setSearchInput]=useState("");
    const [buttonColour, setButtonColour]=useState("white");
    const[sparqlQuery,setSparqlQuery]=useState("");
    const[sparqlResult,setSparlResult]=useState("");
    const[NlResult,setNlResult]=useState("Enter Your Query in Search Bar Above");
    const[showSparqlQuery,setShowSparqlQuery]=useState(false);
    const[showSparqlResult,setShowSparqlResult]=useState(false);
    const[showNlResult,setShowNlResult]=useState(false);


    const config={headers:{'Content-Type':'application/json'}};

    const handleChange=(event)=>{
      setSearchInput(event.target.value);
    }

    const handleSubmit= async(event)=>{
      event.preventDefault();
      setButtonColour("yellow");
      setNlResult("Loading......");
      try {
        console.log("Sending request with searchInput:", searchInput); // Log the searchInput value
        const response = await axios.post("http://localhost:8080/searchEngine",  searchInput,config );
        console.log("Response from backend:", response.data); // Log the response from the backend
        setShowNlResult(!showNlResult);
        await fetchNlResult();

      } catch (error) {
        console.error("Error processing:", error);
      }
    }

    const handleViewSparqlQuery=()=>{
      setShowSparqlQuery(!showSparqlQuery);
      if(!showSparqlQuery){
        fetchQuery(); //Fetch SPARQL query when the button is clicked and show is false
      }
    }

    const handleViewSparqlResult=()=>{
      setShowSparqlResult(!showSparqlResult);
      if(!showSparqlResult){
        fetchResult();
      }
    }
    const handleViewNlResult=()=>{
      if(!showNlResult){
        fetchNlResult();
      }
    }

    const fetchQuery=async () =>{
      const result= await axios.get("http://localhost:8080/sparqlQuery")
      console.log(result.data);
      setSparqlQuery(result.data)    
    }
    const fetchResult=async () =>{
      const result1= await axios.get("http://localhost:8080/sparqlResult");
      console.log(result1.data);
      setSparlResult(result1.data);
       // .then(res =>{
       //   const result=res.data;
       //   setSparqlQuery(result);
       //   setShowSparqlQuery(true);

      //})      
    }
    const fetchNlResult=async () =>{
      const result2= await axios.get("http://localhost:8080/naturalLanguageResult");
      console.log(result2.data);
      setNlResult(result2.data); 
    }

    return(
      <div className='MainPage'>
          <div className="Search">
            <form onSubmit={handleSubmit}
                        className="form">
              <input 
              
                type="text" 
                placeholder="Enter your search query on the VRTI KG"
                onChange={handleChange} 
                value={searchInput}
                className="input"
                />
              <button type="submit" className="searchButton" style={{backgroundColor: buttonColour}} 
              // only want to fetchNLResult when the data is available
             >Search</button>
            </form>
          </div>
          <div className='resultBox'>
          {showNlResult &&(
            <div>
            <p>Processed Result: {NlResult}</p>
            

          </div>
          )}
          </div>
          <div className="resultContainer">
            <div className='SparqlResult'>
              <button onClick={handleViewSparqlResult}>View Raw SPARQL Result</button>
              {showSparqlResult && (
                <div>
                  <p> SPARQL Result: {sparqlResult}</p>
                  </div>
              )}
            </div>
            <div>
              <button onClick={handleViewSparqlQuery}>View SPARQL Query</button>
              {showSparqlQuery && (
                <div className='SparqlResult'>
                  <p> SPARQL Query: {sparqlQuery}</p>
                  </div>
              )}
            </div>
          </div>
          
      </div>
    );
      
  }
export default MainPage;