#!/usr/bin/env bash
#
# Usage:
#        run.sh server
#   -or-
#        run.sh client

build()
{
	echo compiling NettyClient and NettyServer
	mkdir -p bin
	javac -d bin -cp libs/netty-3.7.0.Final.jar src/*
}

runserver()
{
	echo running NettyServer
	java -cp bin:libs/netty-3.7.0.Final.jar NettyServer
}

runclient()
{
	echo running NettyClient
	java -cp bin:libs/netty-3.7.0.Final.jar NettyClient
}


if [[ $1 == client ]]; then
	RUN=runclient
elif [[ $1 == server ]]; then
	RUN=runserver
else
	echo "usage: $0 [client|server]"
	exit 1
fi

build
$RUN
