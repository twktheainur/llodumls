# llodumls
a LLOD (Lemon-Ontolex) converter for UMLS

## Table of Contents

* [llodumls](#llodumls)
     * [I. Requirements](#i-requirements)
        * [I.1. Installing Redis](#i1-installing-redis)
     * [II. Development](#ii-development)
        * [II.1. Building](#ii1-building)
        * [II.2. Creating runnable jar](#ii2-creating-runnable-jar)
     * [III. Resources](#iii-resources)

## I. Requirements

* An internet connection to retrive dependencies through maven
* Java 1.8
* Apache maven 3+ 
* Redis (caching)

### I.1. Installing Redis

The evaluation programme uses a Redis cache server to minimise redundent network activity and to allow resuming the annotation in case of network failure. You need to install redis on you machine:

- On **macOS**, you can use home-brew to install redis. 

  1. First install home-brew if you don't already have it. Open a terminal and run:

     ```shell
     $ /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
     ```

  2. Install redis:

     ```shell
     $ brew install redis
     ```

  3. Run redis by opening a terminal and typing:

     ```shell
     $ redis-server
     ```

  Redis will run in the foreground, please leave the window open, unless you want to close redis. You can interrupt the execution with Ctrl + C. Redis will create a database file named dump.rdb, you may delete the file after you have finished with the reprodction. Please use Redis's default port: 6379.

- On **Linux**:

  - Debian or Ubuntu: 

    https://www.digitalocean.com/community/tutorials/how-to-install-and-use-redis

  - Fedora/CentOs:

    http://blog.andolasoft.com/2013/07/how-to-install-and-configure-redis-server-on-centosfedora-server.html

  - ArchLinux:

    https://wiki.archlinux.org/index.php/Redis

  - On other distributions, the instructions are similar to the Debian or Fedora instructions, except for the installation of the dependencies: make sure you have build tools installed (gcc toolchain with automake/autoheader) as well as tcl.


- On **Windows**: 
  - You can use a native Windows fork of redis like so: https://github.com/ServiceStack/redis-windows#running-microsofts-native-port-of-redis
  - Or start the native unix redis using vagrant like so: https://github.com/ServiceStack/redis-windows#running-the-latest-version-of-redis-with-vagrant

## II. Development 

This is maven project. I recommend that we all use IntelliJ IDEA as an IDE, there is a free community edition (https://www.jetbrains.com/idea/download). 



### II.1. Building 

```shell
$ mvn install 
```



### II.2. Creating runnable jar

```Shell
$ mvn assembly:assembly
```



## III. Resources

* Ontolex specification:
  * Core: https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Core
  * Vartrans: https://www.w3.org/community/ontolex/wiki/Final_Model_Specification#Variation_.26_Translation_.28vartrans.29 
* NIF Specification: http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core/nif-core.html
* NIF Paper with examples [Page 8]: http://svn.aksw.org/papers/2013/ISWC_NIF/public.pdf
* UMLS Format Documentation: https://www.ncbi.nlm.nih.gov/books/NBK9685/
* Quaero Corpus for Evaluation: https://quaerofrenchmed.limsi.fr









