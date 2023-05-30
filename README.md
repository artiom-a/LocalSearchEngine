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
And of course LinkDex itself is open source with a [public repository][dill]
on GitHub.

## Installation

Linkdex requires [Java](https://www.java.com/ru/download/ie_manual.jsp?locale=ru) 8+ to run.

Install the dependencies and start the server.
</article>