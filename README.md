cuecloud-java
=============

This Project provides a CueCloud API wrapper for Java.

Dependancies
------------
1. [Apache Codecs Library](http://commons.apache.org/proper/commons-codec/)
2. [FlexJson Library](http://flexjson.sourceforge.net/)

These dependancies are pre-packaged in the library available in the links below. However, it is advisable that you use the versions from the  above link as they are guaranteed to be the most recent versions.

Initial Setup
---------
You can download the JAR file plus dependencies from [https://www.cuecloud.com/api/java/cuecloud-java-bin.zip](https://www.cuecloud.com/api/java/cuecloud-java-bin.zip "CueCloud Library"). Add the downloaded files as library dependencies in your IDE or [add the jar files in the classpath of your target system](http://javarevisited.blogspot.com/2012/10/5-ways-to-add-multiple-jar-to-classpath-java.html) if you wish to use the library in a commandline or Server based Java program.


The Javadoc for this library is at [https://www.cuecloud.com/api/java/javadoc](https://www.cuecloud.com/api/java/javadoc "Java Doc"). You can reference this URL directly in your IDE as the JavaDoc Source for the libray. Alternatively, and preferrably for speed, Download the Java Doc zip file from [https://www.cuecloud.com/api/java/cuecloud-javadoc.zip](https://www.cuecloud.com/api/java/cuecloud-javadoc.zip "Javadoc Zip") and attach it as javadoc source in your IDE.

Quick Start Guide
-----------------

You will need `API KEY` and `API PASSWORD` from http://www.cuecloud.com/

Once you have the `API KEY` and `API PASSWORD`, Create or Open the target Project in your favorite IDE and  Create a CueCloud Object as shown below:

    private static final String YOUR_API_KEY ="your.cuecloud.api.key";
    private static final String YOUR_API_PASS="your.cuecloud.api.pass";
    try {
            cueCloud = new CueCloud(YOUR_API_KEY, YOUR_API_PASS);
        } catch (CueCloud.CueCloudException ex) {
            ex.printStackTrace();
            Logger.getLogger(CueCloudTest.class.getName()).log(Level.SEVERE, ex.getMessage(),ex);
        }


For developers or Individuals hosting cuecloud server instance locally for development or testing purpose, you can specify localhost as the URL to make requests to your local instance.

    private static final String YOUR_API_KEY ="your.cuecloud.api.key";
    private static final String YOUR_API_PASS="your.cuecloud.api.pass";
    try {
            cueCloud = new CueCloud(YOUR_API_KEY, YOUR_API_PASS, "http://localhost");
        } catch (CueCloud.CueCloudException ex) {
            ex.printStackTrace();
            Logger.getLogger(CueCloudTest.class.getName()).log(Level.SEVERE, ex.getMessage(),ex);
        }
        
Once you have a CueCloud Java Object, you can make your target method calls. Be sure to catch CueCloudExcepton where necessary.

    cuecloud.validateUser();
    
JUnit Tests
-----------
This repository ships with JUnit tests for the project. You can clone the repository or [download as Zip](https://github.com/cuecloud/cuecloud-java/archive/master.zip) file to access and run the Junit Tests in your favorite IDE. 
Running the Junit Tests will require `Junit-4.x` library.
    
Copyright and license
---------------------
Copyright 2014 CueCloud. Licensed under the MIT License.
