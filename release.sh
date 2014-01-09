#!/bin/bash
mvn -e -B release:clean -P'arq-glassfish_remote_3.1_(rest)' release:prepare -P'arq-glassfish_remote_3.1_(rest)'