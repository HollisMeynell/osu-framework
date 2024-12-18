#!/bin/bash

git fetch --all

git pull

cd docs

pnpm install

pnpm run build