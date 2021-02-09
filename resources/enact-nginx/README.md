# ENACT NGinx 

This is a Docker image of the proxy that is used by the builtin
availability mechanisms provided by GeneSIS. That is, if you specify
"Builtin" in the availability, GeneSIS will deploy a NGinx server
acting as a "load balancer" or proxy in front of the replicas.

The files are located on two locations:

 - `/enact/' contains the scripts and the list of endpoints.
 - '/etc/nginx/' contains the configuration of the NGinx server.
 
Here are the files together with a short description:

 * `endpoints.sh` this the file that control the addition
   (resp. removal) of endpoints/replicas. There is no need to edit
   `endpoints.dat` by hand.
   
 * `endpoints.dat` is a text file that contains the list of
   replicas/endpoints together with their status: down if the last
   health check failed, and up if it succeed.
   
 * `watchdog.sh` is a Bash script that is triggered by CRON at fixed
   intervals. In turn, it triggers the health check (yet another
   script, but provided by the user and uploaded in the container by
   GeneSIS), and triggers `endpoints.sh` depending on the health
   status.
   
 * `publish.sh` is a Bash script that automatically build and publish
   the enact-nginx image.
   
 * `nginx.conf` is the initial NGinx configuration that is used before
   any replicas is activated.
   
 * `enact_template.conf` is a template NGinx configuration that the
   `endpoints.sh` uses to rebuild the NGinx configuration each time it
   switches from one replica to the next.

 * `Dockerfile` governs how the enact-image is built.
 

# How Does it Work?
A shell script is embedded (see the Dockerfile). This script comes
along with a list of endpoints (i.e., `endpoints.dat`)


# How to Update the Image?
This image also comes with a script to automate updating the
enact-nginx image. Currently the image is available on DockerHub at
`fchauvel/enact-nginx`. **Note that if you publish this image
somewhere else, you must update the file `src/engine/engine.js` in the
GeneSIS code base.

Should you make any change to the files


