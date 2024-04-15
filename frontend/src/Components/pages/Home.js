import React from 'react';
import "./Home.css";
import { Link } from 'react-router-dom';

function Home() {
    return (
      <div className="homepage">
          <h1>
            Welcome To The Natural Language Querying System of the VRTI KG
          </h1>
          <Link to="/searchCase">
            <button className="button"> Click To Get Started</button>
          </Link>
        </div>
    );
  }
  
  export default Home;