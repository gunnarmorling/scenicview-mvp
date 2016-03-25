# Hibernate ScenicView, Great Views on your Data

Hibernate ScenicView is an engine for denormalizing data from primary datastores (e.g. an RDMBS) into secondary datastores (e.g. a document store).
It integrates with Hibernate ORM and Hibernate OGM.
You configure the denormalizations you want using a fluent API, and Hibernate ScenicView will take it from there.

## Why would I want denormalized data?

TODO

## What's working?

* Writing a copy of data from a primary store to another store
* Two Backends: Hash map for testing and MongoDB
* Inline associated data (simple object references and collections, entities and element collections of basic types)
* Configuration of connections
* Configuration of denormalization jobs using fluent API

## What's next

* Updates
* Deletions
* Updating embedding aggregates when embedded data changes
* Cassandra backend to experiment with column-oriented store
* Using other properties as id (so they can be used as PK in Cassandra)
* Queries (simple unmanaged projections as first step)
* ...

## Building the project

Hibernate ScenicView is built using Maven. Run the following command to build everything:

    mvn clean install

Different denormalization backends (MongoDB etc.) are controlled using Docker when running integration tests.
Therefore you need to have Docker installed and running on your system in order to run the full test suite.
When using docker-machine on your Mac or Windows computer, run the following commands:

    docker-machine start default
    eval "$(docker-machine env default)"
    export DOCKER_HOST_IP=`docker-machine ip default`

Otherwise, set the `DOCKER_HOST_IP` environment variable to whatever your Docker host's ip address is.
