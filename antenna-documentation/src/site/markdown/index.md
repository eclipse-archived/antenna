# Introduction and Goals

Antenna is thought to enforce our Third party policies. For that reason 
Antenna is able to build/create:

* a disclosure document containing:
    * a list of all Third party components and their licenses
    * the referred licenses itself
* a source zip containing:
    * all available sources of the included open source Third party 
    components
* a report which lists:
    * all artifacts with unknown/critical licenses
    * all artifacts with missing source files
    * all proprietary artifacts
    * all overwritten attributes
    * all artifacts that are added/removed  via the configuration file
    * unnecessary elements of the configuration file
    * failures, which happend during the execution of Antenna
* For gathering all necessary information, Antenna must be able to deal with 
different systems like Jenkins, Maven etc... Furthermore it must be 
possible to held and read license texts from a separate source.
