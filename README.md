What is GluFeeder
================
GluFeeder is a tool used by outbrain to enhance glu. It uses glu's API as well as other tools such as subversion, teamcity for builds, yum, yammer etc.  
The feeder had been in production use for over 8 months as of today and is continually improving and adding features.  
The main goal of the feeder is to bridge the gap between outbrain's special requirements such as working with external services (subversion, yammer, nagios) and glu's core functionality as exposed by glu's api. The feeder is used to to implement Continuous Deployment at outbrain (there are a few additional tools that help such as a subversion hook but they are out of scope).

Current Condition
-----------------
While the feeder is in production for a few months and is in generally considered stable (although new features keep getting added), this specific copy of the feeder is not considered production ready.
This copy is just a snapshot of the current work and is meant to be used as a reference to outbrain's work (as requested by users on the mailing list).  
It is not maintained or supported.   
It has a build file and all the tests pass (although some were emtied due to security consideration) but as noted, again, it's not meant to be a functional piece of software, it's just a current snapshot of how things are at outbrain.

The following blog posts describe parts of the tool in plain english 

 - http://prettyprint.me/2011/01/24/continuous-deployment-at-outbrain/ 
 - http://prettyprint.me/2011/07/31/visualizing-our-deployment-pipeline/

We hope it will serve you well.

