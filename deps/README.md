This directory contains the base dependencies of the project as git submodules. The following forked submodules are configured:

* https://github.com/AdaptiveMe/che-core
* https://github.com/AdaptiveMe/che
* https://github.com/AdaptiveMe/user-dashboard
* https://github.com/AdaptiveMe/cli

These submodules should not contain any changes with regard to their origin repo.

## Submodules update

* ``` git submodule update --recursive --remote ``` - to update submodules recursively.

## user-dashboard

* ``` sudo gem install compass --pre ``` - to install compass required by https://github.com/AdaptiveMe/user-dashboard. 
