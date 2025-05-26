#!/bin/bash
#更新依赖

set -o nounset
set -o errexit

scriptPath=$(cd `dirname $0`; pwd)
cd ${scriptPath}/../../shell-file
while getopts ":l" opt; do
  case $opt in
    l)
      echo "debug更新..."
      sh update_navi_all.sh -l 1
      ;;
    \?)
      sh update_navi_all.sh -o 1
      ;;
  esac
done
