import React from 'react';
import "./Home.css";
import { Link } from 'react-router-dom';

function Home() {
    return (
      <div className="homepage">
          <h1>
            Welcome
          </h1>
          <Link to="/searchCase">
            <button className="button"> Click To Get Started</button>
          </Link>
        </div>
    );
  }
  
  export default Home;