<article class="markdown-body entry-content container-lg" itemprop="text"><p align="center" dir="auto">
<img src="src/main/resources/images/path1269.png" alt="Awesome" data-canonical-src="https://awesome.re/badge.svg" style="max-width: 30%;">
<p align="center" dir="auto">

</p>

# LinkDex

Linkdex is a simple HTML crawler. This crawler is intended to crawl sites from a configuration file specified by the user. Before use this tool, please read these tips below.


## Features. How a search engine works
- Before launching the application, the configuration file specifies the addresses of the sites that the engine should search for
- The search engine must independently crawl all the pages of the given sites and index them
- The user sends a request through the engine API
- The query is in a certain way transformed into a list of words translated into the base form
- The index looks for pages where all these words occur
- Search results are ranked, sorted and returned to the user

## Tech
Linkdex uses a number of open source projects to work properly:
- Spring Boot
- Apache
- jsoup
- Apache Lucene
- Swagger 
etc.
## Installation

Linkdex requires [Java](https://www.java.com/ru/download/ie_manual.jsp?locale=ru) 8+ to run.
Download and install locally on your computer or create a database on an existing MySQL database server to run the program
[MySQL](https://dev.mysql.com/downloads/mysql/)
> Note: In this example, we are using the database name search_engine, username and password skillbox_engine
> 
```yaml 
spring:
datasource:
url: jdbc:mysql://localhost:3306/search_engine
username: skillbox_engine
password: skillbox_engine
```

Install the dependencies and start the server.
- Make sure to be in the root directory
- Clean and build the project, run the command:
```sh
  mvn install
```
- This will generate a jar file with all the dependencies which we will run once it has been created.
- Move the application.yaml file to the root of the generated .jar file
- run .jar file with command:
```sh
  java -jar [path]/[file.jar]
```
Verify the deployment by navigating to your server address in
your preferred browser.
```sh
  http://localhost:8080
```
</article>