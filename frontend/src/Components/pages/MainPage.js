import "./MainPage.css";
import React, { useState } from 'react';
import axios from 'axios';


function MainPage() {
    const [searchInput,setSearchInput]=useState("");
    const [buttonColour, setButtonColour]=useState("white");
    const[sparqlQuery,setSparqlQuery]=useState("Enter Your Query in Search Bar Above");
    const[showSparqlQuery,setShowSparqlQuery]=useState(false);
    const config={headers:{'Content-Type':'application/json'}};

    const handleChange=(event)=>{
      setSearchInput(event.target.value);
    }

    const handleSubmit= async(event)=>{
      event.preventDefault();
      setButtonColour("yellow");
      setSparqlQuery("Loading......");
      try {
        console.log("Sending request with searchInput:", searchInput); // Log the searchInput value
        const response = await axios.post("http://localhost:8080/searchEngine",  searchInput,config );
        console.log("Response from backend:", response.data); // Log the response from the backend
      } catch (error) {
        console.error("Error processing:", error);
      }
    }

    const handleViewSparqlQuery=()=>{
      setShowSparqlQuery(!showSparqlQuery);
    }

    const fetchQuery=async () =>{
      const response=await axios.get("http://localhost:8080/sparqlQuery",config);
      setSparqlQuery(response.data.sparqlQuery);
      setShowSparqlQuery(true);
      
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
              >Search</button>
            </form>
          </div>
          <div className='resultBox'>
            <p>{sparqlQuery}</p>
            

          </div>
          <div className='rawSparqlResult'>
            <button>View Raw SPARQL Result</button>
            <button onClick={handleViewSparqlQuery}>View SPARQL Query</button>
            {showSparqlQuery && (
              <div>
                <p> SPARQL Query: {sparqlQuery}</p>
                </div>
            )}
          </div>
          
      </div>
    );
      
  }
export default MainPage;