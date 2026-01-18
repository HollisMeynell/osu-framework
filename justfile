set shell := ["bash", "-cu"]

ROOT := justfile_directory()
DEPLOY_DIR  := "/home/spring/match"

default:
    just -l

update-code:
    #!/usr/bin/env bash
    set -euxo pipefail
    git fetch --all
    git reset --hard origin/master
    git pull
    echo "ready"


build-docs: update-code
    #!/usr/bin/env bash
    set -euxo pipefail
    cd {{ROOT}} / "docs"
    pnpm i
    pnpm run build
    echo "down"

build-app: update-code
    #!/usr/bin/env bash
    set -euxo pipefail
    gradle :spring-application:shadowJar
    sudo mv spring-application/build/libs/app.jar {{DEPLOY_DIR}}
