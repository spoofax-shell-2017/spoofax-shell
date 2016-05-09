#!/bin/sh
mvn site site:stage
mvn scm-publish:publish-scm
