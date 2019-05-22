In order to update the manifest we need to proceed as follows:

1. Build the two images
2. tag them as nicolasferry/multiarch-node-red-thingml:arm and nicolasferry/multiarch-node-red-thingml:amd64
3. Push both of them
4. Push the nicolasferry/multiarch-node-red-thingml with option --purge (to remove local cache)
5. create the manifest 
    docker manifest create nicolasferry/multiarch-node-red-thingml  nicolasferry/multiarch-node-red-thingml:amd64 nicolasferry/multiarch-node-red-thingml:arm
6. Push the manifest