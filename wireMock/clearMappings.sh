#!/bin/bash
kubectl exec -it $1 -n aruba -- rm -r /home/wiremock/mappings/
