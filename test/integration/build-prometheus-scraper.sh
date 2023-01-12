#!/bin/bash

# TODO: could I build jars, so I can delete the git directories and commit the
# built jars? Commiting the classes seems excessive.
git clone https://github.com/jmazzitelli/prometheus-scraper.git
pushd prometheus-scraper
mvn compile
popd

git clone https://github.com/jboss-logging/jboss-logging.git
pushd jboss-logging
mvn compile
popd

