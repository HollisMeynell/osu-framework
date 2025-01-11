#!/bin/bash

if [ "$1" == "sleep" ]; then
    echo "sleep 5s"
    sleep 5
fi

git fetch --all

git pull

cd docs

pnpm install

pnpm run build

if [ -d "./build" ]; then
  if [ -d "./release" ]; then
    rm -rf ./release
  fi
  mv ./build ./release
fi