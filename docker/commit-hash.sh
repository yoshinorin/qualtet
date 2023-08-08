#!/bin/sh -xe

touch qualtet.build_hash
git show --format='%h' --no-patch > qualtet.build_hash
