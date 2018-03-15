# Reflective Web App
This application is a small simple spring boot web app that reflects the http request parameter *reflect* back to the webpage.

## Examples
Safe
* http://localhost:8080/?reflect=hej

Malicious
* http://localhost:8080/?reflect=%3Cscript%3Edocument.body.style.backgroundColor=%22red%22;%3C/script%3E

