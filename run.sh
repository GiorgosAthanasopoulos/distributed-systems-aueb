#!/usr/bin/env bash

mvn exec:java -Dexec.mainClass="giorgosathanasopoulos.com.github.distributed_systems_aueb.Main" -Dexec.args="$@"
