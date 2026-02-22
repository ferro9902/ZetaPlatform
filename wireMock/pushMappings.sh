#!/bin/bash
# Syntax: kubectl cp <local-source-path> <namespace>/<pod-name>:<destination-path>

kubectl cp ./mappings/ aruba/$1:/home/wiremock/
